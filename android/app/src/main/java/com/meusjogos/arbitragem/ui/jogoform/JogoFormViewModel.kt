package com.meusjogos.arbitragem.ui.jogoform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meusjogos.arbitragem.core.model.Jogo
import com.meusjogos.arbitragem.core.model.StatusPagamento
import com.meusjogos.arbitragem.core.util.DateUtils
import com.meusjogos.arbitragem.data.repository.JogoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate

enum class ModoFormulario { NOVO, EDITAR, EDITAR_NOVO_DUPLICADO }

data class JogoFormUiState(
    val carregando: Boolean = true,
    val modo: ModoFormulario = ModoFormulario.NOVO,
    val dataTexto: String = "",
    val horarioTexto: String = "",
    val competicao: String = "",
    val modalidade: String = "",
    val categoria: String = "",
    val equipeMandante: String = "",
    val equipeVisitante: String = "",
    val cidade: String = "",
    val estadio: String = "",
    val funcao: String = "",
    val valorCentavos: Long = 0L,
    val quantidadePartidas: Int = 1,
    val status: StatusPagamento = StatusPagamento.A_RECEBER,
    val dataRecebimentoTexto: String = "",
    val observacoes: String = "",
    val erroData: String? = null,
    val erroValor: String? = null,
    val salvando: Boolean = false,
    val salvo: Boolean = false,
    val dataCriacaoOriginal: Instant? = null,
) {
    val tituloTela: String
        get() = when (modo) {
            ModoFormulario.NOVO -> "Novo jogo"
            ModoFormulario.EDITAR -> "Editar jogo"
            ModoFormulario.EDITAR_NOVO_DUPLICADO -> "Editar novo jogo"
        }

    /** REGRA nova: cadastro em lote (N partidas com o mesmo valor) só faz sentido para um jogo novo. */
    val permiteVariasPartidas: Boolean get() = modo == ModoFormulario.NOVO

    val valorTotalCentavos: Long get() = valorCentavos * quantidadePartidas
}

class JogoFormViewModel(
    private val repository: JogoRepository,
    private val jogoId: Long,
    private val duplicado: Boolean,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        JogoFormUiState(
            carregando = jogoId != 0L,
            modo = if (jogoId == 0L) ModoFormulario.NOVO else if (duplicado) ModoFormulario.EDITAR_NOVO_DUPLICADO else ModoFormulario.EDITAR,
            dataTexto = DateUtils.formatarData(LocalDate.now()),
        ),
    )
    val uiState: StateFlow<JogoFormUiState> = _uiState.asStateFlow()

    init {
        if (jogoId != 0L) {
            viewModelScope.launch {
                val jogo = repository.buscarPorId(jogoId)
                if (jogo != null) {
                    _uiState.update { it.preencherComJogo(jogo) }
                } else {
                    _uiState.update { it.copy(carregando = false) }
                }
            }
        }
    }

    private fun JogoFormUiState.preencherComJogo(jogo: Jogo): JogoFormUiState = copy(
        carregando = false,
        dataTexto = DateUtils.formatarData(jogo.data),
        horarioTexto = jogo.horario?.let { DateUtils.formatarHora(it) } ?: "",
        competicao = jogo.competicao ?: "",
        modalidade = jogo.modalidade ?: "",
        categoria = jogo.categoria ?: "",
        equipeMandante = jogo.equipeMandante ?: "",
        equipeVisitante = jogo.equipeVisitante ?: "",
        cidade = jogo.cidade ?: "",
        estadio = jogo.estadio ?: "",
        funcao = jogo.funcao ?: "",
        valorCentavos = jogo.valorCentavos,
        status = jogo.statusPagamento,
        dataRecebimentoTexto = jogo.dataRecebimento?.let { DateUtils.formatarData(it) } ?: "",
        observacoes = jogo.observacoes ?: "",
        dataCriacaoOriginal = jogo.dataCriacao,
    )

    fun atualizarData(texto: String) = _uiState.update { it.copy(dataTexto = texto, erroData = null) }
    fun atualizarHorario(texto: String) = _uiState.update { it.copy(horarioTexto = texto) }
    fun atualizarCompeticao(texto: String) = _uiState.update { it.copy(competicao = texto) }
    fun atualizarModalidade(texto: String) = _uiState.update { it.copy(modalidade = texto) }
    fun atualizarCategoria(texto: String) = _uiState.update { it.copy(categoria = texto) }
    fun atualizarEquipeMandante(texto: String) = _uiState.update { it.copy(equipeMandante = texto) }
    fun atualizarEquipeVisitante(texto: String) = _uiState.update { it.copy(equipeVisitante = texto) }
    fun atualizarCidade(texto: String) = _uiState.update { it.copy(cidade = texto) }
    fun atualizarEstadio(texto: String) = _uiState.update { it.copy(estadio = texto) }
    fun atualizarFuncao(texto: String) = _uiState.update { it.copy(funcao = texto) }
    fun atualizarValor(centavos: Long) = _uiState.update { it.copy(valorCentavos = centavos, erroValor = null) }

    /** REGRA nova: quantidade de partidas do cadastro em lote (mínimo 1, no máximo 30 de uma vez). */
    fun atualizarQuantidadePartidas(quantidade: Int) =
        _uiState.update { it.copy(quantidadePartidas = quantidade.coerceIn(1, 30)) }
    fun atualizarObservacoes(texto: String) = _uiState.update { it.copy(observacoes = texto) }
    fun atualizarDataRecebimento(texto: String) = _uiState.update { it.copy(dataRecebimentoTexto = texto) }

    /** REGRA 11/12: status pode ser trocado manualmente; ao ir para RECEBIDO, sugere a data de hoje. */
    fun atualizarStatus(novoStatus: StatusPagamento) = _uiState.update { atual ->
        val precisaDataRecebimento = novoStatus == StatusPagamento.RECEBIDO && atual.dataRecebimentoTexto.isBlank()
        atual.copy(
            status = novoStatus,
            dataRecebimentoTexto = if (precisaDataRecebimento) DateUtils.formatarData(LocalDate.now()) else atual.dataRecebimentoTexto,
        )
    }

    /** REGRA 2/6: só DATA e VALOR são obrigatórios — todo o resto pode ficar em branco. */
    fun salvar() {
        val estado = _uiState.value
        val data = DateUtils.parseData(estado.dataTexto)
        if (data == null) {
            _uiState.update { it.copy(erroData = "Informe uma data válida (DD/MM/AAAA)") }
            return
        }
        if (estado.valorCentavos <= 0L) {
            _uiState.update { it.copy(erroValor = "Informe o valor do jogo") }
            return
        }

        val jogo = Jogo(
            id = if (estado.modo == ModoFormulario.NOVO) 0L else jogoId,
            data = data,
            horario = DateUtils.parseHora(estado.horarioTexto),
            competicao = estado.competicao.trim().ifBlank { null },
            modalidade = estado.modalidade.trim().ifBlank { null },
            categoria = estado.categoria.trim().ifBlank { null },
            equipeMandante = estado.equipeMandante.trim().ifBlank { null },
            equipeVisitante = estado.equipeVisitante.trim().ifBlank { null },
            cidade = estado.cidade.trim().ifBlank { null },
            estadio = estado.estadio.trim().ifBlank { null },
            funcao = estado.funcao.trim().ifBlank { null },
            valorCentavos = estado.valorCentavos,
            statusPagamento = estado.status,
            dataRecebimento = if (estado.status == StatusPagamento.RECEBIDO) {
                DateUtils.parseData(estado.dataRecebimentoTexto) ?: LocalDate.now()
            } else {
                null
            },
            observacoes = estado.observacoes.trim().ifBlank { null },
            dataCriacao = estado.dataCriacaoOriginal ?: Instant.now(),
        )

        val quantidade = if (estado.permiteVariasPartidas) estado.quantidadePartidas else 1

        _uiState.update { it.copy(salvando = true) }
        viewModelScope.launch {
            if (quantidade <= 1) {
                repository.salvar(jogo)
            } else {
                // Cadastro em lote: N partidas idênticas, cada uma com o valor por partida
                // informado — os totais (a receber/recebido/geral) somam sozinhos, pois cada
                // partida vira um jogo independente, podendo depois ser recebido separadamente.
                repeat(quantidade) { repository.salvar(jogo.copy(id = 0L)) }
            }
            _uiState.update { it.copy(salvando = false, salvo = true) }
        }
    }
}

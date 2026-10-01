package com.meusjogos.arbitragem.ui.jogoform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meusjogos.arbitragem.core.logic.sugestoesPara
import com.meusjogos.arbitragem.core.model.Competicao
import com.meusjogos.arbitragem.core.model.Jogo
import com.meusjogos.arbitragem.core.model.StatusPagamento
import com.meusjogos.arbitragem.core.util.DateUtils
import com.meusjogos.arbitragem.data.repository.CompeticaoRepository
import com.meusjogos.arbitragem.data.repository.JogoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    val sugestoesCompeticao: List<Competicao> = emptyList(),
    /** Preenchida quando a tentativa de salvar esbarra numa competição ENCERRADA (REGRA 8) —
     * a tela mostra o aviso e oferece "Reabrir competição". */
    val competicaoBloqueada: Competicao? = null,
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
    private val competicaoRepository: CompeticaoRepository,
    private val jogoId: Long,
    private val duplicado: Boolean,
    private val competicaoPreSelecionadaId: Long = 0L,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        JogoFormUiState(
            carregando = jogoId != 0L,
            modo = if (jogoId == 0L) ModoFormulario.NOVO else if (duplicado) ModoFormulario.EDITAR_NOVO_DUPLICADO else ModoFormulario.EDITAR,
            dataTexto = DateUtils.formatarData(LocalDate.now()),
        ),
    )
    val uiState: StateFlow<JogoFormUiState> = _uiState.asStateFlow()

    /** Carregada uma vez ao abrir a tela — usada para as sugestões do autocomplete (REGRA 6). */
    private var competicoesConhecidas: List<Competicao> = emptyList()

    init {
        viewModelScope.launch {
            competicoesConhecidas = competicaoRepository.observarCompeticoes().first()
            if (jogoId != 0L) {
                val jogo = repository.buscarPorId(jogoId)
                _uiState.update { if (jogo != null) it.preencherComJogo(jogo) else it.copy(carregando = false) }
            } else if (competicaoPreSelecionadaId != 0L) {
                val preSelecionada = competicaoRepository.buscarPorId(competicaoPreSelecionadaId)
                if (preSelecionada != null) {
                    _uiState.update {
                        it.copy(
                            carregando = false,
                            competicao = preSelecionada.nome,
                            cidade = preSelecionada.cidade ?: "",
                            modalidade = preSelecionada.modalidade ?: "",
                        )
                    }
                } else {
                    _uiState.update { it.copy(carregando = false) }
                }
            } else {
                _uiState.update { it.copy(carregando = false) }
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

    /** REGRA 6/17: a cada letra digitada, recalcula as sugestões que combinam com o texto —
     * competições encerradas não são sugeridas (saem do "banco de cadastradas" para jogos novos),
     * mas continuam podendo ser digitadas de propósito e reabertas se necessário. */
    fun atualizarCompeticao(texto: String) = _uiState.update {
        it.copy(
            competicao = texto,
            sugestoesCompeticao = competicoesConhecidas.filterNot { c -> c.encerrada }.sugestoesPara(texto),
            competicaoBloqueada = null,
        )
    }

    /** REGRA 4: ao tocar numa sugestão, preenche cidade e modalidade automaticamente. */
    fun selecionarCompeticao(competicao: Competicao) = _uiState.update {
        it.copy(
            competicao = competicao.nome,
            cidade = competicao.cidade ?: it.cidade,
            modalidade = competicao.modalidade ?: it.modalidade,
            sugestoesCompeticao = emptyList(),
            competicaoBloqueada = null,
        )
    }

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

    fun fecharAvisoCompeticaoEncerrada() = _uiState.update { it.copy(competicaoBloqueada = null) }

    /** REGRA 8: reabre a competição bloqueada e tenta salvar de novo, sem o usuário precisar sair do formulário. */
    fun reabrirCompeticaoESalvar() {
        val bloqueada = _uiState.value.competicaoBloqueada ?: return
        viewModelScope.launch {
            competicaoRepository.reabrir(bloqueada)
            competicoesConhecidas = competicaoRepository.observarCompeticoes().first()
            _uiState.update { it.copy(competicaoBloqueada = null) }
            salvar()
        }
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

        _uiState.update { it.copy(salvando = true) }
        viewModelScope.launch {
            val nomeCompeticao = estado.competicao.trim()
            var competicaoResolvida: Competicao? = null
            // REGRA 3/16: resolve (reaproveita ou cria) a competição ANTES de salvar o jogo, para
            // nunca gravar uma competição equivalente duplicada.
            if (nomeCompeticao.isNotBlank()) {
                competicaoResolvida = competicaoRepository.buscarOuCriar(
                    nomeCompeticao,
                    estado.cidade.trim().ifBlank { null },
                    estado.modalidade.trim().ifBlank { null },
                )
                // REGRA 8: só bloqueia a criação de um jogo NOVO numa competição encerrada —
                // editar um jogo já existente nunca fica travado por isso.
                val criandoJogoNovo = estado.modo != ModoFormulario.EDITAR
                if (criandoJogoNovo && competicaoResolvida.encerrada) {
                    _uiState.update { it.copy(salvando = false, competicaoBloqueada = competicaoResolvida) }
                    return@launch
                }
            }

            val jogo = Jogo(
                id = if (estado.modo == ModoFormulario.NOVO) 0L else jogoId,
                data = data,
                horario = DateUtils.parseHora(estado.horarioTexto),
                competicao = competicaoResolvida?.nome ?: nomeCompeticao.ifBlank { null },
                competicaoId = competicaoResolvida?.id,
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

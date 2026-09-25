package com.meusjogos.arbitragem.ui.recebimento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meusjogos.arbitragem.core.logic.cidadesDisponiveis
import com.meusjogos.arbitragem.core.logic.competicoesDisponiveis
import com.meusjogos.arbitragem.core.logic.contarPorCidade
import com.meusjogos.arbitragem.core.logic.contarPorCompeticao
import com.meusjogos.arbitragem.core.logic.contarPorModalidade
import com.meusjogos.arbitragem.core.logic.filtrarEPesquisar
import com.meusjogos.arbitragem.core.logic.modalidadesDisponiveis
import com.meusjogos.arbitragem.core.logic.ordenarMaisRecentePrimeiro
import com.meusjogos.arbitragem.core.model.FiltroJogos
import com.meusjogos.arbitragem.core.model.FiltroPeriodo
import com.meusjogos.arbitragem.core.model.FiltroStatus
import com.meusjogos.arbitragem.core.model.Jogo
import com.meusjogos.arbitragem.data.repository.JogoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class RecebimentoUiState(
    val carregando: Boolean = true,
    val jogosFiltrados: List<Jogo> = emptyList(),
    val filtro: FiltroJogos = FiltroJogos(status = FiltroStatus.A_RECEBER),
    val competicoesDisponiveis: List<String> = emptyList(),
    val modalidadesDisponiveis: List<String> = emptyList(),
    val cidadesDisponiveis: List<String> = emptyList(),
    val selecionados: Set<Long> = emptySet(),
    val processando: Boolean = false,
    val mensagem: String? = null,
    /** Contadores do topo — respeitam competição/cidade/data/pesquisa, mas SEMPRE olham para todos os status. */
    val totalPendentes: Int = 0,
    val totalRecebidos: Int = 0,
    val totalGeral: Int = 0,
    /** Para o botão "➕ Receber": cada grupo (com pelo menos um jogo pendente) e quantos jogos
     * pendentes tem — sempre olhando para TODOS os jogos, não só os filtrados no momento. */
    val pendentesPorCompeticao: List<Pair<String, Int>> = emptyList(),
    val pendentesPorModalidade: List<Pair<String, Int>> = emptyList(),
    val pendentesPorCidade: List<Pair<String, Int>> = emptyList(),
)

/**
 * Área "Recebimento": marcar vários jogos como recebidos de uma vez — por
 * competição, por cidade, ou por seleção manual/toque longo — sem precisar
 * abrir jogo por jogo. Reaproveita a mesma base (observarJogos), o mesmo
 * filtro (FiltroJogos/filtrarEPesquisar) e a mesma escrita em lote
 * (marcarVariosComoRecebido) já usados na lista "Meus Jogos"; o recebimento
 * individual em JogoDetailScreen continua existindo, sem nenhuma alteração.
 * A baixa em lote só ATUALIZA jogos já existentes pelo id — nunca cria ou
 * duplica nenhum registro.
 */
class RecebimentoViewModel(private val repository: JogoRepository) : ViewModel() {

    private val filtro = MutableStateFlow(FiltroJogos(status = FiltroStatus.A_RECEBER))
    private val selecionadosBrutos = MutableStateFlow<Set<Long>>(emptySet())
    private val processando = MutableStateFlow(false)
    private val mensagem = MutableStateFlow<String?>(null)

    val uiState: StateFlow<RecebimentoUiState> = combine(
        repository.observarJogos(), filtro, selecionadosBrutos, processando, mensagem,
    ) { jogos, filtroAtual, selecaoBruta, proc, msg ->
        val jogosFiltrados = jogos.filtrarEPesquisar(filtroAtual).ordenarMaisRecentePrimeiro()
        val jogosParaContadores = jogos.filtrarEPesquisar(filtroAtual.copy(status = FiltroStatus.TODOS))
        val idsVisiveis = jogosFiltrados.map { it.id }.toSet()
        val jogosPendentes = jogos.filter { !it.recebido }
        RecebimentoUiState(
            carregando = false,
            jogosFiltrados = jogosFiltrados,
            filtro = filtroAtual,
            competicoesDisponiveis = jogos.competicoesDisponiveis(),
            modalidadesDisponiveis = jogos.modalidadesDisponiveis(),
            cidadesDisponiveis = jogos.cidadesDisponiveis(),
            selecionados = selecaoBruta.intersect(idsVisiveis),
            processando = proc,
            mensagem = msg,
            totalPendentes = jogosParaContadores.count { !it.recebido },
            totalRecebidos = jogosParaContadores.count { it.recebido },
            totalGeral = jogosParaContadores.size,
            pendentesPorCompeticao = jogosPendentes.contarPorCompeticao(),
            pendentesPorModalidade = jogosPendentes.contarPorModalidade(),
            pendentesPorCidade = jogosPendentes.contarPorCidade(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecebimentoUiState())

    fun atualizarPesquisa(texto: String) {
        filtro.value = filtro.value.copy(pesquisa = texto)
    }

    fun atualizarStatus(status: FiltroStatus) {
        filtro.value = filtro.value.copy(status = status)
    }

    fun atualizarPeriodo(periodo: FiltroPeriodo) {
        filtro.value = filtro.value.copy(periodo = periodo)
    }

    fun atualizarCompeticao(competicao: String?) {
        filtro.value = filtro.value.copy(competicao = competicao)
    }

    fun atualizarModalidade(modalidade: String?) {
        filtro.value = filtro.value.copy(modalidade = modalidade)
    }

    fun atualizarCidade(cidade: String?) {
        filtro.value = filtro.value.copy(cidade = cidade)
    }

    /** Usado pelo botão "➕ Receber -> 🎯 Jogo": mostra a lista de pendentes sem nenhum
     * agrupamento aplicado, mantendo status/período/pesquisa como estavam. */
    fun limparAgrupamento() {
        filtro.value = filtro.value.copy(competicao = null, modalidade = null, cidade = null)
    }

    fun limparFiltros() {
        filtro.value = FiltroJogos(status = FiltroStatus.A_RECEBER)
    }

    /** Alterna a seleção de um jogo (toque longo para entrar no modo, toque simples depois). */
    fun alternarSelecao(jogoId: Long) {
        val atual = selecionadosBrutos.value
        selecionadosBrutos.value = if (jogoId in atual) atual - jogoId else atual + jogoId
    }

    /** "Selecionar todos" — todos os jogos exibidos com o filtro/pesquisa atuais. */
    fun selecionarTodosVisiveis() {
        selecionadosBrutos.value = uiState.value.jogosFiltrados.map { it.id }.toSet()
    }

    /** "Desmarcar todos" — limpa a seleção sem sair do modo de seleção sozinho (fica vazio = sai). */
    fun desmarcarTodos() {
        selecionadosBrutos.value = emptySet()
    }

    /** Baixa em lote: marca todos os jogos selecionados como recebidos, de uma única vez, sem duplicar nada.
     * Protegido contra duplo toque — se já houver uma marcação em andamento, ignora a chamada. */
    fun marcarSelecionadosComoRecebido(dataRecebimento: LocalDate = LocalDate.now()) {
        if (processando.value) return
        val estadoAtual = uiState.value
        val jogosParaMarcar = estadoAtual.jogosFiltrados.filter { it.id in estadoAtual.selecionados && !it.recebido }
        if (jogosParaMarcar.isEmpty()) return
        val quantidade = jogosParaMarcar.size
        processando.value = true
        viewModelScope.launch {
            repository.marcarVariosComoRecebido(jogosParaMarcar, dataRecebimento)
            selecionadosBrutos.value = emptySet()
            mensagem.value = "✓ Recebimento concluído — $quantidade " +
                "${if (quantidade == 1) "jogo marcado" else "jogos marcados"} como recebido(s) com sucesso."
            processando.value = false
        }
    }

    fun limparMensagem() {
        mensagem.value = null
    }
}

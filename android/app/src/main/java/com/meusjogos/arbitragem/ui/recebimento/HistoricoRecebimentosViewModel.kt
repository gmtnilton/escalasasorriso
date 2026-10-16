package com.meusjogos.arbitragem.ui.recebimento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meusjogos.arbitragem.core.logic.cidadesDisponiveis
import com.meusjogos.arbitragem.core.logic.competicoesDisponiveis
import com.meusjogos.arbitragem.core.logic.filtrarHistoricoRecebimentos
import com.meusjogos.arbitragem.core.logic.modalidadesDisponiveis
import com.meusjogos.arbitragem.core.logic.recebidos
import com.meusjogos.arbitragem.core.model.FiltroHistoricoRecebimentos
import com.meusjogos.arbitragem.core.model.FiltroPeriodo
import com.meusjogos.arbitragem.core.model.Jogo
import com.meusjogos.arbitragem.core.model.OrdenacaoRecebimento
import com.meusjogos.arbitragem.data.repository.JogoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HistoricoRecebimentosUiState(
    val carregando: Boolean = true,
    val jogosFiltrados: List<Jogo> = emptyList(),
    val filtro: FiltroHistoricoRecebimentos = FiltroHistoricoRecebimentos(),
    val competicoesDisponiveis: List<String> = emptyList(),
    val modalidadesDisponiveis: List<String> = emptyList(),
    val cidadesDisponiveis: List<String> = emptyList(),
    val totalFiltradoCentavos: Long = 0L,
)

/**
 * Tela "HISTÓRICO DE VALORES RECEBIDOS" (VERSÃO 1.4, seção 2/4/5) — lista, filtra e ordena os
 * jogos já recebidos (cada um é um pagamento, já que este app registra um recebimento por jogo).
 * Só lê — nunca cria nem altera nenhum lançamento (REGRA 8 da seção 8). O recebimento individual,
 * a edição e o recibo em PDF continuam em JogoDetailScreen/GerarReciboScreen, sem duplicação.
 */
class HistoricoRecebimentosViewModel(private val repository: JogoRepository) : ViewModel() {

    private val filtro = MutableStateFlow(FiltroHistoricoRecebimentos())

    val uiState: StateFlow<HistoricoRecebimentosUiState> = combine(
        repository.observarJogos(), filtro,
    ) { jogos, filtroAtual ->
        val recebidos = jogos.recebidos()
        val filtrados = jogos.filtrarHistoricoRecebimentos(filtroAtual)
        HistoricoRecebimentosUiState(
            carregando = false,
            jogosFiltrados = filtrados,
            filtro = filtroAtual,
            competicoesDisponiveis = recebidos.competicoesDisponiveis(),
            modalidadesDisponiveis = recebidos.modalidadesDisponiveis(),
            cidadesDisponiveis = recebidos.cidadesDisponiveis(),
            totalFiltradoCentavos = filtrados.sumOf { it.valorCentavos },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoricoRecebimentosUiState())

    fun atualizarPesquisa(texto: String) {
        filtro.value = filtro.value.copy(pesquisa = texto)
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

    fun atualizarOrdenacao(ordenacao: OrdenacaoRecebimento) {
        filtro.value = filtro.value.copy(ordenacao = ordenacao)
    }

    fun limparFiltros() {
        filtro.value = FiltroHistoricoRecebimentos()
    }
}

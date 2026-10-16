package com.meusjogos.arbitragem.ui.recebimento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meusjogos.arbitragem.core.logic.ItemHistoricoRecebimento
import com.meusjogos.arbitragem.core.logic.agruparHistoricoPorCompeticao
import com.meusjogos.arbitragem.core.logic.cidadesDisponiveis
import com.meusjogos.arbitragem.core.logic.competicoesDisponiveis
import com.meusjogos.arbitragem.core.logic.modalidadesDisponiveis
import com.meusjogos.arbitragem.core.logic.recebidos
import com.meusjogos.arbitragem.core.model.FiltroHistoricoRecebimentos
import com.meusjogos.arbitragem.core.model.FiltroPeriodo
import com.meusjogos.arbitragem.core.model.OrdenacaoRecebimento
import com.meusjogos.arbitragem.data.repository.CompeticaoRepository
import com.meusjogos.arbitragem.data.repository.JogoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HistoricoRecebimentosUiState(
    val carregando: Boolean = true,
    val itens: List<ItemHistoricoRecebimento> = emptyList(),
    val filtro: FiltroHistoricoRecebimentos = FiltroHistoricoRecebimentos(),
    val competicoesDisponiveis: List<String> = emptyList(),
    val modalidadesDisponiveis: List<String> = emptyList(),
    val cidadesDisponiveis: List<String> = emptyList(),
    val totalFiltradoCentavos: Long = 0L,
)

/**
 * Tela "HISTÓRICO DE VALORES RECEBIDOS" (VERSÃO 1.4, seção 2/4/5) — organizada por competição
 * (um card por competição, igual aos módulos "Competições em aberto"/"Competições encerradas a
 * receber"), com busca, filtros e ordenação sobre os pagamentos (jogos recebidos, já que este app
 * registra um recebimento por jogo). Só lê — nunca cria nem altera nenhum lançamento (REGRA 8 da
 * seção 8). O recebimento individual, a edição e o recibo em PDF continuam em
 * JogoDetailScreen/GerarReciboScreen, sem duplicação — acessados a partir da tela da competição.
 */
class HistoricoRecebimentosViewModel(
    private val repository: JogoRepository,
    private val competicaoRepository: CompeticaoRepository,
) : ViewModel() {

    private val filtro = MutableStateFlow(FiltroHistoricoRecebimentos())

    val uiState: StateFlow<HistoricoRecebimentosUiState> = combine(
        repository.observarJogos(), competicaoRepository.observarCompeticoes(), filtro,
    ) { jogos, competicoes, filtroAtual ->
        val recebidos = jogos.recebidos()
        val itens = jogos.agruparHistoricoPorCompeticao(filtroAtual, competicoes)
        HistoricoRecebimentosUiState(
            carregando = false,
            itens = itens,
            filtro = filtroAtual,
            competicoesDisponiveis = recebidos.competicoesDisponiveis(),
            modalidadesDisponiveis = recebidos.modalidadesDisponiveis(),
            cidadesDisponiveis = recebidos.cidadesDisponiveis(),
            totalFiltradoCentavos = itens.sumOf { it.valorRecebidoCentavos },
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

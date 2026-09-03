package com.meusjogos.arbitragem.ui.jogos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meusjogos.arbitragem.core.logic.competicoesDisponiveis
import com.meusjogos.arbitragem.core.logic.filtrarEPesquisar
import com.meusjogos.arbitragem.core.logic.funcoesDisponiveis
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

data class JogosListUiState(
    val carregando: Boolean = true,
    val jogosFiltrados: List<Jogo> = emptyList(),
    val filtro: FiltroJogos = FiltroJogos(),
    val competicoesDisponiveis: List<String> = emptyList(),
    val funcoesDisponiveis: List<String> = emptyList(),
    val totalSemFiltro: Int = 0,
)

class JogosListViewModel(private val repository: JogoRepository) : ViewModel() {

    private val filtro = MutableStateFlow(FiltroJogos())

    val uiState: StateFlow<JogosListUiState> = combine(repository.observarJogos(), filtro) { jogos, filtroAtual ->
        JogosListUiState(
            carregando = false,
            jogosFiltrados = jogos.filtrarEPesquisar(filtroAtual).ordenarMaisRecentePrimeiro(),
            filtro = filtroAtual,
            competicoesDisponiveis = jogos.competicoesDisponiveis(),
            funcoesDisponiveis = jogos.funcoesDisponiveis(),
            totalSemFiltro = jogos.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), JogosListUiState())

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

    fun atualizarFuncao(funcao: String?) {
        filtro.value = filtro.value.copy(funcao = funcao)
    }

    fun limparFiltros() {
        filtro.value = filtro.value.copy(
            status = FiltroStatus.TODOS,
            periodo = FiltroPeriodo.Todos,
            competicao = null,
            funcao = null,
        )
    }
}

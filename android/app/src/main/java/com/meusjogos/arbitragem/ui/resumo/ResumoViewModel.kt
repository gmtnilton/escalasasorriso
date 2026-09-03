package com.meusjogos.arbitragem.ui.resumo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meusjogos.arbitragem.core.logic.anosDisponiveis
import com.meusjogos.arbitragem.core.logic.estatisticas
import com.meusjogos.arbitragem.core.logic.resumoDoAno
import com.meusjogos.arbitragem.core.logic.resumoDoMes
import com.meusjogos.arbitragem.core.logic.serieMensal
import com.meusjogos.arbitragem.core.model.Estatisticas
import com.meusjogos.arbitragem.core.model.PontoMensal
import com.meusjogos.arbitragem.core.model.ResumoAnual
import com.meusjogos.arbitragem.core.model.ResumoPeriodo
import com.meusjogos.arbitragem.data.repository.JogoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate

data class SelecaoPeriodo(val ano: Int, val mes: Int)

data class ResumoUiState(
    val carregando: Boolean = true,
    val anoSelecionado: Int = LocalDate.now().year,
    val mesSelecionado: Int = LocalDate.now().monthValue,
    val anosDisponiveis: List<Int> = listOf(LocalDate.now().year),
    val resumoMensal: ResumoPeriodo = ResumoPeriodo(0, 0, 0),
    val resumoAnual: ResumoAnual = ResumoAnual(LocalDate.now().year, 0, 0, 0),
    val estatisticasGerais: Estatisticas = Estatisticas.VAZIO,
    val serieMensal: List<PontoMensal> = emptyList(),
)

class ResumoViewModel(repository: JogoRepository) : ViewModel() {

    private val selecao = MutableStateFlow(SelecaoPeriodo(LocalDate.now().year, LocalDate.now().monthValue))

    val uiState: StateFlow<ResumoUiState> = combine(repository.observarJogos(), selecao) { jogos, sel ->
        ResumoUiState(
            carregando = false,
            anoSelecionado = sel.ano,
            mesSelecionado = sel.mes,
            anosDisponiveis = (jogos.anosDisponiveis() + LocalDate.now().year).distinct().sortedDescending(),
            resumoMensal = jogos.resumoDoMes(sel.ano, sel.mes),
            resumoAnual = jogos.resumoDoAno(sel.ano),
            estatisticasGerais = jogos.estatisticas(),
            serieMensal = jogos.serieMensal(sel.ano),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ResumoUiState())

    fun selecionarMes(mes: Int) = selecao.update { it.copy(mes = mes) }

    fun selecionarAno(ano: Int) = selecao.update { it.copy(ano = ano) }
}

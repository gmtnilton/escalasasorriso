package com.meusjogos.arbitragem.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meusjogos.arbitragem.core.logic.quantidadeAReceber
import com.meusjogos.arbitragem.core.logic.quantidadeRecebidos
import com.meusjogos.arbitragem.core.logic.resumoDoMes
import com.meusjogos.arbitragem.core.logic.totalAReceberCentavos
import com.meusjogos.arbitragem.core.logic.totalGeralCentavos
import com.meusjogos.arbitragem.core.logic.totalRecebidoCentavos
import com.meusjogos.arbitragem.core.model.ResumoPeriodo
import com.meusjogos.arbitragem.core.util.DateUtils
import com.meusjogos.arbitragem.data.repository.CompeticaoRepository
import com.meusjogos.arbitragem.data.repository.JogoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class DashboardUiState(
    val carregando: Boolean = true,
    val totalAReceberCentavos: Long = 0L,
    val totalRecebidoCentavos: Long = 0L,
    val totalGeralCentavos: Long = 0L,
    val totalJogos: Int = 0,
    val jogosRecebidos: Int = 0,
    val jogosAReceber: Int = 0,
    /** REGRA nova — quantidade de competições 🟢 em andamento e 🔴 encerradas, exibidas no Início
     * logo após a quantidade total de jogos. */
    val competicoesAbertas: Int = 0,
    val competicoesEncerradas: Int = 0,
    val resumoPeriodoAtual: ResumoPeriodo = ResumoPeriodo(0, 0, 0),
    val rotuloPeriodoAtual: String = "",
)

class DashboardViewModel(
    repository: JogoRepository,
    competicaoRepository: CompeticaoRepository,
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.observarJogos(),
        competicaoRepository.observarCompeticoes(),
    ) { jogos, competicoes ->
        val hoje = LocalDate.now()
        DashboardUiState(
            carregando = false,
            totalAReceberCentavos = jogos.totalAReceberCentavos(),
            totalRecebidoCentavos = jogos.totalRecebidoCentavos(),
            totalGeralCentavos = jogos.totalGeralCentavos(),
            totalJogos = jogos.size,
            jogosRecebidos = jogos.quantidadeRecebidos(),
            jogosAReceber = jogos.quantidadeAReceber(),
            competicoesAbertas = competicoes.count { !it.encerrada },
            competicoesEncerradas = competicoes.count { it.encerrada },
            resumoPeriodoAtual = jogos.resumoDoMes(hoje.year, hoje.monthValue),
            rotuloPeriodoAtual = DateUtils.nomeMesAno(hoje.year, hoje.monthValue),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())
}

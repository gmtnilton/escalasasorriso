package com.meusjogos.arbitragem.ui.competicao

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meusjogos.arbitragem.core.logic.CompeticaoComResumo
import com.meusjogos.arbitragem.core.logic.encerradasComPendencia
import com.meusjogos.arbitragem.data.repository.CompeticaoRepository
import com.meusjogos.arbitragem.data.repository.JogoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class CompeticoesEncerradasPendentesUiState(
    val carregando: Boolean = true,
    val itens: List<CompeticaoComResumo> = emptyList(),
)

/**
 * Tela "COMPETIÇÕES ENCERRADAS A RECEBER" (VERSÃO 1.3) — lista todas as competições já
 * ENCERRADAS que ainda têm saldo pendente, sempre recalculada a partir dos jogos e competições
 * atuais (reage sozinha a qualquer recebimento, edição, encerramento ou reabertura).
 */
class CompeticoesEncerradasPendentesViewModel(
    jogoRepository: JogoRepository,
    competicaoRepository: CompeticaoRepository,
) : ViewModel() {

    val uiState: StateFlow<CompeticoesEncerradasPendentesUiState> = combine(
        jogoRepository.observarJogos(),
        competicaoRepository.observarCompeticoes(),
    ) { jogos, competicoes ->
        CompeticoesEncerradasPendentesUiState(
            carregando = false,
            itens = competicoes.encerradasComPendencia(jogos),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CompeticoesEncerradasPendentesUiState())
}

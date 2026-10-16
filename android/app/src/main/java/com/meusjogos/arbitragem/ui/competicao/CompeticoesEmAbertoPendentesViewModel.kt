package com.meusjogos.arbitragem.ui.competicao

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meusjogos.arbitragem.core.logic.CompeticaoComResumo
import com.meusjogos.arbitragem.core.logic.emAndamentoComPendencia
import com.meusjogos.arbitragem.core.logic.jogosDaCompeticaoId
import com.meusjogos.arbitragem.data.repository.CompeticaoRepository
import com.meusjogos.arbitragem.data.repository.JogoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

/** Um item da lista "Competições em aberto a receber" — [primeiroJogo] é a data do jogo mais
 * antigo da competição (usada como "data de início", já que não há um campo próprio para isso). */
data class ItemCompeticaoAberta(val item: CompeticaoComResumo, val primeiroJogo: LocalDate?)

data class CompeticoesEmAbertoPendentesUiState(
    val carregando: Boolean = true,
    val itens: List<ItemCompeticaoAberta> = emptyList(),
)

/**
 * Tela "COMPETIÇÕES EM ABERTO A RECEBER" (VERSÃO 1.4, seção 1) — espelha
 * [CompeticoesEncerradasPendentesViewModel] para as competições ainda EM ANDAMENTO: some da lista
 * tão logo a competição seja encerrada (passa para o módulo de encerradas, se ainda houver
 * pendência) ou tão logo o saldo pendente chegue a zero.
 */
class CompeticoesEmAbertoPendentesViewModel(
    jogoRepository: JogoRepository,
    competicaoRepository: CompeticaoRepository,
) : ViewModel() {

    val uiState: StateFlow<CompeticoesEmAbertoPendentesUiState> = combine(
        jogoRepository.observarJogos(),
        competicaoRepository.observarCompeticoes(),
    ) { jogos, competicoes ->
        val itens = competicoes.emAndamentoComPendencia(jogos).map { item ->
            val primeiroJogo = jogos.jogosDaCompeticaoId(item.competicao.id).minOfOrNull { it.data }
            ItemCompeticaoAberta(item, primeiroJogo)
        }
        CompeticoesEmAbertoPendentesUiState(carregando = false, itens = itens)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CompeticoesEmAbertoPendentesUiState())
}

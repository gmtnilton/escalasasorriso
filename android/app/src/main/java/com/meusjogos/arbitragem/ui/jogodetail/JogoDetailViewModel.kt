package com.meusjogos.arbitragem.ui.jogodetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meusjogos.arbitragem.core.model.Jogo
import com.meusjogos.arbitragem.data.repository.JogoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class JogoDetailUiState(
    val carregando: Boolean = true,
    val jogo: Jogo? = null,
    val excluido: Boolean = false,
)

class JogoDetailViewModel(
    private val repository: JogoRepository,
    private val jogoId: Long,
) : ViewModel() {

    private val excluido = MutableStateFlow(false)

    /**
     * Observa a base inteira (como as demais telas) em vez de carregar o
     * jogo uma única vez — assim, ao voltar de "Editar", os detalhes já
     * aparecem atualizados automaticamente, sem precisar recarregar.
     */
    val uiState: StateFlow<JogoDetailUiState> = combine(repository.observarJogos(), excluido) { jogos, foiExcluido ->
        JogoDetailUiState(
            carregando = false,
            jogo = jogos.firstOrNull { it.id == jogoId },
            excluido = foiExcluido,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), JogoDetailUiState())

    /** REGRA 11: marca como recebido e registra a data (hoje, por padrão — editável antes de confirmar). */
    fun marcarComoRecebido(dataRecebimento: LocalDate = LocalDate.now()) {
        val jogo = uiState.value.jogo ?: return
        viewModelScope.launch { repository.marcarComoRecebido(jogo, dataRecebimento) }
    }

    /** REGRA 12: RECEBIDO -> A RECEBER, devolvendo o valor para o total pendente. */
    fun desfazerRecebimento() {
        val jogo = uiState.value.jogo ?: return
        viewModelScope.launch { repository.desfazerRecebimento(jogo) }
    }

    fun excluir() {
        val jogo = uiState.value.jogo ?: return
        viewModelScope.launch {
            repository.excluir(jogo)
            excluido.value = true
        }
    }

    /** REGRA 9/10: cria a cópia (sempre A RECEBER) e informa o novo id para abrir a edição. */
    fun duplicar(onDuplicado: (Long) -> Unit) {
        val jogo = uiState.value.jogo ?: return
        viewModelScope.launch {
            val copia = repository.duplicar(jogo)
            onDuplicado(copia.id)
        }
    }
}

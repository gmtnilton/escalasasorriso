package com.meusjogos.arbitragem.ui.competicao

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meusjogos.arbitragem.core.logic.jogosDaCompeticaoId
import com.meusjogos.arbitragem.core.logic.ordenarMaisRecentePrimeiro
import com.meusjogos.arbitragem.core.logic.resumoDaCompeticao
import com.meusjogos.arbitragem.core.model.Competicao
import com.meusjogos.arbitragem.core.model.Jogo
import com.meusjogos.arbitragem.core.model.ResumoCompeticao
import com.meusjogos.arbitragem.data.recibo.ReciboCompeticaoRepository
import com.meusjogos.arbitragem.data.repository.CompeticaoRepository
import com.meusjogos.arbitragem.data.repository.JogoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class CompeticaoDetailUiState(
    val carregando: Boolean = true,
    val competicao: Competicao? = null,
    val resumo: ResumoCompeticao = ResumoCompeticao.VAZIO,
    val jogos: List<Jogo> = emptyList(),
    val temRecibo: Boolean = false,
    val processando: Boolean = false,
    val mensagem: String? = null,
)

/**
 * Tela da competição (VERSÃO 1.2): resumo, lista de jogos, encerrar/reabrir,
 * recebimento total/parcial e editar (com unificação quando o novo
 * nome/cidade/modalidade coincidir com outra competição já existente).
 */
class CompeticaoDetailViewModel(
    private val jogoRepository: JogoRepository,
    private val competicaoRepository: CompeticaoRepository,
    private val reciboCompeticaoRepository: ReciboCompeticaoRepository,
    competicaoIdInicial: Long,
) : ViewModel() {

    var competicaoIdAtual: Long = competicaoIdInicial
        private set

    private val processando = MutableStateFlow(false)
    private val mensagem = MutableStateFlow<String?>(null)
    private val _uiState = MutableStateFlow(CompeticaoDetailUiState())
    val uiState: StateFlow<CompeticaoDetailUiState> = _uiState.asStateFlow()

    private var jobObservacao: Job? = null

    init {
        observar()
        atualizarTemRecibo()
    }

    private fun observar() {
        jobObservacao?.cancel()
        jobObservacao = viewModelScope.launch {
            combine(
                jogoRepository.observarJogos(),
                competicaoRepository.observarCompeticoes(),
                processando,
                mensagem,
            ) { jogos, competicoes, proc, msg ->
                val jogosDaCompeticao = jogos.jogosDaCompeticaoId(competicaoIdAtual).ordenarMaisRecentePrimeiro()
                CompeticaoDetailUiState(
                    carregando = false,
                    competicao = competicoes.firstOrNull { it.id == competicaoIdAtual },
                    resumo = jogos.resumoDaCompeticao(competicaoIdAtual),
                    jogos = jogosDaCompeticao,
                    temRecibo = _uiState.value.temRecibo,
                    processando = proc,
                    mensagem = msg,
                )
            }.collect { novoEstado -> _uiState.value = novoEstado }
        }
    }

    private fun atualizarTemRecibo() {
        viewModelScope.launch {
            val temRecibo = reciboCompeticaoRepository.buscarPorCompeticaoId(competicaoIdAtual) != null
            _uiState.update { it.copy(temRecibo = temRecibo) }
        }
    }

    /**
     * Encerra a competição; quando [marcarComoRecebido] é true (usuário já marcou "já recebi o
     * pagamento" na confirmação de encerramento), também marca de uma vez todos os jogos ainda
     * pendentes como recebidos — evita precisar de um segundo toque em "RECEBIMENTO TOTAL".
     */
    fun encerrar(marcarComoRecebido: Boolean = false) {
        val competicao = uiState.value.competicao ?: return
        val pendentes = if (marcarComoRecebido) uiState.value.jogos.filter { !it.recebido } else emptyList()
        viewModelScope.launch {
            competicaoRepository.encerrar(competicao)
            if (pendentes.isNotEmpty()) {
                jogoRepository.marcarVariosComoRecebido(pendentes, LocalDate.now())
                mensagem.value = "✓ Competição encerrada e pagamento marcado como recebido."
            }
        }
    }

    fun reabrir() {
        val competicao = uiState.value.competicao ?: return
        viewModelScope.launch { competicaoRepository.reabrir(competicao) }
    }

    /** REGRA 9/10/11 — recebimento total (ou parcial, se parte já estava recebida): marca só os
     * jogos ainda A RECEBER; protegido contra duplo toque. */
    fun receberPendentes(dataRecebimento: LocalDate = LocalDate.now()) {
        if (processando.value) return
        val pendentes = uiState.value.jogos.filter { !it.recebido }
        if (pendentes.isEmpty()) return
        processando.value = true
        viewModelScope.launch {
            jogoRepository.marcarVariosComoRecebido(pendentes, dataRecebimento)
            mensagem.value = "✓ Recebimento registrado — ${pendentes.size} ${if (pendentes.size == 1) "jogo marcado" else "jogos marcados"} como recebido(s)."
            processando.value = false
        }
    }

    fun limparMensagem() {
        mensagem.value = null
    }

    /**
     * REGRA 2 — edita nome/cidade/modalidade; se unificar com outra competição já existente,
     * passa a observar a sobrevivente. Retorna o id final (pode ter mudado) para a tela navegar
     * de volta a esta mesma rota apontando para o id certo.
     */
    suspend fun editar(novoNome: String, novaCidade: String?, novaModalidade: String?): Long {
        val competicao = uiState.value.competicao ?: return competicaoIdAtual
        val sobrevivente = competicaoRepository.editar(competicao, novoNome, novaCidade, novaModalidade)
        competicaoIdAtual = sobrevivente.id
        observar()
        atualizarTemRecibo()
        return sobrevivente.id
    }
}

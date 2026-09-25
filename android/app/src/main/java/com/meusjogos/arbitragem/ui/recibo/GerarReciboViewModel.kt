package com.meusjogos.arbitragem.ui.recibo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meusjogos.arbitragem.core.model.Jogo
import com.meusjogos.arbitragem.core.model.Recibo
import com.meusjogos.arbitragem.core.util.DateUtils
import com.meusjogos.arbitragem.data.recibo.ReciboPdfGenerator
import com.meusjogos.arbitragem.data.recibo.ReciboRepository
import com.meusjogos.arbitragem.data.repository.JogoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.time.LocalDate

private const val NOME_RECEBEDOR_PADRAO = "NILTON RODRIGO RIBEIRO"

data class GerarReciboUiState(
    val carregando: Boolean = true,
    val jogo: Jogo? = null,
    val existeRecibo: Boolean = false,
    val pagadorNome: String = "",
    val pagadorDocumento: String = "",
    val recebedorNome: String = NOME_RECEBEDOR_PADRAO,
    val recebedorDocumento: String = "",
    val valorCentavos: Long = 0L,
    val dataPagamentoTexto: String = "",
    val descricao: String = "",
    val numeroFormatado: String? = null,
) {
    val tituloBotaoPrincipal: String
        get() = if (existeRecibo) "🧾 Atualizar e compartilhar" else "🧾 Gerar recibo em PDF"
}

/**
 * Formulário de recibo de pagamento de um jogo já recebido (REGRA 13/14) —
 * só é aberto a partir do detalhe de um jogo RECEBIDO. Reaproveita o mesmo
 * recibo do jogo (por jogoId) sempre que já existir um, tanto para
 * pré-preencher o formulário quanto para salvar (nunca cria um segundo
 * recibo para o mesmo jogo).
 */
class GerarReciboViewModel(
    private val jogoRepository: JogoRepository,
    private val reciboRepository: ReciboRepository,
    private val jogoId: Long,
) : ViewModel() {

    private val pdfGenerator = ReciboPdfGenerator()

    private val _uiState = MutableStateFlow(GerarReciboUiState())
    val uiState: StateFlow<GerarReciboUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val jogo = jogoRepository.buscarPorId(jogoId)
            val reciboExistente = reciboRepository.buscarPorJogoId(jogoId)
            _uiState.update {
                if (reciboExistente != null) {
                    it.copy(
                        carregando = false,
                        jogo = jogo,
                        existeRecibo = true,
                        pagadorNome = reciboExistente.pagadorNome,
                        pagadorDocumento = reciboExistente.pagadorDocumento,
                        recebedorNome = reciboExistente.recebedorNome,
                        recebedorDocumento = reciboExistente.recebedorDocumento,
                        valorCentavos = reciboExistente.valorCentavos,
                        dataPagamentoTexto = DateUtils.formatarData(reciboExistente.dataPagamento),
                        descricao = reciboExistente.descricao ?: "",
                        numeroFormatado = reciboExistente.numeroFormatado,
                    )
                } else {
                    it.copy(
                        carregando = false,
                        jogo = jogo,
                        existeRecibo = false,
                        valorCentavos = jogo?.valorCentavos ?: 0L,
                        dataPagamentoTexto = DateUtils.formatarData(jogo?.dataRecebimento ?: LocalDate.now()),
                        descricao = jogo?.confronto?.let { confronto ->
                            "Pagamento referente à arbitragem da partida $confronto."
                        } ?: "",
                    )
                }
            }
        }
    }

    fun atualizarPagadorNome(texto: String) = _uiState.update { it.copy(pagadorNome = texto) }
    fun atualizarPagadorDocumento(texto: String) = _uiState.update { it.copy(pagadorDocumento = texto) }
    fun atualizarRecebedorNome(texto: String) = _uiState.update { it.copy(recebedorNome = texto) }
    fun atualizarRecebedorDocumento(texto: String) = _uiState.update { it.copy(recebedorDocumento = texto) }
    fun atualizarValor(centavos: Long) = _uiState.update { it.copy(valorCentavos = centavos) }
    fun atualizarDataPagamento(texto: String) = _uiState.update { it.copy(dataPagamentoTexto = texto) }
    fun atualizarDescricao(texto: String) = _uiState.update { it.copy(descricao = texto) }

    fun nomeArquivoPdf(): String = ReciboPdfGenerator.nomeArquivo(jogoId)

    /** Salva (ou atualiza) o recibo deste jogo — sempre o mesmo registro por jogoId, nunca duplica. */
    suspend fun salvarRecibo(): Recibo {
        val estado = _uiState.value
        val recibo = Recibo(
            jogoId = jogoId,
            pagadorNome = estado.pagadorNome.trim(),
            pagadorDocumento = estado.pagadorDocumento.trim(),
            recebedorNome = estado.recebedorNome.trim(),
            recebedorDocumento = estado.recebedorDocumento.trim(),
            valorCentavos = estado.valorCentavos,
            dataPagamento = DateUtils.parseData(estado.dataPagamentoTexto) ?: LocalDate.now(),
            descricao = estado.descricao.trim().ifBlank { null },
        )
        val salvo = reciboRepository.salvar(recibo)
        _uiState.update { it.copy(existeRecibo = true, numeroFormatado = salvo.numeroFormatado) }
        return salvo
    }

    suspend fun gerarPdf(recibo: Recibo, saida: OutputStream) {
        pdfGenerator.gerar(recibo, _uiState.value.jogo, saida)
    }
}

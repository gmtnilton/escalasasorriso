package com.meusjogos.arbitragem.ui.competicao

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meusjogos.arbitragem.core.logic.jogosDaCompeticaoId
import com.meusjogos.arbitragem.core.logic.resumoDaCompeticao
import com.meusjogos.arbitragem.core.model.Competicao
import com.meusjogos.arbitragem.core.model.Jogo
import com.meusjogos.arbitragem.core.model.ReciboCompeticao
import com.meusjogos.arbitragem.core.util.DateUtils
import com.meusjogos.arbitragem.data.recibo.ReciboCompeticaoPdfGenerator
import com.meusjogos.arbitragem.data.recibo.ReciboCompeticaoRepository
import com.meusjogos.arbitragem.data.repository.CompeticaoRepository
import com.meusjogos.arbitragem.data.repository.JogoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.time.LocalDate

private const val NOME_RECEBEDOR_PADRAO = "NILTON RODRIGO RIBEIRO"

data class CompeticaoReciboUiState(
    val carregando: Boolean = true,
    val competicao: Competicao? = null,
    val existeRecibo: Boolean = false,
    val pagadorNome: String = "",
    val pagadorDocumento: String = "",
    val recebedorNome: String = NOME_RECEBEDOR_PADRAO,
    val recebedorDocumento: String = "",
    val valorCentavos: Long = 0L,
    val quantidadeJogos: Int = 0,
    val dataPagamentoTexto: String = "",
    val descricao: String = "",
    val numeroFormatado: String? = null,
) {
    val tituloBotaoPrincipal: String
        get() = if (existeRecibo) "📄 Atualizar e compartilhar" else "📄 Gerar recibo em PDF"
}

/** Recibo do RECEBIMENTO TOTAL/PARCIAL de uma competição (VERSÃO 1.2, REGRA 12) — mesmo padrão
 * de upsert por id do recibo por jogo (GerarReciboViewModel): nunca duplica o recibo. */
class CompeticaoReciboViewModel(
    private val jogoRepository: JogoRepository,
    private val competicaoRepository: CompeticaoRepository,
    private val reciboCompeticaoRepository: ReciboCompeticaoRepository,
    private val competicaoId: Long,
) : ViewModel() {

    private val pdfGenerator = ReciboCompeticaoPdfGenerator()

    private val _uiState = MutableStateFlow(CompeticaoReciboUiState())
    val uiState: StateFlow<CompeticaoReciboUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val competicao = competicaoRepository.buscarPorId(competicaoId)
            val jogos = jogoRepository.observarJogos().first()
            val resumo = jogos.resumoDaCompeticao(competicaoId)
            val reciboExistente = reciboCompeticaoRepository.buscarPorCompeticaoId(competicaoId)
            _uiState.update {
                if (reciboExistente != null) {
                    it.copy(
                        carregando = false,
                        competicao = competicao,
                        existeRecibo = true,
                        pagadorNome = reciboExistente.pagadorNome,
                        pagadorDocumento = reciboExistente.pagadorDocumento,
                        recebedorNome = reciboExistente.recebedorNome,
                        recebedorDocumento = reciboExistente.recebedorDocumento,
                        valorCentavos = reciboExistente.valorCentavos,
                        quantidadeJogos = reciboExistente.quantidadeJogos,
                        dataPagamentoTexto = DateUtils.formatarData(reciboExistente.dataPagamento),
                        descricao = reciboExistente.descricao ?: "",
                        numeroFormatado = reciboExistente.numeroFormatado,
                    )
                } else {
                    it.copy(
                        carregando = false,
                        competicao = competicao,
                        existeRecibo = false,
                        valorCentavos = resumo.valorRecebidoCentavos,
                        quantidadeJogos = resumo.jogosRecebidos,
                        dataPagamentoTexto = DateUtils.formatarData(LocalDate.now()),
                        descricao = competicao?.let { c -> "Pagamento referente ao recebimento da competição ${c.nome}." } ?: "",
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

    fun nomeArquivoPdf(): String = ReciboCompeticaoPdfGenerator.nomeArquivo(competicaoId)

    suspend fun salvarRecibo(): ReciboCompeticao {
        val estado = _uiState.value
        val recibo = ReciboCompeticao(
            competicaoId = competicaoId,
            pagadorNome = estado.pagadorNome.trim(),
            pagadorDocumento = estado.pagadorDocumento.trim(),
            recebedorNome = estado.recebedorNome.trim(),
            recebedorDocumento = estado.recebedorDocumento.trim(),
            valorCentavos = estado.valorCentavos,
            quantidadeJogos = estado.quantidadeJogos,
            dataPagamento = DateUtils.parseData(estado.dataPagamentoTexto) ?: LocalDate.now(),
            descricao = estado.descricao.trim().ifBlank { null },
        )
        val salvo = reciboCompeticaoRepository.salvar(recibo)
        _uiState.update { it.copy(existeRecibo = true, numeroFormatado = salvo.numeroFormatado) }
        return salvo
    }

    suspend fun gerarPdf(recibo: ReciboCompeticao, saida: OutputStream) {
        val jogosRecebidos: List<Jogo> = jogoRepository.observarJogos().first().jogosDaCompeticaoId(competicaoId).filter { it.recebido }
        pdfGenerator.gerar(recibo, _uiState.value.competicao, jogosRecebidos, saida)
    }
}

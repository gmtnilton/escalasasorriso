package com.meusjogos.arbitragem.data.recibo

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.meusjogos.arbitragem.core.model.Competicao
import com.meusjogos.arbitragem.core.model.Jogo
import com.meusjogos.arbitragem.core.model.ReciboCompeticao
import com.meusjogos.arbitragem.core.util.CurrencyUtils
import com.meusjogos.arbitragem.core.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.time.LocalDate

/**
 * Gera o recibo do RECEBIMENTO TOTAL/PARCIAL de uma competição em PDF
 * (VERSÃO 1.2, REGRA 12) — mesmo estilo visual do recibo por jogo
 * ([ReciboPdfGenerator]), mas identificado como "RECIBO DE PAGAMENTO —
 * COMPETIÇÃO" e com a lista dos jogos abrangidos pelo pagamento. Paginação
 * simples: quando a lista de jogos não cabe numa página, continua na
 * próxima.
 */
class ReciboCompeticaoPdfGenerator {

    /** Mantém o estado da página/canvas atual do documento, avançando de página sozinho quando o
     * conteúdo não cabe mais — evita desenhar por engano num canvas de uma página já finalizada. */
    private class EstadoDocumento(private val documento: PdfDocument) {
        private var pagina: PdfDocument.Page = iniciarPagina()
        var canvas: Canvas = pagina.canvas
            private set
        var y: Float = MARGEM + 18f
            private set

        private fun iniciarPagina(): PdfDocument.Page =
            documento.startPage(PdfDocument.PageInfo.Builder(LARGURA_A4, ALTURA_A4, documento.pages.size + 1).create())

        fun garantirEspaco(alturaNecessaria: Float) {
            if (y + alturaNecessaria <= ALTURA_A4 - MARGEM) return
            documento.finishPage(pagina)
            pagina = iniciarPagina()
            canvas = pagina.canvas
            y = MARGEM + 18f
        }

        fun avancar(dy: Float) {
            y += dy
        }

        fun finalizar() {
            documento.finishPage(pagina)
        }
    }

    suspend fun gerar(
        recibo: ReciboCompeticao,
        competicao: Competicao?,
        jogosPagos: List<Jogo>,
        saida: OutputStream,
    ): Unit = withContext(Dispatchers.Default) {
        val documento = PdfDocument()
        val estado = EstadoDocumento(documento)

        desenharCabecalho(estado, recibo)
        desenharCaixaValor(estado, recibo)
        desenharSecaoPagadorRecebedor(estado, recibo)
        desenharReferenteA(estado, recibo, competicao)
        if (jogosPagos.isNotEmpty()) desenharListaJogos(estado, jogosPagos)
        estado.garantirEspaco(70f)
        desenharAssinaturas(estado)

        estado.finalizar()
        documento.writeTo(saida)
        documento.close()
    }

    private fun desenharCabecalho(estado: EstadoDocumento, recibo: ReciboCompeticao) {
        val pTituloGrande = Paint().apply { textSize = 18f; isFakeBoldText = true; color = COR_TITULO }
        val pSubtitulo = Paint().apply { textSize = 11f; isFakeBoldText = true; color = COR_DOURADO }
        val pDireita = Paint().apply { textSize = 9.5f; color = COR_MUTED; textAlign = Paint.Align.RIGHT }

        estado.canvas.drawText("RECIBO DE PAGAMENTO", MARGEM, estado.y, pTituloGrande)
        estado.canvas.drawText(recibo.numeroFormatado, LARGURA_A4 - MARGEM, estado.y, pDireita)
        estado.avancar(15f)
        estado.canvas.drawText("COMPETIÇÃO", MARGEM, estado.y, pSubtitulo)
        estado.canvas.drawText("Emitido em ${DateUtils.formatarData(LocalDate.now())}", LARGURA_A4 - MARGEM, estado.y, pDireita)
        estado.avancar(18f)
        estado.canvas.drawLine(MARGEM, estado.y, LARGURA_A4 - MARGEM, estado.y, Paint().apply { color = COR_DOURADO; strokeWidth = 1.4f })
        estado.avancar(26f)
    }

    private fun desenharCaixaValor(estado: EstadoDocumento, recibo: ReciboCompeticao) {
        val alturaCaixaValor = 64f
        estado.canvas.drawRect(MARGEM, estado.y, LARGURA_A4 - MARGEM, estado.y + alturaCaixaValor, Paint().apply { color = COR_FUNDO_VALOR })
        val centroX = LARGURA_A4 / 2f
        estado.canvas.drawText(
            "VALOR PAGO",
            centroX,
            estado.y + 22f,
            Paint().apply { textSize = 10f; color = COR_MUTED; textAlign = Paint.Align.CENTER },
        )
        estado.canvas.drawText(
            CurrencyUtils.formatar(recibo.valorCentavos),
            centroX,
            estado.y + 48f,
            Paint().apply { textSize = 22f; isFakeBoldText = true; color = COR_TITULO; textAlign = Paint.Align.CENTER },
        )
        estado.avancar(alturaCaixaValor + 28f)
    }

    private fun desenharSecaoPagadorRecebedor(estado: EstadoDocumento, recibo: ReciboCompeticao) {
        secao(estado, "Pagador")
        linha(estado, "Nome / Razão social", recibo.pagadorNome)
        linha(estado, "CPF / CNPJ", recibo.pagadorDocumento)
        estado.avancar(6f)
        secao(estado, "Recebedor")
        linha(estado, "Nome completo", recibo.recebedorNome)
        linha(estado, "CPF", recibo.recebedorDocumento)
        estado.avancar(6f)
    }

    private fun desenharReferenteA(estado: EstadoDocumento, recibo: ReciboCompeticao, competicao: Competicao?) {
        secao(estado, "Referente a")
        linha(estado, "Data do pagamento", DateUtils.formatarData(recibo.dataPagamento))
        if (competicao != null) {
            linha(estado, "Competição", competicao.nome)
            val identificacao = listOfNotNull(
                competicao.modalidade?.takeIf(String::isNotBlank),
                competicao.cidade?.takeIf(String::isNotBlank),
            ).joinToString(" — ")
            if (identificacao.isNotBlank()) linha(estado, "Modalidade / Cidade", identificacao)
        }
        linha(estado, "Quantidade de jogos pagos", recibo.quantidadeJogos.toString())
        recibo.descricao?.takeIf(String::isNotBlank)?.let { linha(estado, "Descrição", it) }
    }

    private fun desenharListaJogos(estado: EstadoDocumento, jogos: List<Jogo>) {
        estado.garantirEspaco(26f)
        secao(estado, "Jogos abrangidos por este pagamento (${jogos.size})")

        val pLinhaTexto = Paint().apply { textSize = 9.5f; color = COR_TEXTO }
        val pLinhaValor = Paint().apply { textSize = 9.5f; color = COR_TEXTO; textAlign = Paint.Align.RIGHT }
        for (jogo in jogos) {
            estado.garantirEspaco(16f)
            val confronto = jogo.confronto ?: "Escala Arbitragem"
            val descricaoJogo = "${DateUtils.formatarDataCurta(jogo.data)} — $confronto"
            estado.canvas.drawText(descricaoJogo.take(70), MARGEM, estado.y, pLinhaTexto)
            estado.canvas.drawText(CurrencyUtils.formatar(jogo.valorCentavos), LARGURA_A4 - MARGEM, estado.y, pLinhaValor)
            estado.avancar(15f)
        }
    }

    private fun desenharAssinaturas(estado: EstadoDocumento) {
        estado.avancar(30f)
        val larguraAssinatura = 200f
        val centroEsquerda = MARGEM + larguraAssinatura / 2
        val centroDireita = LARGURA_A4 - MARGEM - larguraAssinatura / 2
        val pLinha = Paint().apply { color = COR_TEXTO; strokeWidth = 0.8f }
        estado.canvas.drawLine(MARGEM, estado.y, MARGEM + larguraAssinatura, estado.y, pLinha)
        estado.canvas.drawLine(LARGURA_A4 - MARGEM - larguraAssinatura, estado.y, LARGURA_A4 - MARGEM, estado.y, pLinha)
        estado.avancar(14f)
        val pRotulo = Paint().apply { textSize = 9f; color = COR_MUTED; textAlign = Paint.Align.CENTER }
        estado.canvas.drawText("Assinatura do pagador", centroEsquerda, estado.y, pRotulo)
        estado.canvas.drawText("Assinatura do recebedor", centroDireita, estado.y, pRotulo)
    }

    private fun secao(estado: EstadoDocumento, nome: String) {
        estado.garantirEspaco(31f)
        estado.canvas.drawText(nome, MARGEM, estado.y, Paint().apply { textSize = 11.5f; isFakeBoldText = true; color = COR_TITULO })
        estado.avancar(7f)
        estado.canvas.drawLine(MARGEM, estado.y, LARGURA_A4 - MARGEM, estado.y, Paint().apply { color = COR_LINHA; strokeWidth = 0.8f })
        estado.avancar(16f)
    }

    private fun linha(estado: EstadoDocumento, campo: String, texto: String) {
        estado.garantirEspaco(31f)
        estado.canvas.drawText(campo, MARGEM, estado.y, Paint().apply { textSize = 9f; color = COR_MUTED })
        estado.avancar(13f)
        estado.canvas.drawText(texto.ifBlank { "—" }, MARGEM, estado.y, Paint().apply { textSize = 11f; color = COR_TEXTO })
        estado.avancar(18f)
    }

    companion object {
        private const val LARGURA_A4 = 595
        private const val ALTURA_A4 = 842
        private const val MARGEM = 40f

        private val COR_TITULO = Color.parseColor("#0F2A52")
        private val COR_DOURADO = Color.parseColor("#8A6D00")
        private val COR_MUTED = Color.parseColor("#5C6678")
        private val COR_TEXTO = Color.parseColor("#15181D")
        private val COR_LINHA = Color.parseColor("#E0E4EA")
        private val COR_FUNDO_VALOR = Color.parseColor("#EAF1FB")

        fun nomeArquivo(competicaoId: Long): String = "recibo-competicao-$competicaoId.pdf"
    }
}

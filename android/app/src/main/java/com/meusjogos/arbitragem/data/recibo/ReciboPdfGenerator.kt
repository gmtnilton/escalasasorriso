package com.meusjogos.arbitragem.data.recibo

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.meusjogos.arbitragem.core.model.Jogo
import com.meusjogos.arbitragem.core.model.Recibo
import com.meusjogos.arbitragem.core.util.CurrencyUtils
import com.meusjogos.arbitragem.core.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.time.LocalDate

/**
 * Gera o recibo de pagamento em PDF — android.graphics.pdf.PdfDocument
 * (nativo do Android, sem depender de biblioteca externa), com aparência
 * profissional: título, número, valor em destaque, dados do pagador e do
 * recebedor, identificação do jogo e espaço para assinatura.
 */
class ReciboPdfGenerator {

    suspend fun gerar(recibo: Recibo, jogo: Jogo?, saida: OutputStream): Unit = withContext(Dispatchers.Default) {
        val documento = PdfDocument()
        val pagina = documento.startPage(PdfDocument.PageInfo.Builder(LARGURA_A4, ALTURA_A4, 1).create())
        desenharConteudo(pagina.canvas, recibo, jogo)
        documento.finishPage(pagina)
        documento.writeTo(saida)
        documento.close()
    }

    private fun desenharConteudo(canvas: Canvas, recibo: Recibo, jogo: Jogo?) {
        val corTitulo = Color.parseColor("#0F2A52")
        val corDourado = Color.parseColor("#8A6D00")
        val corMuted = Color.parseColor("#5C6678")
        val corTexto = Color.parseColor("#15181D")
        val corLinha = Color.parseColor("#E0E4EA")
        val corFundoValor = Color.parseColor("#EAF1FB")

        val pTituloGrande = Paint().apply { textSize = 22f; isFakeBoldText = true; color = corTitulo }
        val pDireita = Paint().apply { textSize = 10.5f; color = corMuted; textAlign = Paint.Align.RIGHT }
        val pSecao = Paint().apply { textSize = 12.5f; isFakeBoldText = true; color = corTitulo }
        val pRotulo = Paint().apply { textSize = 10f; color = corMuted }
        val pValorTexto = Paint().apply { textSize = 12f; color = corTexto }
        val pLinha = Paint().apply { color = corLinha; strokeWidth = 1f }
        val pRegua = Paint().apply { color = corDourado; strokeWidth = 2f }
        val pValorGrande = Paint().apply { textSize = 28f; isFakeBoldText = true; color = corTitulo; textAlign = Paint.Align.CENTER }
        val pValorRotulo = Paint().apply { textSize = 11f; color = corMuted; textAlign = Paint.Align.CENTER }
        val pAssinaturaLinha = Paint().apply { color = corTexto; strokeWidth = 1f }
        val pAssinaturaRotulo = Paint().apply { textSize = 10f; color = corMuted; textAlign = Paint.Align.CENTER }

        var y = MARGEM + 26f

        canvas.drawText("RECIBO DE PAGAMENTO", MARGEM, y, pTituloGrande)
        canvas.drawText(recibo.numeroFormatado, LARGURA_A4 - MARGEM, y, pDireita)
        y += 15f
        canvas.drawText("Emitido em ${DateUtils.formatarData(LocalDate.now())}", LARGURA_A4 - MARGEM, y, pDireita)
        y += 22f
        canvas.drawLine(MARGEM, y, LARGURA_A4 - MARGEM, y, pRegua)
        y += 30f

        val alturaCaixaValor = 74f
        canvas.drawRect(MARGEM, y, LARGURA_A4 - MARGEM, y + alturaCaixaValor, Paint().apply { color = corFundoValor })
        val centroHorizontal = LARGURA_A4 / 2f
        canvas.drawText("VALOR PAGO", centroHorizontal, y + 26f, pValorRotulo)
        canvas.drawText(CurrencyUtils.formatar(recibo.valorCentavos), centroHorizontal, y + 56f, pValorGrande)
        y += alturaCaixaValor + 32f

        fun secao(nome: String) {
            canvas.drawText(nome, MARGEM, y, pSecao)
            y += 8f
            canvas.drawLine(MARGEM, y, LARGURA_A4 - MARGEM, y, pLinha)
            y += 18f
        }

        fun linha(campo: String, texto: String) {
            canvas.drawText(campo, MARGEM, y, pRotulo)
            y += 14f
            canvas.drawText(texto.ifBlank { "—" }, MARGEM, y, pValorTexto)
            y += 20f
        }

        secao("Pagador")
        linha("Nome / Razão social", recibo.pagadorNome)
        linha("CPF / CNPJ", recibo.pagadorDocumento)
        y += 6f

        secao("Recebedor")
        linha("Nome completo", recibo.recebedorNome)
        linha("CPF", recibo.recebedorDocumento)
        y += 6f

        secao("Referente a")
        linha("Data do pagamento", DateUtils.formatarData(recibo.dataPagamento))
        if (jogo != null) {
            val identificacao = listOfNotNull(
                jogo.modalidade?.takeIf(String::isNotBlank),
                jogo.competicao?.takeIf(String::isNotBlank),
            ).joinToString(" — ")
            if (identificacao.isNotBlank()) linha("Modalidade / Competição", identificacao)
            jogo.cidade?.takeIf(String::isNotBlank)?.let { linha("Cidade", it) }
            linha("Jogo", jogo.confronto ?: "Escala Arbitragem")
            linha("Data do jogo", DateUtils.formatarData(jogo.data))
        }
        recibo.descricao?.takeIf(String::isNotBlank)?.let { linha("Descrição", it) }

        y += 36f
        val larguraAssinatura = 220f
        val centroEsquerda = MARGEM + larguraAssinatura / 2
        val centroDireita = LARGURA_A4 - MARGEM - larguraAssinatura / 2
        canvas.drawLine(MARGEM, y, MARGEM + larguraAssinatura, y, pAssinaturaLinha)
        canvas.drawLine(LARGURA_A4 - MARGEM - larguraAssinatura, y, LARGURA_A4 - MARGEM, y, pAssinaturaLinha)
        y += 16f
        canvas.drawText("Assinatura do pagador", centroEsquerda, y, pAssinaturaRotulo)
        canvas.drawText("Assinatura do recebedor", centroDireita, y, pAssinaturaRotulo)
    }

    companion object {
        private const val LARGURA_A4 = 595
        private const val ALTURA_A4 = 842
        private const val MARGEM = 40f

        fun nomeArquivo(jogoId: Long): String = "recibo-jogo-$jogoId.pdf"
    }
}

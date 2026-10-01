package com.meusjogos.arbitragem.data.backup

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.meusjogos.arbitragem.core.model.Estatisticas
import com.meusjogos.arbitragem.core.model.PontoMensal
import com.meusjogos.arbitragem.core.model.ResumoAnual
import com.meusjogos.arbitragem.core.model.ResumoPeriodo
import com.meusjogos.arbitragem.core.util.CurrencyUtils
import com.meusjogos.arbitragem.core.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Gera o relatório de resumo em PDF a partir dos dados já calculados na
 * tela de Resumo — usa android.graphics.pdf.PdfDocument (nativo do
 * Android), sem depender de nenhuma biblioteca externa.
 */
class PdfReportGenerator {

    suspend fun gerar(
        rotuloMes: String,
        resumoMensal: ResumoPeriodo,
        resumoAnual: ResumoAnual,
        estatisticas: Estatisticas,
        serieMensal: List<PontoMensal>,
        saida: OutputStream,
    ): Unit = withContext(Dispatchers.Default) {
        val documento = PdfDocument()
        val pagina = documento.startPage(PdfDocument.PageInfo.Builder(LARGURA_A4, ALTURA_A4, 1).create())
        desenharConteudo(pagina.canvas, rotuloMes, resumoMensal, resumoAnual, estatisticas, serieMensal)
        documento.finishPage(pagina)
        documento.writeTo(saida)
        documento.close()
    }

    private fun desenharConteudo(
        canvas: Canvas,
        rotuloMes: String,
        resumoMensal: ResumoPeriodo,
        resumoAnual: ResumoAnual,
        estatisticas: Estatisticas,
        serieMensal: List<PontoMensal>,
    ) {
        val corTitulo = Color.parseColor("#0F2A52")
        val corMuted = Color.parseColor("#5C6678")
        val corTexto = Color.parseColor("#15181D")
        val corLinha = Color.parseColor("#E0E4EA")

        val pTitulo = Paint().apply { textSize = 20f; isFakeBoldText = true; color = corTitulo }
        val pSubtitulo = Paint().apply { textSize = 11f; color = corMuted }
        val pSecao = Paint().apply { textSize = 13f; isFakeBoldText = true; color = corTitulo }
        val pRotulo = Paint().apply { textSize = 11f; color = corMuted }
        val pValor = Paint().apply { textSize = 11f; isFakeBoldText = true; color = corTexto }
        val pLinha = Paint().apply { color = corLinha; strokeWidth = 1f }

        var y = MARGEM + 24f

        canvas.drawText("Escalas Árbitros — Relatório de resumo", MARGEM, y, pTitulo)
        y += 18f

        val geradoEm = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        canvas.drawText("Gerado em $geradoEm — Proprietário do sistema: NILTON RODRIGO RIBEIRO", MARGEM, y, pSubtitulo)
        y += 30f

        fun secao(nome: String) {
            canvas.drawText(nome, MARGEM, y, pSecao)
            y += 8f
            canvas.drawLine(MARGEM, y, LARGURA_A4 - MARGEM, y, pLinha)
            y += 18f
        }

        fun linha(campo: String, texto: String) {
            canvas.drawText(campo, MARGEM, y, pRotulo)
            canvas.drawText(texto, LARGURA_A4 - MARGEM - pValor.measureText(texto), y, pValor)
            y += 18f
        }

        secao(rotuloMes)
        linha("Jogos no mês", resumoMensal.totalJogos.toString())
        linha("Recebido", CurrencyUtils.formatar(resumoMensal.totalRecebidoCentavos))
        linha("A receber", CurrencyUtils.formatar(resumoMensal.totalAReceberCentavos))
        linha("Total geral", CurrencyUtils.formatar(resumoMensal.totalGeralCentavos))
        y += 14f

        secao("Resumo anual — ${resumoAnual.ano}")
        linha("Total de jogos", resumoAnual.totalJogos.toString())
        linha("Total recebido", CurrencyUtils.formatar(resumoAnual.totalRecebidoCentavos))
        linha("Total a receber", CurrencyUtils.formatar(resumoAnual.totalAReceberCentavos))
        linha("Total geral", CurrencyUtils.formatar(resumoAnual.totalGeralCentavos))
        linha("Média por jogo", CurrencyUtils.formatar(resumoAnual.mediaPorJogoCentavos))
        y += 14f

        secao("Estatísticas gerais")
        linha("Total de jogos", estatisticas.totalJogos.toString())
        linha("Recebido", CurrencyUtils.formatar(estatisticas.totalRecebidoCentavos))
        linha("A receber", CurrencyUtils.formatar(estatisticas.totalAReceberCentavos))
        linha("Total geral", CurrencyUtils.formatar(estatisticas.totalGeralCentavos))
        linha("Média por jogo", CurrencyUtils.formatar(estatisticas.mediaPorJogoCentavos))
        linha("Maior valor de jogo", CurrencyUtils.formatar(estatisticas.maiorValorCentavos))
        linha("Jogos recebidos", estatisticas.jogosRecebidos.toString())
        linha("Jogos pendentes", estatisticas.jogosPendentes.toString())
        y += 14f

        secao("Jogos por mês — ${resumoAnual.ano}")
        for (ponto in serieMensal) {
            linha(DateUtils.nomeMes(ponto.mes), "${ponto.totalJogos} jogo(s) — ${CurrencyUtils.formatar(ponto.totalGeralCentavos)}")
        }
    }

    companion object {
        private const val LARGURA_A4 = 595
        private const val ALTURA_A4 = 842
        private const val MARGEM = 40f

        fun nomeArquivoSugerido(): String = "escalas-arbitros-resumo-${LocalDate.now()}.pdf"
    }
}

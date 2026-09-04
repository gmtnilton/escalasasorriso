package com.meusjogos.arbitragem.ui.resumo.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

private val ABREVIACOES_MES = listOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez")
private const val LARGURA_COLUNA_ROTULO = 28

/**
 * Gráfico de barras simples (um valor por mês) — REGRA 18: "Jogos por mês".
 * Desenhado com composables comuns (Box com altura proporcional), sem
 * depender de bibliotecas externas de gráficos. As linhas de referência
 * (0, 10, 20, 30...) deixam claro em qual "nível" cada mês parou, numa
 * escala arredondada calculada a partir do maior valor da série.
 */
@Composable
fun GraficoBarrasMensal(
    valores: List<Int>,
    cor: Color,
    modifier: Modifier = Modifier,
    alturaMaxima: Dp = 110.dp,
) {
    val maximoReal = (valores.maxOrNull() ?: 0).coerceAtLeast(1)
    val ticks = escalaAgradavel(maximoReal)
    val topo = ticks.last()
    val corGrade = MaterialTheme.colorScheme.outlineVariant
    val corRotulo = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth().height(alturaMaxima)) {
            ticks.forEach { valorTick ->
                val fracao = valorTick.toFloat() / topo.toFloat()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                        .offset(y = alturaMaxima * (1f - fracao) - 6.dp),
                ) {
                    Text(
                        text = valorTick.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = corRotulo,
                        modifier = Modifier.width(LARGURA_COLUNA_ROTULO.dp),
                    )
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(corGrade))
                }
            }

            Row(
                modifier = Modifier.fillMaxSize().padding(start = LARGURA_COLUNA_ROTULO.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom,
            ) {
                valores.forEach { valor ->
                    val altura = (alturaMaxima * (valor.toFloat() / topo.toFloat()))
                        .let { if (valor > 0) it.coerceAtLeast(3.dp) else 0.dp }
                    Box(
                        modifier = Modifier
                            .width(12.dp)
                            .height(altura)
                            .background(cor, RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)),
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(start = LARGURA_COLUNA_ROTULO.dp, top = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            ABREVIACOES_MES.forEach { mes ->
                Text(text = mes, style = MaterialTheme.typography.labelMedium, color = corRotulo)
            }
        }
    }
}

/**
 * Escala "arredondada" para o eixo do gráfico (0, 10, 20, 30... ou 0, 5,
 * 10, 15..., dependendo do maior valor da série) — o clássico algoritmo de
 * "nice numbers", para as linhas de referência caírem em números redondos
 * e fáceis de ler, não em frações do valor máximo real.
 */
private fun escalaAgradavel(maximoReal: Int, alvoDivisoes: Int = 4): List<Int> {
    val passoBruto = maximoReal.toDouble() / alvoDivisoes
    val magnitude = 10.0.pow(floor(log10(passoBruto)))
    val passoNormalizado = passoBruto / magnitude
    val passoAgradavel = when {
        passoNormalizado <= 1.0 -> 1.0
        passoNormalizado <= 2.0 -> 2.0
        passoNormalizado <= 5.0 -> 5.0
        else -> 10.0
    } * magnitude
    val passo = passoAgradavel.roundToInt().coerceAtLeast(1)
    val topo = (ceil(maximoReal.toDouble() / passo) * passo).roundToInt().coerceAtLeast(passo)
    return (0..topo step passo).toList()
}

/**
 * Gráfico de barras duplas por mês — cobre, no mesmo gráfico, "Valores por
 * mês" e "Recebido x A receber" (REGRA 18).
 */
@Composable
fun GraficoBarrasDuplasMensal(
    valoresA: List<Long>,
    valoresB: List<Long>,
    corA: Color,
    corB: Color,
    modifier: Modifier = Modifier,
    alturaMaxima: Dp = 110.dp,
) {
    val maximo = (valoresA.zip(valoresB) { a, b -> maxOf(a, b) }.maxOrNull() ?: 0L).coerceAtLeast(1L)
    Row(
        modifier = modifier.fillMaxWidth().height(alturaMaxima + 28.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom,
    ) {
        for (indice in ABREVIACOES_MES.indices) {
            val a = valoresA.getOrElse(indice) { 0L }
            val b = valoresB.getOrElse(indice) { 0L }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Box(
                        modifier = Modifier
                            .width(7.dp)
                            .height(alturaBarra(a, maximo, alturaMaxima))
                            .background(corA, RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)),
                    )
                    Box(
                        modifier = Modifier
                            .width(7.dp)
                            .height(alturaBarra(b, maximo, alturaMaxima))
                            .background(corB, RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)),
                    )
                }
                Text(
                    text = ABREVIACOES_MES[indice],
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

private fun alturaBarra(valor: Long, maximo: Long, alturaMaxima: Dp): Dp {
    if (valor <= 0L) return 0.dp
    return (alturaMaxima * (valor.toFloat() / maximo)).coerceAtLeast(3.dp)
}

/** Legenda simples de cor + rótulo, usada abaixo dos gráficos de duas séries. */
@Composable
fun LegendaCor(cor: Color, rotulo: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.width(10.dp).height(10.dp).background(cor, RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = rotulo, style = MaterialTheme.typography.labelMedium)
    }
}

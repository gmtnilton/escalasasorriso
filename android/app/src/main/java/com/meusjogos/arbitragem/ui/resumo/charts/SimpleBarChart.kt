package com.meusjogos.arbitragem.ui.resumo.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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

private val ABREVIACOES_MES = listOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez")

/**
 * Gráfico de barras simples (um valor por mês) — REGRA 18: "Jogos por mês".
 * Desenhado com composables comuns (Box com altura proporcional), sem
 * depender de bibliotecas externas de gráficos.
 */
@Composable
fun GraficoBarrasMensal(
    valores: List<Int>,
    cor: Color,
    modifier: Modifier = Modifier,
    alturaMaxima: Dp = 110.dp,
) {
    val maximo = (valores.maxOrNull() ?: 0).coerceAtLeast(1)
    Row(
        modifier = modifier.fillMaxWidth().height(alturaMaxima + 28.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom,
    ) {
        valores.forEachIndexed { indice, valor ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val altura = (alturaMaxima * (valor.toFloat() / maximo)).let { if (valor > 0) it.coerceAtLeast(3.dp) else 0.dp }
                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .height(altura)
                        .background(cor, RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)),
                )
                Text(
                    text = ABREVIACOES_MES[indice],
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
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

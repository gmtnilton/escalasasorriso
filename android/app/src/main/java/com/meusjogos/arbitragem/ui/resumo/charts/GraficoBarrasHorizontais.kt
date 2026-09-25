package com.meusjogos.arbitragem.ui.resumo.charts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Barras horizontais simples (uma por categoria, ex.: modalidade) — REGRA
 * 11 da V1.1: "📊 Jogos por modalidade". Desenhado com composables comuns
 * (Box com largura proporcional ao maior valor), sem biblioteca externa.
 */
@Composable
fun GraficoBarrasHorizontais(
    itens: List<Pair<String, Int>>,
    cor: Color,
    modifier: Modifier = Modifier,
) {
    val maximo = (itens.maxOfOrNull { it.second } ?: 0).coerceAtLeast(1)
    val corTrilha = MaterialTheme.colorScheme.surfaceVariant

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        itens.forEach { (nome, valor) ->
            val fracaoAnimada by animateFloatAsState(
                targetValue = valor.toFloat() / maximo.toFloat(),
                animationSpec = tween(500),
                label = "barra-$nome",
            )
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = nome,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "$valor ${if (valor == 1) "jogo" else "jogos"}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = cor,
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .height(10.dp)
                        .background(corTrilha, RoundedCornerShape(5.dp)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = fracaoAnimada.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(cor, RoundedCornerShape(5.dp)),
                    )
                }
            }
        }
    }
}

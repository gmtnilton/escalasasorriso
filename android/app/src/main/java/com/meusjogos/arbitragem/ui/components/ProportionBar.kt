package com.meusjogos.arbitragem.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Barra de proporção discreta: quanto do total já foi recebido vs. quanto
 * ainda falta receber — a "representação gráfica" pedida para o resumo
 * financeiro do dashboard, sem exagero visual.
 */
@Composable
fun ProportionBar(
    fracaoRecebida: Float,
    modifier: Modifier = Modifier,
    corRecebido: Color = MaterialTheme.colorScheme.primary,
    corAReceber: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    val fracao = fracaoRecebida.coerceIn(0f, 1f)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(50)),
    ) {
        if (fracao > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(fracao)
                    .background(corRecebido),
            )
        }
        if (fracao < 1f) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f - fracao)
                    .background(corAReceber),
            )
        }
    }
}

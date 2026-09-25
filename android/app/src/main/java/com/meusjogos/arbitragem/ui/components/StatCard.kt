package com.meusjogos.arbitragem.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Card de indicador financeiro/numérico (TOTAL A RECEBER, RECEBIDO,
 * GERAL, JOGOS APITADOS...) — número sempre com muito mais destaque que
 * o rótulo, para os valores saltarem aos olhos (REGRA 14). O ícone fica
 * num badge circular colorido para chamar mais atenção; quando
 * [gradienteFundo] é informado (cards de destaque), ele substitui
 * [corFundo] e o texto passa a ser branco para contrastar com o gradiente.
 */
@Composable
fun StatCard(
    titulo: String,
    valor: String,
    modifier: Modifier = Modifier,
    icone: String? = null,
    corValor: Color = MaterialTheme.colorScheme.onSurface,
    corFundo: Color = MaterialTheme.colorScheme.surfaceVariant,
    gradienteFundo: Brush? = null,
    subtitulo: String? = null,
) {
    val emGradiente = gradienteFundo != null
    val corTitulo = if (emGradiente) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
    val corValorFinal = if (emGradiente) Color.White else corValor
    val corBadge = if (emGradiente) Color.White.copy(alpha = 0.22f) else corValorFinal.copy(alpha = 0.16f)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (emGradiente) Color.Transparent else corFundo),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(
            modifier = Modifier
                .then(if (gradienteFundo != null) Modifier.background(gradienteFundo) else Modifier)
                .padding(18.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icone != null) {
                    Box(
                        modifier = Modifier.size(30.dp).background(corBadge, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(icone, style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(text = titulo, style = MaterialTheme.typography.labelLarge, color = corTitulo)
            }
            Text(
                text = valor,
                style = MaterialTheme.typography.headlineMedium,
                color = corValorFinal,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp),
            )
            if (subtitulo != null) {
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = corTitulo,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

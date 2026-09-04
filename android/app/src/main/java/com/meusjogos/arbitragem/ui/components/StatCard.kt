package com.meusjogos.arbitragem.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Card de indicador financeiro/numérico (TOTAL A RECEBER, RECEBIDO,
 * GERAL, JOGOS APITADOS...) — número sempre com muito mais destaque que
 * o rótulo, para os valores saltarem aos olhos (REGRA 14).
 */
@Composable
fun StatCard(
    titulo: String,
    valor: String,
    modifier: Modifier = Modifier,
    icone: String? = null,
    corValor: Color = MaterialTheme.colorScheme.onSurface,
    corFundo: Color = MaterialTheme.colorScheme.surfaceVariant,
    subtitulo: String? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = corFundo),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = if (icone != null) "$icone $titulo" else titulo,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.headlineMedium,
                color = corValor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 6.dp),
            )
            if (subtitulo != null) {
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

package com.meusjogos.arbitragem.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meusjogos.arbitragem.core.util.CurrencyUtils

/**
 * Card financeiro clicável do Início — mesmo padrão visual para os três módulos (VERSÃO 1.3/1.4,
 * seção 10: "não criar uma interface visualmente diferente dos módulos existentes"): ícone +
 * título, valor em destaque, e uma ou mais linhas de detalhe. Usado por "Competições encerradas —
 * valores em aberto", "Competições em aberto — valores a receber" e "Valores recebidos".
 */
@Composable
fun ModuloFinanceiroCard(
    titulo: String,
    icone: String,
    valorCentavos: Long,
    linhas: List<String>,
    corFundo: Color,
    corTexto: Color,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = corFundo),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icone, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = corTexto,
                )
            }
            Text(
                text = CurrencyUtils.formatar(valorCentavos),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = corTexto,
                modifier = Modifier.padding(top = 8.dp),
            )
            linhas.forEach { linha ->
                Text(
                    text = linha,
                    style = MaterialTheme.typography.bodyMedium,
                    color = corTexto,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

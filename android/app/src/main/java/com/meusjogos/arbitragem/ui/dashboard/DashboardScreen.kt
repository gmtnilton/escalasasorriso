package com.meusjogos.arbitragem.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meusjogos.arbitragem.core.util.CurrencyUtils
import com.meusjogos.arbitragem.ui.components.StatCard
import com.meusjogos.arbitragem.ui.theme.LocalStatusColors

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val estado by viewModel.uiState.collectAsState()
    val coresStatus = LocalStatusColors.current

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp, top = 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "Meus Jogos de Arbitragem",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Painel financeiro e de arbitragem",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
            )
        }

        item {
            StatCard(
                titulo = "TOTAL GERAL",
                valor = CurrencyUtils.formatar(estado.totalGeralCentavos),
                corFundo = MaterialTheme.colorScheme.primaryContainer,
                corValor = MaterialTheme.colorScheme.onPrimaryContainer,
                subtitulo = "Recebido + a receber",
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    titulo = "🔴 A RECEBER",
                    valor = CurrencyUtils.formatar(estado.totalAReceberCentavos),
                    corFundo = coresStatus.aReceberContainer,
                    corValor = coresStatus.aReceber,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    titulo = "🟢 RECEBIDO",
                    valor = CurrencyUtils.formatar(estado.totalRecebidoCentavos),
                    corFundo = coresStatus.recebidoContainer,
                    corValor = coresStatus.recebido,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                ContadorCard(titulo = "Total de jogos", valor = estado.totalJogos, modifier = Modifier.weight(1f))
                ContadorCard(titulo = "Recebidos", valor = estado.jogosRecebidos, modifier = Modifier.weight(1f))
                ContadorCard(titulo = "A receber", valor = estado.jogosAReceber, modifier = Modifier.weight(1f))
            }
        }

        item {
            ResumoPeriodoCard(
                rotulo = estado.rotuloPeriodoAtual,
                totalJogos = estado.resumoPeriodoAtual.totalJogos,
                recebidoCentavos = estado.resumoPeriodoAtual.totalRecebidoCentavos,
                aReceberCentavos = estado.resumoPeriodoAtual.totalAReceberCentavos,
                totalCentavos = estado.resumoPeriodoAtual.totalGeralCentavos,
            )
        }
    }
}

@Composable
private fun ContadorCard(titulo: String, valor: Int, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = valor.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ResumoPeriodoCard(
    rotulo: String,
    totalJogos: Int,
    recebidoCentavos: Long,
    aReceberCentavos: Long,
    totalCentavos: Long,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = rotulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = "$totalJogos ${if (totalJogos == 1) "jogo" else "jogos"}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
            LinhaResumo("Recebido", CurrencyUtils.formatar(recebidoCentavos), LocalStatusColors.current.recebido)
            LinhaResumo("A receber", CurrencyUtils.formatar(aReceberCentavos), LocalStatusColors.current.aReceber)
            LinhaResumo("Total", CurrencyUtils.formatar(totalCentavos), MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun LinhaResumo(rotulo: String, valor: String, cor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = rotulo, style = MaterialTheme.typography.bodyMedium)
        Text(text = valor, style = MaterialTheme.typography.bodyMedium, color = cor, fontWeight = FontWeight.SemiBold)
    }
}

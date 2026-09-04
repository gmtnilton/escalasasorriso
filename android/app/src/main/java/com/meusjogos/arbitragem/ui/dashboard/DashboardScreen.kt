package com.meusjogos.arbitragem.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import com.meusjogos.arbitragem.ui.components.ProportionBar
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
            start = 16.dp, end = 16.dp,
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column {
                Text(
                    text = "Olá, vamos para mais um jogo 👋",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Controle dos seus jogos de arbitragem",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    titulo = "A RECEBER",
                    icone = "💰",
                    valor = CurrencyUtils.formatar(estado.totalAReceberCentavos),
                    corFundo = coresStatus.aReceberContainer,
                    corValor = coresStatus.aReceber,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    titulo = "RECEBIDO",
                    icone = "✅",
                    valor = CurrencyUtils.formatar(estado.totalRecebidoCentavos),
                    corFundo = coresStatus.recebidoContainer,
                    corValor = coresStatus.recebido,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    titulo = "TOTAL GERAL",
                    icone = "📊",
                    valor = CurrencyUtils.formatar(estado.totalGeralCentavos),
                    corFundo = MaterialTheme.colorScheme.primaryContainer,
                    corValor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    titulo = "JOGOS APITADOS",
                    icone = "⚽",
                    valor = "${estado.totalJogos}",
                    subtitulo = if (estado.totalJogos == 1) "jogo" else "jogos",
                    corFundo = MaterialTheme.colorScheme.tertiaryContainer,
                    corValor = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            ResumoFinanceiroCard(
                recebidoCentavos = estado.totalRecebidoCentavos,
                aReceberCentavos = estado.totalAReceberCentavos,
                totalCentavos = estado.totalGeralCentavos,
            )
        }

        item {
            ResumoMesAtualCard(
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
private fun ResumoFinanceiroCard(recebidoCentavos: Long, aReceberCentavos: Long, totalCentavos: Long) {
    val coresStatus = LocalStatusColors.current
    val fracaoRecebida = if (totalCentavos > 0) recebidoCentavos.toFloat() / totalCentavos.toFloat() else 0f
    val fracaoAnimada by animateFloatAsState(targetValue = fracaoRecebida, animationSpec = tween(500), label = "proporcao")

    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Resumo financeiro", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            ProportionBar(
                fracaoRecebida = fracaoAnimada,
                corRecebido = coresStatus.recebido,
                corAReceber = coresStatus.aReceber,
                modifier = Modifier.padding(top = 14.dp),
            )

            LinhaResumo("A receber", CurrencyUtils.formatar(aReceberCentavos), coresStatus.aReceber, Modifier.padding(top = 16.dp))
            LinhaResumo("Recebido", CurrencyUtils.formatar(recebidoCentavos), coresStatus.recebido)
            LinhaResumo("Total geral", CurrencyUtils.formatar(totalCentavos), MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun ResumoMesAtualCard(
    rotulo: String,
    totalJogos: Int,
    recebidoCentavos: Long,
    aReceberCentavos: Long,
    totalCentavos: Long,
) {
    val coresStatus = LocalStatusColors.current
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("📅 $rotulo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "$totalJogos ${if (totalJogos == 1) "jogo" else "jogos"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            LinhaResumo("Recebido", CurrencyUtils.formatar(recebidoCentavos), coresStatus.recebido, Modifier.padding(top = 12.dp))
            LinhaResumo("A receber", CurrencyUtils.formatar(aReceberCentavos), coresStatus.aReceber)
            LinhaResumo("Total", CurrencyUtils.formatar(totalCentavos), MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun LinhaResumo(rotulo: String, valor: String, cor: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = rotulo, style = MaterialTheme.typography.bodyMedium)
        Text(text = valor, style = MaterialTheme.typography.bodyLarge, color = cor, fontWeight = FontWeight.Bold)
    }
}

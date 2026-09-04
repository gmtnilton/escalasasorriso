package com.meusjogos.arbitragem.ui.resumo

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meusjogos.arbitragem.core.util.CurrencyUtils
import com.meusjogos.arbitragem.core.util.DateUtils
import com.meusjogos.arbitragem.ui.components.ProportionBar
import com.meusjogos.arbitragem.ui.resumo.charts.GraficoBarrasDuplasMensal
import com.meusjogos.arbitragem.ui.resumo.charts.GraficoBarrasMensal
import com.meusjogos.arbitragem.ui.resumo.charts.LegendaCor
import com.meusjogos.arbitragem.ui.theme.LocalStatusColors
import kotlinx.coroutines.launch

@Composable
fun ResumoScreen(
    viewModel: ResumoViewModel,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val estado by viewModel.uiState.collectAsState()
    val coresStatus = LocalStatusColors.current
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()

    val lancadorExportarPdf = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        escopo.launch {
            context.contentResolver.openOutputStream(uri)?.use { saida ->
                viewModel.gerarRelatorioPdf(saida)
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 32.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("📊 Resumo", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = { lancadorExportarPdf.launch(viewModel.nomeArquivoPdf()) }) {
                    Icon(Icons.Filled.PictureAsPdf, contentDescription = "Exportar relatório em PDF")
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SeletorMes(estado.mesSelecionado, onSelecionar = viewModel::selecionarMes)
                SeletorAno(estado.anoSelecionado, estado.anosDisponiveis, onSelecionar = viewModel::selecionarAno)
            }
        }

        item {
            CardMesHero(
                rotulo = DateUtils.nomeMesAno(estado.anoSelecionado, estado.mesSelecionado),
                totalJogos = estado.resumoMensal.totalJogos,
                recebidoCentavos = estado.resumoMensal.totalRecebidoCentavos,
                aReceberCentavos = estado.resumoMensal.totalAReceberCentavos,
                totalCentavos = estado.resumoMensal.totalGeralCentavos,
            )
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        estado.anoSelecionado.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    LinhaValor("Total de jogos", estado.resumoAnual.totalJogos.toString())
                    LinhaValor("Total recebido", CurrencyUtils.formatar(estado.resumoAnual.totalRecebidoCentavos), coresStatus.recebido)
                    LinhaValor("Total a receber", CurrencyUtils.formatar(estado.resumoAnual.totalAReceberCentavos), coresStatus.aReceber)
                    LinhaValor("Total geral", CurrencyUtils.formatar(estado.resumoAnual.totalGeralCentavos))
                    LinhaValor("Média por jogo", CurrencyUtils.formatar(estado.resumoAnual.mediaPorJogoCentavos))
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Estatísticas gerais", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    val stats = estado.estatisticasGerais
                    LinhaValor("Total de jogos", stats.totalJogos.toString())
                    LinhaValor("Recebido", CurrencyUtils.formatar(stats.totalRecebidoCentavos), coresStatus.recebido)
                    LinhaValor("A receber", CurrencyUtils.formatar(stats.totalAReceberCentavos), coresStatus.aReceber)
                    LinhaValor("Total geral", CurrencyUtils.formatar(stats.totalGeralCentavos))
                    LinhaValor("Média por jogo", CurrencyUtils.formatar(stats.mediaPorJogoCentavos))
                    LinhaValor("Maior valor de jogo", CurrencyUtils.formatar(stats.maiorValorCentavos))
                    LinhaValor("Jogos recebidos", stats.jogosRecebidos.toString())
                    LinhaValor("Jogos pendentes", stats.jogosPendentes.toString())
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        "Jogos por mês — ${estado.anoSelecionado}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    GraficoBarrasMensal(
                        valores = estado.serieMensal.map { it.totalJogos },
                        cor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        "Recebido x A receber por mês — ${estado.anoSelecionado}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    GraficoBarrasDuplasMensal(
                        valoresA = estado.serieMensal.map { it.totalRecebidoCentavos },
                        valoresB = estado.serieMensal.map { it.totalAReceberCentavos },
                        corA = coresStatus.recebido,
                        corB = coresStatus.aReceber,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 8.dp)) {
                        LegendaCor(coresStatus.recebido, "Recebido")
                        LegendaCor(coresStatus.aReceber, "A receber")
                    }
                }
            }
        }
    }
}

/** Card em destaque do mês selecionado — valor total com muito mais peso visual, e a proporção recebido/a receber. */
@Composable
private fun CardMesHero(
    rotulo: String,
    totalJogos: Int,
    recebidoCentavos: Long,
    aReceberCentavos: Long,
    totalCentavos: Long,
) {
    val coresStatus = LocalStatusColors.current
    val fracaoRecebida = if (totalCentavos > 0) recebidoCentavos.toFloat() / totalCentavos.toFloat() else 0f
    val fracaoAnimada by animateFloatAsState(targetValue = fracaoRecebida, animationSpec = tween(500), label = "proporcao-mes")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "📅 $rotulo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    "$totalJogos ${if (totalJogos == 1) "jogo" else "jogos"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Text(
                text = CurrencyUtils.formatar(totalCentavos),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = 8.dp),
            )

            ProportionBar(
                fracaoRecebida = fracaoAnimada,
                corRecebido = coresStatus.recebido,
                corAReceber = coresStatus.aReceber,
                modifier = Modifier.padding(top = 14.dp),
            )

            LinhaValor("Recebido", CurrencyUtils.formatar(recebidoCentavos), coresStatus.recebido, Modifier.padding(top = 14.dp))
            LinhaValor("A receber", CurrencyUtils.formatar(aReceberCentavos), coresStatus.aReceber)
        }
    }
}

@Composable
private fun LinhaValor(
    rotulo: String,
    valor: String,
    cor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(rotulo, style = MaterialTheme.typography.bodyMedium)
        Text(valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = cor)
    }
}

@Composable
private fun SeletorMes(mesSelecionado: Int, onSelecionar: (Int) -> Unit) {
    var expandido by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { expandido = true }, shape = RoundedCornerShape(16.dp)) {
        Icon(Icons.Filled.CalendarMonth, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
        Text(DateUtils.nomeMes(mesSelecionado))
    }
    DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
        (1..12).forEach { mes ->
            DropdownMenuItem(text = { Text(DateUtils.nomeMes(mes)) }, onClick = { onSelecionar(mes); expandido = false })
        }
    }
}

@Composable
private fun SeletorAno(anoSelecionado: Int, anosDisponiveis: List<Int>, onSelecionar: (Int) -> Unit) {
    var expandido by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { expandido = true }, shape = RoundedCornerShape(16.dp)) {
        Text(anoSelecionado.toString())
    }
    DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
        anosDisponiveis.forEach { ano ->
            DropdownMenuItem(text = { Text(ano.toString()) }, onClick = { onSelecionar(ano); expandido = false })
        }
    }
}

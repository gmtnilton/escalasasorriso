package com.meusjogos.arbitragem.ui.resumo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meusjogos.arbitragem.core.util.CurrencyUtils
import com.meusjogos.arbitragem.core.util.DateUtils
import com.meusjogos.arbitragem.ui.resumo.charts.GraficoBarrasDuplasMensal
import com.meusjogos.arbitragem.ui.resumo.charts.GraficoBarrasMensal
import com.meusjogos.arbitragem.ui.resumo.charts.LegendaCor
import com.meusjogos.arbitragem.ui.theme.LocalStatusColors

@Composable
fun ResumoScreen(
    viewModel: ResumoViewModel,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val estado by viewModel.uiState.collectAsState()
    val coresStatus = LocalStatusColors.current

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp, top = 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 32.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("📊 Resumo", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SeletorMes(estado.mesSelecionado, onSelecionar = viewModel::selecionarMes)
                SeletorAno(estado.anoSelecionado, estado.anosDisponiveis, onSelecionar = viewModel::selecionarAno)
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        DateUtils.nomeMesAno(estado.anoSelecionado, estado.mesSelecionado),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    LinhaValor("Jogos", estado.resumoMensal.totalJogos.toString())
                    LinhaValor("Recebido", CurrencyUtils.formatar(estado.resumoMensal.totalRecebidoCentavos), coresStatus.recebido)
                    LinhaValor("A receber", CurrencyUtils.formatar(estado.resumoMensal.totalAReceberCentavos), coresStatus.aReceber)
                    LinhaValor("Total", CurrencyUtils.formatar(estado.resumoMensal.totalGeralCentavos))
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
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
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
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
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
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
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
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

@Composable
private fun LinhaValor(rotulo: String, valor: String, cor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(rotulo, style = MaterialTheme.typography.bodyMedium)
        Text(valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = cor)
    }
}

@Composable
private fun SeletorMes(mesSelecionado: Int, onSelecionar: (Int) -> Unit) {
    var expandido by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { expandido = true }) { Text(DateUtils.nomeMes(mesSelecionado)) }
    DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
        (1..12).forEach { mes ->
            DropdownMenuItem(text = { Text(DateUtils.nomeMes(mes)) }, onClick = { onSelecionar(mes); expandido = false })
        }
    }
}

@Composable
private fun SeletorAno(anoSelecionado: Int, anosDisponiveis: List<Int>, onSelecionar: (Int) -> Unit) {
    var expandido by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { expandido = true }) { Text(anoSelecionado.toString()) }
    DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
        anosDisponiveis.forEach { ano ->
            DropdownMenuItem(text = { Text(ano.toString()) }, onClick = { onSelecionar(ano); expandido = false })
        }
    }
}

package com.meusjogos.arbitragem.ui.competicao

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meusjogos.arbitragem.core.logic.CompeticaoComResumo
import com.meusjogos.arbitragem.core.util.CurrencyUtils
import com.meusjogos.arbitragem.core.util.DateUtils
import com.meusjogos.arbitragem.ui.components.EmptyState
import com.meusjogos.arbitragem.ui.components.ProportionBar
import com.meusjogos.arbitragem.ui.theme.LocalStatusColors
import java.time.ZoneId
import kotlin.math.roundToInt

/**
 * Tela "COMPETIÇÕES ENCERRADAS A RECEBER" (VERSÃO 1.3, seção 4) — aberta ao tocar no card do
 * Início; cada item mostra o resumo completo da competição encerrada e um botão para abrir os
 * detalhes (jogos pendentes, recebimentos, recibo em PDF).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompeticoesEncerradasPendentesScreen(
    viewModel: CompeticoesEncerradasPendentesViewModel,
    onVoltar: () -> Unit,
    onVerCompeticao: (Long) -> Unit,
) {
    val estado by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Competições encerradas a receber") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) { Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar") }
                },
            )
        },
    ) { padding ->
        when {
            estado.carregando -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            estado.itens.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(
                    icone = Icons.Filled.CheckCircle,
                    titulo = "Nenhuma pendência",
                    descricao = "Todas as competições encerradas já foram totalmente recebidas.",
                )
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(estado.itens, key = { it.competicao.id }) { item ->
                    CompeticaoPendenteCard(item = item, onVerCompeticao = { onVerCompeticao(item.competicao.id) })
                }
            }
        }
    }
}

@Composable
private fun CompeticaoPendenteCard(item: CompeticaoComResumo, onVerCompeticao: () -> Unit) {
    val coresStatus = LocalStatusColors.current
    val competicao = item.competicao
    val resumo = item.resumo

    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(competicao.nome, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            competicao.subtitulo?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
            }
            Text(
                text = "🔴 ENCERRADA",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp),
            )
            competicao.dataEncerramento?.let { data ->
                Text(
                    text = "Encerrada em ${DateUtils.formatarData(data.atZone(ZoneId.systemDefault()).toLocalDate())}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            LinhaInfo("Jogos", resumo.totalJogos.toString())
            LinhaInfo("Jogos recebidos", resumo.jogosRecebidos.toString(), coresStatus.recebido)
            LinhaInfo("Jogos pendentes", resumo.jogosPendentes.toString(), coresStatus.aReceber)

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            LinhaInfo("Valor total", CurrencyUtils.formatar(resumo.valorTotalCentavos))
            LinhaInfo("Valor recebido", CurrencyUtils.formatar(resumo.valorRecebidoCentavos), coresStatus.recebido)
            LinhaInfo("VALOR A RECEBER", CurrencyUtils.formatar(resumo.valorPendenteCentavos), coresStatus.aReceber)

            ProportionBar(
                fracaoRecebida = resumo.percentualRecebido,
                corRecebido = coresStatus.recebido,
                corAReceber = coresStatus.aReceber,
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(
                text = "${(resumo.percentualRecebido * 100).roundToInt()}% já recebido",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )

            Button(onClick = onVerCompeticao, modifier = Modifier.fillMaxWidth().padding(top = 14.dp)) {
                Text("VER COMPETIÇÃO")
            }
        }
    }
}

@Composable
private fun LinhaInfo(rotulo: String, valor: String, cor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(rotulo, style = MaterialTheme.typography.bodyMedium)
        Text(valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = cor)
    }
}

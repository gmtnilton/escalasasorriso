package com.meusjogos.arbitragem.ui.recebimento

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meusjogos.arbitragem.core.logic.ItemHistoricoRecebimento
import com.meusjogos.arbitragem.core.util.CurrencyUtils
import com.meusjogos.arbitragem.core.util.DateUtils
import com.meusjogos.arbitragem.ui.competicao.LinhaInfoFinanceira
import com.meusjogos.arbitragem.ui.components.EmptyState
import com.meusjogos.arbitragem.ui.theme.LocalStatusColors

/**
 * Tela "HISTÓRICO DE VALORES RECEBIDOS" (VERSÃO 1.4, seção 2/4/5) — organizada por competição, um
 * card por competição (mesma organização dos módulos "Competições em aberto"/"Competições
 * encerradas a receber"), com busca, filtros e ordenação. Tocar em "VER COMPETIÇÃO" abre a tela da
 * competição já existente, de onde cada jogo recebido pode ser aberto (edição, recibo em PDF),
 * sem duplicar nenhuma dessas telas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoricoRecebimentosScreen(
    viewModel: HistoricoRecebimentosViewModel,
    onVoltar: () -> Unit,
    onVerCompeticao: (Long) -> Unit,
) {
    val estado by viewModel.uiState.collectAsState()
    val coresStatus = LocalStatusColors.current
    var mostrarFiltros by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Histórico de valores recebidos") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) { Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar") }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = estado.filtro.pesquisa,
                    onValueChange = viewModel::atualizarPesquisa,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("🔎 Pesquisar jogo, equipe ou cidade...") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (estado.filtro.pesquisa.isNotEmpty()) {
                            IconButton(onClick = { viewModel.atualizarPesquisa("") }) {
                                Icon(Icons.Filled.Close, contentDescription = "Limpar pesquisa")
                            }
                        }
                    },
                )
                BadgedBox(badge = { if (estado.filtro.ativo) Badge() }) {
                    OutlinedButton(onClick = { mostrarFiltros = true }, shape = RoundedCornerShape(16.dp)) {
                        Icon(Icons.Filled.FilterList, contentDescription = "Filtros")
                    }
                }
            }

            if (estado.itens.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${estado.itens.size} ${if (estado.itens.size == 1) "competição" else "competições"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = CurrencyUtils.formatar(estado.totalFiltradoCentavos),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = coresStatus.recebido,
                        )
                    }
                }
            }

            when {
                estado.carregando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                estado.itens.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icone = Icons.Filled.CheckCircle,
                        titulo = "Nenhum recebimento encontrado",
                        descricao = "Ajuste os filtros ou aguarde o primeiro recebimento ser registrado.",
                    )
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(estado.itens, key = { it.competicaoId ?: it.nome }) { item ->
                        CompeticaoRecebidaCard(
                            item = item,
                            onVerCompeticao = item.competicaoId?.let { id -> { onVerCompeticao(id) } },
                        )
                    }
                }
            }
        }
    }

    if (mostrarFiltros) {
        HistoricoRecebimentosFiltroSheet(
            filtro = estado.filtro,
            competicoesDisponiveis = estado.competicoesDisponiveis,
            modalidadesDisponiveis = estado.modalidadesDisponiveis,
            cidadesDisponiveis = estado.cidadesDisponiveis,
            onPeriodoChange = viewModel::atualizarPeriodo,
            onCompeticaoChange = viewModel::atualizarCompeticao,
            onModalidadeChange = viewModel::atualizarModalidade,
            onCidadeChange = viewModel::atualizarCidade,
            onOrdenacaoChange = viewModel::atualizarOrdenacao,
            onLimpar = viewModel::limparFiltros,
            onFechar = { mostrarFiltros = false },
        )
    }
}

/**
 * Card de uma competição no histórico de valores recebidos — mesmo padrão visual dos cards de
 * "Competições em aberto"/"Competições encerradas a receber" (título, subtítulo, status, divisor,
 * linhas de informação, botão), com os campos específicos de recebimento (quantidade de
 * pagamentos, valor recebido no filtro atual, data do último pagamento).
 */
@Composable
private fun CompeticaoRecebidaCard(item: ItemHistoricoRecebimento, onVerCompeticao: (() -> Unit)?) {
    val coresStatus = LocalStatusColors.current
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(item.nome, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            val subtitulo = listOfNotNull(
                item.cidade?.takeIf(String::isNotBlank),
                item.modalidade?.takeIf(String::isNotBlank),
            ).joinToString(" • ")
            if (subtitulo.isNotBlank()) {
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Text(
                text = when {
                    item.competicaoId == null -> "📄 SEM COMPETIÇÃO VINCULADA"
                    item.encerrada -> "🔴 ENCERRADA"
                    else -> "🟢 EM ANDAMENTO"
                },
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (item.competicaoId == null) MaterialTheme.colorScheme.onSurfaceVariant else coresStatus.recebido,
                modifier = Modifier.padding(top = 8.dp),
            )
            item.ultimoPagamento?.let {
                Text(
                    text = "Último pagamento em ${DateUtils.formatarData(it)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            LinhaInfoFinanceira(
                rotulo = if (item.quantidadeJogos == 1) "Pagamento" else "Pagamentos",
                valor = item.quantidadeJogos.toString(),
            )
            item.porFuncao.forEach { (funcao, quantidade) ->
                LinhaInfoFinanceira("  • $funcao", quantidade.toString(), MaterialTheme.colorScheme.onSurfaceVariant)
            }
            LinhaInfoFinanceira(
                rotulo = "Valor recebido",
                valor = CurrencyUtils.formatar(item.valorRecebidoCentavos),
                cor = coresStatus.recebido,
            )

            if (onVerCompeticao != null) {
                Button(onClick = onVerCompeticao, modifier = Modifier.fillMaxWidth().padding(top = 14.dp)) {
                    Text("VER COMPETIÇÃO")
                }
            }
        }
    }
}

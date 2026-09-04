package com.meusjogos.arbitragem.ui.jogos

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.meusjogos.arbitragem.core.util.DateUtils
import com.meusjogos.arbitragem.ui.components.CampoDataTexto
import com.meusjogos.arbitragem.ui.components.EmptyState
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun JogosListScreen(
    viewModel: JogosListViewModel,
    onJogoClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val estado by viewModel.uiState.collectAsState()
    var mostrarFiltros by remember { mutableStateOf(false) }
    var mostrarConfirmarLote by remember { mutableStateOf(false) }
    var dataRecebimentoLoteTexto by remember { mutableStateOf(DateUtils.formatarData(LocalDate.now())) }
    val pendentesFiltrados = estado.jogosFiltrados.count { !it.recebido }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "⚽ Meus Jogos",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(
                start = 16.dp, end = 16.dp,
                top = contentPadding.calculateTopPadding() + 16.dp,
                bottom = 12.dp,
            ),
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedTextField(
                value = estado.filtro.pesquisa,
                onValueChange = viewModel::atualizarPesquisa,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Pesquisar jogos...") },
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
                OutlinedButton(
                    onClick = { mostrarFiltros = true },
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(Icons.Filled.FilterList, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Filtrar")
                }
            }
        }

        if (pendentesFiltrados > 0) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "$pendentesFiltrados ${if (pendentesFiltrados == 1) "jogo a receber" else "jogos a receber"} nesta lista",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = {
                    dataRecebimentoLoteTexto = DateUtils.formatarData(LocalDate.now())
                    mostrarConfirmarLote = true
                }) {
                    Text("Marcar todos como recebidos")
                }
            }
        }

        if (estado.jogosFiltrados.isNotEmpty() && (estado.contagemPorCidade.isNotEmpty() || estado.contagemPorModalidade.isNotEmpty())) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LinhaContagem(titulo = "Por cidade", itens = estado.contagemPorCidade)
                LinhaContagem(titulo = "Por modalidade", itens = estado.contagemPorModalidade)
            }
        }

        if (estado.jogosFiltrados.isEmpty()) {
            if (estado.totalSemFiltro == 0) {
                EmptyState(
                    icone = Icons.Filled.SportsSoccer,
                    titulo = "Nenhum jogo cadastrado",
                    descricao = "Toque em \"Novo jogo\" para registrar seu primeiro jogo apitado.",
                )
            } else {
                EmptyState(
                    icone = Icons.Filled.Search,
                    titulo = "Nenhum jogo encontrado",
                    descricao = "Tente ajustar a pesquisa ou os filtros aplicados.",
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, top = 16.dp,
                    bottom = contentPadding.calculateBottomPadding() + 96.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(estado.jogosFiltrados, key = { it.id }) { jogo ->
                    JogoItem(
                        jogo = jogo,
                        onClick = { onJogoClick(jogo.id) },
                        modifier = Modifier.animateItemPlacement(),
                    )
                }
            }
        }
    }

    if (mostrarFiltros) {
        FiltroSheet(
            filtro = estado.filtro,
            competicoesDisponiveis = estado.competicoesDisponiveis,
            funcoesDisponiveis = estado.funcoesDisponiveis,
            onStatusChange = viewModel::atualizarStatus,
            onPeriodoChange = viewModel::atualizarPeriodo,
            onCompeticaoChange = viewModel::atualizarCompeticao,
            onFuncaoChange = viewModel::atualizarFuncao,
            onLimpar = viewModel::limparFiltros,
            onFechar = { mostrarFiltros = false },
        )
    }

    if (mostrarConfirmarLote) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmarLote = false },
            title = { Text("Marcar $pendentesFiltrados jogo(s) como recebido(s)?") },
            text = {
                Column {
                    Text(
                        "Isso marca como recebidos todos os jogos a receber que estão sendo exibidos " +
                            "agora — considerando a pesquisa e os filtros aplicados. Por exemplo: filtre por " +
                            "uma competição e receba todos os jogos dela de uma vez.",
                    )
                    CampoDataTexto(
                        texto = dataRecebimentoLoteTexto,
                        onTextoChange = { dataRecebimentoLoteTexto = it },
                        label = "Data do recebimento",
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val data = DateUtils.parseData(dataRecebimentoLoteTexto) ?: LocalDate.now()
                    viewModel.marcarFiltradosComoRecebido(data)
                    mostrarConfirmarLote = false
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmarLote = false }) { Text("Cancelar") }
            },
        )
    }
}

/** Contagem de jogos por cidade/modalidade, em chips horizontalmente roláveis. */
@Composable
private fun LinhaContagem(titulo: String, itens: List<Pair<String, Int>>) {
    if (itens.isEmpty()) return
    Column {
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp).horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            itens.forEach { (nome, quantidade) ->
                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Text(
                        text = "$nome · $quantidade",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}

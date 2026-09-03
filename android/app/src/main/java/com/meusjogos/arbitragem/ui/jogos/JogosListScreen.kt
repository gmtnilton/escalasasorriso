package com.meusjogos.arbitragem.ui.jogos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.meusjogos.arbitragem.ui.components.EmptyState

@Composable
fun JogosListScreen(
    viewModel: JogosListViewModel,
    onJogoClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val estado by viewModel.uiState.collectAsState()
    var mostrarFiltros by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(
                start = 16.dp, end = 16.dp,
                top = contentPadding.calculateTopPadding() + 16.dp,
                bottom = 8.dp,
            ),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Text(
                text = "⚽ Meus Jogos",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            BadgedBox(badge = { if (estado.filtro.ativo) Badge() }) {
                IconButton(onClick = { mostrarFiltros = true }) {
                    Icon(Icons.Filled.FilterList, contentDescription = "Filtros")
                }
            }
        }

        OutlinedTextField(
            value = estado.filtro.pesquisa,
            onValueChange = viewModel::atualizarPesquisa,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            placeholder = { Text("Pesquisar equipe, competição, local...") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (estado.filtro.pesquisa.isNotEmpty()) {
                    IconButton(onClick = { viewModel.atualizarPesquisa("") }) {
                        Icon(Icons.Filled.Close, contentDescription = "Limpar pesquisa")
                    }
                }
            },
        )

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
                    start = 16.dp, end = 16.dp, top = 8.dp,
                    bottom = contentPadding.calculateBottomPadding() + 96.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(estado.jogosFiltrados, key = { it.id }) { jogo ->
                    JogoItem(jogo = jogo, onClick = { onJogoClick(jogo.id) })
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
}

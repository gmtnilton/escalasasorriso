package com.meusjogos.arbitragem.ui.recebimento

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.meusjogos.arbitragem.core.model.Jogo
import com.meusjogos.arbitragem.core.util.CurrencyUtils
import com.meusjogos.arbitragem.core.util.DateUtils
import com.meusjogos.arbitragem.ui.components.CampoDataTexto
import com.meusjogos.arbitragem.ui.components.EmptyState
import com.meusjogos.arbitragem.ui.components.StatusChip
import com.meusjogos.arbitragem.ui.theme.LocalStatusColors
import java.time.LocalDate

/**
 * Área "Recebimento": baixa em lote — por competição, por cidade, ou por
 * seleção manual/toque longo — sem precisar abrir jogo por jogo. O
 * recebimento individual continua existindo normalmente em JogoDetailScreen
 * (por isso o toque simples fora do modo de seleção abre o detalhe do jogo).
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RecebimentoScreen(
    viewModel: RecebimentoViewModel,
    onJogoClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val estado by viewModel.uiState.collectAsState()
    val coresStatus = LocalStatusColors.current
    var mostrarFiltros by remember { mutableStateOf(false) }
    var mostrarConfirmar by remember { mutableStateOf(false) }
    var dataRecebimentoTexto by remember { mutableStateOf(DateUtils.formatarData(LocalDate.now())) }
    val snackbarHostState = remember { SnackbarHostState() }
    val modoSelecao = estado.selecionados.isNotEmpty()

    LaunchedEffect(estado.mensagem) {
        estado.mensagem?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limparMensagem()
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(
                    start = 16.dp, end = 16.dp,
                    top = contentPadding.calculateTopPadding() + 16.dp,
                    bottom = 12.dp,
                ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (modoSelecao) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = viewModel::limparSelecao) {
                            Icon(Icons.Filled.Close, contentDescription = "Cancelar seleção")
                        }
                        Text(
                            text = "✓ ${estado.selecionados.size} " +
                                if (estado.selecionados.size == 1) "jogo selecionado" else "jogos selecionados",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                } else {
                    Column {
                        Text("💰 Recebimento", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "🔴 ${estado.totalPendentesFiltrados} " +
                                if (estado.totalPendentesFiltrados == 1) "jogo pendente" else "jogos pendentes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = coresStatus.aReceber,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }

                if (estado.jogosFiltrados.isNotEmpty()) {
                    TextButton(onClick = viewModel::selecionarTodosVisiveis) {
                        Text("Selecionar todos")
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = estado.filtro.pesquisa,
                    onValueChange = viewModel::atualizarPesquisa,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Pesquisar jogo específico...") },
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

            if (estado.jogosFiltrados.isEmpty()) {
                EmptyState(
                    icone = Icons.Filled.CheckCircle,
                    titulo = "Nenhum jogo pendente aqui",
                    descricao = "Ajuste os filtros para ver outros jogos, ou aproveite — está tudo em dia!",
                    modifier = Modifier.padding(top = 24.dp),
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp, top = 8.dp,
                        bottom = contentPadding.calculateBottomPadding() + (if (modoSelecao) 96.dp else 32.dp),
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(estado.jogosFiltrados, key = { it.id }) { jogo ->
                        RecebimentoJogoItem(
                            jogo = jogo,
                            selecionado = jogo.id in estado.selecionados,
                            modoSelecao = modoSelecao,
                            onClick = {
                                if (modoSelecao) viewModel.alternarSelecao(jogo.id) else onJogoClick(jogo.id)
                            },
                            onLongClick = { viewModel.alternarSelecao(jogo.id) },
                            modifier = Modifier.animateItemPlacement(),
                        )
                    }
                }
            }
        }

        if (modoSelecao) {
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Button(
                    onClick = {
                        dataRecebimentoTexto = DateUtils.formatarData(LocalDate.now())
                        mostrarConfirmar = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(bottom = contentPadding.calculateBottomPadding()),
                ) {
                    Text("MARCAR COMO RECEBIDOS (${estado.selecionados.size})")
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = contentPadding.calculateBottomPadding() + 16.dp),
        ) { dados -> Snackbar(snackbarData = dados) }
    }

    if (mostrarFiltros) {
        RecebimentoFiltroSheet(
            filtro = estado.filtro,
            competicoesDisponiveis = estado.competicoesDisponiveis,
            cidadesDisponiveis = estado.cidadesDisponiveis,
            onStatusChange = viewModel::atualizarStatus,
            onPeriodoChange = viewModel::atualizarPeriodo,
            onCompeticaoChange = viewModel::atualizarCompeticao,
            onCidadeChange = viewModel::atualizarCidade,
            onLimpar = viewModel::limparFiltros,
            onFechar = { mostrarFiltros = false },
        )
    }

    if (mostrarConfirmar) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmar = false },
            title = { Text("Marcar jogos como recebidos?") },
            text = {
                Column {
                    Text(
                        "Você está marcando ${estado.selecionados.size} " +
                            "${if (estado.selecionados.size == 1) "jogo" else "jogos"} como recebido(s). " +
                            "Deseja continuar?",
                    )
                    CampoDataTexto(
                        texto = dataRecebimentoTexto,
                        onTextoChange = { dataRecebimentoTexto = it },
                        label = "Data do recebimento",
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val data = DateUtils.parseData(dataRecebimentoTexto) ?: LocalDate.now()
                    viewModel.marcarSelecionadosComoRecebido(data)
                    mostrarConfirmar = false
                }) { Text("CONFIRMAR") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmar = false }) { Text("CANCELAR") }
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecebimentoJogoItem(
    jogo: Jogo,
    selecionado: Boolean,
    modoSelecao: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coresStatus = LocalStatusColors.current
    val corValor = if (jogo.recebido) coresStatus.recebido else coresStatus.aReceber

    Card(
        modifier = modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selecionado) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (modoSelecao) {
                Checkbox(
                    checked = selecionado,
                    onCheckedChange = { onClick() },
                    modifier = Modifier.padding(end = 8.dp),
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "⚽ ${jogo.confronto ?: "Escala Arbitragem"}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                val horario = jogo.horario
                val dataHora = if (horario != null) {
                    "${DateUtils.formatarData(jogo.data)} • ${DateUtils.formatarHora(horario)}"
                } else {
                    DateUtils.formatarData(jogo.data)
                }
                Text(
                    text = dataHora,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
                val subtitulo = listOfNotNull(
                    jogo.competicao?.takeIf(String::isNotBlank),
                    jogo.cidade?.takeIf(String::isNotBlank),
                ).joinToString(" • ")
                if (subtitulo.isNotBlank()) {
                    Text(
                        text = subtitulo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    text = CurrencyUtils.formatar(jogo.valorCentavos),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = corValor,
                )
                StatusChip(jogo.statusPagamento, modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}

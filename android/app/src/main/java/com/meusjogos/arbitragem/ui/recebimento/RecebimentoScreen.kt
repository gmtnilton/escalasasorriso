package com.meusjogos.arbitragem.ui.recebimento

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.Color
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
    var mostrarEscolhaTipo by remember { mutableStateOf(false) }
    var tipoParaEscolherGrupo by remember { mutableStateOf<TipoAgrupamentoRecebimento?>(null) }
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
            Column(
                modifier = Modifier.fillMaxWidth().padding(
                    start = 16.dp, end = 16.dp,
                    top = contentPadding.calculateTopPadding() + 16.dp,
                    bottom = 12.dp,
                ),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (modoSelecao) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = viewModel::desmarcarTodos) {
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
                        Text("📥 Recebimento", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    }

                    if (estado.jogosFiltrados.isNotEmpty()) {
                        Row {
                            if (modoSelecao) {
                                TextButton(onClick = viewModel::desmarcarTodos) { Text("Desmarcar todos") }
                            }
                            TextButton(onClick = viewModel::selecionarTodosVisiveis) { Text("Selecionar todos") }
                        }
                    }
                }

                if (!modoSelecao) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        ContadorRecebimento(emoji = "🔴", valor = estado.totalPendentes, rotulo = "Pendentes", cor = coresStatus.aReceber)
                        ContadorRecebimento(emoji = "🟢", valor = estado.totalRecebidos, rotulo = "Recebidos", cor = coresStatus.recebido)
                        ContadorRecebimento(emoji = "⚽", valor = estado.totalGeral, rotulo = "Total", cor = MaterialTheme.colorScheme.onSurface)
                    }

                    Button(
                        onClick = { mostrarEscolhaTipo = true },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("RECEBER", fontWeight = FontWeight.Bold)
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
                        start = 16.dp, end = 16.dp, top = 16.dp,
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
                    enabled = !estado.processando,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(bottom = contentPadding.calculateBottomPadding()),
                ) {
                    if (estado.processando) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Processando...")
                    } else {
                        Text("📥 MARCAR COMO RECEBIDOS (${estado.selecionados.size})")
                    }
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
            modalidadesDisponiveis = estado.modalidadesDisponiveis,
            cidadesDisponiveis = estado.cidadesDisponiveis,
            onStatusChange = viewModel::atualizarStatus,
            onPeriodoChange = viewModel::atualizarPeriodo,
            onCompeticaoChange = viewModel::atualizarCompeticao,
            onModalidadeChange = viewModel::atualizarModalidade,
            onCidadeChange = viewModel::atualizarCidade,
            onLimpar = viewModel::limparFiltros,
            onFechar = { mostrarFiltros = false },
        )
    }

    if (mostrarEscolhaTipo) {
        EscolherTipoRecebimentoSheet(
            onEscolher = { tipo ->
                mostrarEscolhaTipo = false
                if (tipo == null) viewModel.limparAgrupamento() else tipoParaEscolherGrupo = tipo
            },
            onFechar = { mostrarEscolhaTipo = false },
        )
    }

    val tipoAtual = tipoParaEscolherGrupo
    if (tipoAtual != null) {
        val grupos = when (tipoAtual) {
            TipoAgrupamentoRecebimento.COMPETICAO -> estado.pendentesPorCompeticao
            TipoAgrupamentoRecebimento.MODALIDADE -> estado.pendentesPorModalidade
            TipoAgrupamentoRecebimento.CIDADE -> estado.pendentesPorCidade
        }
        EscolherGrupoRecebimentoSheet(
            tipo = tipoAtual,
            grupos = grupos,
            onSelecionar = { nome ->
                when (tipoAtual) {
                    TipoAgrupamentoRecebimento.COMPETICAO -> viewModel.atualizarCompeticao(nome)
                    TipoAgrupamentoRecebimento.MODALIDADE -> viewModel.atualizarModalidade(nome)
                    TipoAgrupamentoRecebimento.CIDADE -> viewModel.atualizarCidade(nome)
                }
                tipoParaEscolherGrupo = null
            },
            onFechar = { tipoParaEscolherGrupo = null },
        )
    }

    if (mostrarConfirmar) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmar = false },
            title = { Text("📥 Confirmar recebimento") },
            text = {
                Column {
                    Text(
                        "Você está prestes a marcar ${estado.selecionados.size} " +
                            "${if (estado.selecionados.size == 1) "jogo" else "jogos"} como recebido(s).",
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

/** Os 3 agrupamentos oferecidos pelo botão "➕ Receber" — "🎯 Jogo" não agrupa nada, então não
 * precisa de um valor aqui (ver [EscolherTipoRecebimentoSheet]). */
private enum class TipoAgrupamentoRecebimento(val titulo: String, val emoji: String) {
    COMPETICAO("Competição", "🏆"),
    MODALIDADE("Modalidade", "⚽"),
    CIDADE("Cidade", "📍"),
}

/** Primeiro passo do botão "➕ Receber": como o usuário quer navegar até os jogos pendentes. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EscolherTipoRecebimentoSheet(
    onEscolher: (TipoAgrupamentoRecebimento?) -> Unit,
    onFechar: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onFechar) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            Text("Como você deseja receber?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Column(modifier = Modifier.padding(top = 12.dp)) {
                OpcaoRecebimento(emoji = "🏆", titulo = "Competição") {
                    onEscolher(TipoAgrupamentoRecebimento.COMPETICAO)
                }
                OpcaoRecebimento(emoji = "⚽", titulo = "Modalidade") {
                    onEscolher(TipoAgrupamentoRecebimento.MODALIDADE)
                }
                OpcaoRecebimento(emoji = "🎯", titulo = "Jogo") { onEscolher(null) }
                OpcaoRecebimento(emoji = "📍", titulo = "Cidade") { onEscolher(TipoAgrupamentoRecebimento.CIDADE) }
            }
        }
    }
}

@Composable
private fun OpcaoRecebimento(emoji: String, titulo: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(emoji, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            Icon(Icons.Filled.ChevronRight, contentDescription = null)
        }
    }
}

/** Segundo passo (Competição/Modalidade/Cidade): lista os grupos que têm jogo pendente, com a
 * contagem — tocar num grupo aplica esse filtro na lista de Recebimento já existente. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EscolherGrupoRecebimentoSheet(
    tipo: TipoAgrupamentoRecebimento,
    grupos: List<Pair<String, Int>>,
    onSelecionar: (String) -> Unit,
    onFechar: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onFechar) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            Text(
                text = "${tipo.emoji} Receber por ${tipo.titulo.lowercase()}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            if (grupos.isEmpty()) {
                Text(
                    text = "Nenhum jogo pendente encontrado.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp),
                )
            } else {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    grupos.forEach { (nome, quantidade) ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp).clickable { onSelecionar(nome) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text(
                                        text = "${tipo.emoji} $nome",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        text = "$quantidade ${if (quantidade == 1) "jogo pendente" else "jogos pendentes"}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Icon(Icons.Filled.ChevronRight, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContadorRecebimento(emoji: String, valor: Int, rotulo: String, cor: Color) {
    Column {
        Text(
            text = "$emoji $valor",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = cor,
        )
        Text(
            text = rotulo,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    val corFundo by animateColorAsState(
        targetValue = if (selecionado) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        animationSpec = tween(200),
        label = "selecao-cor",
    )

    Card(
        modifier = modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongClick),
        colors = CardDefaults.cardColors(containerColor = corFundo),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
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
                jogo.competicao?.takeIf(String::isNotBlank)?.let { competicao ->
                    Text(
                        text = "🏆 ${competicao.uppercase()}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = "⚽ ${jogo.confronto ?: "Escala Arbitragem"}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
                val horario = jogo.horario
                val camposLinha = listOfNotNull(
                    "📅 ${DateUtils.formatarData(jogo.data)}",
                    horario?.let { "⏰ ${DateUtils.formatarHora(it)}" },
                    jogo.cidade?.takeIf(String::isNotBlank)?.let { "📍 $it" },
                ).joinToString("   ")
                Text(
                    text = camposLinha,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StatusChip(jogo.statusPagamento)
                    Text(
                        text = CurrencyUtils.formatar(jogo.valorCentavos),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = corValor,
                    )
                }
            }
        }
    }
}

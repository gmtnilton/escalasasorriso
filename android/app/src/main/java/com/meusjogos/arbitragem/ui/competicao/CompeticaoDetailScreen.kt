package com.meusjogos.arbitragem.ui.competicao

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meusjogos.arbitragem.core.model.Jogo
import com.meusjogos.arbitragem.core.model.ResumoCompeticao
import com.meusjogos.arbitragem.core.util.CurrencyUtils
import com.meusjogos.arbitragem.core.util.DateUtils
import com.meusjogos.arbitragem.ui.components.EmptyState
import com.meusjogos.arbitragem.ui.components.SectionHeader
import com.meusjogos.arbitragem.ui.components.StatusChip
import com.meusjogos.arbitragem.ui.theme.LocalStatusColors
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Tela da competição (VERSÃO 1.2, REGRA 13): cabeçalho com status, resumo
 * financeiro, lista de jogos e as ações que dependem do status — Encerrar
 * (em andamento) ou Recebimento total/Gerar recibo/Reabrir (encerrada).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompeticaoDetailScreen(
    viewModel: CompeticaoDetailViewModel,
    onVoltar: () -> Unit,
    onNovoJogo: (competicaoId: Long) -> Unit,
    onJogoClick: (Long) -> Unit,
    onGerarRecibo: (competicaoId: Long) -> Unit,
    onCompeticaoAtualizada: (novoId: Long) -> Unit,
) {
    val estado by viewModel.uiState.collectAsState()
    var mostrarEncerrar by remember { mutableStateOf(false) }
    var mostrarReceber by remember { mutableStateOf(false) }
    var mostrarEditar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val escopo = rememberCoroutineScope()

    LaunchedEffect(estado.mensagem) {
        estado.mensagem?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limparMensagem()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(estado.competicao?.nome ?: "Competição") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) { Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar") }
                },
                actions = {
                    if (estado.competicao != null) {
                        IconButton(onClick = { mostrarEditar = true }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar competição")
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(snackbarData = it) } },
    ) { padding ->
        val competicao = estado.competicao

        when {
            estado.carregando -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            competicao == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Competição não encontrada.")
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    CabecalhoCompeticaoCard(nome = competicao.nome, subtitulo = competicao.subtitulo, encerrada = competicao.encerrada)
                }

                item { ResumoCompeticaoCard(estado.resumo) }

                item {
                    BotoesAcaoCompeticao(
                        encerrada = competicao.encerrada,
                        temPendente = estado.resumo.valorPendenteCentavos > 0,
                        temRecibo = estado.temRecibo,
                        algumRecebido = estado.resumo.jogosRecebidos > 0,
                        processando = estado.processando,
                        onNovoJogo = { onNovoJogo(competicao.id) },
                        onEncerrar = { mostrarEncerrar = true },
                        onReabrir = viewModel::reabrir,
                        onReceber = { mostrarReceber = true },
                        onGerarRecibo = { onGerarRecibo(competicao.id) },
                    )
                }

                item {
                    SectionHeader(titulo = "Jogos (${estado.jogos.size})")
                }

                if (estado.jogos.isEmpty()) {
                    item {
                        EmptyState(
                            icone = Icons.Filled.SportsSoccer,
                            titulo = "Nenhum jogo nesta competição",
                            descricao = "Toque em \"Novo jogo\" para cadastrar o primeiro.",
                        )
                    }
                } else {
                    items(estado.jogos, key = { it.id }) { jogo ->
                        JogoDaCompeticaoItem(jogo = jogo, onClick = { onJogoClick(jogo.id) })
                    }
                }
            }
        }
    }

    if (mostrarEncerrar) {
        ConfirmarEncerrarDialog(
            resumo = estado.resumo,
            onConfirmar = { viewModel.encerrar(); mostrarEncerrar = false },
            onCancelar = { mostrarEncerrar = false },
        )
    }

    if (mostrarReceber) {
        ConfirmarRecebimentoTotalDialog(
            competicaoNome = estado.competicao?.nome ?: "",
            resumo = estado.resumo,
            onConfirmar = { data -> viewModel.receberPendentes(data); mostrarReceber = false },
            onCancelar = { mostrarReceber = false },
        )
    }

    if (mostrarEditar && estado.competicao != null) {
        EditarCompeticaoDialog(
            nomeInicial = estado.competicao!!.nome,
            cidadeInicial = estado.competicao!!.cidade ?: "",
            modalidadeInicial = estado.competicao!!.modalidade ?: "",
            onCancelar = { mostrarEditar = false },
            onConfirmar = { nome, cidade, modalidade ->
                val idAntesDeEditar = viewModel.competicaoIdAtual
                escopo.launch {
                    val novoId = viewModel.editar(nome, cidade, modalidade)
                    mostrarEditar = false
                    if (novoId != idAntesDeEditar) onCompeticaoAtualizada(novoId)
                }
            },
        )
    }
}

@Composable
private fun CabecalhoCompeticaoCard(nome: String, subtitulo: String?, encerrada: Boolean) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(nome, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (!subtitulo.isNullOrBlank()) {
                Text(subtitulo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
            }
            Row(modifier = Modifier.padding(top = 12.dp)) {
                if (encerrada) {
                    StatusTextoChip(texto = "🔴 ENCERRADA", cor = MaterialTheme.colorScheme.error)
                } else {
                    val coresStatus = LocalStatusColors.current
                    StatusTextoChip(texto = "🟢 EM ANDAMENTO", cor = coresStatus.recebido)
                }
            }
        }
    }
}

@Composable
private fun StatusTextoChip(texto: String, cor: androidx.compose.ui.graphics.Color) {
    Text(texto, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = cor)
}

@Composable
private fun ResumoCompeticaoCard(resumo: ResumoCompeticao) {
    val coresStatus = LocalStatusColors.current
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Resumo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            LinhaResumo("Jogos", resumo.totalJogos.toString())
            LinhaResumo("Recebidos", resumo.jogosRecebidos.toString(), coresStatus.recebido)
            LinhaResumo("Pendentes", resumo.jogosPendentes.toString(), coresStatus.aReceber)
            androidx.compose.material3.Divider(modifier = Modifier.padding(vertical = 10.dp))
            LinhaResumo("Valor total", CurrencyUtils.formatar(resumo.valorTotalCentavos))
            LinhaResumo("Recebido", CurrencyUtils.formatar(resumo.valorRecebidoCentavos), coresStatus.recebido)
            LinhaResumo("Pendente", CurrencyUtils.formatar(resumo.valorPendenteCentavos), coresStatus.aReceber)
        }
    }
}

@Composable
private fun LinhaResumo(rotulo: String, valor: String, cor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(rotulo, style = MaterialTheme.typography.bodyMedium)
        Text(valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = cor)
    }
}

@Composable
private fun BotoesAcaoCompeticao(
    encerrada: Boolean,
    temPendente: Boolean,
    temRecibo: Boolean,
    algumRecebido: Boolean,
    processando: Boolean,
    onNovoJogo: () -> Unit,
    onEncerrar: () -> Unit,
    onReabrir: () -> Unit,
    onReceber: () -> Unit,
    onGerarRecibo: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (!encerrada) {
            Button(onClick = onNovoJogo, modifier = Modifier.fillMaxWidth()) { Text("➕ Novo jogo") }
            OutlinedButton(onClick = onEncerrar, modifier = Modifier.fillMaxWidth()) { Text("🔒 Encerrar competição") }
        } else {
            if (temPendente) {
                Button(
                    onClick = onReceber,
                    enabled = !processando,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("💰 RECEBIMENTO TOTAL DA COMPETIÇÃO") }
            }
            if (algumRecebido) {
                OutlinedButton(onClick = onGerarRecibo, modifier = Modifier.fillMaxWidth()) {
                    Text(if (temRecibo) "📄 Recibo gerado" else "📄 Gerar recibo")
                }
            }
            OutlinedButton(onClick = onReabrir, modifier = Modifier.fillMaxWidth()) { Text("🔓 Reabrir competição") }
        }
    }
}

@Composable
private fun JogoDaCompeticaoItem(jogo: Jogo, onClick: () -> Unit) {
    val coresStatus = LocalStatusColors.current
    val corValor = if (jogo.recebido) coresStatus.recebido else MaterialTheme.colorScheme.onSurface
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text(
                    text = "⚽ ${jogo.confronto ?: "Escala Arbitragem"}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                )
                StatusChip(jogo.statusPagamento)
            }
            val horario = jogo.horario
            val dataHora = if (horario != null) "${DateUtils.formatarData(jogo.data)} • ${DateUtils.formatarHora(horario)}" else DateUtils.formatarData(jogo.data)
            val local = listOfNotNull(jogo.cidade?.takeIf(String::isNotBlank), jogo.estadio?.takeIf(String::isNotBlank)).joinToString(" • ")
            Text(
                text = if (local.isNotBlank()) "$dataHora • $local" else dataHora,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = CurrencyUtils.formatar(jogo.valorCentavos),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = corValor,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun ConfirmarEncerrarDialog(resumo: ResumoCompeticao, onConfirmar: () -> Unit, onCancelar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Deseja realmente encerrar esta competição?") },
        text = {
            Column {
                LinhaResumo("Jogos", resumo.totalJogos.toString())
                LinhaResumo("Valor total", CurrencyUtils.formatar(resumo.valorTotalCentavos))
                LinhaResumo("Recebido", CurrencyUtils.formatar(resumo.valorRecebidoCentavos))
                LinhaResumo("Pendente", CurrencyUtils.formatar(resumo.valorPendenteCentavos))
            }
        },
        confirmButton = { TextButton(onClick = onConfirmar) { Text("ENCERRAR COMPETIÇÃO") } },
        dismissButton = { TextButton(onClick = onCancelar) { Text("CANCELAR") } },
    )
}

@Composable
private fun ConfirmarRecebimentoTotalDialog(
    competicaoNome: String,
    resumo: ResumoCompeticao,
    onConfirmar: (LocalDate) -> Unit,
    onCancelar: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Deseja registrar o recebimento total desta competição?") },
        text = {
            Column {
                Text(competicaoNome, fontWeight = FontWeight.Bold)
                LinhaResumo("Valor pendente", CurrencyUtils.formatar(resumo.valorPendenteCentavos))
                LinhaResumo("Jogos pendentes", resumo.jogosPendentes.toString())
            }
        },
        confirmButton = { TextButton(onClick = { onConfirmar(LocalDate.now()) }) { Text("CONFIRMAR RECEBIMENTO") } },
        dismissButton = { TextButton(onClick = onCancelar) { Text("CANCELAR") } },
    )
}

@Composable
private fun EditarCompeticaoDialog(
    nomeInicial: String,
    cidadeInicial: String,
    modalidadeInicial: String,
    onCancelar: () -> Unit,
    onConfirmar: (nome: String, cidade: String?, modalidade: String?) -> Unit,
) {
    var nome by remember { mutableStateOf(nomeInicial) }
    var cidade by remember { mutableStateOf(cidadeInicial) }
    var modalidade by remember { mutableStateOf(modalidadeInicial) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Editar competição") },
        text = {
            Column {
                OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = cidade,
                    onValueChange = { cidade = it },
                    label = { Text("Cidade") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                )
                OutlinedTextField(
                    value = modalidade,
                    onValueChange = { modalidade = it },
                    label = { Text("Modalidade") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                )
                Text(
                    "Se já existir outra competição com esse mesmo nome, cidade e modalidade, as duas serão unificadas automaticamente — nenhum jogo ou recebimento é perdido.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirmar(nome, cidade.ifBlank { null }, modalidade.ifBlank { null }) }, enabled = nome.isNotBlank()) {
                Text("Salvar")
            }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } },
    )
}

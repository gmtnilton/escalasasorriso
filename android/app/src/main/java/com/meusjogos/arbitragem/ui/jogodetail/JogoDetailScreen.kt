package com.meusjogos.arbitragem.ui.jogodetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.unit.dp
import com.meusjogos.arbitragem.core.model.Jogo
import com.meusjogos.arbitragem.core.model.StatusPagamento
import com.meusjogos.arbitragem.core.util.CurrencyUtils
import com.meusjogos.arbitragem.core.util.DateUtils
import com.meusjogos.arbitragem.ui.components.CampoDataTexto
import com.meusjogos.arbitragem.ui.components.ConfirmarAcaoDialog
import com.meusjogos.arbitragem.ui.components.SectionHeader
import com.meusjogos.arbitragem.ui.components.StatusChip
import com.meusjogos.arbitragem.ui.theme.LocalStatusColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JogoDetailScreen(
    viewModel: JogoDetailViewModel,
    onVoltar: () -> Unit,
    onEditar: () -> Unit,
    onDuplicarConcluido: (Long) -> Unit,
    onExcluirConcluido: () -> Unit,
) {
    val estado by viewModel.uiState.collectAsState()
    var mostrarConfirmarRecebido by remember { mutableStateOf(false) }
    var mostrarConfirmarExcluir by remember { mutableStateOf(false) }
    var dataRecebimentoEscolhida by remember { mutableStateOf(DateUtils.formatarData(java.time.LocalDate.now())) }

    LaunchedEffect(estado.excluido) {
        if (estado.excluido) onExcluirConcluido()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do jogo") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { padding ->
        val jogo = estado.jogo

        when {
            estado.carregando -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            jogo == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Jogo não encontrado.")
            }
            else -> Column(
                modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                DetalhesCard(jogo)

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onEditar,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("✏️ Editar") }

                    if (jogo.statusPagamento == StatusPagamento.A_RECEBER) {
                        Button(
                            onClick = {
                                dataRecebimentoEscolhida = DateUtils.formatarData(java.time.LocalDate.now())
                                mostrarConfirmarRecebido = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("💰 Marcar como recebido") }
                    } else {
                        OutlinedButton(
                            onClick = viewModel::desfazerRecebimento,
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("↩️ Desfazer recebimento") }
                    }

                    OutlinedButton(
                        onClick = { viewModel.duplicar(onDuplicarConcluido) },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("📋 Duplicar jogo") }
                }

                Divider(modifier = Modifier.padding(top = 4.dp))

                TextButton(
                    onClick = { mostrarConfirmarExcluir = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Filled.DeleteOutline, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                    Text("Excluir jogo")
                }
            }
        }
    }

    if (mostrarConfirmarRecebido) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmarRecebido = false },
            title = { Text("Marcar como recebido?") },
            text = {
                Column {
                    Text("Confirma o recebimento deste jogo? Você pode ajustar a data do recebimento abaixo.")
                    CampoDataTexto(
                        texto = dataRecebimentoEscolhida,
                        onTextoChange = { dataRecebimentoEscolhida = it },
                        label = "Data do recebimento",
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val data = DateUtils.parseData(dataRecebimentoEscolhida) ?: java.time.LocalDate.now()
                    viewModel.marcarComoRecebido(data)
                    mostrarConfirmarRecebido = false
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmarRecebido = false }) { Text("Cancelar") }
            },
        )
    }

    if (mostrarConfirmarExcluir) {
        ConfirmarAcaoDialog(
            titulo = "Excluir jogo?",
            mensagem = "Essa ação não pode ser desfeita.",
            textoConfirmar = "Excluir",
            onConfirmar = {
                viewModel.excluir()
                mostrarConfirmarExcluir = false
            },
            onCancelar = { mostrarConfirmarExcluir = false },
        )
    }
}

@Composable
private fun DetalhesCard(jogo: Jogo) {
    val coresStatus = LocalStatusColors.current
    val corValor = if (jogo.recebido) coresStatus.recebido else MaterialTheme.colorScheme.onSurface

    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Cabeçalho: confronto (ou fallback), data/horário e valor + status em destaque.
            Text(
                text = "⚽ ${jogo.confronto ?: "Jogo de arbitragem"}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
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

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = CurrencyUtils.formatar(jogo.valorCentavos),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = corValor,
                )
                StatusChip(jogo.statusPagamento)
            }

            val competicaoInfo = listOfNotNull(
                jogo.competicao?.takeIf(String::isNotBlank),
                jogo.modalidade?.takeIf(String::isNotBlank),
                jogo.categoria?.takeIf(String::isNotBlank),
            )
            if (competicaoInfo.isNotEmpty()) {
                Secao(titulo = "Competição") {
                    Text(competicaoInfo.joinToString(" • "), style = MaterialTheme.typography.bodyLarge)
                }
            }

            val localInfo = listOfNotNull(
                jogo.cidade?.takeIf(String::isNotBlank),
                jogo.estadio?.takeIf(String::isNotBlank),
                jogo.funcao?.takeIf(String::isNotBlank)?.let { "Função: $it" },
            )
            if (localInfo.isNotEmpty()) {
                Secao(titulo = "Local e arbitragem") {
                    Text(localInfo.joinToString(" • "), style = MaterialTheme.typography.bodyLarge)
                }
            }

            jogo.dataRecebimento?.let { data ->
                Secao(titulo = "Pagamento") {
                    LinhaDetalhe("Recebido em", DateUtils.formatarData(data))
                }
            }

            jogo.observacoes?.takeIf(String::isNotBlank)?.let { texto ->
                Secao(titulo = "Observações") {
                    Text(texto, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun Secao(titulo: String, conteudo: @Composable () -> Unit) {
    Divider(modifier = Modifier.padding(top = 18.dp, bottom = 14.dp))
    SectionHeader(titulo = titulo)
    conteudo()
}

@Composable
private fun LinhaDetalhe(rotulo: String, valor: String) {
    Column {
        Text(text = rotulo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = valor, style = MaterialTheme.typography.bodyLarge)
    }
}

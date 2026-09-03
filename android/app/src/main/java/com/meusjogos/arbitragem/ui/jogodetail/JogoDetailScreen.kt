package com.meusjogos.arbitragem.ui.jogodetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
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
import com.meusjogos.arbitragem.ui.components.StatusChip

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

                Button(
                    onClick = onEditar,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("✏️ EDITAR") }

                if (jogo.statusPagamento == StatusPagamento.A_RECEBER) {
                    Button(
                        onClick = {
                            dataRecebimentoEscolhida = DateUtils.formatarData(java.time.LocalDate.now())
                            mostrarConfirmarRecebido = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("💰 MARCAR COMO RECEBIDO") }
                } else {
                    OutlinedButton(
                        onClick = viewModel::desfazerRecebimento,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("↩️ DESFAZER RECEBIMENTO") }
                }

                OutlinedButton(
                    onClick = { viewModel.duplicar(onDuplicarConcluido) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("📋 DUPLICAR JOGO") }

                OutlinedButton(
                    onClick = { mostrarConfirmarExcluir = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("🗑️ EXCLUIR") }
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
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = CurrencyUtils.formatar(jogo.valorCentavos),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                StatusChip(jogo.statusPagamento)
            }

            Divider()

            LinhaDetalhe("Data", DateUtils.formatarData(jogo.data))
            jogo.horario?.let { LinhaDetalhe("Horário", DateUtils.formatarHora(it)) }
            jogo.confronto?.let { LinhaDetalhe("Confronto", it) }
            jogo.competicao?.let { LinhaDetalhe("Competição", it) }
            jogo.categoria?.let { LinhaDetalhe("Categoria", it) }
            jogo.local?.let { LinhaDetalhe("Local", it) }
            jogo.funcao?.let { LinhaDetalhe("Função", it) }
            jogo.dataRecebimento?.let { LinhaDetalhe("Recebido em", DateUtils.formatarData(it)) }
            jogo.observacoes?.let { LinhaDetalhe("Observações", it) }
        }
    }
}

@Composable
private fun LinhaDetalhe(rotulo: String, valor: String) {
    Column {
        Text(text = rotulo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = valor, style = MaterialTheme.typography.bodyLarge)
    }
}

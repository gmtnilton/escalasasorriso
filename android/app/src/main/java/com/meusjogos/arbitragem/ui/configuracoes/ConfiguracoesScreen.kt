package com.meusjogos.arbitragem.ui.configuracoes

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

@Composable
fun ConfiguracoesScreen(
    viewModel: ConfiguracoesViewModel,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val estado by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val lancadorBackup = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        escopo.launch {
            val conteudo = viewModel.gerarConteudoBackup()
            context.contentResolver.openOutputStream(uri)?.use { saida ->
                OutputStreamWriter(saida, Charsets.UTF_8).use { it.write(conteudo) }
            }
            viewModel.aoBackupConcluido()
        }
    }

    val lancadorRestaurar = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        escopo.launch {
            val conteudo = context.contentResolver.openInputStream(uri)?.use { entrada ->
                BufferedReader(InputStreamReader(entrada, Charsets.UTF_8)).readText()
            }
            if (conteudo != null) {
                viewModel.restaurarBackup(conteudo)
            } else {
                viewModel.reportarErro("Não foi possível ler o arquivo selecionado.")
            }
        }
    }

    val lancadorExportarCsv = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        escopo.launch {
            val conteudo = viewModel.gerarConteudoCsv()
            context.contentResolver.openOutputStream(uri)?.use { saida ->
                OutputStreamWriter(saida, Charsets.UTF_8).use { it.write(conteudo) }
            }
            viewModel.aoExportarCsvConcluido()
        }
    }

    LaunchedEffect(estado.mensagem) {
        estado.mensagem?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limparMensagem()
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = contentPadding.calculateTopPadding() + 16.dp,
                bottom = contentPadding.calculateBottomPadding() + 32.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text("⚙️ Configurações", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        ListItem(
                            headlineContent = { Text("Fazer backup") },
                            supportingContent = { Text("Salva todos os jogos em um arquivo .json") },
                            leadingContent = { Icon(Icons.Filled.CloudUpload, contentDescription = null) },
                            modifier = Modifier.clickable { lancadorBackup.launch(viewModel.nomeArquivoBackup()) },
                        )
                        ListItem(
                            headlineContent = { Text("Restaurar backup") },
                            supportingContent = { Text("Substitui os jogos atuais pelos de um arquivo .json") },
                            leadingContent = { Icon(Icons.Filled.CloudDownload, contentDescription = null) },
                            modifier = Modifier.clickable {
                                lancadorRestaurar.launch(arrayOf("application/json", "text/*", "*/*"))
                            },
                        )
                        ListItem(
                            headlineContent = { Text("Exportar dados (CSV)") },
                            supportingContent = { Text("Gera uma planilha para abrir no Excel/Planilhas") },
                            leadingContent = { Icon(Icons.Filled.TableChart, contentDescription = null) },
                            modifier = Modifier.clickable { lancadorExportarCsv.launch(viewModel.nomeArquivoCsv()) },
                        )
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text("Meus Jogos de Arbitragem") },
                        supportingContent = { Text("Versão 1.0.0 — funciona 100% offline, seus dados ficam só no aparelho.") },
                        leadingContent = { Icon(Icons.Filled.Info, contentDescription = null) },
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = contentPadding.calculateBottomPadding() + 16.dp),
        ) { dados ->
            Snackbar(snackbarData = dados)
        }
    }
}

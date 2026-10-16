package com.meusjogos.arbitragem.ui.recibo

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.meusjogos.arbitragem.ui.components.CampoDataTexto
import com.meusjogos.arbitragem.ui.components.CampoValorMonetario
import com.meusjogos.arbitragem.ui.components.SectionHeader
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

/**
 * Formulário de recibo de pagamento (REGRA 13-18): pagador, recebedor,
 * valor e data já vêm preenchidos (do jogo, ou do recibo já existente) mas
 * tudo é editável antes de gerar. "Gerar/Atualizar" salva o recibo e abre o
 * compartilhamento do Android (WhatsApp, e-mail etc.); "Salvar no aparelho"
 * grava o mesmo PDF onde o usuário escolher.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GerarReciboScreen(
    viewModel: GerarReciboViewModel,
    onVoltar: () -> Unit,
) {
    val estado by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()
    var processando by remember { mutableStateOf(false) }

    val lancadorSalvar = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        escopo.launch {
            val recibo = viewModel.salvarRecibo()
            context.contentResolver.openOutputStream(uri)?.use { saida -> viewModel.gerarPdf(recibo, saida) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🧾 Recibo de pagamento") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { padding ->
        if (estado.carregando) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            estado.numeroFormatado?.let { numero ->
                Text(
                    "Recibo $numero já gerado para este jogo — os dados abaixo podem ser ajustados a qualquer momento.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            FormSection(titulo = "Pagador") {
                OutlinedTextField(
                    value = estado.pagadorNome,
                    onValueChange = viewModel::atualizarPagadorNome,
                    label = { Text("Nome / Razão social") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = estado.pagadorDocumento,
                    onValueChange = viewModel::atualizarPagadorDocumento,
                    label = { Text("CPF / CNPJ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            FormSection(titulo = "Recebedor") {
                OutlinedTextField(
                    value = estado.recebedorNome,
                    onValueChange = viewModel::atualizarRecebedorNome,
                    label = { Text("Nome completo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = estado.recebedorDocumento,
                    onValueChange = viewModel::atualizarRecebedorDocumento,
                    label = { Text("CPF") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            FormSection(titulo = "Pagamento") {
                CampoValorMonetario(
                    valorCentavos = estado.valorCentavos,
                    onValorChange = viewModel::atualizarValor,
                    label = "Valor recebido",
                    modifier = Modifier.fillMaxWidth(),
                )
                CampoDataTexto(
                    texto = estado.dataPagamentoTexto,
                    onTextoChange = viewModel::atualizarDataPagamento,
                    label = "Data do pagamento",
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = estado.descricao,
                    onValueChange = viewModel::atualizarDescricao,
                    label = { Text("Descrição / Referente a (opcional)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    enabled = !processando,
                    onClick = {
                        if (processando) return@Button
                        processando = true
                        escopo.launch {
                            val recibo = viewModel.salvarRecibo()
                            val diretorio = File(context.cacheDir, "recibos").apply { mkdirs() }
                            val arquivo = File(diretorio, viewModel.nomeArquivoPdf())
                            FileOutputStream(arquivo).use { saida -> viewModel.gerarPdf(recibo, saida) }
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", arquivo)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Compartilhar recibo"))
                            processando = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (processando) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text(estado.tituloBotaoPrincipal)
                    }
                }

                OutlinedButton(
                    onClick = { lancadorSalvar.launch(viewModel.nomeArquivoPdf()) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("💾 Salvar PDF no aparelho") }
            }
        }
    }
}

@Composable
private fun FormSection(titulo: String, conteudo: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeader(titulo = titulo)
            conteudo()
        }
    }
}

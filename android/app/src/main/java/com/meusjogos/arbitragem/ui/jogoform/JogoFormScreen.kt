package com.meusjogos.arbitragem.ui.jogoform

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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.meusjogos.arbitragem.core.model.StatusPagamento
import com.meusjogos.arbitragem.ui.components.CampoComOpcoes
import com.meusjogos.arbitragem.ui.components.CampoDataTexto
import com.meusjogos.arbitragem.ui.components.CampoHoraTexto
import com.meusjogos.arbitragem.ui.components.CampoValorMonetario

private val COMPETICOES_PADRAO = listOf("Campeonato Estadual", "Campeonato Municipal", "Copa", "Amistoso", "Base", "Feminino", "Outro")
private val CATEGORIAS_PADRAO = listOf("Profissional", "Amador", "Sub-20", "Sub-17", "Sub-15", "Feminino", "Outro")
private val FUNCOES_PADRAO = listOf("Árbitro", "Assistente 1", "Assistente 2", "Quarto árbitro", "VAR", "Outro")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JogoFormScreen(
    viewModel: JogoFormViewModel,
    onSalvarConcluido: () -> Unit,
    onCancelar: () -> Unit,
) {
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(estado.salvo) {
        if (estado.salvo) onSalvarConcluido()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(estado.tituloTela) },
                navigationIcon = {
                    IconButton(onClick = onCancelar) {
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                "Só data e valor são obrigatórios — complete o resto quando quiser.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                CampoDataTexto(
                    texto = estado.dataTexto,
                    onTextoChange = viewModel::atualizarData,
                    isError = estado.erroData != null,
                    supportingText = estado.erroData,
                    modifier = Modifier.weight(1.3f),
                )
                CampoHoraTexto(
                    texto = estado.horarioTexto,
                    onTextoChange = viewModel::atualizarHorario,
                    modifier = Modifier.weight(1f),
                )
            }

            CampoComOpcoes(
                valor = estado.competicao,
                onValorChange = viewModel::atualizarCompeticao,
                label = "Competição",
                opcoes = COMPETICOES_PADRAO,
                modifier = Modifier.fillMaxWidth(),
            )

            CampoComOpcoes(
                valor = estado.categoria,
                onValorChange = viewModel::atualizarCategoria,
                label = "Categoria",
                opcoes = CATEGORIAS_PADRAO,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = estado.equipeMandante,
                onValueChange = viewModel::atualizarEquipeMandante,
                label = { Text("Equipe mandante") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = estado.equipeVisitante,
                onValueChange = viewModel::atualizarEquipeVisitante,
                label = { Text("Equipe visitante") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = estado.local,
                onValueChange = viewModel::atualizarLocal,
                label = { Text("Local / estádio") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            CampoComOpcoes(
                valor = estado.funcao,
                onValorChange = viewModel::atualizarFuncao,
                label = "Função",
                opcoes = FUNCOES_PADRAO,
                modifier = Modifier.fillMaxWidth(),
            )

            CampoValorMonetario(
                valorCentavos = estado.valorCentavos,
                onValorChange = viewModel::atualizarValor,
                isError = estado.erroValor != null,
                supportingText = estado.erroValor,
                modifier = Modifier.fillMaxWidth(),
            )

            Column {
                Text("Status", style = MaterialTheme.typography.labelLarge)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                    SegmentedButton(
                        selected = estado.status == StatusPagamento.A_RECEBER,
                        onClick = { viewModel.atualizarStatus(StatusPagamento.A_RECEBER) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    ) { Text("🔴 A receber") }
                    SegmentedButton(
                        selected = estado.status == StatusPagamento.RECEBIDO,
                        onClick = { viewModel.atualizarStatus(StatusPagamento.RECEBIDO) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    ) { Text("🟢 Recebido") }
                }
            }

            if (estado.status == StatusPagamento.RECEBIDO) {
                CampoDataTexto(
                    texto = estado.dataRecebimentoTexto,
                    onTextoChange = viewModel::atualizarDataRecebimento,
                    label = "Data do recebimento",
                    obrigatorio = false,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            OutlinedTextField(
                value = estado.observacoes,
                onValueChange = viewModel::atualizarObservacoes,
                label = { Text("Observações") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = viewModel::salvar,
                enabled = !estado.salvando,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 24.dp),
            ) {
                Text(if (estado.salvando) "Salvando..." else "SALVAR")
            }
        }
    }
}

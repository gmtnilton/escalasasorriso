package com.meusjogos.arbitragem.ui.jogoform

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.meusjogos.arbitragem.core.model.StatusPagamento
import com.meusjogos.arbitragem.core.util.CurrencyUtils
import com.meusjogos.arbitragem.data.preferences.AtivacaoPreferences
import com.meusjogos.arbitragem.ui.components.AtivacaoDialog
import com.meusjogos.arbitragem.ui.components.CampoComOpcoes
import com.meusjogos.arbitragem.ui.components.CampoDataTexto
import com.meusjogos.arbitragem.ui.components.CampoHoraTexto
import com.meusjogos.arbitragem.ui.components.CampoValorMonetario
import com.meusjogos.arbitragem.ui.components.SectionHeader

private val COMPETICOES_PADRAO = listOf("Campeonato Estadual", "Campeonato Municipal", "Copa", "Amistoso", "Base", "Feminino", "Outro")
private val CATEGORIAS_PADRAO = listOf("Profissional", "Amador", "Sub-20", "Sub-17", "Sub-15", "Feminino", "Outro")
private val FUNCOES_PADRAO = listOf("Árbitro", "Assistente 1", "Assistente 2", "Quarto árbitro", "VAR", "Anotador", "Outro")
private val MODALIDADES_PADRAO = listOf("Futebol de Campo", "Futebol Society", "Futsal", "Beach Soccer", "Outro")

/** Cidades da região de Sorriso-MT — sugestões; o campo aceita qualquer outra cidade digitada. */
private val CIDADES_PADRAO = listOf(
    "Sorriso", "Lucas do Rio Verde", "Nova Mutum", "Tapurah", "Ipiranga do Norte",
    "Vera", "Feliz Natal", "Sinop", "Cláudia", "União do Sul", "Itaúba",
    "Nova Ubiratã", "Santa Carmem", "Novo Mundo",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JogoFormScreen(
    viewModel: JogoFormViewModel,
    ativacaoPreferences: AtivacaoPreferences,
    onSalvarConcluido: () -> Unit,
    onCancelar: () -> Unit,
) {
    val estado by viewModel.uiState.collectAsState()
    val ativado by ativacaoPreferences.ativado.collectAsState()
    var mostrarAtivacao by remember { mutableStateOf(false) }

    // REGRA: só CRIAR um jogo (novo ou duplicado) exige ativação — editar um
    // jogo já existente nunca fica bloqueado.
    val exigeAtivacao = estado.modo != ModoFormulario.EDITAR

    fun tentarSalvar() {
        if (exigeAtivacao && !ativado) {
            mostrarAtivacao = true
        } else {
            viewModel.salvar()
        }
    }

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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Só data e valor são obrigatórios — complete o resto quando quiser.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            FormSection(titulo = "Informações do jogo") {
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
                    valor = estado.modalidade,
                    onValorChange = viewModel::atualizarModalidade,
                    label = "Modalidade",
                    opcoes = MODALIDADES_PADRAO,
                    modifier = Modifier.fillMaxWidth(),
                )

                CampoComOpcoes(
                    valor = estado.categoria,
                    onValorChange = viewModel::atualizarCategoria,
                    label = "Categoria",
                    opcoes = CATEGORIAS_PADRAO,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            FormSection(titulo = "Equipes", subtitulo = "Opcional — preencha se já souber os times.") {
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
            }

            FormSection(titulo = "Arbitragem") {
                CampoComOpcoes(
                    valor = estado.cidade,
                    onValorChange = viewModel::atualizarCidade,
                    label = "Cidade",
                    opcoes = CIDADES_PADRAO,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = estado.estadio,
                    onValueChange = viewModel::atualizarEstadio,
                    label = { Text("Estádio / ginásio") },
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
            }

            FormSection(titulo = "Pagamento") {
                CampoValorMonetario(
                    valorCentavos = estado.valorCentavos,
                    onValorChange = viewModel::atualizarValor,
                    isError = estado.erroValor != null,
                    supportingText = estado.erroValor,
                    label = if (estado.quantidadePartidas > 1) "Valor por partida" else "Valor",
                    modifier = Modifier.fillMaxWidth(),
                )

                if (estado.permiteVariasPartidas) {
                    CampoQuantidadePartidas(
                        quantidade = estado.quantidadePartidas,
                        onQuantidadeChange = viewModel::atualizarQuantidadePartidas,
                        valorTotalFormatado = CurrencyUtils.formatar(estado.valorTotalCentavos),
                    )
                }

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
            }

            FormSection(titulo = "Observações") {
                OutlinedTextField(
                    value = estado.observacoes,
                    onValueChange = viewModel::atualizarObservacoes,
                    label = { Text("Observações") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Button(
                onClick = ::tentarSalvar,
                enabled = !estado.salvando,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 24.dp),
            ) {
                Text(if (estado.salvando) "Salvando..." else "Salvar")
            }
        }
    }

    if (mostrarAtivacao) {
        AtivacaoDialog(
            ativacaoPreferences = ativacaoPreferences,
            onAtivado = {
                mostrarAtivacao = false
                viewModel.salvar()
            },
            onFechar = { mostrarAtivacao = false },
        )
    }
}

/** Agrupa um bloco do formulário em um card com título — organiza o cadastro em seções claras. */
@Composable
private fun FormSection(
    titulo: String,
    subtitulo: String? = null,
    conteudo: @Composable () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column {
                SectionHeader(titulo = titulo)
                if (subtitulo != null) {
                    Text(
                        text = subtitulo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
            }
            conteudo()
        }
    }
}

/**
 * Cadastro em lote: "apitei 3 partidas hoje, todas por R$ 150" — informa a
 * quantidade e o app cria um jogo para cada uma, todas A RECEBER, com o
 * total já somado automaticamente nos totais do dashboard.
 */
@Composable
private fun CampoQuantidadePartidas(
    quantidade: Int,
    onQuantidadeChange: (Int) -> Unit,
    valorTotalFormatado: String,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Quantidade de partidas", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(onClick = { onQuantidadeChange(quantidade - 1) }, enabled = quantidade > 1) {
                        Icon(Icons.Filled.Remove, contentDescription = "Diminuir quantidade")
                    }
                    Text(
                        text = quantidade.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(48.dp),
                        textAlign = TextAlign.Center,
                    )
                    FilledTonalIconButton(onClick = { onQuantidadeChange(quantidade + 1) }, enabled = quantidade < 30) {
                        Icon(Icons.Filled.Add, contentDescription = "Aumentar quantidade")
                    }
                }
                if (quantidade > 1) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = valorTotalFormatado,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            if (quantidade > 1) {
                Text(
                    text = "$quantidade jogos serão criados, um para cada partida.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

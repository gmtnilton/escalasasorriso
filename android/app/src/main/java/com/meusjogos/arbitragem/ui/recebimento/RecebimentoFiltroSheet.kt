package com.meusjogos.arbitragem.ui.recebimento

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meusjogos.arbitragem.core.model.FiltroJogos
import com.meusjogos.arbitragem.core.model.FiltroPeriodo
import com.meusjogos.arbitragem.core.model.FiltroStatus
import com.meusjogos.arbitragem.core.util.DateUtils
import com.meusjogos.arbitragem.ui.components.CampoDataTexto

/** Filtros da área Recebimento: status, período, competição e cidade — os mesmos dados já usados na lista "Meus Jogos". */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecebimentoFiltroSheet(
    filtro: FiltroJogos,
    competicoesDisponiveis: List<String>,
    cidadesDisponiveis: List<String>,
    onStatusChange: (FiltroStatus) -> Unit,
    onPeriodoChange: (FiltroPeriodo) -> Unit,
    onCompeticaoChange: (String?) -> Unit,
    onCidadeChange: (String?) -> Unit,
    onLimpar: () -> Unit,
    onFechar: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onFechar) {
        Column(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            Text("Filtros", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            SecaoFiltro(titulo = "Status") {
                FilterChip(
                    selected = filtro.status == FiltroStatus.A_RECEBER,
                    onClick = { onStatusChange(FiltroStatus.A_RECEBER) },
                    label = { Text("Pendente") },
                )
                FilterChip(
                    selected = filtro.status == FiltroStatus.RECEBIDOS,
                    onClick = { onStatusChange(FiltroStatus.RECEBIDOS) },
                    label = { Text("Recebido") },
                )
                FilterChip(
                    selected = filtro.status == FiltroStatus.TODOS,
                    onClick = { onStatusChange(FiltroStatus.TODOS) },
                    label = { Text("Todos") },
                )
            }

            SecaoFiltro(titulo = "Data") {
                val opcoes = listOf(
                    "Todas" to FiltroPeriodo.Todos,
                    "Hoje" to FiltroPeriodo.Hoje,
                    "Esta semana" to FiltroPeriodo.EstaSemana,
                    "Este mês" to FiltroPeriodo.EsteMes,
                    "Mês anterior" to FiltroPeriodo.MesAnterior,
                    "Este ano" to FiltroPeriodo.EsteAno,
                )
                opcoes.forEach { (rotulo, valor) ->
                    FilterChip(
                        selected = filtro.periodo == valor,
                        onClick = { onPeriodoChange(valor) },
                        label = { Text(rotulo) },
                    )
                }
                FilterChip(
                    selected = filtro.periodo is FiltroPeriodo.Personalizado,
                    onClick = {
                        if (filtro.periodo !is FiltroPeriodo.Personalizado) {
                            val hoje = java.time.LocalDate.now()
                            onPeriodoChange(FiltroPeriodo.Personalizado(hoje.withDayOfMonth(1), hoje))
                        }
                    },
                    label = { Text("Personalizado") },
                )
            }

            val periodoPersonalizado = filtro.periodo as? FiltroPeriodo.Personalizado
            if (periodoPersonalizado != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    var textoInicio by remember(periodoPersonalizado) {
                        mutableStateOf(DateUtils.formatarData(periodoPersonalizado.inicio))
                    }
                    var textoFim by remember(periodoPersonalizado) {
                        mutableStateOf(DateUtils.formatarData(periodoPersonalizado.fim))
                    }
                    CampoDataTexto(
                        texto = textoInicio,
                        onTextoChange = { novo ->
                            textoInicio = novo
                            DateUtils.parseData(novo)?.let {
                                onPeriodoChange(periodoPersonalizado.copy(inicio = it))
                            }
                        },
                        label = "De",
                        modifier = Modifier.weight(1f),
                    )
                    CampoDataTexto(
                        texto = textoFim,
                        onTextoChange = { novo ->
                            textoFim = novo
                            DateUtils.parseData(novo)?.let {
                                onPeriodoChange(periodoPersonalizado.copy(fim = it))
                            }
                        },
                        label = "Até",
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            if (competicoesDisponiveis.isNotEmpty()) {
                SecaoDropdown(
                    titulo = "Competição",
                    valorSelecionado = filtro.competicao,
                    opcoes = competicoesDisponiveis,
                    onSelecionar = onCompeticaoChange,
                )
            }

            if (cidadesDisponiveis.isNotEmpty()) {
                SecaoDropdown(
                    titulo = "Cidade",
                    valorSelecionado = filtro.cidade,
                    opcoes = cidadesDisponiveis,
                    onSelecionar = onCidadeChange,
                )
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onLimpar) { Text("Limpar filtros") }
                TextButton(onClick = onFechar) { Text("Aplicar") }
            }
        }
    }
}

@Composable
private fun SecaoFiltro(titulo: String, conteudo: @Composable () -> Unit) {
    Text(
        text = titulo,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
    )
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) { conteudo() }
}

@Composable
private fun SecaoDropdown(
    titulo: String,
    valorSelecionado: String?,
    opcoes: List<String>,
    onSelecionar: (String?) -> Unit,
) {
    var expandido by remember { mutableStateOf(false) }
    Text(
        text = titulo,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
    )
    OutlinedButton(onClick = { expandido = true }) {
        Text(valorSelecionado ?: "Todas")
    }
    DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
        DropdownMenuItem(text = { Text("Todas") }, onClick = { onSelecionar(null); expandido = false })
        opcoes.forEach { opcao ->
            DropdownMenuItem(text = { Text(opcao) }, onClick = { onSelecionar(opcao); expandido = false })
        }
    }
}

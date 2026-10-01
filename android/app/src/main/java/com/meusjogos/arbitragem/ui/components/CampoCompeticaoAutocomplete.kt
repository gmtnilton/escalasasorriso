package com.meusjogos.arbitragem.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.meusjogos.arbitragem.core.model.Competicao

/**
 * Campo de competição com autocomplete (VERSÃO 1.2, REGRA 6): digitando o
 * nome, mostra as competições existentes que combinam (nome + "Cidade •
 * Modalidade"); tocar numa sugestão preenche a competição e dispara
 * [onCompeticaoSelecionada] para a tela preencher cidade/modalidade
 * automaticamente (REGRA 4). Quando o texto digitado não corresponde a
 * nenhuma existente, mostra "+ Criar nova competição" — o campo continua
 * sempre editável por texto livre, igual aos demais campos do formulário.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoCompeticaoAutocomplete(
    valor: String,
    onValorChange: (String) -> Unit,
    sugestoes: List<Competicao>,
    onCompeticaoSelecionada: (Competicao) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Competição",
) {
    var expandido by remember { mutableStateOf(false) }
    val existeExata = sugestoes.any { it.nome.equals(valor.trim(), ignoreCase = true) }
    val mostrarMenu = expandido && valor.isNotBlank()

    ExposedDropdownMenuBox(
        expanded = mostrarMenu,
        onExpandedChange = { expandido = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = valor,
            onValueChange = {
                onValorChange(it)
                expandido = true
            },
            label = { Text(label) },
            singleLine = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mostrarMenu) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        DropdownMenu(
            expanded = mostrarMenu,
            onDismissRequest = { expandido = false },
            properties = PopupProperties(focusable = false),
        ) {
            sugestoes.forEach { competicao ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(competicao.nome, fontWeight = FontWeight.SemiBold)
                            competicao.subtitulo?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                    onClick = {
                        onCompeticaoSelecionada(competicao)
                        expandido = false
                    },
                )
            }
            if (!existeExata && valor.isNotBlank()) {
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                            Text("Criar nova competição \"${valor.trim()}\"")
                        }
                    },
                    onClick = { expandido = false },
                )
            }
        }
    }
}

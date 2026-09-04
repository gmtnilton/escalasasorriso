package com.meusjogos.arbitragem.ui.components

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * Campo com sugestões pré-definidas (competição, categoria, função — REGRA
 * 5), mas sempre editável por texto livre: nenhuma dessas opções é
 * obrigatória, e o usuário pode digitar qualquer valor personalizado em vez
 * de escolher da lista.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoComOpcoes(
    valor: String,
    onValorChange: (String) -> Unit,
    label: String,
    opcoes: List<String>,
    modifier: Modifier = Modifier,
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = valor,
            onValueChange = onValorChange,
            label = { Text(label) },
            singleLine = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier.menuAnchor(),
        )
        DropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false },
        ) {
            opcoes.forEach { opcao ->
                DropdownMenuItem(
                    text = { Text(opcao) },
                    onClick = {
                        onValorChange(opcao)
                        expandido = false
                    },
                )
            }
        }
    }
}

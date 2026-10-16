package com.meusjogos.arbitragem.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.meusjogos.arbitragem.data.preferences.AtivacaoPreferences

/**
 * Modal de ativação — só aparece quando o usuário tenta cadastrar um jogo
 * pela primeira vez sem o app ainda estar ativado neste aparelho (REGRA:
 * navegar, ver jogos e usar o resto do app nunca exige o código).
 */
@Composable
fun AtivacaoDialog(
    ativacaoPreferences: AtivacaoPreferences,
    onAtivado: () -> Unit,
    onFechar: () -> Unit,
) {
    var codigo by remember { mutableStateOf("") }
    var erro by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onFechar,
        icon = { Icon(Icons.Filled.Lock, contentDescription = null) },
        title = { Text("🔐 Ativação necessária") },
        text = {
            Column {
                Text("Para cadastrar uma escala, informe o código de ativação.")
                OutlinedTextField(
                    value = codigo,
                    onValueChange = {
                        codigo = it
                        erro = false
                    },
                    label = { Text("Digite o código de ativação") },
                    singleLine = true,
                    isError = erro,
                    supportingText = {
                        if (erro) Text("Código de ativação inválido.")
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (ativacaoPreferences.tentarAtivar(codigo)) {
                    onAtivado()
                } else {
                    erro = true
                }
            }) { Text("ATIVAR") }
        },
        dismissButton = {
            TextButton(onClick = onFechar) { Text("Cancelar") }
        },
    )
}

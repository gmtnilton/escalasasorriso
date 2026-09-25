package com.meusjogos.arbitragem.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.meusjogos.arbitragem.core.util.DateUtils

/** Campo de horário HH:mm (opcional — REGRA 5), com atalho de relógio. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoHoraTexto(
    texto: String,
    onTextoChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Horário",
) {
    var mostrarSeletor by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = texto,
        onValueChange = { novo -> onTextoChange(DateUtils.aplicarMascaraHora(novo)) },
        label = { Text(label) },
        placeholder = { Text("HH:MM") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        trailingIcon = {
            IconButton(onClick = { mostrarSeletor = true }) {
                Icon(Icons.Filled.AccessTime, contentDescription = "Escolher horário")
            }
        },
        modifier = modifier,
    )

    if (mostrarSeletor) {
        val horaAtual = DateUtils.parseHora(texto)
        val estadoSeletor = rememberTimePickerState(
            initialHour = horaAtual?.hour ?: 15,
            initialMinute = horaAtual?.minute ?: 0,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { mostrarSeletor = false },
            confirmButton = {
                TextButton(onClick = {
                    val h = estadoSeletor.hour.toString().padStart(2, '0')
                    val m = estadoSeletor.minute.toString().padStart(2, '0')
                    onTextoChange("$h:$m")
                    mostrarSeletor = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarSeletor = false }) { Text("Cancelar") }
            },
            text = { TimePicker(state = estadoSeletor, modifier = Modifier.padding(8.dp)) },
        )
    }
}

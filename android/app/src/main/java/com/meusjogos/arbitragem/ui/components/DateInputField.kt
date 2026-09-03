package com.meusjogos.arbitragem.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.meusjogos.arbitragem.core.util.DateUtils
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Campo de data no formato brasileiro dd/MM/aaaa (REGRA 5), com um botão de
 * calendário como atalho — mas sempre editável por digitação direta,
 * essencial para o cadastro ultrarrápido (REGRA 6).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoDataTexto(
    texto: String,
    onTextoChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Data",
    obrigatorio: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
) {
    var mostrarSeletor by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = texto,
        onValueChange = { novo -> onTextoChange(DateUtils.aplicarMascaraData(novo)) },
        label = { Text(if (obrigatorio) "$label *" else label) },
        placeholder = { Text("DD/MM/AAAA") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        isError = isError,
        supportingText = supportingText?.let { texto2 -> { Text(texto2) } },
        trailingIcon = {
            IconButton(onClick = { mostrarSeletor = true }) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = "Escolher no calendário")
            }
        },
        modifier = modifier,
    )

    if (mostrarSeletor) {
        val dataAtual = DateUtils.parseData(texto) ?: LocalDate.now()
        val estadoSeletor = rememberDatePickerState(
            initialSelectedDateMillis = dataAtual.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { mostrarSeletor = false },
            confirmButton = {
                TextButton(onClick = {
                    estadoSeletor.selectedDateMillis?.let { millis ->
                        val data = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        onTextoChange(DateUtils.formatarData(data))
                    }
                    mostrarSeletor = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarSeletor = false }) { Text("Cancelar") }
            },
        ) {
            DatePicker(state = estadoSeletor)
        }
    }
}

package com.meusjogos.arbitragem.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import com.meusjogos.arbitragem.core.util.CurrencyUtils

/**
 * Campo de valor com máscara "R$ 0,00" (REGRA 5): cada dígito digitado
 * entra pela direita, como em qualquer campo de valor monetário de banco —
 * não exige que o usuário digite vírgula ou ponto.
 */
@Composable
fun CampoValorMonetario(
    valorCentavos: Long,
    onValorChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Valor",
    obrigatorio: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
) {
    var textoAtual by remember(valorCentavos) {
        val formatado = CurrencyUtils.formatar(valorCentavos)
        mutableStateOf(TextFieldValue(text = formatado, selection = TextRange(formatado.length)))
    }

    OutlinedTextField(
        value = textoAtual,
        onValueChange = { novoValor ->
            val centavos = CurrencyUtils.paraCentavosMascarado(novoValor.text)
            val formatado = CurrencyUtils.formatar(centavos)
            textoAtual = TextFieldValue(text = formatado, selection = TextRange(formatado.length))
            onValorChange(centavos)
        },
        label = { Text(if (obrigatorio) "$label *" else label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        isError = isError,
        supportingText = supportingText?.let { texto -> { Text(texto) } },
        modifier = modifier,
    )
}

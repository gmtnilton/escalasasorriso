package com.meusjogos.arbitragem.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Título de seção — organiza o formulário/detalhes em blocos (ex.: "Equipes", "Pagamento"). */
@Composable
fun SectionHeader(titulo: String, modifier: Modifier = Modifier) {
    Text(
        text = titulo.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(bottom = 10.dp),
    )
}

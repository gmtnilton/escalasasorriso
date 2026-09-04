package com.meusjogos.arbitragem.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meusjogos.arbitragem.core.model.StatusPagamento
import com.meusjogos.arbitragem.ui.theme.LocalStatusColors

/** Selo visual de status — 🔴 A RECEBER / 🟢 RECEBIDO — usado na lista e nos detalhes (REGRA 23). */
@Composable
fun StatusChip(status: StatusPagamento, modifier: Modifier = Modifier) {
    val cores = LocalStatusColors.current
    val recebido = status == StatusPagamento.RECEBIDO
    val corTexto = if (recebido) cores.recebido else cores.aReceber
    val corFundo = if (recebido) cores.recebidoContainer else cores.aReceberContainer
    val emoji = if (recebido) "🟢" else "🔴"
    val texto = if (recebido) "RECEBIDO" else "A RECEBER"

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = corFundo,
    ) {
        Text(
            text = "$emoji $texto",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = corTexto,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

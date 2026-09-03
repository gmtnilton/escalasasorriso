package com.meusjogos.arbitragem.ui.jogos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meusjogos.arbitragem.core.model.Jogo
import com.meusjogos.arbitragem.core.util.CurrencyUtils
import com.meusjogos.arbitragem.core.util.DateUtils
import com.meusjogos.arbitragem.ui.components.StatusChip
import com.meusjogos.arbitragem.ui.theme.LocalStatusColors

/**
 * Card compacto de um jogo na lista "Meus Jogos" (REGRA 7): mostra data,
 * confronto (se as equipes estiverem preenchidas), competição/função (se
 * existirem) e valor/status — sem espaços vazios para campos não
 * preenchidos.
 */
@Composable
fun JogoItem(jogo: Jogo, onClick: () -> Unit) {
    val coresStatus = LocalStatusColors.current
    val corValor = if (jogo.recebido) coresStatus.recebido else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = DateUtils.formatarData(jogo.data),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                StatusChip(jogo.statusPagamento)
            }

            jogo.confronto?.let { confronto ->
                Text(
                    text = confronto,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }

            val subtitulo = listOfNotNull(
                jogo.competicao?.takeIf(String::isNotBlank),
                jogo.cidade?.takeIf(String::isNotBlank),
                jogo.funcao?.takeIf(String::isNotBlank),
            ).joinToString(" • ")
            if (subtitulo.isNotBlank()) {
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }

            Text(
                text = CurrencyUtils.formatar(jogo.valorCentavos),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = corValor,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

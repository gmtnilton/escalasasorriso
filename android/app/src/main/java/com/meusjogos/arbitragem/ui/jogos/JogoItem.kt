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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.meusjogos.arbitragem.core.model.Jogo
import com.meusjogos.arbitragem.core.util.CurrencyUtils
import com.meusjogos.arbitragem.core.util.DateUtils
import com.meusjogos.arbitragem.ui.components.StatusChip
import com.meusjogos.arbitragem.ui.theme.LocalStatusColors

/**
 * Card de um jogo na lista "Meus Jogos" (REGRA 7): título com o confronto —
 * ou "⚽ Jogo de arbitragem" quando as equipes não foram preenchidas —,
 * data/horário, competição/modalidade/local/função (só o que existir) e
 * valor + status em destaque. Nenhum campo vazio é exibido.
 */
@Composable
fun JogoItem(jogo: Jogo, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val coresStatus = LocalStatusColors.current
    val corValor = if (jogo.recebido) coresStatus.recebido else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = "⚽ ${jogo.confronto ?: "Jogo de arbitragem"}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                )
                StatusChip(jogo.statusPagamento)
            }

            val dataHora = if (jogo.horario != null) {
                "${DateUtils.formatarData(jogo.data)} • ${DateUtils.formatarHora(jogo.horario)}"
            } else {
                DateUtils.formatarData(jogo.data)
            }
            Text(
                text = dataHora,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )

            val subtitulo = listOfNotNull(
                jogo.competicao?.takeIf(String::isNotBlank),
                jogo.modalidade?.takeIf(String::isNotBlank),
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
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

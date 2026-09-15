package com.meusjogos.arbitragem.ui.jogos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meusjogos.arbitragem.core.model.Jogo

/**
 * Detalhe de uma cidade: todos os jogos vinculados a ela — mesmos registros já
 * existentes na lista principal, sem criar, duplicar ou alterar nada — aberto
 * ao tocar num chip "Por cidade" em Meus Jogos. Fechar o painel volta direto
 * para a lista de cidades.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CidadeDetalheSheet(
    cidade: String,
    jogos: List<Jogo>,
    onJogoClick: (Long) -> Unit,
    onFechar: () -> Unit,
) {
    val pendentes = jogos.count { !it.recebido }
    val recebidos = jogos.size - pendentes

    ModalBottomSheet(onDismissRequest = onFechar) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            Text("📍 $cidade", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                text = "${jogos.size} ${if (jogos.size == 1) "jogo" else "jogos"} · " +
                    "🔴 $pendentes pendente(s) · 🟢 $recebidos recebido(s)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
            )

            LazyColumn(
                modifier = Modifier.heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 8.dp),
            ) {
                items(jogos, key = { it.id }) { jogo ->
                    JogoItem(jogo = jogo, onClick = { onJogoClick(jogo.id); onFechar() })
                }
            }
        }
    }
}

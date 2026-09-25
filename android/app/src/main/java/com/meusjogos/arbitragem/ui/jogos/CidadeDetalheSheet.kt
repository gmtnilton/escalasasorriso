package com.meusjogos.arbitragem.ui.jogos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meusjogos.arbitragem.core.logic.contarPorCompeticao
import com.meusjogos.arbitragem.core.logic.jogosDaCompeticao
import com.meusjogos.arbitragem.core.model.Jogo

private sealed class NivelCidade {
    data object Inicio : NivelCidade()
    data class Jogos(val competicao: String?) : NivelCidade()
}

/**
 * Detalhe de uma cidade (REGRA 9 da V1.1): mostra as competições que têm
 * jogo ali — tocar numa competição mostra os jogos dela; "Todos os jogos
 * desta cidade" mantém o comportamento anterior (lista plana), preservando
 * o que já funcionava. Mesmos registros já existentes, sem criar, duplicar
 * ou alterar nada. Fechar o painel volta direto para a lista de cidades.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CidadeDetalheSheet(
    cidade: String,
    jogos: List<Jogo>,
    onJogoClick: (Long) -> Unit,
    onFechar: () -> Unit,
) {
    var nivel by remember(cidade) { mutableStateOf<NivelCidade>(NivelCidade.Inicio) }
    val pendentes = jogos.count { !it.recebido }
    val recebidos = jogos.size - pendentes
    val competicoes = jogos.contarPorCompeticao()

    ModalBottomSheet(onDismissRequest = onFechar) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            when (val nivelAtual = nivel) {
                NivelCidade.Inicio -> {
                    Text("📍 $cidade", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${jogos.size} ${if (jogos.size == 1) "jogo" else "jogos"} · " +
                            "🔴 $pendentes pendente(s) · 🟢 $recebidos recebido(s)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                    )

                    CardGrupo(emoji = "⚽", nome = "Todos os jogos desta cidade", quantidade = jogos.size) {
                        nivel = NivelCidade.Jogos(competicao = null)
                    }

                    if (competicoes.isNotEmpty()) {
                        Text(
                            text = "🏆 Competições",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 20.dp, bottom = 4.dp),
                        )
                        Column {
                            competicoes.forEach { (nome, quantidade) ->
                                CardGrupo(emoji = "🏆", nome = nome, quantidade = quantidade) {
                                    nivel = NivelCidade.Jogos(competicao = nome)
                                }
                            }
                        }
                    }
                }

                is NivelCidade.Jogos -> {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                        IconButton(onClick = { nivel = NivelCidade.Inicio }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                        Column {
                            Text("📍 $cidade", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            nivelAtual.competicao?.let {
                                Text(
                                    "🏆 $it",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    val jogosNivel = nivelAtual.competicao?.let { jogos.jogosDaCompeticao(it) } ?: jogos
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 480.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 8.dp),
                    ) {
                        items(jogosNivel, key = { it.id }) { jogo ->
                            JogoItem(jogo = jogo, onClick = { onJogoClick(jogo.id); onFechar() })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CardGrupo(emoji: String, nome: String, quantidade: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$emoji $nome",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "$quantidade",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Filled.ChevronRight, contentDescription = null)
        }
    }
}

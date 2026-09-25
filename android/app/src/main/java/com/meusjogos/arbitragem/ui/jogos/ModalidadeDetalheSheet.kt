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
import com.meusjogos.arbitragem.core.logic.contarPorCidade
import com.meusjogos.arbitragem.core.logic.contarPorCompeticao
import com.meusjogos.arbitragem.core.logic.jogosDaCidade
import com.meusjogos.arbitragem.core.logic.jogosDaCompeticao
import com.meusjogos.arbitragem.core.model.Jogo

private sealed class NivelModalidade {
    data object Resumo : NivelModalidade()
    data class CompeticoesDaCidade(val cidade: String) : NivelModalidade()
    data class Jogos(val cidade: String?, val competicao: String?) : NivelModalidade()
}

/**
 * Detalhe de uma modalidade (REGRAS 7/8 da V1.1): total de jogos, jogos por
 * cidade e competições relacionadas — tocar numa cidade mostra as
 * competições daquela cidade (dentro da modalidade), tocar numa competição
 * mostra os jogos dela. Navegação hierárquica Modalidade -> Cidade ->
 * Competição -> Jogos, sempre sobre os registros já existentes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalidadeDetalheSheet(
    modalidade: String,
    jogos: List<Jogo>,
    onJogoClick: (Long) -> Unit,
    onFechar: () -> Unit,
) {
    var nivel by remember(modalidade) { mutableStateOf<NivelModalidade>(NivelModalidade.Resumo) }
    val cidades = jogos.contarPorCidade()
    val competicoes = jogos.contarPorCompeticao()

    ModalBottomSheet(onDismissRequest = onFechar) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            when (val nivelAtual = nivel) {
                NivelModalidade.Resumo -> {
                    Text("⚽ $modalidade", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Total: ${jogos.size} ${if (jogos.size == 1) "jogo" else "jogos"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                    )

                    if (cidades.isNotEmpty()) {
                        Text(
                            text = "📍 Cidade",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
                        )
                        Column {
                            cidades.forEach { (nome, quantidade) ->
                                CardGrupoModalidade(emoji = "📍", nome = nome, quantidade = quantidade) {
                                    nivel = NivelModalidade.CompeticoesDaCidade(cidade = nome)
                                }
                            }
                        }
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
                                CardGrupoModalidade(emoji = "🏆", nome = nome, quantidade = quantidade) {
                                    nivel = NivelModalidade.Jogos(cidade = null, competicao = nome)
                                }
                            }
                        }
                    }
                }

                is NivelModalidade.CompeticoesDaCidade -> {
                    val jogosDaCidade = jogos.jogosDaCidade(nivelAtual.cidade)
                    val competicoesDaCidade = jogosDaCidade.contarPorCompeticao()

                    CabecalhoComVoltar(
                        onVoltar = { nivel = NivelModalidade.Resumo },
                        titulo = "⚽ $modalidade",
                        subtitulo = "📍 ${nivelAtual.cidade}",
                    )

                    CardGrupoModalidade(emoji = "⚽", nome = "Todos os jogos aqui", quantidade = jogosDaCidade.size) {
                        nivel = NivelModalidade.Jogos(cidade = nivelAtual.cidade, competicao = null)
                    }

                    if (competicoesDaCidade.isNotEmpty()) {
                        Text(
                            text = "🏆 Competições",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 20.dp, bottom = 4.dp),
                        )
                        Column {
                            competicoesDaCidade.forEach { (nome, quantidade) ->
                                CardGrupoModalidade(emoji = "🏆", nome = nome, quantidade = quantidade) {
                                    nivel = NivelModalidade.Jogos(cidade = nivelAtual.cidade, competicao = nome)
                                }
                            }
                        }
                    }
                }

                is NivelModalidade.Jogos -> {
                    val cidade = nivelAtual.cidade
                    val competicao = nivelAtual.competicao
                    val jogosNivel = jogos
                        .let { lista -> cidade?.let { lista.jogosDaCidade(it) } ?: lista }
                        .let { lista -> competicao?.let { lista.jogosDaCompeticao(it) } ?: lista }

                    CabecalhoComVoltar(
                        onVoltar = {
                            nivel = if (cidade != null) NivelModalidade.CompeticoesDaCidade(cidade) else NivelModalidade.Resumo
                        },
                        titulo = "⚽ $modalidade",
                        subtitulo = listOfNotNull(cidade?.let { "📍 $it" }, competicao?.let { "🏆 $it" })
                            .joinToString("  ")
                            .ifBlank { null },
                    )

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
private fun CabecalhoComVoltar(onVoltar: () -> Unit, titulo: String, subtitulo: String?) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
        IconButton(onClick = onVoltar) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
        }
        Column {
            Text(titulo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            subtitulo?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun CardGrupoModalidade(emoji: String, nome: String, quantidade: Int, onClick: () -> Unit) {
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

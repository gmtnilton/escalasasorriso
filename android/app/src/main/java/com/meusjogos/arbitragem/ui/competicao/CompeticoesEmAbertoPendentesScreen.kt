package com.meusjogos.arbitragem.ui.competicao

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.meusjogos.arbitragem.core.util.DateUtils
import com.meusjogos.arbitragem.ui.components.EmptyState
import com.meusjogos.arbitragem.ui.theme.LocalStatusColors

/**
 * Tela "COMPETIÇÕES EM ABERTO A RECEBER" (VERSÃO 1.4, seção 1) — mesmo padrão visual e de
 * navegação da tela "Competições encerradas a receber" (V1.3), para as competições ainda em
 * andamento que já têm saldo pendente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompeticoesEmAbertoPendentesScreen(
    viewModel: CompeticoesEmAbertoPendentesViewModel,
    onVoltar: () -> Unit,
    onVerCompeticao: (Long) -> Unit,
) {
    val estado by viewModel.uiState.collectAsState()
    val coresStatus = LocalStatusColors.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Competições em aberto a receber") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) { Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar") }
                },
            )
        },
    ) { padding ->
        when {
            estado.carregando -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            estado.itens.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(
                    icone = Icons.Filled.CheckCircle,
                    titulo = "Nenhuma pendência",
                    descricao = "Nenhuma competição em andamento tem valores pendentes agora.",
                )
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(estado.itens, key = { it.item.competicao.id }) { entrada ->
                    CompeticaoComResumoCard(
                        item = entrada.item,
                        statusTexto = "🟢 EM ANDAMENTO",
                        statusCor = coresStatus.recebido,
                        rotuloData = entrada.primeiroJogo?.let { "Primeiro jogo em ${DateUtils.formatarData(it)}" },
                        onVerCompeticao = { onVerCompeticao(entrada.item.competicao.id) },
                    )
                }
            }
        }
    }
}

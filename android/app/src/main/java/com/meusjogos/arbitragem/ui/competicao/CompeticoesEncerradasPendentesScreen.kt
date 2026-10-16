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
import java.time.ZoneId

/**
 * Tela "COMPETIÇÕES ENCERRADAS A RECEBER" (VERSÃO 1.3, seção 4) — aberta ao tocar no card do
 * Início; cada item mostra o resumo completo da competição encerrada e um botão para abrir os
 * detalhes (jogos pendentes, recebimentos, recibo em PDF).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompeticoesEncerradasPendentesScreen(
    viewModel: CompeticoesEncerradasPendentesViewModel,
    onVoltar: () -> Unit,
    onVerCompeticao: (Long) -> Unit,
) {
    val estado by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Competições encerradas a receber") },
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
                    descricao = "Todas as competições encerradas já foram totalmente recebidas.",
                )
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(estado.itens, key = { it.competicao.id }) { item ->
                    CompeticaoComResumoCard(
                        item = item,
                        statusTexto = "🔴 ENCERRADA",
                        statusCor = MaterialTheme.colorScheme.error,
                        rotuloData = item.competicao.dataEncerramento?.let { data ->
                            "Encerrada em ${DateUtils.formatarData(data.atZone(ZoneId.systemDefault()).toLocalDate())}"
                        },
                        onVerCompeticao = { onVerCompeticao(item.competicao.id) },
                    )
                }
            }
        }
    }
}


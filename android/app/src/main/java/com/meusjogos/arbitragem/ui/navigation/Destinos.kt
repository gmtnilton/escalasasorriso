package com.meusjogos.arbitragem.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.ui.graphics.vector.ImageVector

/** As 5 telas principais, acessíveis pela navegação inferior (REGRA 24). */
enum class DestinoPrincipal(
    val rota: String,
    val titulo: String,
    val iconeSelecionado: ImageVector,
    val iconeNaoSelecionado: ImageVector,
) {
    INICIO("dashboard", "Início", Icons.Filled.Home, Icons.Outlined.Home),
    JOGOS("jogos", "Jogos", Icons.Filled.SportsSoccer, Icons.Outlined.SportsSoccer),
    RECEBIMENTO("recebimento", "Receber", Icons.Filled.Payments, Icons.Outlined.Payments),
    RESUMO("resumo", "Resumo", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    CONFIGURACOES("configuracoes", "Config.", Icons.Filled.Settings, Icons.Outlined.Settings),
}

/** Demais rotas do app, empilhadas por cima da navegação inferior. */
object Rotas {
    const val ARG_JOGO_ID = "jogoId"
    const val ARG_DUPLICADO = "duplicado"

    const val JOGO_FORM_BASE = "jogo_form"
    const val JOGO_FORM = "$JOGO_FORM_BASE?$ARG_JOGO_ID={$ARG_JOGO_ID}&$ARG_DUPLICADO={$ARG_DUPLICADO}"

    const val JOGO_DETAIL_BASE = "jogo_detail"
    const val JOGO_DETAIL = "$JOGO_DETAIL_BASE/{$ARG_JOGO_ID}"

    /** id = 0 -> cadastro ultrarrápido de um jogo novo. */
    fun jogoFormNovo(): String = "$JOGO_FORM_BASE?$ARG_JOGO_ID=0&$ARG_DUPLICADO=false"

    fun jogoFormEditar(jogoId: Long): String = "$JOGO_FORM_BASE?$ARG_JOGO_ID=$jogoId&$ARG_DUPLICADO=false"

    fun jogoFormDuplicado(jogoId: Long): String = "$JOGO_FORM_BASE?$ARG_JOGO_ID=$jogoId&$ARG_DUPLICADO=true"

    fun jogoDetail(jogoId: Long): String = "$JOGO_DETAIL_BASE/$jogoId"
}

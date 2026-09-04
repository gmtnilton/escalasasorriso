package com.meusjogos.arbitragem.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.meusjogos.arbitragem.data.preferences.TemaPreferences
import com.meusjogos.arbitragem.data.repository.JogoRepository
import com.meusjogos.arbitragem.ui.configuracoes.ConfiguracoesScreen
import com.meusjogos.arbitragem.ui.configuracoes.ConfiguracoesViewModel
import com.meusjogos.arbitragem.ui.dashboard.DashboardScreen
import com.meusjogos.arbitragem.ui.dashboard.DashboardViewModel
import com.meusjogos.arbitragem.ui.jogodetail.JogoDetailScreen
import com.meusjogos.arbitragem.ui.jogodetail.JogoDetailViewModel
import com.meusjogos.arbitragem.ui.jogoform.JogoFormScreen
import com.meusjogos.arbitragem.ui.jogoform.JogoFormViewModel
import com.meusjogos.arbitragem.ui.jogos.JogosListScreen
import com.meusjogos.arbitragem.ui.jogos.JogosListViewModel
import com.meusjogos.arbitragem.ui.resumo.ResumoScreen
import com.meusjogos.arbitragem.ui.resumo.ResumoViewModel
import com.meusjogos.arbitragem.util.ViewModelFactory

/** Grafo de navegação raiz: barra inferior (4 telas principais) + telas empilhadas. */
@Composable
fun MeusJogosNavGraph(repository: JogoRepository, temaPreferences: TemaPreferences) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val rotaAtual = backStackEntry?.destination?.route

    val destinosPrincipais = DestinoPrincipal.entries
    val telaPrincipalAtual = destinosPrincipais.firstOrNull { it.rota == rotaAtual }
    val mostrarNovoJogoFab = telaPrincipalAtual == DestinoPrincipal.INICIO || telaPrincipalAtual == DestinoPrincipal.JOGOS

    Scaffold(
        bottomBar = {
            if (telaPrincipalAtual != null) {
                NavigationBar {
                    destinosPrincipais.forEach { destino ->
                        val selecionado = backStackEntry?.destination?.hierarchy
                            ?.any { it.route == destino.rota } == true
                        NavigationBarItem(
                            selected = selecionado,
                            onClick = {
                                navController.navigate(destino.rota) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    if (selecionado) destino.iconeSelecionado else destino.iconeNaoSelecionado,
                                    contentDescription = destino.titulo,
                                )
                            },
                            label = { Text(destino.titulo) },
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (mostrarNovoJogoFab) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(Rotas.jogoFormNovo()) },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Novo jogo") },
                )
            }
        },
    ) { paddingInterno ->
        NavHost(
            navController = navController,
            startDestination = DestinoPrincipal.INICIO.rota,
            enterTransition = { fadeIn(animationSpec = tween(220)) },
            exitTransition = { fadeOut(animationSpec = tween(160)) },
            popEnterTransition = { fadeIn(animationSpec = tween(220)) },
            popExitTransition = { fadeOut(animationSpec = tween(160)) },
        ) {
            composable(DestinoPrincipal.INICIO.rota) {
                val viewModel: DashboardViewModel = viewModel(
                    factory = ViewModelFactory { DashboardViewModel(repository) },
                )
                DashboardScreen(viewModel = viewModel, contentPadding = paddingInterno)
            }

            composable(DestinoPrincipal.JOGOS.rota) {
                val viewModel: JogosListViewModel = viewModel(
                    factory = ViewModelFactory { JogosListViewModel(repository) },
                )
                JogosListScreen(
                    viewModel = viewModel,
                    contentPadding = paddingInterno,
                    onJogoClick = { jogoId -> navController.navigate(Rotas.jogoDetail(jogoId)) },
                )
            }

            composable(DestinoPrincipal.RESUMO.rota) {
                val viewModel: ResumoViewModel = viewModel(
                    factory = ViewModelFactory { ResumoViewModel(repository) },
                )
                ResumoScreen(viewModel = viewModel, contentPadding = paddingInterno)
            }

            composable(DestinoPrincipal.CONFIGURACOES.rota) {
                val viewModel: ConfiguracoesViewModel = viewModel(
                    factory = ViewModelFactory { ConfiguracoesViewModel(repository, temaPreferences) },
                )
                ConfiguracoesScreen(viewModel = viewModel, contentPadding = paddingInterno)
            }

            composable(
                route = Rotas.JOGO_FORM,
                arguments = listOf(
                    navArgument(Rotas.ARG_JOGO_ID) { type = NavType.LongType; defaultValue = 0L },
                    navArgument(Rotas.ARG_DUPLICADO) { type = NavType.BoolType; defaultValue = false },
                ),
            ) { entrada ->
                val jogoId = entrada.arguments?.getLong(Rotas.ARG_JOGO_ID) ?: 0L
                val duplicado = entrada.arguments?.getBoolean(Rotas.ARG_DUPLICADO) ?: false
                val viewModel: JogoFormViewModel = viewModel(
                    factory = ViewModelFactory { JogoFormViewModel(repository, jogoId, duplicado) },
                )
                JogoFormScreen(
                    viewModel = viewModel,
                    onSalvarConcluido = { navController.popBackStack() },
                    onCancelar = { navController.popBackStack() },
                )
            }

            composable(
                route = Rotas.JOGO_DETAIL,
                arguments = listOf(navArgument(Rotas.ARG_JOGO_ID) { type = NavType.LongType }),
            ) { entrada ->
                val jogoId = entrada.arguments?.getLong(Rotas.ARG_JOGO_ID) ?: 0L
                val viewModel: JogoDetailViewModel = viewModel(
                    factory = ViewModelFactory { JogoDetailViewModel(repository, jogoId) },
                )
                JogoDetailScreen(
                    viewModel = viewModel,
                    onVoltar = { navController.popBackStack() },
                    onEditar = { navController.navigate(Rotas.jogoFormEditar(jogoId)) },
                    onDuplicarConcluido = { novoId ->
                        navController.navigate(Rotas.jogoFormDuplicado(novoId)) {
                            popUpTo(DestinoPrincipal.JOGOS.rota)
                        }
                    },
                    onExcluirConcluido = { navController.popBackStack() },
                )
            }
        }
    }
}

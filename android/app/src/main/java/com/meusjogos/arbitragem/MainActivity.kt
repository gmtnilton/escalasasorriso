package com.meusjogos.arbitragem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.meusjogos.arbitragem.data.preferences.ModoTema
import com.meusjogos.arbitragem.ui.navigation.MeusJogosNavGraph
import com.meusjogos.arbitragem.ui.theme.MeusJogosArbitragemTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as MeusJogosApplication
        val repository = app.repository
        val temaPreferences = app.temaPreferences
        val ativacaoPreferences = app.ativacaoPreferences

        setContent {
            val modoTema by temaPreferences.modo.collectAsState()
            val temaEscuro = when (modoTema) {
                ModoTema.CLARO -> false
                ModoTema.ESCURO -> true
                ModoTema.SISTEMA -> isSystemInDarkTheme()
            }
            MeusJogosArbitragemTheme(darkTheme = temaEscuro) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MeusJogosNavGraph(
                        repository = repository,
                        temaPreferences = temaPreferences,
                        ativacaoPreferences = ativacaoPreferences,
                    )
                }
            }
        }
    }
}

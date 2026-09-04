package com.meusjogos.arbitragem.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Navy30,
    onPrimary = Color.White,
    primaryContainer = Navy90,
    onPrimaryContainer = Navy10,
    secondary = Slate40,
    onSecondary = Color.White,
    secondaryContainer = Slate90,
    onSecondaryContainer = Navy10,
    tertiary = Dourado40,
    onTertiary = Color.White,
    tertiaryContainer = Dourado90,
    onTertiaryContainer = Dourado30,
    error = Vermelho40,
    errorContainer = Vermelho90,
    onError = Color.White,
    onErrorContainer = Vermelho40,
    background = Neutro99,
    onBackground = Neutro10,
    surface = Neutro99,
    onSurface = Neutro10,
    surfaceVariant = Neutro95,
    onSurfaceVariant = Neutro20,
    outline = Slate40,
)

private val DarkColorScheme = darkColorScheme(
    primary = Navy80,
    onPrimary = Navy20,
    primaryContainer = Navy40,
    onPrimaryContainer = Navy95,
    secondary = Slate80,
    onSecondary = Navy20,
    secondaryContainer = Slate40,
    onSecondaryContainer = Slate90,
    tertiary = Dourado80,
    onTertiary = Navy20,
    tertiaryContainer = Dourado40,
    onTertiaryContainer = Dourado90,
    error = Vermelho80,
    errorContainer = Vermelho40,
    onError = Navy20,
    onErrorContainer = Vermelho90,
    background = Neutro10,
    onBackground = Neutro90,
    surface = Neutro10,
    onSurface = Neutro90,
    surfaceVariant = Neutro20,
    onSurfaceVariant = Neutro90,
    outline = Slate80,
)

/** Cores semânticas de status (recebido/a receber), acessíveis via [LocalStatusColors]. */
data class StatusColors(
    val recebido: Color,
    val recebidoContainer: Color,
    val aReceber: Color,
    val aReceberContainer: Color,
)

val LocalStatusColors = staticCompositionLocalOf {
    StatusColors(
        recebido = StatusRecebidoVerde,
        recebidoContainer = StatusRecebidoVerdeContainer,
        aReceber = StatusAReceberVermelho,
        aReceberContainer = StatusAReceberVermelhoContainer,
    )
}

@Composable
fun MeusJogosArbitragemTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val statusColors = if (darkTheme) {
        StatusColors(
            recebido = StatusRecebidoVerde,
            recebidoContainer = StatusRecebidoVerdeContainerDark,
            aReceber = StatusAReceberVermelho,
            aReceberContainer = StatusAReceberVermelhoContainerDark,
        )
    } else {
        StatusColors(
            recebido = StatusRecebidoVerde,
            recebidoContainer = StatusRecebidoVerdeContainer,
            aReceber = StatusAReceberVermelho,
            aReceberContainer = StatusAReceberVermelhoContainer,
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            // primary é escuro no tema claro (navy) e claro no tema escuro
            // (navy claro) — os ícones da status bar precisam do contraste
            // oposto ao da própria cor de fundo da barra, por isso o
            // booleano é o mesmo de darkTheme, não o seu inverso.
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    CompositionLocalProvider(LocalStatusColors provides statusColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MeusJogosTypography,
            shapes = MeusJogosShapes,
            content = content,
        )
    }
}

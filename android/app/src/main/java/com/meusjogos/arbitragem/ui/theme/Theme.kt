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
    primary = VerdeCampo40,
    onPrimary = Color.White,
    primaryContainer = VerdeCampo90,
    onPrimaryContainer = VerdeCampo10,
    secondary = Dourado50,
    onSecondary = Color.White,
    secondaryContainer = Dourado90,
    onSecondaryContainer = Dourado40,
    tertiary = Azul40,
    onTertiary = Color.White,
    tertiaryContainer = Azul90,
    onTertiaryContainer = Azul40,
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
    outline = Neutro20,
)

private val DarkColorScheme = darkColorScheme(
    primary = VerdeCampo80,
    onPrimary = VerdeCampo20,
    primaryContainer = VerdeCampo30,
    onPrimaryContainer = VerdeCampo90,
    secondary = Dourado80,
    onSecondary = VerdeCampo20,
    secondaryContainer = Dourado40,
    onSecondaryContainer = Dourado90,
    tertiary = Azul80,
    onTertiary = VerdeCampo20,
    tertiaryContainer = Azul40,
    onTertiaryContainer = Azul90,
    error = Vermelho80,
    errorContainer = Vermelho40,
    onError = VerdeCampo20,
    onErrorContainer = Vermelho90,
    background = Neutro10,
    onBackground = Neutro90,
    surface = Neutro10,
    onSurface = Neutro90,
    surfaceVariant = Neutro20,
    onSurfaceVariant = Neutro90,
    outline = Neutro90,
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
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
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

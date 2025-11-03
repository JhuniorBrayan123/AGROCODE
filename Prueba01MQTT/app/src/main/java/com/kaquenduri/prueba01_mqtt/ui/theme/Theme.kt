package com.kaquenduri.prueba01_mqtt.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    secondary = SecondaryPeach,
    tertiary = SecondaryPeachDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    secondary = SecondaryPeach,
    tertiary = SecondaryPeach

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun Prueba01MQTTTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    colorPrimario: androidx.compose.ui.graphics.Color = PrimaryGreen,
    colorSecundario: androidx.compose.ui.graphics.Color = SecondaryPeach,
    tamañoTexto: Float = 16f,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme.copy(
            primary = colorPrimario,
            secondary = colorSecundario
        )
        else -> LightColorScheme.copy(
            primary = colorPrimario,
            secondary = colorSecundario
        )
    }

    val customTypography = Typography.copy(
        bodyLarge = Typography.bodyLarge.copy(fontSize = tamañoTexto.sp),
        titleLarge = Typography.titleLarge.copy(fontSize = (tamañoTexto + 6).sp),
        titleMedium = Typography.titleMedium.copy(fontSize = (tamañoTexto + 2).sp),
        labelLarge = Typography.labelLarge.copy(fontSize = (tamañoTexto + 1).sp)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = customTypography,
        content = content
    )
}
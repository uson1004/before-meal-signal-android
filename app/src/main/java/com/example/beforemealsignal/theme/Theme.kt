package com.example.beforemealsignal.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme =
  darkColorScheme(
    primary = SignalPrimary,
    secondary = SignalInfo,
    tertiary = SignalWarning,
    error = SignalDanger,
    background = SignalInk,
    surface = SignalInk,
    onPrimary = SignalSurface,
    onSecondary = SignalSurface,
    onTertiary = SignalInk,
    onError = SignalSurface,
    onBackground = SignalBackground,
    onSurface = SignalBackground,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = SignalPrimary,
    secondary = SignalInfo,
    tertiary = SignalWarning,
    error = SignalDanger,
    background = SignalBackground,
    surface = SignalSurface,
    primaryContainer = SignalPrimarySoft,
    errorContainer = SignalDangerSoft,
    tertiaryContainer = SignalWarningSoft,
    onPrimary = SignalSurface,
    onSecondary = SignalSurface,
    onTertiary = SignalInk,
    onError = SignalSurface,
    onBackground = SignalInk,
    onSurface = SignalInk,
  )

@Composable
fun BeforeMealSignalTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

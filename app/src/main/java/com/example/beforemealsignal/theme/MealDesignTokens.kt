package com.example.beforemealsignal.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object MealDesignTokens {
  object Colors {
    val Canvas = Color(0xFFF4EAD9)
    val CanvasDeep = Color(0xFFEADBC3)
    val Surface = Color(0xFFFFFBF3)
    val SurfaceRaised = Color(0xFFFFFFFF)
    val SurfacePressed = Color(0xFFF1E5D4)
    val Ink = Color(0xFF241D1A)
    val Muted = Color(0xFF756A5D)
    val Line = Color(0xFFE2D1BC)

    val Coral = Color(0xFFFF5B4E)
    val CoralDeep = Color(0xFFB93426)
    val CoralSoft = Color(0xFFFFE1DD)

    val Mint = Color(0xFF39B87D)
    val MintDeep = Color(0xFF126F4F)
    val MintSoft = Color(0xFFDFF7E9)

    val Navy = Color(0xFF153A55)
    val NavyDeep = Color(0xFF0B2538)
    val NavySoft = Color(0xFFD7ECF5)

    val Amber = Color(0xFFFFC43B)
    val AmberDeep = Color(0xFF8F6000)
    val AmberSoft = Color(0xFFFFF0C0)

    val Danger = Color(0xFFE6424F)
    val DangerDeep = Color(0xFF76131D)
    val DangerSoft = Color(0xFFFFE0E4)
  }

  object Radius {
    val Sheet = 32.dp
    val Hero = 30.dp
    val Card = 24.dp
    val Control = 18.dp
    val Small = 14.dp
    val Pill = 100.dp
  }

  object Space {
    val Xs = 4.dp
    val Sm = 8.dp
    val Md = 12.dp
    val Lg = 16.dp
    val Xl = 20.dp
    val Xxl = 24.dp
  }

  object Depth {
    val Surface = 3.dp
    val Card = 8.dp
    val Hero = 16.dp
    val Floating = 12.dp
    val Button = 6.dp
  }
}

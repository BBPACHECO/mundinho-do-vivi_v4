package com.mundodedescobertas.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Ink = Color(0xFF35354F)
val Cream = Color(0xFFFFF9F2)
val Lavender = Color(0xFF7862AD)
val Rounded = FontFamily(androidx.compose.ui.text.font.Typeface(android.graphics.Typeface.create("sans-serif-rounded", android.graphics.Typeface.NORMAL)))
private val Colors = lightColorScheme(
    primary = Color(0xFF66508F), onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE2FA), onPrimaryContainer = Ink,
    secondary = Color(0xFF47685D), background = Cream, onBackground = Ink,
    surface = Color(0xFFFFFCF8), onSurface = Ink,
    surfaceVariant = Color(0xFFF0EAF5), onSurfaceVariant = Color(0xFF595367),
    outline = Color(0xFF807689), error = Color(0xFF9A3341),
)
private val Type = Typography(
    headlineLarge = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 39.sp),
    headlineMedium = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.Bold, fontSize = 27.sp, lineHeight = 34.sp),
    titleLarge = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 29.sp),
    titleMedium = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 25.sp),
    bodyLarge = TextStyle(fontFamily = Rounded, fontSize = 17.sp, lineHeight = 25.sp),
    bodyMedium = TextStyle(fontFamily = Rounded, fontSize = 15.sp, lineHeight = 22.sp),
    labelLarge = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 22.sp),
)

@Composable
fun ViviTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, typography = Type, content = content)
}

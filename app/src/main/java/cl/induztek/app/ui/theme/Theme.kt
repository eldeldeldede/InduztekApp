package cl.induztek.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaClaro = lightColorScheme(
    primary = AzulInduztek,
    onPrimary = Color.White,
    primaryContainer = AzulInduztekClaro,
    onPrimaryContainer = AzulInduztek,
    secondary = AmbarInduztek,
    onSecondary = Color.Black,
    secondaryContainer = AmbarInduztekClaro,
    onSecondaryContainer = Color(0xFF3B2A00),
    background = Color(0xFFF4F6F8),
    surface = Color.White,
    surfaceVariant = Color(0xFFE3E8EC),
    onSurfaceVariant = Neutro,
    outline = Color(0xFF8396A3)
)

private val EsquemaOscuro = darkColorScheme(
    primary = AzulOscuro80,
    onPrimary = Color(0xFF0E2A3B),
    primaryContainer = AzulOscuroContenedor,
    onPrimaryContainer = AzulInduztekClaro,
    secondary = Ambar80,
    onSecondary = Color(0xFF3B2A00)
)

@Composable
fun InduztekAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography = Tipografia,
        content = content
    )
}

package cl.induztek.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
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
    background = Color(0xFFF7F9FC),
    surface = Color(0xFFF7F9FC)
)

private val EsquemaOscuro = darkColorScheme(
    primary = AzulOscuro80,
    onPrimary = Color(0xFF00315E),
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

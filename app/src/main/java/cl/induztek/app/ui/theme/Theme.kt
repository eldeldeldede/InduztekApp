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
    onPrimaryContainer = Color(0xFF1F3347),
    secondary = AmbarInduztek,
    onSecondary = Color(0xFF3B2A00),
    secondaryContainer = AmbarInduztekClaro,
    onSecondaryContainer = Color(0xFF3B2A00),
    background = FondoGris,
    onBackground = Color(0xFF1F2933),
    surface = SuperficieGris,
    onSurface = Color(0xFF1F2933),
    surfaceVariant = SuperficieGrisOscura,
    onSurfaceVariant = Neutro,
    surfaceContainerLowest = Color(0xFFF8F9FB),
    surfaceContainerLow = Color(0xFFF1F3F6),
    surfaceContainer = Color(0xFFECEFF3),
    surfaceContainerHigh = Color(0xFFE7EBF0),
    surfaceContainerHighest = Color(0xFFE1E6EC),
    outline = ContornoGris,
    outlineVariant = Color(0xFFC3CCD5)
)

private val EsquemaOscuro = darkColorScheme(
    primary = AzulOscuro80,
    onPrimary = Color(0xFF1F3347),
    primaryContainer = AzulOscuroContenedor,
    onPrimaryContainer = AzulInduztekClaro,
    secondary = Ambar80,
    onSecondary = Color(0xFF3B2A00),
    background = Color(0xFF1D2329),
    surface = Color(0xFF262D34),
    surfaceVariant = Color(0xFF34404A)
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

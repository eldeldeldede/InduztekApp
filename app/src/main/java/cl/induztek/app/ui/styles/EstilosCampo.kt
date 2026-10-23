package cl.induztek.app.ui.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

// Estilo común de todos los campos de texto (como EstilosCampo de MiRegistro):
// así todos los formularios se ven iguales sin repetir el diseño en cada pantalla.
object EstilosCampo {
    val forma = RoundedCornerShape(12.dp)

    @Composable
    fun colores(): TextFieldColors = OutlinedTextFieldDefaults.colors()
}

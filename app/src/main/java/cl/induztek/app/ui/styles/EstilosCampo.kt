package cl.induztek.app.ui.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

object EstilosCampo {
    val forma = RoundedCornerShape(12.dp)

    @Composable
    fun colores(): TextFieldColors = OutlinedTextFieldDefaults.colors()
}

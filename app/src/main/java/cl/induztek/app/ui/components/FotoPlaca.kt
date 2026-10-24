package cl.induztek.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cl.induztek.app.R
import cl.induztek.app.util.FotosPlaca

// Muestra la foto de la placa guardada en `ruta`. Si el archivo no existe, no dibuja nada.
// remember(ruta): la foto se decodifica una sola vez mientras la ruta no cambie.
@Composable
fun FotoPlaca(ruta: String, modifier: Modifier = Modifier) {
    val imagen = remember(ruta) { FotosPlaca.cargarReducida(ruta)?.asImageBitmap() }
    if (imagen != null) {
        Image(
            bitmap = imagen,
            contentDescription = stringResource(R.string.cd_foto_placa),
            contentScale = ContentScale.Crop,
            modifier = modifier
                .fillMaxWidth()
                .heightIn(max = 220.dp)
                .clip(RoundedCornerShape(12.dp))
        )
    }
}

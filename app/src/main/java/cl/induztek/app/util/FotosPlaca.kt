package cl.induztek.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object FotosPlaca {

    private fun carpeta(contexto: Context): File =
        File(contexto.filesDir, "placas").also { it.mkdirs() }

    fun crearArchivo(contexto: Context): File =
        File(carpeta(contexto), "placa_${System.currentTimeMillis()}.jpg")

    fun uriDe(contexto: Context, archivo: File): Uri =
        FileProvider.getUriForFile(contexto, "${contexto.packageName}.fileprovider", archivo)

    fun borrar(ruta: String?) {
        if (ruta != null) File(ruta).delete()
    }

    fun cargarReducida(ruta: String, anchoMaximo: Int = 800): Bitmap? {
        val archivo = File(ruta)
        if (!archivo.exists()) return null
        val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(ruta, medidas)
        var reduccion = 1
        while (medidas.outWidth / reduccion > anchoMaximo) reduccion *= 2
        val opciones = BitmapFactory.Options().apply { inSampleSize = reduccion }
        return BitmapFactory.decodeFile(ruta, opciones)
    }
}

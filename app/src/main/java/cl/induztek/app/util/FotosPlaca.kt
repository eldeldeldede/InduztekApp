package cl.induztek.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

// =====================================================================
//  FotosPlaca.kt — ayudas para la foto de la placa del equipo (recurso nativo: CÁMARA)
//  ---------------------------------------------------------------------
//  La app NO accede directamente a la cámara: le pide la foto a la app de cámara del
//  teléfono y le indica DÓNDE guardarla con una Uri de FileProvider. Así solo se comparte
//  esa carpeta (filesDir/placas) y nada más.
//  En la base de datos se guarda solo la RUTA del archivo (equipo.foto_placa).
// =====================================================================
object FotosPlaca {

    private fun carpeta(contexto: Context): File =
        File(contexto.filesDir, "placas").also { it.mkdirs() }

    // Archivo nuevo y vacío donde la cámara dejará la foto
    fun crearArchivo(contexto: Context): File =
        File(carpeta(contexto), "placa_${System.currentTimeMillis()}.jpg")

    // Uri "content://" que la cámara puede escribir (autoridad declarada en AndroidManifest.xml)
    fun uriDe(contexto: Context, archivo: File): Uri =
        FileProvider.getUriForFile(contexto, "${contexto.packageName}.fileprovider", archivo)

    fun borrar(ruta: String?) {
        if (ruta != null) File(ruta).delete()
    }

    // Lee la foto reducida: una foto de cámara pesa varios MB y llenaría la memoria si se carga entera.
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

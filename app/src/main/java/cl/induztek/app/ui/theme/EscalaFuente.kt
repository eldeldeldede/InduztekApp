package cl.induztek.app.ui.theme

import android.content.Context
import androidx.annotation.StringRes
import cl.induztek.app.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class NivelFuente(val factor: Float, @StringRes val etiqueta: Int) {
    PEQUENA(0.88f, R.string.fuente_pequena),
    NORMAL(1f, R.string.fuente_normal),
    GRANDE(1.15f, R.string.fuente_grande),
    MUY_GRANDE(1.3f, R.string.fuente_muy_grande)
}

object EscalaFuente {
    private const val ARCHIVO = "ajustes"
    private const val CLAVE = "nivel_fuente"

    private val _nivel = MutableStateFlow(NivelFuente.NORMAL)
    val nivel: StateFlow<NivelFuente> = _nivel.asStateFlow()

    fun cargar(context: Context) {
        val guardado = context.applicationContext
            .getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
            .getString(CLAVE, null)
        _nivel.value = NivelFuente.entries.firstOrNull { it.name == guardado } ?: NivelFuente.NORMAL
    }

    fun establecer(context: Context, nivel: NivelFuente) {
        _nivel.value = nivel
        context.applicationContext
            .getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
            .edit()
            .putString(CLAVE, nivel.name)
            .apply()
    }
}

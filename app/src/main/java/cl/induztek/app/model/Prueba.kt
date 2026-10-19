package cl.induztek.app.model

import android.text.BoringLayout

data class Prueba(
    val idPrueba: Long,
    val idProtocolo: Long,
    val idTipoPrueba: Long,
    val elemento: String,
    val momento: String,
    val realizada: Boolean,
    val motivoNoRealizada: String?,
    val idInstrumento: Long,
    val idResultado: Long
)

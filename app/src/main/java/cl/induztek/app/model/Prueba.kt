package cl.induztek.app.model

enum class Momento(val etiqueta: String) {
    UNICA("Única"),
    PREVIA("Previa al mantenimiento"),
    POSTERIOR("Posterior al mantenimiento")
}

data class Prueba(
    val idPrueba: Long = 0,
    val idProtocolo: Long,
    val idTipoPrueba: Long,
    val elemento: String,
    val momento: Momento = Momento.UNICA,
    val realizada: Boolean = true,
    val motivoNoRealizada: String? = null,
    val idInstrumento: Long? = null,
    val idResultado: Long? = null,
    val comentario: String? = null
)

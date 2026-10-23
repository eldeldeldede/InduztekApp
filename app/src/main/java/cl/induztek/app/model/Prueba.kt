package cl.induztek.app.model

// Aclaración nº 4: si una prueba se repite tras un mantenimiento, ambas se guardan
// y se distinguen con este campo. Para el resultado vale la POSTERIOR.
enum class Momento(val etiqueta: String) {
    UNICA("Única"),
    PREVIA("Previa al mantenimiento"),
    POSTERIOR("Posterior al mantenimiento")
}

// =====================================================================
//  Prueba.kt — una prueba hecha (o no) dentro de un protocolo
//
//  CORREGIDO respecto a la versión anterior:
//   - Se borró `import android.text.BoringLayout` (no se usaba; Android Studio lo
//     agregó solo al autocompletar "Boolean").
//   - `momento` pasa de String a enum Momento.
//   - idInstrumento e idResultado pasan a nullable: una prueba "No realizada"
//     (Aclaración nº 2) no tiene instrumento ni resultado.
//   - Se agrega `comentario`.
// =====================================================================
data class Prueba(
    val idPrueba: Long = 0,
    val idProtocolo: Long,
    val idTipoPrueba: Long,
    val elemento: String,              // "AT-T", "Fase H1-X1", "Polo 1"...
    val momento: Momento = Momento.UNICA,
    val realizada: Boolean = true,
    val motivoNoRealizada: String? = null, // obligatorio si realizada = false
    val idInstrumento: Long? = null,       // calibración vigente a la fecha de la prueba (RN-11)
    val idResultado: Long? = null,
    val comentario: String? = null
)

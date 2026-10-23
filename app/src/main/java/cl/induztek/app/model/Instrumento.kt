package cl.induztek.app.model

// CORREGIDO: faltaba `marca` (Anexo 8). Las fechas de calibración NO viven aquí sino
// en Calibracion, porque un instrumento tiene VARIAS calibraciones a lo largo de los años
// (así se puede demostrar que estaba calibrado el día de un protocolo antiguo).
data class Instrumento(
    val idInstrumento: Long,
    val nombre: String,
    val marca: String? = null,
    val modelo: String? = null,
    val numeroSerie: String
)

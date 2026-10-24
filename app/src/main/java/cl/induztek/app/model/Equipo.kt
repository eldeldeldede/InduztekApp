package cl.induztek.app.model

// =====================================================================
//  Equipo.kt — una fila de la tabla "equipo" (Anexo 8, RF-02, RN-01)
//
//  CORREGIDO respecto a la versión anterior:
//   - `tensiones` se separa en tensionPrimariaKv y tensionSecundariaKv (como el MER).
//   - Se agregan anioFabricacion y observaciones.
//   - maxContactosUohm pasa a nullable: solo aplica a interruptores y celdas MT.
//   - marca, modelo y numeroSerie pasan a nullable: la placa puede no tenerlos
//     (en el Anexo 9 ningún equipo trae "modelo").
//
//  Regla de negocio RN-01: el TAG es único DENTRO de una instalación
//  (dos instalaciones distintas pueden tener un "TR-01"). Eso se garantiza en la
//  base con UNIQUE (id_instalacion, tag) — ver Esquema.kt.
// =====================================================================
data class Equipo(
    val idEquipo: Long = 0,
    val idInstalacion: Long,
    val tag: String,
    val idTipoEquipo: Long,
    val marca: String? = null,
    val modelo: String? = null,
    val numeroSerie: String? = null,
    val anioFabricacion: Int? = null,
    val potenciaKva: Double? = null,
    val tensionPrimariaKv: Double? = null,
    val tensionSecundariaKv: Double? = null,
    val corrienteNominalA: Double? = null,
    val maxContactosUohm: Double? = null,
    val observaciones: String? = null,
    // Ruta del archivo con la foto de la placa (recurso nativo: cámara). Es opcional.
    val fotoPlaca: String? = null,
    // No es columna de "equipo": viene del JOIN con tipo_equipo (para mostrar "Transformador")
    val tipoEquipoNombre: String = ""
)

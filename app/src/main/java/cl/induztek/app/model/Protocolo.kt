package cl.induztek.app.model

import java.time.LocalDateTime

// =====================================================================
//  Protocolo.kt — una fila de la tabla "protocolo" (un protocolo POR EQUIPO y visita)
//
//  CORREGIDO respecto a la versión anterior:
//   - temperaturaAmbiente y pctHumedad pasan a nullable: en estado BORRADOR todavía
//     pueden faltar; solo son obligatorias para pasar a REGISTRADO_TERRENO (HU-02).
//   - descripcionMantenimiento pasa a nullable (solo existe si hubo mantenimiento).
//   - Se agrega `uuid` (identificador único generado en el dispositivo, para que dos
//     celulares sin señal nunca creen el mismo protocolo — RN-02) y `observaciones`.
// =====================================================================
data class Protocolo(
    val idProtocolo: Long = 0,
    val uuid: String,
    val numero: String,                    // visible: "PT-2026-0401" (RN-02, único)
    val idVisita: Long,
    val idEquipo: Long,
    val idUsuario: Long,                   // foreign key con id del usuario que registra los datos
    val fechaHoraPrueba: LocalDateTime,
    val temperaturaAmbiente: Double? = null, // °C. Obligatorio para pasar a REGISTRADO_TERRENO
    val pctHumedad: Double? = null,          // %. Obligatorio junto a la temperatura
    val descripcionMantenimiento: String? = null,
    val observaciones: String? = null,
    val idEstadoProtocolo: Long,
    val idProtocoloReemplazado: Long? = null
)

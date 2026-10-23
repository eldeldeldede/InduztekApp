package cl.induztek.app.validacion

import cl.induztek.app.model.CodigoRol
import cl.induztek.app.model.TipoEstadoProtocolo
import cl.induztek.app.model.TipoEstadoProtocolo.ANULADO
import cl.induztek.app.model.TipoEstadoProtocolo.BORRADOR
import cl.induztek.app.model.TipoEstadoProtocolo.FORMALIZADO
import cl.induztek.app.model.TipoEstadoProtocolo.OBSERVADO
import cl.induztek.app.model.TipoEstadoProtocolo.REGISTRADO_TERRENO
import java.time.DayOfWeek
import java.time.LocalDate

// =====================================================================
//  ReglasProtocolo.kt — RN-04 (estados), RN-12, RN-13, RN-16 y RN-17 (permisos)
//  ---------------------------------------------------------------------
//  El InduztekDbHelper solo GUARDA (cambiarEstadoProtocolo no pregunta nada).
//  Lo que decide si un cambio está PERMITIDO es este archivo. El ViewModel hace:
//
//      val error = ReglasProtocolo.errorTransicion(...)
//      if (error != null) { mostrar el mensaje; return }       // no se toca la base
//      db.cambiarEstadoProtocolo(...)
//
//  Todas las funciones devuelven `String?`: el MENSAJE de error listo para mostrar, o null
//  si todo está bien. Es el mismo patrón de errorNombre / errorCorreo de MiRegistro.
// =====================================================================
object ReglasProtocolo {

    private val PERSONAL_TERRENO = setOf(CodigoRol.TECNICO, CodigoRol.INGENIERO)
    private val REVISORES = setOf(CodigoRol.INGENIERO, CodigoRol.JEFE_TECNICO)

    // ---------- RN-04 · Transiciones de estado ----------
    //
    //  | Desde               | Hacia               | Quién / condición                              |
    //  | (nuevo)             | Borrador            | Técnico o Ingeniero    (lo hace insertarProtocoloBorrador) |
    //  | Borrador            | Registrado terreno  | Técnico o Ingeniero + ≥1 prueba completa + temperatura y humedad |
    //  | Registrado terreno  | Formalizado         | Ingeniero o Jefe técnico                       |
    //  | Registrado terreno  | Observado           | Ingeniero o Jefe técnico, con motivo           |
    //  | Observado           | Registrado terreno  | Técnico o Ingeniero (ya corregido)             |
    //  | Cualquiera menos Formalizado | Anulado    | Ingeniero o Jefe técnico, con motivo           |
    //
    //  motivo                        : texto escrito por el usuario (Observado y Anulado lo exigen)
    //  tienePruebaCompleta           : hay al menos una prueba realizada con todas sus mediciones
    //  tieneCondicionesAmbientales   : temperatura y humedad ya guardadas
    //  faltaComentarioVariacion      : RN-09, hay una variación significativa sin comentario del ingeniero
    fun errorTransicion(
        desde: TipoEstadoProtocolo,
        hacia: TipoEstadoProtocolo,
        rol: CodigoRol,
        motivo: String? = null,
        tienePruebaCompleta: Boolean = true,
        tieneCondicionesAmbientales: Boolean = true,
        faltaComentarioVariacion: Boolean = false
    ): String? {
        // Un Formalizado es definitivo: "no se puede modificar ni anular" (RN-04).
        if (desde == FORMALIZADO) return "Un protocolo formalizado no se puede modificar ni anular. Emite uno nuevo que lo reemplace."
        if (desde == ANULADO) return "Un protocolo anulado no se puede modificar."

        return when (hacia) {
            ANULADO -> when {
                rol !in REVISORES -> "Solo un Ingeniero o el Jefe técnico puede anular un protocolo."
                motivo.isNullOrBlank() -> "Debes indicar el motivo de la anulación."
                else -> null
            }

            REGISTRADO_TERRENO -> when {
                desde != BORRADOR && desde != OBSERVADO -> "Solo se puede registrar desde Borrador u Observado."
                rol !in PERSONAL_TERRENO -> "Solo un Técnico o un Ingeniero puede registrar en terreno."
                !tienePruebaCompleta -> "Debes completar al menos una prueba antes de registrar en terreno."
                !tieneCondicionesAmbientales -> "Debes ingresar la temperatura y la humedad."
                else -> null
            }

            FORMALIZADO -> when {
                desde != REGISTRADO_TERRENO -> "Solo se puede formalizar un protocolo registrado en terreno."
                rol !in REVISORES -> "Solo un Ingeniero o el Jefe técnico puede formalizar."
                faltaComentarioVariacion -> "Hay una variación significativa: deja un comentario antes de formalizar."
                else -> null
            }

            OBSERVADO -> when {
                desde != REGISTRADO_TERRENO -> "Solo se puede observar un protocolo registrado en terreno."
                rol !in REVISORES -> "Solo un Ingeniero o el Jefe técnico puede observar un protocolo."
                motivo.isNullOrBlank() -> "Debes indicar el motivo de la observación."
                else -> null
            }

            // Nadie "vuelve" a Borrador: el Borrador solo existe al crear el protocolo.
            BORRADOR -> "Un protocolo no puede volver a Borrador."
        }
    }

    // Lista de estados a los que `rol` PODRÍA mover un protocolo desde `desde`.
    // La pantalla la usa para mostrar solo los botones que corresponden (sin los condicionales).
    fun estadosPosibles(desde: TipoEstadoProtocolo, rol: CodigoRol): List<TipoEstadoProtocolo> =
        TipoEstadoProtocolo.entries.filter { hacia ->
            hacia != desde && errorTransicion(
                desde, hacia, rol,
                motivo = "x" // se supone un motivo: aquí solo importa quién puede y desde dónde
            ) == null
        }

    // ---------- RN-17 · ¿Quién puede MODIFICAR un protocolo? ----------
    //
    //  - Técnico: solo los que él creó, y solo en Borrador u Observado.
    //  - Ingeniero y Jefe técnico: mientras no esté Formalizado ni Anulado.
    //  - Cliente: nunca (solo lee).
    fun puedeModificar(
        rol: CodigoRol,
        idUsuario: Long,
        idDuenoProtocolo: Long,
        estado: TipoEstadoProtocolo
    ): Boolean = when (rol) {
        CodigoRol.CLIENTE -> false
        CodigoRol.TECNICO -> idUsuario == idDuenoProtocolo && (estado == BORRADOR || estado == OBSERVADO)
        CodigoRol.INGENIERO, CodigoRol.JEFE_TECNICO -> estado != FORMALIZADO && estado != ANULADO
    }

    // ---------- RN-12 · Instrumentos ----------
    // "Solo el Jefe técnico puede registrar o actualizar instrumentos y sus calibraciones."
    fun puedeGestionarInstrumentos(rol: CodigoRol): Boolean = rol == CodigoRol.JEFE_TECNICO

    // ---------- RN-13 · Cierre de visita ----------
    // "Una visita solo se cierra cuando todos sus protocolos están en Registrado en terreno
    // (o Anulado)." Decisión del equipo: una visita sin ningún protocolo tampoco se cierra,
    // porque el acta no tendría equipos que mostrar (RN-14).
    fun errorCierreVisita(estadosDeLosProtocolos: List<TipoEstadoProtocolo>): String? {
        if (estadosDeLosProtocolos.isEmpty()) return "La visita no tiene protocolos."
        val pendientes = estadosDeLosProtocolos.count { it != REGISTRADO_TERRENO && it != ANULADO }
        return if (pendientes > 0)
            "Hay $pendientes protocolo(s) sin registrar en terreno (Borrador u Observado)."
        else null
    }

    // ---------- RN-16 · Meta de 2 días hábiles ----------
    // "Todo protocolo debe estar Formalizado dentro de 2 días hábiles desde la prueba."
    // Día hábil = lunes a viernes (los feriados no están modelados: es una simplificación).
    const val META_DIAS_HABILES = 2

    // Días hábiles transcurridos DESPUÉS de `desde`, hasta `hasta` inclusive.
    //   Prueba el viernes -> el lunes ha pasado 1 día hábil, el martes 2, el miércoles 3.
    fun diasHabilesEntre(desde: LocalDate, hasta: LocalDate): Int {
        var dias = 0
        var dia = desde.plusDays(1)
        while (!dia.isAfter(hasta)) {
            if (dia.dayOfWeek != DayOfWeek.SATURDAY && dia.dayOfWeek != DayOfWeek.SUNDAY) dias++
            dia = dia.plusDays(1)
        }
        return dias
    }

    // Un protocolo está ATRASADO si aún no se formaliza ni anula y ya pasó la meta.
    fun estaAtrasado(fechaPrueba: LocalDate, hoy: LocalDate, estado: TipoEstadoProtocolo): Boolean =
        estado != FORMALIZADO && estado != ANULADO &&
            diasHabilesEntre(fechaPrueba, hoy) > META_DIAS_HABILES
}

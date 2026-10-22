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

object ReglasProtocolo {

    private val PERSONAL_TERRENO = setOf(CodigoRol.TECNICO, CodigoRol.INGENIERO)
    private val REVISORES = setOf(CodigoRol.INGENIERO, CodigoRol.JEFE_TECNICO)

    fun errorTransicion(
        desde: TipoEstadoProtocolo,
        hacia: TipoEstadoProtocolo,
        rol: CodigoRol,
        motivo: String? = null,
        tienePruebaCompleta: Boolean = true,
        tieneCondicionesAmbientales: Boolean = true,
        faltaComentarioVariacion: Boolean = false
    ): String? {
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

            BORRADOR -> "Un protocolo no puede volver a Borrador."
        }
    }

    fun estadosPosibles(desde: TipoEstadoProtocolo, rol: CodigoRol): List<TipoEstadoProtocolo> =
        TipoEstadoProtocolo.entries.filter { hacia ->
            hacia != desde && errorTransicion(
                desde, hacia, rol,
                motivo = "x"
            ) == null
        }

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

    fun puedeGestionarInstrumentos(rol: CodigoRol): Boolean = rol == CodigoRol.JEFE_TECNICO

    fun errorCierreVisita(estadosDeLosProtocolos: List<TipoEstadoProtocolo>): String? {
        if (estadosDeLosProtocolos.isEmpty()) return "La visita no tiene protocolos."
        val pendientes = estadosDeLosProtocolos.count { it != REGISTRADO_TERRENO && it != ANULADO }
        return if (pendientes > 0)
            "Hay $pendientes protocolo(s) sin registrar en terreno (Borrador u Observado)."
        else null
    }

    const val META_DIAS_HABILES = 2

    fun diasHabilesEntre(desde: LocalDate, hasta: LocalDate): Int {
        var dias = 0
        var dia = desde.plusDays(1)
        while (!dia.isAfter(hasta)) {
            if (dia.dayOfWeek != DayOfWeek.SATURDAY && dia.dayOfWeek != DayOfWeek.SUNDAY) dias++
            dia = dia.plusDays(1)
        }
        return dias
    }

    fun estaAtrasado(fechaPrueba: LocalDate, hoy: LocalDate, estado: TipoEstadoProtocolo): Boolean =
        estado != FORMALIZADO && estado != ANULADO &&
                diasHabilesEntre(fechaPrueba, hoy) > META_DIAS_HABILES
}
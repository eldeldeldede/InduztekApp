package cl.induztek.app.navigation

object Rutas {
    const val LOGIN = "login"
    const val INSTALACIONES = "instalaciones"

    const val ARG_ID_INSTALACION = "idInstalacion"
    const val ARG_ID_EQUIPO = "idEquipo"
    const val ARG_ID_PROTOCOLO = "idProtocolo"
    const val ARG_ID_VISITA = "idVisita"

    const val EQUIPOS = "equipos/{$ARG_ID_INSTALACION}"
    const val ALTA_EQUIPO = "alta_equipo/{$ARG_ID_INSTALACION}"
    const val HISTORIAL_EQUIPO = "historial/{$ARG_ID_EQUIPO}"
    const val NUEVO_PROTOCOLO = "nuevo_protocolo/{$ARG_ID_EQUIPO}"
    const val MEDICIONES = "mediciones/{$ARG_ID_PROTOCOLO}"
    const val CIERRE_VISITA = "cierre_visita/{$ARG_ID_VISITA}"
    const val ACTAS = "actas/{$ARG_ID_INSTALACION}"

    fun equipos(idInstalacion: Long) = "equipos/$idInstalacion"
    fun altaEquipo(idInstalacion: Long) = "alta_equipo/$idInstalacion"
    fun historialEquipo(idEquipo: Long) = "historial/$idEquipo"
    fun nuevoProtocolo(idEquipo: Long) = "nuevo_protocolo/$idEquipo"
    fun mediciones(idProtocolo: Long) = "mediciones/$idProtocolo"
    fun cierreVisita(idVisita: Long) = "cierre_visita/$idVisita"
    fun actas(idInstalacion: Long) = "actas/$idInstalacion"
}

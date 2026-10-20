package cl.induztek.app.navigation

// =====================================================================
//  Rutas.kt — las "direcciones" de cada pantalla
//  ---------------------------------------------------------------------
//  Navigation Compose navega con texto, como una URL. Las partes entre llaves son
//  ARGUMENTOS: "equipos/{idInstalacion}" se convierte en "equipos/1" al navegar.
//  Las funciones de abajo arman la ruta con el id y evitan errores al escribir el texto a mano.
// =====================================================================
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
    const val MEDICIONES = "mediciones/{$ARG_ID_PROTOCOLO}"
    const val CIERRE_VISITA = "cierre_visita/{$ARG_ID_VISITA}"

    fun equipos(idInstalacion: Long) = "equipos/$idInstalacion"
    fun altaEquipo(idInstalacion: Long) = "alta_equipo/$idInstalacion"
    fun historialEquipo(idEquipo: Long) = "historial/$idEquipo"
    fun mediciones(idProtocolo: Long) = "mediciones/$idProtocolo"
    fun cierreVisita(idVisita: Long) = "cierre_visita/$idVisita"
}

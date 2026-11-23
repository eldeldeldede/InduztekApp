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
    const val CAMBIAR_CLAVE = "cambiar_clave"   // primer ingreso: obliga a reemplazar la clave temporal
    const val CREAR_USUARIO = "crear_usuario"   // solo Jefe técnico
    const val RECUPERAR_CLAVE = "recuperar_clave"           // pública: se abre desde el login
    const val TRABAJADORES = "trabajadores"   // solo Jefe técnico
    const val CLIENTES = "clientes"                         // personal: lista, alta y edición de clientes
    const val PERFIL = "perfil"                             // cada usuario edita sus datos
    const val CAMBIAR_CLAVE_VOLUNTARIO = "cambiar_clave_voluntario"

    const val ARG_ID_INSTALACION = "idInstalacion"
    const val ARG_ID_EQUIPO = "idEquipo"
    const val ARG_ID_PROTOCOLO = "idProtocolo"
    const val ARG_ID_VISITA = "idVisita"

    const val EQUIPOS = "equipos/{$ARG_ID_INSTALACION}"
    const val ALTA_EQUIPO = "alta_equipo/{$ARG_ID_INSTALACION}"
    const val EDITAR_EQUIPO = "editar_equipo/{$ARG_ID_EQUIPO}"   // solo Jefe técnico
    const val HISTORIAL_EQUIPO = "historial/{$ARG_ID_EQUIPO}"
    const val NUEVO_PROTOCOLO = "nuevo_protocolo/{$ARG_ID_EQUIPO}"
    const val MEDICIONES = "mediciones/{$ARG_ID_PROTOCOLO}"
    const val CIERRE_VISITA = "cierre_visita/{$ARG_ID_VISITA}"
    const val ACTAS = "actas/{$ARG_ID_INSTALACION}"
    const val INSTALACION_FORM = "instalacion_form/{$ARG_ID_INSTALACION}"   // id 0 = instalación nueva

    fun equipos(idInstalacion: Long) = "equipos/$idInstalacion"
    fun altaEquipo(idInstalacion: Long) = "alta_equipo/$idInstalacion"
    fun editarEquipo(idEquipo: Long) = "editar_equipo/$idEquipo"
    fun historialEquipo(idEquipo: Long) = "historial/$idEquipo"
    fun nuevoProtocolo(idEquipo: Long) = "nuevo_protocolo/$idEquipo"
    fun mediciones(idProtocolo: Long) = "mediciones/$idProtocolo"
    fun cierreVisita(idVisita: Long) = "cierre_visita/$idVisita"
    fun actas(idInstalacion: Long) = "actas/$idInstalacion"
    fun instalacionForm(idInstalacion: Long = 0) = "instalacion_form/$idInstalacion"
}

package cl.induztek.app.repository

import cl.induztek.app.local.AlmacenLocal
import cl.induztek.app.local.EstadoOperacion
import cl.induztek.app.local.OperacionPendiente
import cl.induztek.app.local.TipoOperacion
import cl.induztek.app.model.Usuario
import cl.induztek.app.network.ApiClient
import cl.induztek.app.network.ResultadoRed
import cl.induztek.app.network.llamarApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

// =====================================================================
//  Sincronizador.kt — envía al servidor lo que se hizo sin conexión
//  ---------------------------------------------------------------------
//  Se ejecuta cuando vuelve la red, al iniciar sesión o al apretar "Sincronizar".
//  Recorre la cola EN ORDEN. Para cada operación:
//    - Listo        -> se quita de la cola.
//    - Falla        -> queda marcada como FALLIDA con el motivo (el técnico la ve y puede descartarla).
//    - Sin conexión -> se detiene y espera la próxima vez.
//  CONFLICTOS (regla: "gana el último que llega"): si el servidor dice que ya existe (409)
//    - visita con la misma OT       -> se usa la que ya existía.
//    - protocolo del mismo equipo   -> se usa el que ya existía.
//    - misma prueba/elemento/momento-> se reemplaza la del servidor por la que llegó ahora.
// =====================================================================
object Sincronizador {

    // Alcance propio (no depende de ninguna pantalla): la sincronización sigue aunque se cambie de pantalla.
    val alcance = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val candado = Mutex()
    private val _sincronizando = MutableStateFlow(false)
    val sincronizando: StateFlow<Boolean> = _sincronizando.asStateFlow()

    // Sube cada vez que termina una sincronización, para que las pantallas puedan recargar.
    private val _version = MutableStateFlow(0)
    val version: StateFlow<Int> = _version.asStateFlow()

    sealed interface Paso {
        data object Listo : Paso
        data class Falla(val motivo: String) : Paso
        data object SinRed : Paso
    }

    suspend fun sincronizar() {
        if (!candado.tryLock()) return
        try {
            _sincronizando.value = true
            val pendientes = AlmacenLocal.operaciones().filter { it.estado == EstadoOperacion.PENDIENTE }
            for (operacion in pendientes) {
                when (val paso = ejecutar(operacion)) {
                    Paso.Listo -> AlmacenLocal.quitarOperacion(operacion.id)
                    is Paso.Falla -> AlmacenLocal.actualizarOperacion(
                        operacion.copy(estado = EstadoOperacion.FALLIDA, error = paso.motivo)
                    )
                    Paso.SinRed -> break
                }
            }
            AlmacenLocal.limpiarCopiasSinOperaciones()
            _version.value += 1
        } finally {
            _sincronizando.value = false
            candado.unlock()
        }
    }

    fun descartar(idOperacion: Long) {
        AlmacenLocal.quitarOperacion(idOperacion)
        AlmacenLocal.limpiarCopiasSinOperaciones()
        _version.value += 1
    }

    fun descartarFallidas() {
        AlmacenLocal.operaciones().filter { it.estado == EstadoOperacion.FALLIDA }.forEach { AlmacenLocal.quitarOperacion(it.id) }
        AlmacenLocal.limpiarCopiasSinOperaciones()
        _version.value += 1
    }

    // Deja la operación otra vez en la fila para reintentarla (por si el error era pasajero).
    fun reintentarFallidas() {
        AlmacenLocal.operaciones().filter { it.estado == EstadoOperacion.FALLIDA }
            .forEach { AlmacenLocal.actualizarOperacion(it.copy(estado = EstadoOperacion.PENDIENTE, error = null)) }
        alcance.launch { sincronizar() }
    }

    private const val DEPENDE = "Depende de un cambio anterior que no se pudo enviar"

    private suspend fun ejecutar(op: OperacionPendiente): Paso = when (op.tipo) {
        TipoOperacion.CREAR_VISITA -> crearVisita(op)
        TipoOperacion.CREAR_PROTOCOLO -> crearProtocolo(op)
        TipoOperacion.ACTUALIZAR_CONDICIONES -> actualizarCondiciones(op)
        TipoOperacion.REGISTRAR_PRUEBA -> registrarPrueba(op)
        TipoOperacion.ELIMINAR_PRUEBA -> eliminarPrueba(op)
        TipoOperacion.REGISTRAR_TERRENO -> registrarTerreno(op)
        TipoOperacion.CERRAR_VISITA -> cerrarVisita(op)
    }

    private suspend fun crearVisita(op: OperacionPendiente): Paso {
        val solicitud = op.visita ?: return Paso.Falla("Operación incompleta")
        return when (val r = llamarApi { ApiClient.protocolos.crearVisita(op.actorId, solicitud) }) {
            is ResultadoRed.Exito -> {
                AlmacenLocal.registrarEquivalencia(op.refVisita!!, r.datos.id)
                Paso.Listo
            }
            is ResultadoRed.ErrorServidor ->
                if (r.codigo == 409) {
                    // La OT ya existía en el servidor: se sigue trabajando sobre esa visita.
                    val existente = (llamarApi { ApiClient.protocolos.visitaPorOt(solicitud.numeroOt) } as? ResultadoRed.Exito)
                        ?.datos?.firstOrNull { it.numeroOt.equals(solicitud.numeroOt, ignoreCase = true) }
                    if (existente != null && existente.instalacionId == solicitud.instalacionId && !existente.cerrada) {
                        AlmacenLocal.registrarEquivalencia(op.refVisita!!, existente.id)
                        Paso.Listo
                    } else Paso.Falla("La OT ${solicitud.numeroOt} ya existe en el servidor (otra instalación o ya cerrada)")
                } else Paso.Falla(r.mensaje)
            ResultadoRed.SinConexion -> Paso.SinRed
        }
    }

    private suspend fun crearProtocolo(op: OperacionPendiente): Paso {
        val idVisita = AlmacenLocal.idReal(op.refVisita ?: return Paso.Falla("Operación incompleta")) ?: return Paso.Falla(DEPENDE)
        val solicitud = (op.protocolo ?: return Paso.Falla("Operación incompleta")).copy(visitaId = idVisita)
        return when (val r = llamarApi { ApiClient.protocolos.crearProtocolo(op.actorId, solicitud) }) {
            is ResultadoRed.Exito -> {
                AlmacenLocal.registrarEquivalencia(op.refProtocolo!!, r.datos.id)
                Paso.Listo
            }
            is ResultadoRed.ErrorServidor ->
                if (r.codigo == 409) {
                    val existente = (llamarApi { ApiClient.protocolos.protocolos(idVisita, solicitud.equipoId) } as? ResultadoRed.Exito)
                        ?.datos?.firstOrNull { it.equipoId == solicitud.equipoId }
                    if (existente != null) {
                        AlmacenLocal.registrarEquivalencia(op.refProtocolo!!, existente.id)
                        Paso.Listo
                    } else Paso.Falla(r.mensaje)
                } else Paso.Falla(r.mensaje)
            ResultadoRed.SinConexion -> Paso.SinRed
        }
    }

    private suspend fun actualizarCondiciones(op: OperacionPendiente): Paso {
        val id = AlmacenLocal.idReal(op.refProtocolo ?: return Paso.Falla("Operación incompleta")) ?: return Paso.Falla(DEPENDE)
        val solicitud = op.condiciones ?: return Paso.Falla("Operación incompleta")
        return when (val r = llamarApi { ApiClient.protocolos.actualizarProtocolo(op.actorId, id, solicitud) }) {
            is ResultadoRed.Exito -> Paso.Listo
            is ResultadoRed.ErrorServidor -> Paso.Falla(r.mensaje)
            ResultadoRed.SinConexion -> Paso.SinRed
        }
    }

    private suspend fun registrarPrueba(op: OperacionPendiente): Paso {
        val id = AlmacenLocal.idReal(op.refProtocolo ?: return Paso.Falla("Operación incompleta")) ?: return Paso.Falla(DEPENDE)
        val solicitud = op.prueba ?: return Paso.Falla("Operación incompleta")
        var r = llamarApi { ApiClient.protocolos.registrarPrueba(op.actorId, id, solicitud) }
        if (r is ResultadoRed.ErrorServidor && r.codigo == 409) {
            // Ya había una prueba igual en el servidor: gana la que llega ahora (se reemplaza).
            val detalle = (llamarApi { ApiClient.protocolos.protocolo(id) } as? ResultadoRed.Exito)?.datos
            val existente = detalle?.pruebas?.firstOrNull {
                it.tipoPruebaId == solicitud.tipoPruebaId && it.elemento.equals(solicitud.elemento, true) && it.momento == solicitud.momento
            }
            if (existente != null) {
                val borrado = llamarApi { ApiClient.protocolos.eliminarPrueba(op.actorId, id, existente.id) }
                if (borrado is ResultadoRed.Exito) r = llamarApi { ApiClient.protocolos.registrarPrueba(op.actorId, id, solicitud) }
            }
        }
        return when (r) {
            is ResultadoRed.Exito -> {
                op.refPrueba?.let { AlmacenLocal.registrarEquivalencia(it, r.datos.id) }
                Paso.Listo
            }
            is ResultadoRed.ErrorServidor -> Paso.Falla(r.mensaje)
            ResultadoRed.SinConexion -> Paso.SinRed
        }
    }

    private suspend fun eliminarPrueba(op: OperacionPendiente): Paso {
        val id = AlmacenLocal.idReal(op.refProtocolo ?: return Paso.Falla("Operación incompleta")) ?: return Paso.Falla(DEPENDE)
        val idPrueba = AlmacenLocal.idReal(op.refPrueba ?: return Paso.Falla("Operación incompleta")) ?: return Paso.Falla(DEPENDE)
        return when (val r = llamarApi { ApiClient.protocolos.eliminarPrueba(op.actorId, id, idPrueba) }) {
            is ResultadoRed.Exito -> Paso.Listo
            // 404: otra persona ya la había eliminado, el resultado es el mismo
            is ResultadoRed.ErrorServidor -> if (r.codigo == 404) Paso.Listo else Paso.Falla(r.mensaje)
            ResultadoRed.SinConexion -> Paso.SinRed
        }
    }

    private suspend fun registrarTerreno(op: OperacionPendiente): Paso {
        val id = AlmacenLocal.idReal(op.refProtocolo ?: return Paso.Falla("Operación incompleta")) ?: return Paso.Falla(DEPENDE)
        return when (val r = llamarApi { ApiClient.protocolos.registrarEnTerreno(op.actorId, id) }) {
            is ResultadoRed.Exito -> Paso.Listo
            is ResultadoRed.ErrorServidor -> {
                // Si ya estaba registrado (o más avanzado) en el servidor, no hay nada más que hacer.
                val estado = (llamarApi { ApiClient.protocolos.protocolo(id) } as? ResultadoRed.Exito)?.datos?.estado
                if (estado != null && estado != "BORRADOR" && estado != "OBSERVADO") Paso.Listo else Paso.Falla(r.mensaje)
            }
            ResultadoRed.SinConexion -> Paso.SinRed
        }
    }

    private suspend fun cerrarVisita(op: OperacionPendiente): Paso {
        val id = AlmacenLocal.idReal(op.refVisita ?: return Paso.Falla("Operación incompleta")) ?: return Paso.Falla(DEPENDE)
        return when (val r = llamarApi { ApiClient.protocolos.cerrarVisita(op.actorId, id) }) {
            is ResultadoRed.Exito -> Paso.Listo
            is ResultadoRed.ErrorServidor -> {
                val cerrada = (llamarApi { ApiClient.protocolos.visita(id) } as? ResultadoRed.Exito)?.datos?.cerrada == true
                if (cerrada) Paso.Listo else Paso.Falla(r.mensaje)
            }
            ResultadoRed.SinConexion -> Paso.SinRed
        }
    }

    // ---------- Precarga: deja guardado lo que el técnico necesitará si se queda sin señal ----------

    suspend fun precargar(usuario: Usuario) {
        // Si no hay servidor, no se intenta nada (evita esperar tiempos de espera uno por uno).
        if (llamarApi { ApiClient.equipos.tiposEquipo() } !is ResultadoRed.Exito) return
        val instalaciones = (Repositorios.equipos.instalaciones(usuario) as? ResultadoRed.Exito)?.datos ?: return
        Repositorios.usuarios.nombres()
        if (!usuario.esCliente) {
            Repositorios.usuarios.listarActivosPorRol(cl.induztek.app.model.CodigoRol.INGENIERO)
            Repositorios.protocolos.instrumentosVigentes()
        }
        val tiposVistos = mutableSetOf<Long>()
        for (instalacion in instalaciones) {
            Repositorios.protocolos.visitas(instalacion.idInstalacion, usuario)
            val equipos = (Repositorios.equipos.equipos(instalacion.idInstalacion, usuario) as? ResultadoRed.Exito)?.datos ?: continue
            if (!usuario.esCliente) {
                val visitas = (Repositorios.protocolos.visitas(instalacion.idInstalacion, usuario) as? ResultadoRed.Exito)?.datos
                visitas?.forEach { Repositorios.protocolos.protocolosPorVisita(it.idVisita, instalacion.idInstalacion, usuario) }
            }
            for (equipo in equipos) {
                Repositorios.equipos.equipo(equipo.idEquipo, usuario)
                if (!usuario.esCliente && tiposVistos.add(equipo.idTipoEquipo)) {
                    Repositorios.protocolos.pruebasDeTipoEquipo(equipo.idTipoEquipo)
                }
                // Último protocolo del equipo: se guarda completo para poder consultarlo sin señal.
                val protocolos = (Repositorios.protocolos.protocolosPorEquipo(equipo.idEquipo, usuario, equipo.tag)
                    as? ResultadoRed.Exito)?.datos
                protocolos?.filter { it.idProtocolo > 0 }?.maxByOrNull { it.fechaHoraPrueba }
                    ?.let { Repositorios.protocolos.protocoloDetalle(it.idProtocolo) }
            }
        }
    }

    // Lo que se hace al iniciar sesión o al volver la red: primero enviar lo pendiente, luego actualizar la caché.
    fun alVolverConexion(usuario: Usuario?) {
        alcance.launch {
            sincronizar()
            if (usuario != null) precargar(usuario)
        }
    }
}

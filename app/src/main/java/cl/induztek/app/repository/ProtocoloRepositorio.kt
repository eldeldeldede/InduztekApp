package cl.induztek.app.repository

import cl.induztek.app.data.aTexto
import cl.induztek.app.local.AlmacenLocal
import cl.induztek.app.local.ContextoPruebaLocal
import cl.induztek.app.local.OperacionPendiente
import cl.induztek.app.local.TipoOperacion
import cl.induztek.app.local.cerradaLocal
import cl.induztek.app.local.conCondiciones
import cl.induztek.app.local.conPrueba
import cl.induztek.app.local.nuevoProtocoloLocal
import cl.induztek.app.local.pruebaLocal
import cl.induztek.app.local.registradoEnTerreno
import cl.induztek.app.local.sinPrueba
import cl.induztek.app.model.HistorialEstado
import cl.induztek.app.model.Instrumento
import cl.induztek.app.model.Parametro
import cl.induztek.app.model.Protocolo
import cl.induztek.app.model.PruebaRegistrada
import cl.induztek.app.model.PuntoHistorial
import cl.induztek.app.model.RecepcionActa
import cl.induztek.app.model.ResumenProtocolo
import cl.induztek.app.model.SerieMedicion
import cl.induztek.app.model.EstadoActa
import cl.induztek.app.model.TipoEstadoProtocolo
import cl.induztek.app.model.TipoResultado
import cl.induztek.app.model.TipoPrueba
import cl.induztek.app.model.Usuario
import cl.induztek.app.model.Visita
import cl.induztek.app.network.ActaDto
import cl.induztek.app.network.ApiClient
import cl.induztek.app.network.HistorialEquipoDto
import cl.induztek.app.network.InstrumentoDto
import cl.induztek.app.network.MotivoSolicitud
import cl.induztek.app.network.ProtocoloActualizacionSolicitud
import cl.induztek.app.network.ProtocoloDto
import cl.induztek.app.network.ProtocoloSolicitud
import cl.induztek.app.network.PruebaSolicitud
import cl.induztek.app.network.RecepcionSolicitud
import cl.induztek.app.network.ResultadoRed
import cl.induztek.app.network.TipoPruebaDto
import cl.induztek.app.network.VisitaDto
import cl.induztek.app.network.VisitaSolicitud
import cl.induztek.app.network.llamarApi
import cl.induztek.app.network.llamarConCache
import cl.induztek.app.network.mapear
import java.time.LocalDate
import java.time.LocalDateTime

// =====================================================================
//  ProtocoloRepositorio.kt — visitas, protocolos, pruebas e instrumentos
//  (protocolos-service para el personal; reportes-service para el cliente y los historiales)
//  ---------------------------------------------------------------------
//  MODO SIN CONEXIÓN (solo personal de terreno):
//   - Las consultas guardan una copia al ir bien y la devuelven si no hay red.
//   - Crear protocolos, anotar mediciones y cerrar visitas se guardan en la cola (AlmacenLocal) y
//     se envían solos al volver la conexión (Sincronizador). Mientras tanto la pantalla usa la copia local.
//   - Regla: si un protocolo/visita ya tiene cambios pendientes, los cambios nuevos también van a la cola,
//     para que el servidor los reciba en el mismo orden en que se hicieron.
// =====================================================================
class ProtocoloRepositorio(private val usuarios: UsuarioRepositorio) {

    private fun ahora(): String = LocalDateTime.now().aTexto()

    private fun encolar(
        tipo: TipoOperacion,
        idActor: Long,
        descripcion: String,
        refVisita: Long? = null,
        refProtocolo: Long? = null,
        refPrueba: Long? = null,
        visita: VisitaSolicitud? = null,
        protocolo: ProtocoloSolicitud? = null,
        condiciones: ProtocoloActualizacionSolicitud? = null,
        prueba: PruebaSolicitud? = null
    ) = AlmacenLocal.encolar(
        OperacionPendiente(
            id = 0, tipo = tipo, actorId = idActor, descripcion = descripcion,
            refVisita = refVisita, refProtocolo = refProtocolo, refPrueba = refPrueba,
            visita = visita, protocolo = protocolo, condiciones = condiciones, prueba = prueba
        )
    )

    // Un protocolo "tiene cambios locales" si se creó sin conexión o ya se modificó sin conexión.
    private fun tieneCambiosLocales(idProtocolo: Long) =
        AlmacenLocal.esLocal(idProtocolo) || AlmacenLocal.protocoloLocal(idProtocolo) != null

    private fun visitaConCambiosLocales(idVisita: Long) =
        AlmacenLocal.esLocal(idVisita) || AlmacenLocal.visitaLocal(idVisita) != null ||
            AlmacenLocal.protocolosLocales().any { it.visitaId == idVisita }

    // ---------- Visitas (actas) ----------

    suspend fun visitas(idInstalacion: Long, usuario: Usuario): ResultadoRed<List<Visita>> =
        if (usuario.esCliente) {
            // El cliente las pide a reportes-service, que valida que la instalación sea de su empresa.
            llamarConCache<List<ActaDto>>("actas_${usuario.idUsuario}_$idInstalacion") {
                ApiClient.reportes.actas(usuario.idUsuario, idInstalacion)
            }.mapear { actas -> actas.map { it.visita.aVisita() } }
        } else {
            llamarConCache<List<VisitaDto>>("visitas_inst_$idInstalacion") {
                ApiClient.protocolos.visitas(idInstalacion)
            }.mapear { lista ->
                val locales = AlmacenLocal.visitasLocales().filter { it.instalacionId == idInstalacion }
                val ids = locales.map { it.id }.toSet()
                (lista.filter { it.id !in ids } + locales).map { it.aVisita() }
            }
        }

    suspend fun estadosDeActas(idInstalacion: Long, usuario: Usuario): Map<Long, EstadoActa> =
        (llamarConCache<List<ActaDto>>("actas_${usuario.idUsuario}_$idInstalacion") {
            ApiClient.reportes.actas(usuario.idUsuario, idInstalacion)
        } as? ResultadoRed.Exito)?.datos.orEmpty().associate { acta ->
            val vigentes = acta.protocolos.orEmpty().filter { it.estado != TipoEstadoProtocolo.ANULADO.name }
            acta.visita.id to EstadoActa(
                pendiente = vigentes.any {
                    it.estado == TipoEstadoProtocolo.REGISTRADO_TERRENO.name || it.estado == TipoEstadoProtocolo.OBSERVADO.name
                },
                formalizada = vigentes.isNotEmpty() && vigentes.all { it.estado == TipoEstadoProtocolo.FORMALIZADO.name },
                resultado = vigentes.mapNotNull { p -> p.resultadoGlobal?.let { TipoResultado.valueOf(it) } }.maxOrNull()
            )
        }

    private suspend fun visitaDto(idVisita: Long): ResultadoRed<VisitaDto> {
        AlmacenLocal.visitaLocal(idVisita)?.let { return ResultadoRed.Exito(it) }
        if (AlmacenLocal.esLocal(idVisita)) return ResultadoRed.ErrorServidor(404, "La visita ya no existe")
        return llamarConCache<VisitaDto>("visita_$idVisita") { ApiClient.protocolos.visita(idVisita) }
    }

    suspend fun visita(idVisita: Long): ResultadoRed<Visita> = visitaDto(idVisita).mapear { it.aVisita() }

    suspend fun recepcion(idVisita: Long): ResultadoRed<RecepcionActa?> =
        visitaDto(idVisita).mapear { it.recepcion?.aRecepcion(idVisita) }

    suspend fun participantes(idVisita: Long): ResultadoRed<List<Long>> =
        visitaDto(idVisita).mapear { it.participantesIds ?: emptyList() }

    // Busca una visita por su número de OT (el servidor la guarda en mayúsculas).
    suspend fun visitaPorOt(numeroOt: String): ResultadoRed<Visita?> =
        llamarApi { ApiClient.protocolos.visitaPorOt(numeroOt) }.mapear { lista ->
            lista.firstOrNull { it.numeroOt.equals(numeroOt, ignoreCase = true) }?.aVisita()
        }

    suspend fun crearVisita(visita: Visita, idsParticipantes: List<Long>, idActor: Long): ResultadoRed<Visita> {
        val solicitud = VisitaSolicitud(
            numeroOt = visita.numeroOt,
            instalacionId = visita.idInstalacion,
            fecha = visita.fecha.aTexto(),
            tipoVisita = visita.tipoVisita.name,
            ingenieroId = visita.idUsuario,
            participantesIds = idsParticipantes
        )
        val resultado = llamarApi { ApiClient.protocolos.crearVisita(idActor, solicitud) }
        if (resultado !is ResultadoRed.SinConexion) return resultado.mapear { it.aVisita() }

        // ----- Sin conexión: la visita queda en el teléfono y en la cola -----
        val existente = AlmacenLocal.visitasLocales().firstOrNull { it.numeroOt.equals(visita.numeroOt, true) }
        if (existente != null) {
            return if (existente.instalacionId == visita.idInstalacion) ResultadoRed.Exito(existente.aVisita())
            else ResultadoRed.ErrorServidor(409, "Ya existe una visita con ese número de OT")
        }
        val local = VisitaDto(
            id = AlmacenLocal.nuevoIdLocal(),
            numeroOt = visita.numeroOt,
            instalacionId = visita.idInstalacion,
            fecha = solicitud.fecha,
            tipoVisita = solicitud.tipoVisita,
            ingenieroId = solicitud.ingenieroId,
            participantesIds = idsParticipantes,
            fechaHoraCierre = null,
            cerrada = false,
            recepcion = null
        )
        AlmacenLocal.guardarVisitaLocal(local)
        encolar(TipoOperacion.CREAR_VISITA, idActor, "Crear la visita ${local.numeroOt}", refVisita = local.id, visita = solicitud)
        return ResultadoRed.Exito(local.aVisita())
    }

    suspend fun cerrarVisita(idVisita: Long, idActor: Long): ResultadoRed<Visita> {
        if (!visitaConCambiosLocales(idVisita)) {
            val r = llamarApi { ApiClient.protocolos.cerrarVisita(idActor, idVisita) }
            if (r !is ResultadoRed.SinConexion) return r.mapear { it.aVisita() }
        }
        val base = (visitaDto(idVisita) as? ResultadoRed.Exito)?.datos ?: return ResultadoRed.SinConexion
        val cerrada = base.cerradaLocal(ahora())
        AlmacenLocal.guardarVisitaLocal(cerrada)
        encolar(TipoOperacion.CERRAR_VISITA, idActor, "Cerrar la visita ${base.numeroOt}", refVisita = idVisita)
        return ResultadoRed.Exito(cerrada.aVisita())
    }

    // La recepción del acta es del cliente y exige que la visita ya esté en el servidor.
    suspend fun registrarRecepcion(idVisita: Long, recepcion: RecepcionSolicitud): ResultadoRed<RecepcionActa?> {
        if (visitaConCambiosLocales(idVisita)) {
            return ResultadoRed.ErrorServidor(0, "La visita tiene cambios pendientes de sincronizar. Conéctate a internet para registrar la recepción.")
        }
        return llamarApi { ApiClient.protocolos.registrarRecepcion(idVisita, recepcion) }
            .mapear { it.recepcion?.aRecepcion(idVisita) }
    }

    // ---------- Protocolos ----------

    // Si el protocolo se creó/cambió sin conexión se entrega la copia local; si no, el del servidor (o su caché).
    suspend fun protocoloDetalle(idProtocolo: Long): ResultadoRed<ProtocoloDto> {
        AlmacenLocal.protocoloLocal(idProtocolo)?.let { return ResultadoRed.Exito(it) }
        if (AlmacenLocal.esLocal(idProtocolo)) return ResultadoRed.ErrorServidor(404, "El protocolo ya no existe")
        return llamarConCache<ProtocoloDto>("protocolo_$idProtocolo") { ApiClient.protocolos.protocolo(idProtocolo) }
    }

    suspend fun protocolo(idProtocolo: Long): ResultadoRed<Protocolo> =
        protocoloDetalle(idProtocolo).mapear { it.aProtocolo() }

    private fun fusionarLocales(servidor: List<ProtocoloDto>, filtro: (ProtocoloDto) -> Boolean): List<ProtocoloDto> {
        val locales = AlmacenLocal.protocolosLocales().filter(filtro)
        val ids = locales.map { it.id }.toSet()
        return servidor.filter { it.id !in ids } + locales
    }

    suspend fun protocolosPorEquipo(idEquipo: Long, usuario: Usuario, tagEquipo: String): ResultadoRed<List<ResumenProtocolo>> {
        val resultado = if (usuario.esCliente)
            llamarConCache<HistorialEquipoDto>("hist_equipo_${usuario.idUsuario}_$idEquipo") {
                ApiClient.reportes.historialEquipo(usuario.idUsuario, idEquipo)
            }.mapear { it.protocolos ?: emptyList() }
        else llamarConCache<List<ProtocoloDto>>("prot_equipo_$idEquipo") { ApiClient.protocolos.protocolos(null, idEquipo) }
            .mapear { lista -> fusionarLocales(lista) { it.equipoId == idEquipo } }
        val nombres = usuarios.nombres()
        return resultado.mapear { lista ->
            lista.map { it.aResumen(tagEquipo, nombres[it.usuarioRegistraId] ?: "") }
        }
    }

    suspend fun protocolosPorVisita(
        idVisita: Long,
        idInstalacion: Long,
        usuario: Usuario
    ): ResultadoRed<List<ResumenProtocolo>> {
        val resultado: ResultadoRed<List<ProtocoloDto>> = when {
            usuario.esCliente ->
                llamarConCache<List<ActaDto>>("actas_${usuario.idUsuario}_$idInstalacion") {
                    ApiClient.reportes.actas(usuario.idUsuario, idInstalacion)
                }.mapear { actas -> actas.firstOrNull { it.visita.id == idVisita }?.protocolos ?: emptyList() }
            AlmacenLocal.esLocal(idVisita) -> ResultadoRed.Exito(emptyList())
            else -> llamarConCache<List<ProtocoloDto>>("prot_visita_$idVisita") { ApiClient.protocolos.protocolos(idVisita, null) }
        }.mapear { lista -> if (usuario.esCliente) lista else fusionarLocales(lista) { it.visitaId == idVisita } }
        val nombres = usuarios.nombres()
        val tags = tagsDeInstalacion(idInstalacion, usuario)
        return resultado.mapear { lista ->
            lista.map { it.aResumen(tags[it.equipoId] ?: "", nombres[it.usuarioRegistraId] ?: "") }
        }
    }

    private suspend fun tagsDeInstalacion(idInstalacion: Long, usuario: Usuario): Map<Long, String> =
        when (val r = llamarConCache<List<cl.induztek.app.network.EquipoDto>>(
            "equipos_${if (usuario.esCliente) usuario.idUsuario else 0}_$idInstalacion"
        ) { ApiClient.equipos.equipos(if (usuario.esCliente) usuario.idUsuario else null, idInstalacion) }) {
            is ResultadoRed.Exito -> r.datos.associate { it.id to it.tag }
            else -> emptyMap()
        }

    suspend fun crearProtocolo(idVisita: Long, idEquipo: Long, idActor: Long): ResultadoRed<Protocolo> {
        if (!AlmacenLocal.esLocal(idVisita)) {
            val r = llamarApi {
                ApiClient.protocolos.crearProtocolo(
                    idActor, ProtocoloSolicitud(idVisita, idEquipo, null, null, null, null, null, null)
                )
            }
            if (r !is ResultadoRed.SinConexion) return r.mapear { it.aProtocolo() }
        }
        // ----- Sin conexión -----
        AlmacenLocal.protocolosLocales().firstOrNull { it.visitaId == idVisita && it.equipoId == idEquipo }
            ?.let { return ResultadoRed.Exito(it.aProtocolo()) }
        val visita = (visitaDto(idVisita) as? ResultadoRed.Exito)?.datos
        val local = nuevoProtocoloLocal(AlmacenLocal.nuevoIdLocal(), idVisita, visita?.numeroOt, idEquipo, idActor, ahora())
        AlmacenLocal.guardarProtocoloLocal(local)
        encolar(
            TipoOperacion.CREAR_PROTOCOLO, idActor, "Crear protocolo de equipo en visita ${visita?.numeroOt ?: ""}",
            refVisita = idVisita, refProtocolo = local.id,
            protocolo = ProtocoloSolicitud(idVisita, idEquipo, null, null, null, null, null, null)
        )
        return ResultadoRed.Exito(local.aProtocolo())
    }

    // Guarda temperatura y humedad. Se reenvían descripción y observaciones actuales para no borrarlas.
    suspend fun actualizarCondiciones(
        protocolo: Protocolo,
        temperaturaC: Double,
        humedadPct: Double,
        idActor: Long
    ): ResultadoRed<Protocolo> {
        val id = protocolo.idProtocolo
        val solicitud = ProtocoloActualizacionSolicitud(
            null, temperaturaC, humedadPct, protocolo.descripcionMantenimiento, protocolo.observaciones
        )
        if (!tieneCambiosLocales(id)) {
            val r = llamarApi { ApiClient.protocolos.actualizarProtocolo(idActor, id, solicitud) }
            if (r !is ResultadoRed.SinConexion) return r.mapear { it.aProtocolo() }
        }
        val base = (protocoloDetalle(id) as? ResultadoRed.Exito)?.datos ?: return ResultadoRed.SinConexion
        val nuevo = base.conCondiciones(temperaturaC, humedadPct)
        AlmacenLocal.guardarProtocoloLocal(nuevo)
        encolar(
            TipoOperacion.ACTUALIZAR_CONDICIONES, idActor, "Condiciones ambientales de ${nuevo.numero}",
            refProtocolo = id, condiciones = solicitud
        )
        return ResultadoRed.Exito(nuevo.aProtocolo())
    }

    suspend fun registrarPrueba(
        idProtocolo: Long,
        solicitud: PruebaSolicitud,
        idActor: Long,
        contexto: ContextoPruebaLocal? = null
    ): ResultadoRed<PruebaRegistrada> {
        if (!tieneCambiosLocales(idProtocolo)) {
            val r = llamarApi { ApiClient.protocolos.registrarPrueba(idActor, idProtocolo, solicitud) }
            if (r !is ResultadoRed.SinConexion) return r.mapear { it.aPruebaRegistrada() }
        }
        val ctx = contexto ?: return ResultadoRed.SinConexion
        val base = (protocoloDetalle(idProtocolo) as? ResultadoRed.Exito)?.datos ?: return ResultadoRed.SinConexion
        val repetida = (base.pruebas ?: emptyList()).any {
            it.tipoPruebaId == solicitud.tipoPruebaId && it.elemento.equals(solicitud.elemento, true) && it.momento == solicitud.momento
        }
        if (repetida) return ResultadoRed.ErrorServidor(409, "Esa prueba ya está registrada para ese elemento y momento")
        val local = pruebaLocal(AlmacenLocal.nuevoIdLocal(), solicitud, ctx)
        AlmacenLocal.guardarProtocoloLocal(base.conPrueba(local))
        encolar(
            TipoOperacion.REGISTRAR_PRUEBA, idActor, "${base.numero}: ${ctx.tipo.nombre} · ${solicitud.elemento}",
            refProtocolo = idProtocolo, refPrueba = local.id, prueba = solicitud
        )
        return ResultadoRed.Exito(local.aPruebaRegistrada())
    }

    suspend fun eliminarPrueba(idProtocolo: Long, idPrueba: Long, idActor: Long): ResultadoRed<Unit> {
        if (!tieneCambiosLocales(idProtocolo) && !AlmacenLocal.esLocal(idPrueba)) {
            val r = llamarApi { ApiClient.protocolos.eliminarPrueba(idActor, idProtocolo, idPrueba) }
            if (r !is ResultadoRed.SinConexion) return r
        }
        val base = (protocoloDetalle(idProtocolo) as? ResultadoRed.Exito)?.datos ?: return ResultadoRed.SinConexion
        AlmacenLocal.guardarProtocoloLocal(base.sinPrueba(idPrueba))
        // Una prueba que nunca llegó al servidor simplemente se quita de la cola.
        if (AlmacenLocal.esLocal(idPrueba)) AlmacenLocal.quitarRegistroDePrueba(idPrueba)
        else encolar(
            TipoOperacion.ELIMINAR_PRUEBA, idActor, "${base.numero}: eliminar una prueba",
            refProtocolo = idProtocolo, refPrueba = idPrueba
        )
        return ResultadoRed.Exito(Unit)
    }

    suspend fun pruebas(idProtocolo: Long): ResultadoRed<List<PruebaRegistrada>> =
        protocoloDetalle(idProtocolo).mapear { p -> (p.pruebas ?: emptyList()).map { it.aPruebaRegistrada() } }

    suspend fun historialEstados(idProtocolo: Long): ResultadoRed<List<HistorialEstado>> =
        protocoloDetalle(idProtocolo).mapear { p -> (p.historial ?: emptyList()).map { it.aHistorial(idProtocolo) } }

    // Cambia el estado del protocolo con la acción que corresponde en el servidor.
    // "Registrar en terreno" también funciona sin conexión; formalizar/observar/anular solo con conexión.
    suspend fun cambiarEstado(
        idProtocolo: Long,
        nuevo: TipoEstadoProtocolo,
        idActor: Long,
        motivo: String? = null
    ): ResultadoRed<Protocolo> {
        if (nuevo == TipoEstadoProtocolo.REGISTRADO_TERRENO) {
            if (!tieneCambiosLocales(idProtocolo)) {
                val r = llamarApi { ApiClient.protocolos.registrarEnTerreno(idActor, idProtocolo) }
                if (r !is ResultadoRed.SinConexion) return r.mapear { it.aProtocolo() }
            }
            val base = (protocoloDetalle(idProtocolo) as? ResultadoRed.Exito)?.datos ?: return ResultadoRed.SinConexion
            val nuevoDto = base.registradoEnTerreno(idActor, ahora(), AlmacenLocal.nuevoIdLocal())
            AlmacenLocal.guardarProtocoloLocal(nuevoDto)
            encolar(
                TipoOperacion.REGISTRAR_TERRENO, idActor, "Registrar en terreno ${base.numero}",
                refProtocolo = idProtocolo
            )
            return ResultadoRed.Exito(nuevoDto.aProtocolo())
        }
        if (tieneCambiosLocales(idProtocolo)) {
            return ResultadoRed.ErrorServidor(
                0, "Este protocolo tiene cambios pendientes de sincronizar. Espera a que se envíen para continuar."
            )
        }
        val resultado = llamarApi {
            when (nuevo) {
                TipoEstadoProtocolo.FORMALIZADO -> ApiClient.protocolos.formalizar(idActor, idProtocolo)
                TipoEstadoProtocolo.OBSERVADO ->
                    ApiClient.protocolos.observar(idActor, idProtocolo, MotivoSolicitud(motivo ?: ""))
                TipoEstadoProtocolo.ANULADO ->
                    ApiClient.protocolos.anular(idActor, idProtocolo, MotivoSolicitud(motivo ?: ""))
                else -> throw IllegalArgumentException("Estado no permitido: $nuevo")
            }
        }
        return resultado.mapear { it.aProtocolo() }
    }

    // Id del protocolo que ya existe para ese equipo en esa visita (tras un 409 al crearlo).
    suspend fun idProtocoloExistente(idVisita: Long, idEquipo: Long): Long? =
        when (val r = llamarApi { ApiClient.protocolos.protocolos(idVisita, idEquipo) }) {
            is ResultadoRed.Exito -> r.datos.firstOrNull { it.equipoId == idEquipo }?.id
            else -> null
        }

    // ---------- Catálogos ----------

    suspend fun instrumentosVigentes(): ResultadoRed<List<Instrumento>> =
        llamarConCache<List<InstrumentoDto>>("instrumentos_vigentes") { ApiClient.protocolos.instrumentos(true) }
            .mapear { lista -> lista.map { it.aInstrumento() } }

    private suspend fun tiposPruebaDto(idTipoEquipo: Long): ResultadoRed<List<TipoPruebaDto>> =
        llamarConCache<List<TipoPruebaDto>>("tipos_prueba_$idTipoEquipo") { ApiClient.protocolos.tiposPrueba(idTipoEquipo) }

    suspend fun pruebasDeTipoEquipo(idTipoEquipo: Long): ResultadoRed<List<TipoPrueba>> =
        tiposPruebaDto(idTipoEquipo).mapear { lista -> lista.map { it.aTipoPrueba() } }

    suspend fun parametrosDeTipoPrueba(idTipoEquipo: Long, idTipoPrueba: Long): ResultadoRed<List<Parametro>> =
        tiposPruebaDto(idTipoEquipo).mapear { lista ->
            lista.firstOrNull { it.id == idTipoPrueba }?.parametros?.map { it.aParametro(idTipoPrueba) } ?: emptyList()
        }

    // ---------- Historial de valores de un equipo (para comparar con la medición anterior) ----------

    // Devuelve cada serie (prueba + elemento + parámetro) con sus puntos, del más reciente al más antiguo.
    // Cuenta solo pruebas realizadas, que no sean "previas" al mantenimiento, de protocolos no anulados.
    suspend fun seriesDeEquipo(idEquipo: Long): ResultadoRed<List<Pair<SerieMedicion, List<PuntoHistorial>>>> =
        llamarConCache<List<ProtocoloDto>>("prot_equipo_$idEquipo") { ApiClient.protocolos.protocolos(null, idEquipo) }
            .mapear { protocolos ->
                val puntos = linkedMapOf<SerieMedicion, MutableList<PuntoHistorial>>()
                protocolos.filter { it.estado != TipoEstadoProtocolo.ANULADO.name }.forEach { p ->
                    (p.pruebas ?: emptyList()).filter { it.realizada && it.momento != "PREVIA" }.forEach { prueba ->
                        (prueba.mediciones ?: emptyList()).forEach { m ->
                            val serie = SerieMedicion(
                                codigoTipoPrueba = prueba.tipoPruebaCodigo,
                                nombreTipoPrueba = prueba.tipoPruebaNombre ?: prueba.tipoPruebaCodigo,
                                elemento = prueba.elemento,
                                codigoParametro = m.parametroCodigo,
                                nombreParametro = m.parametroNombre ?: m.parametroCodigo,
                                unidad = m.unidad ?: ""
                            )
                            puntos.getOrPut(serie) { mutableListOf() }.add(
                                PuntoHistorial(p.id, p.numero, p.aProtocolo().fechaHoraPrueba, m.valor)
                            )
                        }
                    }
                }
                puntos.map { (serie, lista) -> serie to lista.sortedByDescending { it.fechaHoraPrueba } }
            }

    fun hoy(): LocalDate = LocalDate.now()
}

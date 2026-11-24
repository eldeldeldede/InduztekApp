package cl.induztek.app.repository

import cl.induztek.app.data.aFecha
import cl.induztek.app.data.aFechaHora
import cl.induztek.app.model.ClasificacionEquipo
import cl.induztek.app.model.Equipo
import cl.induztek.app.model.HistorialEstado
import cl.induztek.app.model.Instalacion
import cl.induztek.app.model.Instrumento
import cl.induztek.app.model.Momento
import cl.induztek.app.model.Parametro
import cl.induztek.app.model.Protocolo
import cl.induztek.app.model.PruebaRegistrada
import cl.induztek.app.model.RecepcionActa
import cl.induztek.app.model.ResumenProtocolo
import cl.induztek.app.model.TipoEquipo
import cl.induztek.app.model.TipoEstadoProtocolo
import cl.induztek.app.model.TipoPrueba
import cl.induztek.app.model.TipoResultado
import cl.induztek.app.model.TipoVisita
import cl.induztek.app.model.Visita
import cl.induztek.app.network.EquipoDto
import cl.induztek.app.network.HistorialDto
import cl.induztek.app.network.InstalacionDto
import cl.induztek.app.network.InstrumentoDto
import cl.induztek.app.network.ParametroDto
import cl.induztek.app.network.ProtocoloDto
import cl.induztek.app.network.PruebaDto
import cl.induztek.app.network.RecepcionDto
import cl.induztek.app.network.TipoEquipoDto
import cl.induztek.app.network.TipoPruebaDto
import cl.induztek.app.network.VisitaDto
import cl.induztek.app.validacion.Unidades

// =====================================================================
//  Mapeos.kt — convierte lo que llega del servidor (DTOs) en los modelos que usa la app
//  Así las pantallas y ViewModels siguen trabajando con Equipo, Protocolo, Visita..., sin saber
//  cómo viaja el JSON. Es el "traductor" de la capa Repository.
// =====================================================================

fun InstalacionDto.aInstalacion(clienteNombre: String = ""): Instalacion = Instalacion(
    idInstalacion = id,
    idUsuario = clienteId,
    nombre = nombre,
    ubicacion = ubicacion ?: "",
    idComuna = comunaId,
    observacionesAcceso = observacionesAcceso,
    comunaNombre = comunaNombre ?: "",
    clienteNombre = clienteNombre
)

fun EquipoDto.aEquipo(): Equipo = Equipo(
    idEquipo = id,
    idInstalacion = instalacionId,
    tag = tag,
    idTipoEquipo = tipoEquipoId,
    marca = marca,
    modelo = modelo,
    numeroSerie = numeroSerie,
    anioFabricacion = anioFabricacion,
    potenciaKva = potenciaKva,
    tensionPrimariaKv = tensionPrimariaKv,
    tensionSecundariaKv = tensionSecundariaKv,
    corrienteNominalA = corrienteNominalA,
    maxContactosUohm = maxContactosUohm,
    observaciones = observaciones,
    tipoEquipoNombre = tipoEquipoNombre ?: ""
)

fun TipoEquipoDto.aTipoEquipo(): TipoEquipo = TipoEquipo(
    idTipoEquipo = id,
    nombre = nombre,
    clasificacion = if (clasificacion == "SECUNDARIO") ClasificacionEquipo.SECUNDARIO else ClasificacionEquipo.PRIMARIO
)

fun TipoPruebaDto.aTipoPrueba(): TipoPrueba = TipoPrueba(idTipoPrueba = id, codigo = codigo, nombre = nombre)

fun ParametroDto.aParametro(idTipoPrueba: Long): Parametro = Parametro(
    idParametro = id,
    idTipoPrueba = idTipoPrueba,
    codigo = codigo,
    nombre = nombre,
    unidad = unidad ?: "—"
)

fun InstrumentoDto.aInstrumento(): Instrumento = Instrumento(
    idInstrumento = id,
    nombre = nombre,
    marca = marca,
    modelo = modelo,
    numeroSerie = numeroSerie
)

fun VisitaDto.aVisita(): Visita = Visita(
    idVisita = id,
    numeroOt = numeroOt,
    idInstalacion = instalacionId,
    fecha = fecha.aFecha(),
    tipoVisita = TipoVisita.valueOf(tipoVisita),
    idUsuario = ingenieroId,
    fechaHoraCierre = fechaHoraCierre?.aFechaHora()
)

fun RecepcionDto.aRecepcion(idVisita: Long): RecepcionActa = RecepcionActa(
    idVisita = idVisita,
    receptorNombre = receptorNombre,
    receptorRut = receptorRut,
    receptorCargo = receptorCargo,
    fechaHoraRecepcion = fechaHoraRecepcion.aFechaHora()
)

fun ProtocoloDto.aProtocolo(): Protocolo = Protocolo(
    idProtocolo = id,
    uuid = numero,
    numero = numero,
    idVisita = visitaId,
    idEquipo = equipoId,
    idUsuario = usuarioRegistraId,
    fechaHoraPrueba = fechaHoraPrueba.aFechaHora(),
    temperaturaAmbiente = temperaturaC,
    pctHumedad = humedadPct,
    descripcionMantenimiento = descripcionMantenimiento,
    observaciones = observaciones,
    idEstadoProtocolo = TipoEstadoProtocolo.valueOf(estado).id,
    idProtocoloReemplazado = protocoloReemplazadoId
)

fun ProtocoloDto.aResumen(tagEquipo: String, tecnicoNombre: String): ResumenProtocolo = ResumenProtocolo(
    idProtocolo = id,
    numero = numero,
    idEquipo = equipoId,
    tagEquipo = tagEquipo,
    fechaHoraPrueba = fechaHoraPrueba.aFechaHora(),
    estado = TipoEstadoProtocolo.valueOf(estado),
    resultadoGlobal = resultadoGlobal?.let { TipoResultado.valueOf(it) },
    tecnicoNombre = tecnicoNombre
)

fun HistorialDto.aHistorial(idProtocolo: Long): HistorialEstado = HistorialEstado(
    idHistorial = id,
    idProtocolo = idProtocolo,
    idEstadoAnterior = estadoAnterior?.let { TipoEstadoProtocolo.valueOf(it).id },
    idEstadoNuevo = TipoEstadoProtocolo.valueOf(estadoNuevo).id,
    idUsuario = usuarioId,
    fechaHora = fechaHora.aFechaHora(),
    motivo = motivo
)

// Una prueba del servidor como la muestra la pantalla de mediciones: con sus líneas "Parámetro: valor unidad".
fun PruebaDto.aPruebaRegistrada(): PruebaRegistrada = PruebaRegistrada(
    idPrueba = id,
    nombreTipoPrueba = tipoPruebaNombre ?: tipoPruebaCodigo,
    elemento = elemento,
    momento = Momento.valueOf(momento),
    realizada = realizada,
    motivoNoRealizada = motivoNoRealizada,
    resultado = resultado?.let { TipoResultado.valueOf(it) },
    lineas = (mediciones ?: emptyList()).map { m ->
        val unidad = if (m.unidad.isNullOrBlank() || m.unidad == "—" || m.unidad == "-") "" else " ${m.unidad}"
        "${m.parametroNombre ?: m.parametroCodigo}: ${Unidades.formatearLegible(m.valor)}$unidad"
    }
)

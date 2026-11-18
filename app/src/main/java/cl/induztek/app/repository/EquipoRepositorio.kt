package cl.induztek.app.repository

import cl.induztek.app.model.Comuna
import cl.induztek.app.model.Equipo
import cl.induztek.app.model.Instalacion
import cl.induztek.app.model.TipoEquipo
import cl.induztek.app.model.Usuario
import cl.induztek.app.network.ApiClient
import cl.induztek.app.network.EquipoSolicitud
import cl.induztek.app.network.InstalacionSolicitud
import cl.induztek.app.network.ResultadoRed
import cl.induztek.app.network.EquipoDto
import cl.induztek.app.network.InstalacionDto
import cl.induztek.app.network.TipoEquipoDto
import cl.induztek.app.network.llamarApi
import cl.induztek.app.network.llamarConCache
import cl.induztek.app.network.mapear

// =====================================================================
//  EquipoRepositorio.kt — instalaciones, equipos y catálogos (equipos-service)
//  Si quien consulta es CLIENTE se pasa su id como `clienteId`: el servidor devuelve solo lo de su empresa.
// =====================================================================
class EquipoRepositorio(private val usuarios: UsuarioRepositorio) {

    private fun clienteDe(usuario: Usuario): Long? = if (usuario.esCliente) usuario.idUsuario else null

    suspend fun instalaciones(usuario: Usuario): ResultadoRed<List<Instalacion>> {
        val resultado = llamarConCache<List<InstalacionDto>>("instalaciones_${clienteDe(usuario) ?: 0}") {
            ApiClient.equipos.instalaciones(clienteDe(usuario))
        }
        if (resultado !is ResultadoRed.Exito) return resultado.mapear { emptyList() }
        // El personal ve a todas las empresas: se completa el nombre de cada una para mostrarlo y buscarlo.
        val nombres = if (usuario.esCliente) mapOf(usuario.idUsuario to usuario.nombre) else usuarios.nombres()
        return ResultadoRed.Exito(resultado.datos.map { it.aInstalacion(nombres[it.clienteId] ?: "") })
    }

    suspend fun instalacion(idInstalacion: Long, usuario: Usuario): ResultadoRed<Instalacion> {
        val resultado = llamarConCache<InstalacionDto>("instalacion_${clienteDe(usuario) ?: 0}_$idInstalacion") {
            ApiClient.equipos.instalacion(idInstalacion, clienteDe(usuario))
        }
        if (resultado !is ResultadoRed.Exito) return resultado.mapear { throw IllegalStateException() }
        val nombre = if (usuario.esCliente) usuario.nombre
        else usuarios.nombres()[resultado.datos.clienteId] ?: ""
        return ResultadoRed.Exito(resultado.datos.aInstalacion(nombre))
    }

    suspend fun equipos(idInstalacion: Long, usuario: Usuario): ResultadoRed<List<Equipo>> =
        llamarConCache<List<EquipoDto>>("equipos_${clienteDe(usuario) ?: 0}_$idInstalacion") {
            ApiClient.equipos.equipos(clienteDe(usuario), idInstalacion)
        }
            .mapear { lista -> lista.map { it.aEquipo() } }

    suspend fun equipo(idEquipo: Long, usuario: Usuario): ResultadoRed<Equipo> =
        llamarConCache<EquipoDto>("equipo_${clienteDe(usuario) ?: 0}_$idEquipo") {
            ApiClient.equipos.equipo(idEquipo, clienteDe(usuario))
        }.mapear { it.aEquipo() }

    suspend fun crearEquipo(equipo: Equipo): ResultadoRed<Equipo> =
        llamarApi {
            ApiClient.equipos.crearEquipo(
                EquipoSolicitud(
                    instalacionId = equipo.idInstalacion,
                    tag = equipo.tag,
                    tipoEquipoId = equipo.idTipoEquipo,
                    marca = equipo.marca,
                    modelo = equipo.modelo,
                    numeroSerie = equipo.numeroSerie,
                    anioFabricacion = equipo.anioFabricacion,
                    potenciaKva = equipo.potenciaKva,
                    tensionPrimariaKv = equipo.tensionPrimariaKv,
                    tensionSecundariaKv = equipo.tensionSecundariaKv,
                    corrienteNominalA = equipo.corrienteNominalA,
                    maxContactosUohm = equipo.maxContactosUohm,
                    observaciones = equipo.observaciones
                )
            )
        }.mapear { it.aEquipo() }

    suspend fun crearInstalacion(solicitud: InstalacionSolicitud): ResultadoRed<Instalacion> =
        llamarApi { ApiClient.equipos.crearInstalacion(solicitud) }.mapear { it.aInstalacion() }

    suspend fun actualizarInstalacion(idInstalacion: Long, solicitud: InstalacionSolicitud): ResultadoRed<Instalacion> =
        llamarApi { ApiClient.equipos.actualizarInstalacion(idInstalacion, solicitud) }.mapear { it.aInstalacion() }

    suspend fun tiposEquipo(): ResultadoRed<List<TipoEquipo>> =
        llamarConCache<List<TipoEquipoDto>>("tipos_equipo") { ApiClient.equipos.tiposEquipo() }.mapear { lista -> lista.map { it.aTipoEquipo() } }

    suspend fun comunas(): ResultadoRed<List<Comuna>> =
        llamarApi { ApiClient.equipos.comunas() }.mapear { lista ->
            lista.map { Comuna(idComuna = it.id, idRegion = it.regionId, nombre = it.nombre) }
        }

    // Id del equipo que ya tiene ese TAG en la instalación (para ofrecer "ver el equipo existente" tras un 409).
    suspend fun idEquipoPorTag(idInstalacion: Long, tag: String): Long? =
        when (val r = llamarApi { ApiClient.equipos.equipos(null, idInstalacion) }) {
            is ResultadoRed.Exito -> r.datos.firstOrNull { it.tag.equals(tag, ignoreCase = true) }?.id
            else -> null
        }
}

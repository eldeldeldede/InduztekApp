package cl.induztek.app.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import cl.induztek.app.model.ClasificacionEquipo
import cl.induztek.app.model.CodigoRol
import cl.induztek.app.model.Calibracion
import cl.induztek.app.model.Comuna
import cl.induztek.app.model.Equipo
import cl.induztek.app.model.HistorialEstado
import cl.induztek.app.model.Instalacion
import cl.induztek.app.model.Instrumento
import cl.induztek.app.model.Medicion
import cl.induztek.app.model.Momento
import cl.induztek.app.model.Parametro
import cl.induztek.app.model.Protocolo
import cl.induztek.app.model.Prueba
import cl.induztek.app.model.PuntoHistorial
import cl.induztek.app.model.RecepcionActa
import cl.induztek.app.model.SerieMedicion
import cl.induztek.app.model.ResultadoGuardado
import cl.induztek.app.model.ResultadoLogin
import cl.induztek.app.model.ResumenProtocolo
import cl.induztek.app.model.TipoEquipo
import cl.induztek.app.model.TipoEstadoProtocolo
import cl.induztek.app.model.TipoPrueba
import cl.induztek.app.model.TipoResultado
import cl.induztek.app.model.TipoVisita
import cl.induztek.app.model.Usuario
import cl.induztek.app.model.Visita
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class InduztekDbHelper(context: Context) :
    SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {

    companion object {
        const val NOMBRE_BD = "induztek.db"

        const val VERSION_BD = 2

        const val CLAVE_DEMO = "Induztek2026"

        private const val SQL_USUARIO_POR_CORREO = """
            SELECT u.*, r.codigo AS rol_codigo
            FROM usuario u
            INNER JOIN rol r ON r.id_rol = u.id_rol
            WHERE lower(u.correo) = lower(?)
        """

        private const val SQL_USUARIO_POR_ID = """
            SELECT u.*, r.codigo AS rol_codigo
            FROM usuario u
            INNER JOIN rol r ON r.id_rol = u.id_rol
            WHERE u.id_usuario = ?
        """

        private const val SQL_USUARIOS_POR_ROL = """
            SELECT u.*, r.codigo AS rol_codigo
            FROM usuario u
            INNER JOIN rol r ON r.id_rol = u.id_rol
            WHERE r.codigo = ? AND u.activo = 1
            ORDER BY u.nombre
        """

        private const val SQL_INSTALACIONES = """
            SELECT i.*, c.nombre AS comuna_nombre, u.nombre AS cliente_nombre
            FROM instalacion i
            INNER JOIN comuna c ON c.id_comuna = i.id_comuna
            INNER JOIN usuario u ON u.id_usuario = i.id_usuario_cliente
            WHERE (CAST(? AS INTEGER) = 0 OR i.id_usuario_cliente = ?)
            ORDER BY i.nombre
        """

        private const val SQL_INSTALACION_POR_ID = """
            SELECT i.*, c.nombre AS comuna_nombre, u.nombre AS cliente_nombre
            FROM instalacion i
            INNER JOIN comuna c ON c.id_comuna = i.id_comuna
            INNER JOIN usuario u ON u.id_usuario = i.id_usuario_cliente
            WHERE (CAST(? AS INTEGER) = 0 OR i.id_usuario_cliente = ?)
              AND i.id_instalacion = ?
        """

        private const val SQL_EQUIPOS_POR_INSTALACION = """
            SELECT e.*, t.nombre AS tipo_equipo_nombre
            FROM equipo e
            INNER JOIN tipo_equipo t ON t.id_tipo_equipo = e.id_tipo_equipo
            INNER JOIN instalacion i ON i.id_instalacion = e.id_instalacion
            WHERE e.id_instalacion = ?
              AND (CAST(? AS INTEGER) = 0 OR i.id_usuario_cliente = ?)
            ORDER BY e.tag
        """

        private const val SQL_EQUIPO_POR_ID = """
            SELECT e.*, t.nombre AS tipo_equipo_nombre
            FROM equipo e
            INNER JOIN tipo_equipo t ON t.id_tipo_equipo = e.id_tipo_equipo
            INNER JOIN instalacion i ON i.id_instalacion = e.id_instalacion
            WHERE e.id_equipo = ?
              AND (CAST(? AS INTEGER) = 0 OR i.id_usuario_cliente = ?)
        """

        private const val SQL_EQUIPO_POR_TAG = """
            SELECT e.*, t.nombre AS tipo_equipo_nombre
            FROM equipo e
            INNER JOIN tipo_equipo t ON t.id_tipo_equipo = e.id_tipo_equipo
            WHERE e.id_instalacion = ? AND e.tag = ?
        """

        private const val SQL_TIPOS_EQUIPO = "SELECT * FROM tipo_equipo ORDER BY id_tipo_equipo"

        private const val SQL_PRUEBAS_DE_TIPO_EQUIPO = """
            SELECT tp.*
            FROM tipo_prueba tp
            INNER JOIN tipo_equipo_prueba tep ON tep.id_tipo_prueba = tp.id_tipo_prueba
            WHERE tep.id_tipo_equipo = ?
            ORDER BY tp.id_tipo_prueba
        """

        private const val SQL_PARAMETROS_DE_TIPO_PRUEBA =
            "SELECT * FROM parametro WHERE id_tipo_prueba = ? ORDER BY id_parametro"

        private const val SQL_COMUNAS = "SELECT * FROM comuna ORDER BY nombre"

        private const val SQL_INSTRUMENTOS = "SELECT * FROM instrumento ORDER BY nombre"

        private const val SQL_INSTRUMENTOS_VIGENTES = """
            SELECT DISTINCT i.*
            FROM instrumento i
            INNER JOIN calibracion c ON c.id_instrumento = i.id_instrumento
            WHERE c.fecha_calibracion <= ? AND c.fecha_vencimiento >= ?
            ORDER BY i.nombre
        """

        private const val SQL_CALIBRACION_VIGENTE = """
            SELECT * FROM calibracion
            WHERE id_instrumento = ? AND fecha_calibracion <= ? AND fecha_vencimiento >= ?
            ORDER BY fecha_vencimiento DESC
            LIMIT 1
        """

        private const val SQL_ULTIMA_CALIBRACION = """
            SELECT * FROM calibracion
            WHERE id_instrumento = ?
            ORDER BY fecha_vencimiento DESC
            LIMIT 1
        """

        private const val SQL_VISITA_POR_ID = "SELECT * FROM visita WHERE id_visita = ?"
        private const val SQL_VISITA_POR_OT = "SELECT * FROM visita WHERE numero_ot = ?"

        private const val SQL_VISITAS_POR_INSTALACION = """
            SELECT v.*
            FROM visita v
            INNER JOIN instalacion i ON i.id_instalacion = v.id_instalacion
            WHERE v.id_instalacion = ?
              AND (CAST(? AS INTEGER) = 0
                   OR (i.id_usuario_cliente = ? AND v.fecha_hora_cierre IS NOT NULL))
            ORDER BY v.fecha DESC, v.id_visita DESC
        """

        private const val SQL_RECEPCION = "SELECT * FROM recepcion_acta WHERE id_visita = ?"

        private const val SQL_PARTICIPANTES = """
            SELECT u.nombre
            FROM visita_participante vp
            INNER JOIN usuario u ON u.id_usuario = vp.id_usuario
            WHERE vp.id_visita = ?
            ORDER BY u.nombre
        """

        private const val SQL_PROTOCOLO_POR_ID = "SELECT * FROM protocolo WHERE id_protocolo = ?"

        private const val SQL_PROTOCOLO_EXISTENTE =
            "SELECT id_protocolo FROM protocolo WHERE id_visita = ? AND id_equipo = ?"

        private const val SQL_CONTAR_NUMEROS = "SELECT COUNT(*) FROM protocolo WHERE numero LIKE ?"
        private const val SQL_ESTADO_ACTUAL = "SELECT id_estado FROM protocolo WHERE id_protocolo = ?"

        private const val SQL_HISTORIAL_ESTADOS =
            "SELECT * FROM historial_estado WHERE id_protocolo = ? ORDER BY fecha_hora, id_historial"

        private const val SQL_RESUMEN_POR_EQUIPO = """
            SELECT p.id_protocolo, p.numero, p.id_equipo, e.tag AS tag_equipo,
                   p.fecha_hora_prueba, p.id_estado,
                   (SELECT MAX(pr.id_resultado) FROM prueba pr
                     WHERE pr.id_protocolo = p.id_protocolo
                       AND pr.realizada = 1 AND pr.momento <> 'PREVIA') AS id_resultado_global,
                   u.nombre AS tecnico_nombre
            FROM protocolo p
            INNER JOIN equipo e ON e.id_equipo = p.id_equipo
            INNER JOIN instalacion i ON i.id_instalacion = e.id_instalacion
            INNER JOIN usuario u ON u.id_usuario = p.id_usuario_registra
            WHERE p.id_equipo = ?
              AND (CAST(? AS INTEGER) = 0
                   OR (i.id_usuario_cliente = ? AND p.id_estado IN (2, 3, 4)))
            ORDER BY p.fecha_hora_prueba DESC
        """

        private const val SQL_RESUMEN_POR_VISITA = """
            SELECT p.id_protocolo, p.numero, p.id_equipo, e.tag AS tag_equipo,
                   p.fecha_hora_prueba, p.id_estado,
                   (SELECT MAX(pr.id_resultado) FROM prueba pr
                     WHERE pr.id_protocolo = p.id_protocolo
                       AND pr.realizada = 1 AND pr.momento <> 'PREVIA') AS id_resultado_global,
                   u.nombre AS tecnico_nombre
            FROM protocolo p
            INNER JOIN equipo e ON e.id_equipo = p.id_equipo
            INNER JOIN instalacion i ON i.id_instalacion = e.id_instalacion
            INNER JOIN usuario u ON u.id_usuario = p.id_usuario_registra
            WHERE p.id_visita = ?
              AND (CAST(? AS INTEGER) = 0
                   OR (i.id_usuario_cliente = ? AND p.id_estado IN (2, 3, 4)))
            ORDER BY e.tag
        """

        private const val SQL_ULTIMOS_DE_INSTALACION = """
            SELECT p.id_protocolo, p.numero, p.id_equipo, e.tag AS tag_equipo,
                   p.fecha_hora_prueba, p.id_estado,
                   (SELECT MAX(pr.id_resultado) FROM prueba pr
                     WHERE pr.id_protocolo = p.id_protocolo
                       AND pr.realizada = 1 AND pr.momento <> 'PREVIA') AS id_resultado_global,
                   u.nombre AS tecnico_nombre
            FROM protocolo p
            INNER JOIN equipo e ON e.id_equipo = p.id_equipo
            INNER JOIN usuario u ON u.id_usuario = p.id_usuario_registra
            WHERE e.id_instalacion = ?
              AND p.id_estado IN (2, 3, 4)
              AND p.fecha_hora_prueba = (SELECT MAX(p2.fecha_hora_prueba) FROM protocolo p2
                                          WHERE p2.id_equipo = p.id_equipo AND p2.id_estado IN (2, 3, 4))
            ORDER BY e.tag
        """

        private const val SQL_PRUEBAS_POR_PROTOCOLO =
            "SELECT * FROM prueba WHERE id_protocolo = ? ORDER BY id_prueba"

        private const val SQL_MEDICIONES = "SELECT * FROM medicion WHERE id_prueba = ? ORDER BY id_medicion"

        private const val SQL_MEDICIONES_CON_CODIGO = """
            SELECT pa.codigo AS codigo, m.valor AS valor
            FROM medicion m
            INNER JOIN parametro pa ON pa.id_parametro = m.id_parametro
            WHERE m.id_prueba = ?
        """

        private const val SQL_SERIES_DE_EQUIPO = """
            SELECT DISTINCT tp.codigo AS tipo_prueba_codigo, tp.nombre AS tipo_prueba_nombre,
                            pr.elemento AS elemento, pa.codigo AS parametro_codigo,
                            pa.nombre AS parametro_nombre, pa.unidad AS unidad,
                            tp.id_tipo_prueba AS orden_prueba, pa.id_parametro AS orden_parametro
            FROM medicion m
            INNER JOIN prueba pr ON pr.id_prueba = m.id_prueba
            INNER JOIN protocolo p ON p.id_protocolo = pr.id_protocolo
            INNER JOIN tipo_prueba tp ON tp.id_tipo_prueba = pr.id_tipo_prueba
            INNER JOIN parametro pa ON pa.id_parametro = m.id_parametro
            WHERE p.id_equipo = ? AND pr.realizada = 1 AND pr.momento <> 'PREVIA' AND p.id_estado <> 5
            ORDER BY tp.id_tipo_prueba, pr.elemento, pa.id_parametro
        """

        private const val SQL_PRUEBA_EXISTENTE = """
            SELECT id_prueba FROM prueba
            WHERE id_protocolo = ? AND id_tipo_prueba = ? AND elemento = ? AND momento = ?
        """

        private const val SQL_HISTORIAL_PARAMETRO = """
            SELECT p.id_protocolo, p.numero, p.fecha_hora_prueba, m.valor
            FROM medicion m
            INNER JOIN prueba pr ON pr.id_prueba = m.id_prueba
            INNER JOIN protocolo p ON p.id_protocolo = pr.id_protocolo
            INNER JOIN tipo_prueba tp ON tp.id_tipo_prueba = pr.id_tipo_prueba
            INNER JOIN parametro pa ON pa.id_parametro = m.id_parametro
            WHERE p.id_equipo = ? AND tp.codigo = ? AND pr.elemento = ? AND pa.codigo = ?
              AND pr.realizada = 1 AND pr.momento <> 'PREVIA' AND p.id_estado <> 5
            ORDER BY p.fecha_hora_prueba DESC
        """
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {

        Esquema.TABLAS.forEach { (_, sql) -> db.execSQL(sql) }
        Esquema.INDICES.forEach { db.execSQL(it) }

        val hashDemo = Seguridad.sha256(CLAVE_DEMO)
        DatosIniciales.SENTENCIAS.forEach { sql ->
            db.execSQL(sql.replace("%HASH%", hashDemo))
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Esquema.TABLAS.map { it.first }.reversed().forEach { nombre ->
            db.execSQL("DROP TABLE IF EXISTS $nombre")
        }
        onCreate(db)
    }

    fun iniciarSesion(correo: String, clave: String): ResultadoLogin {
        val usuario = consultar(SQL_USUARIO_POR_CORREO, listOf(correo.trim()), leer = ::leerUsuario).firstOrNull()
        return when {
            usuario == null || usuario.passwordHash != Seguridad.sha256(clave) ->
                ResultadoLogin.CredencialesInvalidas
            !usuario.activo -> ResultadoLogin.CuentaInactiva
            else -> ResultadoLogin.Exito(usuario)
        }
    }

    fun buscarUsuarioPorId(idUsuario: Long): Usuario? =
        consultar(SQL_USUARIO_POR_ID, listOf(idUsuario), leer = ::leerUsuario).firstOrNull()

    fun listarUsuariosActivosPorRol(rol: CodigoRol): List<Usuario> =
        consultar(SQL_USUARIOS_POR_ROL, listOf(rol.codigo), leer = ::leerUsuario)

    fun listarInstalaciones(usuario: Usuario): List<Instalacion> =
        consultar(SQL_INSTALACIONES, filtroCliente(usuario), leer = ::leerInstalacion)

    fun buscarInstalacion(idInstalacion: Long, usuario: Usuario): Instalacion? =
        consultar(SQL_INSTALACION_POR_ID, filtroCliente(usuario) + idInstalacion, leer = ::leerInstalacion)
            .firstOrNull()

    fun listarEquipos(idInstalacion: Long, usuario: Usuario): List<Equipo> =
        consultar(SQL_EQUIPOS_POR_INSTALACION, listOf<Any>(idInstalacion) + filtroCliente(usuario), leer = ::leerEquipo)

    fun buscarEquipo(idEquipo: Long, usuario: Usuario): Equipo? =
        consultar(SQL_EQUIPO_POR_ID, listOf<Any>(idEquipo) + filtroCliente(usuario), leer = ::leerEquipo).firstOrNull()

    fun insertarEquipo(equipo: Equipo): ResultadoGuardado {

        val tag = equipo.tag.trim().uppercase()

        val existente = consultar(SQL_EQUIPO_POR_TAG, listOf(equipo.idInstalacion, tag), leer = ::leerEquipo)
            .firstOrNull()
        if (existente != null) return ResultadoGuardado.Duplicado(existente.idEquipo)

        val id = writableDatabase.insert("equipo", null, valoresDe(equipo.copy(tag = tag)))
        return if (id == -1L) ResultadoGuardado.Fallo else ResultadoGuardado.Creado(id)
    }

    fun listarTiposEquipo(): List<TipoEquipo> = consultar(SQL_TIPOS_EQUIPO) { c ->
        TipoEquipo(
            idTipoEquipo = c.largo("id_tipo_equipo"),
            nombre = c.texto("nombre"),

            clasificacion = ClasificacionEquipo.valueOf(c.texto("clasificacion"))
        )
    }

    fun listarPruebasDeTipoEquipo(idTipoEquipo: Long): List<TipoPrueba> =
        consultar(SQL_PRUEBAS_DE_TIPO_EQUIPO, listOf(idTipoEquipo)) { c ->
            TipoPrueba(
                idTipoPrueba = c.largo("id_tipo_prueba"),
                codigo = c.texto("codigo"),
                nombre = c.texto("nombre")
            )
        }

    fun listarParametrosDeTipoPrueba(idTipoPrueba: Long): List<Parametro> =
        consultar(SQL_PARAMETROS_DE_TIPO_PRUEBA, listOf(idTipoPrueba)) { c ->
            Parametro(
                idParametro = c.largo("id_parametro"),
                idTipoPrueba = c.largo("id_tipo_prueba"),
                codigo = c.texto("codigo"),
                nombre = c.texto("nombre"),
                unidad = c.texto("unidad")
            )
        }

    fun listarComunas(): List<Comuna> = consultar(SQL_COMUNAS) { c ->
        Comuna(idComuna = c.largo("id_comuna"), idRegion = c.largo("id_region"), nombre = c.texto("nombre"))
    }

    fun listarInstrumentos(): List<Instrumento> = consultar(SQL_INSTRUMENTOS, leer = ::leerInstrumento)

    fun listarInstrumentosVigentes(fecha: LocalDate): List<Instrumento> =
        consultar(SQL_INSTRUMENTOS_VIGENTES, listOf(fecha.aTexto(), fecha.aTexto()), leer = ::leerInstrumento)

    fun buscarCalibracionVigente(idInstrumento: Long, fecha: LocalDate): Calibracion? =
        consultar(SQL_CALIBRACION_VIGENTE, listOf(idInstrumento, fecha.aTexto(), fecha.aTexto()), leer = ::leerCalibracion)
            .firstOrNull()

    fun buscarUltimaCalibracion(idInstrumento: Long): Calibracion? =
        consultar(SQL_ULTIMA_CALIBRACION, listOf(idInstrumento), leer = ::leerCalibracion).firstOrNull()

    fun insertarVisita(visita: Visita, idsParticipantes: List<Long>): ResultadoGuardado {
        val numeroOt = visita.numeroOt.trim()
        val existente = consultar(SQL_VISITA_POR_OT, listOf(numeroOt), leer = ::leerVisita).firstOrNull()
        if (existente != null) return ResultadoGuardado.Duplicado(existente.idVisita)

        return try {
            transaccion { db ->
                val valores = ContentValues().apply {
                    put("numero_ot", numeroOt)
                    put("id_instalacion", visita.idInstalacion)
                    put("fecha", visita.fecha.aTexto())
                    put("tipo_visita", visita.tipoVisita.name)
                    put("id_ingeniero_cargo", visita.idUsuario)
                }
                val id = db.insert("visita", null, valores)

                check(id != -1L) { "No se pudo guardar la visita" }

                idsParticipantes.forEach { idUsuario ->
                    val fila = ContentValues().apply {
                        put("id_visita", id)
                        put("id_usuario", idUsuario)
                    }
                    check(db.insert("visita_participante", null, fila) != -1L) { "Participante inválido" }
                }
                ResultadoGuardado.Creado(id)
            }
        } catch (e: IllegalStateException) {
            ResultadoGuardado.Fallo
        }
    }

    fun buscarVisita(idVisita: Long): Visita? =
        consultar(SQL_VISITA_POR_ID, listOf(idVisita), leer = ::leerVisita).firstOrNull()

    fun listarVisitas(idInstalacion: Long, usuario: Usuario): List<Visita> =
        consultar(SQL_VISITAS_POR_INSTALACION, listOf<Any>(idInstalacion) + filtroCliente(usuario), leer = ::leerVisita)

    fun cerrarVisita(idVisita: Long, fechaHora: LocalDateTime): Boolean {
        val valores = ContentValues().apply { put("fecha_hora_cierre", fechaHora.aTexto()) }
        val filas = writableDatabase.update(
            "visita", valores, "id_visita = ? AND fecha_hora_cierre IS NULL", arrayOf(idVisita.toString())
        )
        return filas == 1
    }

    fun insertarRecepcion(recepcion: RecepcionActa): Boolean {
        val valores = ContentValues().apply {
            put("id_visita", recepcion.idVisita)
            put("receptor_nombre", recepcion.receptorNombre.trim())
            put("receptor_rut", recepcion.receptorRut.trim())
            put("receptor_cargo", recepcion.receptorCargo.trim())
            put("fecha_hora_recepcion", recepcion.fechaHoraRecepcion.aTexto())
        }
        return writableDatabase.insert("recepcion_acta", null, valores) != -1L
    }

    fun buscarRecepcion(idVisita: Long): RecepcionActa? =
        consultar(SQL_RECEPCION, listOf(idVisita)) { c ->
            RecepcionActa(
                idVisita = c.largo("id_visita"),
                receptorNombre = c.texto("receptor_nombre"),
                receptorRut = c.texto("receptor_rut"),
                receptorCargo = c.texto("receptor_cargo"),
                fechaHoraRecepcion = c.texto("fecha_hora_recepcion").aFechaHora()
            )
        }.firstOrNull()

    fun listarNombresParticipantes(idVisita: Long): List<String> =
        consultar(SQL_PARTICIPANTES, listOf(idVisita)) { c -> c.texto("nombre") }

    fun listarProtocolosPorEquipo(idEquipo: Long, usuario: Usuario): List<ResumenProtocolo> =
        consultar(SQL_RESUMEN_POR_EQUIPO, listOf<Any>(idEquipo) + filtroCliente(usuario), leer = ::leerResumen)

    fun listarProtocolosPorVisita(idVisita: Long, usuario: Usuario): List<ResumenProtocolo> =
        consultar(SQL_RESUMEN_POR_VISITA, listOf<Any>(idVisita) + filtroCliente(usuario), leer = ::leerResumen)

    fun listarUltimosProtocolosDeInstalacion(idInstalacion: Long): List<ResumenProtocolo> =
        consultar(SQL_ULTIMOS_DE_INSTALACION, listOf(idInstalacion), leer = ::leerResumen)

    fun buscarProtocolo(idProtocolo: Long): Protocolo? =
        consultar(SQL_PROTOCOLO_POR_ID, listOf(idProtocolo), leer = ::leerProtocolo).firstOrNull()

    fun insertarProtocoloBorrador(
        idVisita: Long,
        idEquipo: Long,
        idUsuario: Long,
        ahora: LocalDateTime = LocalDateTime.now()
    ): ResultadoGuardado {
        val existente = consultar(SQL_PROTOCOLO_EXISTENTE, listOf(idVisita, idEquipo)) { it.getLong(0) }.firstOrNull()
        if (existente != null) return ResultadoGuardado.Duplicado(existente)

        return try {
            transaccion { db ->
                val valores = ContentValues().apply {

                    put("uuid", UUID.randomUUID().toString())
                    put("numero", siguienteNumero(db, idUsuario, ahora.year))
                    put("id_visita", idVisita)
                    put("id_equipo", idEquipo)
                    put("id_usuario_registra", idUsuario)
                    put("fecha_hora_prueba", ahora.aTexto())
                    put("id_estado", TipoEstadoProtocolo.BORRADOR.id)
                }
                val id = db.insert("protocolo", null, valores)
                check(id != -1L) { "No se pudo guardar el protocolo" }

                insertarHistorial(db, id, null, TipoEstadoProtocolo.BORRADOR.id, idUsuario, ahora, null)
                ResultadoGuardado.Creado(id)
            }
        } catch (e: IllegalStateException) {
            ResultadoGuardado.Fallo
        }
    }

    fun actualizarCondicionesAmbientales(idProtocolo: Long, temperaturaC: Double, humedadPct: Double): Boolean {
        val valores = ContentValues().apply {
            put("temperatura_c", temperaturaC)
            put("humedad_pct", humedadPct)
        }
        return try {
            writableDatabase.update("protocolo", valores, "id_protocolo = ?", arrayOf(idProtocolo.toString())) == 1
        } catch (e: SQLException) {
            false
        }
    }

    fun cambiarEstadoProtocolo(
        idProtocolo: Long,
        nuevo: TipoEstadoProtocolo,
        idUsuario: Long,
        motivo: String? = null,
        ahora: LocalDateTime = LocalDateTime.now()
    ): Boolean = try {
        transaccion { db ->
            val anterior = consultar(SQL_ESTADO_ACTUAL, listOf(idProtocolo), db) { it.getLong(0) }.firstOrNull()
            if (anterior == null) {
                false
            } else {
                val valores = ContentValues().apply { put("id_estado", nuevo.id) }
                db.update("protocolo", valores, "id_protocolo = ?", arrayOf(idProtocolo.toString()))
                insertarHistorial(db, idProtocolo, anterior, nuevo.id, idUsuario, ahora, motivo)
                true
            }
        }
    } catch (e: IllegalStateException) {
        false
    }

    fun listarHistorialEstados(idProtocolo: Long): List<HistorialEstado> =
        consultar(SQL_HISTORIAL_ESTADOS, listOf(idProtocolo)) { c ->
            HistorialEstado(
                idHistorial = c.largo("id_historial"),
                idProtocolo = c.largo("id_protocolo"),
                idEstadoAnterior = c.largoONulo("id_estado_anterior"),
                idEstadoNuevo = c.largo("id_estado_nuevo"),
                idUsuario = c.largo("id_usuario"),
                fechaHora = c.texto("fecha_hora").aFechaHora(),
                motivo = c.textoONulo("motivo")
            )
        }

    private fun siguienteNumero(db: SQLiteDatabase, idUsuario: Long, anio: Int): String {
        val prefijo = "PT-$anio-T$idUsuario-"
        val cantidad = consultar(SQL_CONTAR_NUMEROS, listOf("$prefijo%"), db) { it.getLong(0) }.first()
        return prefijo + (cantidad + 1).toString().padStart(4, '0')
    }

    private fun insertarHistorial(
        db: SQLiteDatabase,
        idProtocolo: Long,
        idEstadoAnterior: Long?,
        idEstadoNuevo: Long,
        idUsuario: Long,
        fechaHora: LocalDateTime,
        motivo: String?
    ) {
        val valores = ContentValues().apply {
            put("id_protocolo", idProtocolo)
            put("id_estado_anterior", idEstadoAnterior)
            put("id_estado_nuevo", idEstadoNuevo)
            put("id_usuario", idUsuario)
            put("fecha_hora", fechaHora.aTexto())
            put("motivo", motivo)
        }
        check(db.insert("historial_estado", null, valores) != -1L) { "No se pudo guardar el historial" }
    }

    fun insertarPrueba(prueba: Prueba, mediciones: Map<Long, Double>): ResultadoGuardado {
        val existente = consultar(
            SQL_PRUEBA_EXISTENTE,
            listOf(prueba.idProtocolo, prueba.idTipoPrueba, prueba.elemento, prueba.momento.name)
        ) { it.getLong(0) }.firstOrNull()
        if (existente != null) return ResultadoGuardado.Duplicado(existente)

        return try {
            transaccion { db ->
                val idPrueba = db.insert("prueba", null, valoresDe(prueba))
                check(idPrueba != -1L) { "No se pudo guardar la prueba" }

                mediciones.forEach { (idParametro, valor) ->
                    val fila = ContentValues().apply {
                        put("id_prueba", idPrueba)
                        put("id_parametro", idParametro)
                        put("valor", valor)
                    }
                    check(db.insert("medicion", null, fila) != -1L) { "No se pudo guardar una medición" }
                }
                ResultadoGuardado.Creado(idPrueba)
            }
        } catch (e: IllegalStateException) {
            ResultadoGuardado.Fallo
        }
    }

    fun eliminarPrueba(idPrueba: Long): Boolean = transaccion { db ->
        db.delete("medicion", "id_prueba = ?", arrayOf(idPrueba.toString()))
        db.delete("prueba", "id_prueba = ?", arrayOf(idPrueba.toString())) == 1
    }

    fun listarPruebas(idProtocolo: Long): List<Prueba> =
        consultar(SQL_PRUEBAS_POR_PROTOCOLO, listOf(idProtocolo), leer = ::leerPrueba)

    fun listarMediciones(idPrueba: Long): List<Medicion> =
        consultar(SQL_MEDICIONES, listOf(idPrueba)) { c ->
            Medicion(
                idMedicion = c.largo("id_medicion"),
                idPrueba = c.largo("id_prueba"),
                idParametro = c.largo("id_parametro"),
                valor = c.decimal("valor")
            )
        }

    fun buscarValoresPorCodigo(idPrueba: Long): Map<String, Double> =
        consultar(SQL_MEDICIONES_CON_CODIGO, listOf(idPrueba)) { c ->
            c.texto("codigo") to c.decimal("valor")
        }.toMap()

    fun listarHistorialDeParametro(
        idEquipo: Long,
        codigoTipoPrueba: String,
        elemento: String,
        codigoParametro: String
    ): List<PuntoHistorial> =
        consultar(SQL_HISTORIAL_PARAMETRO, listOf(idEquipo, codigoTipoPrueba, elemento, codigoParametro)) { c ->
            PuntoHistorial(
                idProtocolo = c.largo("id_protocolo"),
                numeroProtocolo = c.texto("numero"),
                fechaHoraPrueba = c.texto("fecha_hora_prueba").aFechaHora(),
                valor = c.decimal("valor")
            )
        }

    fun listarSeriesDeEquipo(idEquipo: Long): List<SerieMedicion> =
        consultar(SQL_SERIES_DE_EQUIPO, listOf(idEquipo)) { c ->
            SerieMedicion(
                codigoTipoPrueba = c.texto("tipo_prueba_codigo"),
                nombreTipoPrueba = c.texto("tipo_prueba_nombre"),
                elemento = c.texto("elemento"),
                codigoParametro = c.texto("parametro_codigo"),
                nombreParametro = c.texto("parametro_nombre"),
                unidad = c.texto("unidad")
            )
        }

    private fun <T> transaccion(bloque: (SQLiteDatabase) -> T): T {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val resultado = bloque(db)
            db.setTransactionSuccessful()
            return resultado
        } finally {
            db.endTransaction()
        }
    }

    private fun <T> consultar(
        sql: String,
        args: List<Any> = emptyList(),
        db: SQLiteDatabase = readableDatabase,
        leer: (Cursor) -> T
    ): List<T> {
        val lista = mutableListOf<T>()
        db.rawQuery(sql, args.map { it.toString() }.toTypedArray()).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(leer(cursor))
            }
        }
        return lista
    }

    private fun filtroCliente(usuario: Usuario): List<Any> =
        if (usuario.esCliente) listOf(1, usuario.idUsuario) else listOf(0, 0)

    private fun valoresDe(e: Equipo) = ContentValues().apply {
        put("id_instalacion", e.idInstalacion)
        put("tag", e.tag)
        put("id_tipo_equipo", e.idTipoEquipo)
        put("marca", e.marca)
        put("modelo", e.modelo)
        put("numero_serie", e.numeroSerie)
        put("anio_fabricacion", e.anioFabricacion)
        put("potencia_kva", e.potenciaKva)
        put("tension_primaria_kv", e.tensionPrimariaKv)
        put("tension_secundaria_kv", e.tensionSecundariaKv)
        put("corriente_nominal_a", e.corrienteNominalA)
        put("max_contactos_uohm", e.maxContactosUohm)
        put("observaciones", e.observaciones)
        put("foto_placa", e.fotoPlaca)
    }

    private fun valoresDe(p: Prueba) = ContentValues().apply {
        put("id_protocolo", p.idProtocolo)
        put("id_tipo_prueba", p.idTipoPrueba)
        put("elemento", p.elemento)
        put("momento", p.momento.name)
        put("realizada", if (p.realizada) 1 else 0)
        put("motivo_no_realizada", p.motivoNoRealizada)
        put("id_instrumento", p.idInstrumento)
        put("id_resultado", p.idResultado)
        put("comentario", p.comentario)
    }

    private fun leerUsuario(c: Cursor) = Usuario(
        idUsuario = c.largo("id_usuario"),
        idRol = c.largo("id_rol"),
        codigoRol = CodigoRol.desdeCodigo(c.texto("rol_codigo")),
        rut = c.texto("rut"),
        nombre = c.texto("nombre"),
        correo = c.texto("correo"),
        telefono = c.textoONulo("telefono"),
        passwordHash = c.texto("password_hash"),
        activo = c.booleano("activo"),
        giro = c.textoONulo("giro"),
        contactoNombre = c.textoONulo("contacto_nombre"),
        contactoCargo = c.textoONulo("contacto_cargo")
    )

    private fun leerInstalacion(c: Cursor) = Instalacion(
        idInstalacion = c.largo("id_instalacion"),
        idUsuario = c.largo("id_usuario_cliente"),
        nombre = c.texto("nombre"),
        ubicacion = c.textoONulo("ubicacion") ?: "",
        idComuna = c.largo("id_comuna"),
        observacionesAcceso = c.textoONulo("observaciones_acceso"),
        comunaNombre = c.texto("comuna_nombre"),
        clienteNombre = c.texto("cliente_nombre")
    )

    private fun leerEquipo(c: Cursor) = Equipo(
        idEquipo = c.largo("id_equipo"),
        idInstalacion = c.largo("id_instalacion"),
        tag = c.texto("tag"),
        idTipoEquipo = c.largo("id_tipo_equipo"),
        marca = c.textoONulo("marca"),
        modelo = c.textoONulo("modelo"),
        numeroSerie = c.textoONulo("numero_serie"),
        anioFabricacion = c.enteroONulo("anio_fabricacion"),
        potenciaKva = c.decimalONulo("potencia_kva"),
        tensionPrimariaKv = c.decimalONulo("tension_primaria_kv"),
        tensionSecundariaKv = c.decimalONulo("tension_secundaria_kv"),
        corrienteNominalA = c.decimalONulo("corriente_nominal_a"),
        maxContactosUohm = c.decimalONulo("max_contactos_uohm"),
        observaciones = c.textoONulo("observaciones"),
        fotoPlaca = c.textoONulo("foto_placa"),
        tipoEquipoNombre = c.texto("tipo_equipo_nombre")
    )

    private fun leerInstrumento(c: Cursor) = Instrumento(
        idInstrumento = c.largo("id_instrumento"),
        nombre = c.texto("nombre"),
        marca = c.textoONulo("marca"),
        modelo = c.textoONulo("modelo"),
        numeroSerie = c.texto("numero_serie")
    )

    private fun leerCalibracion(c: Cursor) = Calibracion(
        idCalibracion = c.largo("id_calibracion"),
        idInstrumento = c.largo("id_instrumento"),
        fechaCalibracion = c.texto("fecha_calibracion").aFecha(),
        fechaVencimiento = c.texto("fecha_vencimiento").aFecha()
    )

    private fun leerVisita(c: Cursor) = Visita(
        idVisita = c.largo("id_visita"),
        numeroOt = c.texto("numero_ot"),
        idInstalacion = c.largo("id_instalacion"),
        fecha = c.texto("fecha").aFecha(),
        tipoVisita = TipoVisita.valueOf(c.texto("tipo_visita")),
        idUsuario = c.largo("id_ingeniero_cargo"),

        fechaHoraCierre = c.textoONulo("fecha_hora_cierre")?.aFechaHora()
    )

    private fun leerProtocolo(c: Cursor) = Protocolo(
        idProtocolo = c.largo("id_protocolo"),
        uuid = c.texto("uuid"),
        numero = c.texto("numero"),
        idVisita = c.largo("id_visita"),
        idEquipo = c.largo("id_equipo"),
        idUsuario = c.largo("id_usuario_registra"),
        fechaHoraPrueba = c.texto("fecha_hora_prueba").aFechaHora(),
        temperaturaAmbiente = c.decimalONulo("temperatura_c"),
        pctHumedad = c.decimalONulo("humedad_pct"),
        descripcionMantenimiento = c.textoONulo("descripcion_mantenimiento"),
        observaciones = c.textoONulo("observaciones"),
        idEstadoProtocolo = c.largo("id_estado"),
        idProtocoloReemplazado = c.largoONulo("id_protocolo_reemplazado")
    )

    private fun leerResumen(c: Cursor) = ResumenProtocolo(
        idProtocolo = c.largo("id_protocolo"),
        numero = c.texto("numero"),
        idEquipo = c.largo("id_equipo"),
        tagEquipo = c.texto("tag_equipo"),
        fechaHoraPrueba = c.texto("fecha_hora_prueba").aFechaHora(),
        estado = TipoEstadoProtocolo.desdeId(c.largo("id_estado")),

        resultadoGlobal = c.largoONulo("id_resultado_global")?.let { TipoResultado.desdeId(it) },
        tecnicoNombre = c.texto("tecnico_nombre")
    )

    private fun leerPrueba(c: Cursor) = Prueba(
        idPrueba = c.largo("id_prueba"),
        idProtocolo = c.largo("id_protocolo"),
        idTipoPrueba = c.largo("id_tipo_prueba"),
        elemento = c.texto("elemento"),
        momento = Momento.valueOf(c.texto("momento")),
        realizada = c.booleano("realizada"),
        motivoNoRealizada = c.textoONulo("motivo_no_realizada"),
        idInstrumento = c.largoONulo("id_instrumento"),
        idResultado = c.largoONulo("id_resultado"),
        comentario = c.textoONulo("comentario")
    )
}

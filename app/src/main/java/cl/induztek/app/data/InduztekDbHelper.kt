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

// =====================================================================
//  InduztekDbHelper.kt — LA base de datos local (induztek.db)
//  ---------------------------------------------------------------------
//  Es el equivalente a UsuarioDbHelper de MiRegistro, pero con 21 tablas en vez de 2.
//  Sigue la misma regla: UN helper por base de datos. Todas las tablas se crean en el
//  MISMO onCreate() y todos los INSERT / SELECT / UPDATE / DELETE viven en esta clase,
//  ordenados por secciones (USUARIOS, EQUIPOS, PROTOCOLOS...).
//
//  Las tablas están en Esquema.kt y los datos precargados en DatosIniciales.kt
//  (separados solo para que este archivo no sea gigante).
//
//      Pantalla (Compose) -> ViewModel (AndroidViewModel) -> InduztekDbHelper -> induztek.db
//
//  Igual que en MiRegistro, el ViewModel crea el helper así:
//      private val db = InduztekDbHelper(application)
//
//  Un recordatorio de los tipos de SQLite (Esquema.kt): Boolean = 0/1, fechas = texto ISO,
//  decimales = REAL. Las fechas se convierten con .aTexto() / .aFecha() (data/Fechas.kt).
//
//  RNF-02 / RNF-03: todo se guarda en este archivo del teléfono, así que la app funciona
//  sin señal y los datos sobreviven aunque se cierre la app o se apague el equipo.
// =====================================================================
class InduztekDbHelper(context: Context) :
    SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {

    companion object {
        const val NOMBRE_BD = "induztek.db"

        // Si cambias las tablas, SUBE este número: Android llamará a onUpgrade.
        const val VERSION_BD = 1

        // Clave de TODOS los usuarios de demostración (personal y clientes).
        // Es una clave ficticia, solo para probar la app con los datos del Anexo 9.
        const val CLAVE_DEMO = "Induztek2026"

        // -----------------------------------------------------------------
        //  CONSULTAS (SELECT). Cada una se lee completa. Los "?" se rellenan con los
        //  argumentos, en orden: NUNCA se pega el texto del usuario dentro del SQL.
        // -----------------------------------------------------------------

        // --- USUARIOS: u.* = todas las columnas; el JOIN agrega el código del rol ---
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

        // Solo cuentas activas (un usuario desactivado no puede ingresar, Aclaración nº 8).
        private const val SQL_USUARIOS_POR_ROL = """
            SELECT u.*, r.codigo AS rol_codigo
            FROM usuario u
            INNER JOIN rol r ON r.id_rol = u.id_rol
            WHERE r.codigo = ? AND u.activo = 1
            ORDER BY u.nombre
        """

        // --- INSTALACIONES ---
        // El filtro (CAST(? AS INTEGER) = 0 OR i.id_usuario_cliente = ?) es la RN-18 aplicada
        // EN LA CONSULTA (ver filtroCliente más abajo).
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

        // --- EQUIPOS ---
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

        // Para detectar un TAG repetido dentro de la MISMA instalación (RN-01).
        private const val SQL_EQUIPO_POR_TAG = """
            SELECT e.*, t.nombre AS tipo_equipo_nombre
            FROM equipo e
            INNER JOIN tipo_equipo t ON t.id_tipo_equipo = e.id_tipo_equipo
            WHERE e.id_instalacion = ? AND e.tag = ?
        """

        // --- CATÁLOGOS: equipo -> pruebas -> parámetros (RN-05) ---
        // Con esto la pantalla de mediciones se ARMA SOLA según el tipo de equipo.
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

        // --- INSTRUMENTOS Y CALIBRACIONES (RN-11) ---
        // Una calibración está VIGENTE en la fecha F cuando:
        //     fecha_calibracion <= F  Y  fecha_vencimiento >= F
        // Como las fechas son texto ISO ("2026-09-26"), se comparan bien con <= y >=.
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

        // La más reciente, esté vigente o no: sirve para decir "venció el 10-10-2026" (HU-04).
        private const val SQL_ULTIMA_CALIBRACION = """
            SELECT * FROM calibracion
            WHERE id_instrumento = ?
            ORDER BY fecha_vencimiento DESC
            LIMIT 1
        """

        // --- VISITAS ---
        private const val SQL_VISITA_POR_ID = "SELECT * FROM visita WHERE id_visita = ?"
        private const val SQL_VISITA_POR_OT = "SELECT * FROM visita WHERE numero_ot = ?"

        // RN-19: el cliente solo ve las visitas de SUS instalaciones y solo las CERRADAS
        // (las que ya tienen acta). El personal de Induztek ve todas.
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

        // --- PROTOCOLOS ---
        private const val SQL_PROTOCOLO_POR_ID = "SELECT * FROM protocolo WHERE id_protocolo = ?"

        // ¿El equipo ya tiene protocolo en esta visita? (Aclaración nº 5: no puede tener dos)
        private const val SQL_PROTOCOLO_EXISTENTE =
            "SELECT id_protocolo FROM protocolo WHERE id_visita = ? AND id_equipo = ?"

        private const val SQL_CONTAR_NUMEROS = "SELECT COUNT(*) FROM protocolo WHERE numero LIKE ?"
        private const val SQL_ESTADO_ACTUAL = "SELECT id_estado FROM protocolo WHERE id_protocolo = ?"

        private const val SQL_HISTORIAL_ESTADOS =
            "SELECT * FROM historial_estado WHERE id_protocolo = ? ORDER BY fecha_hora, id_historial"

        // "RESUMEN": una fila por protocolo, lista para mostrar en una lista.
        //  id_resultado_global = RN-10: "el peor resultado de sus pruebas". Como los ids de
        //  resultado están ordenados por gravedad (1 Aprobado < 2 Observado < 3 Rechazado),
        //  basta con MAX(). Se ignoran las pruebas NO realizadas (Aclaración 2) y las
        //  mediciones PREVIAS al mantenimiento (Aclaración 4: vale la posterior).
        //  Si el protocolo aún no tiene pruebas con resultado, el MAX da NULL.

        // Historial de UN equipo, del más reciente al más antiguo (HU-11, HU-12).
        // Para un CLIENTE (RN-18 y RN-19): solo sus instalaciones y solo protocolos en estado
        // 2 (Registrado en terreno), 3 (Observado) o 4 (Formalizado). Nunca Borrador ni Anulado.
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

        // Todos los protocolos de UNA visita (cierre de visita y acta, RN-13 y RN-14).
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

        // Aclaración nº 9: "En terreno, aun sin señal, el técnico debe poder consultar al menos el
        // último protocolo de cada equipo de la instalación que está visitando".
        // La subconsulta busca, por equipo, la fecha más reciente entre los protocolos ya
        // registrados (estados 2, 3 y 4: se ignoran Borrador y Anulado).
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

        // --- PRUEBAS Y MEDICIONES ---
        private const val SQL_PRUEBAS_POR_PROTOCOLO =
            "SELECT * FROM prueba WHERE id_protocolo = ? ORDER BY id_prueba"

        private const val SQL_MEDICIONES = "SELECT * FROM medicion WHERE id_prueba = ? ORDER BY id_medicion"

        // Mediciones de una prueba junto al CÓDIGO del parámetro ("LECTURA_1MIN"): así la lógica
        // puede hacer valores["LECTURA_10MIN"] / valores["LECTURA_1MIN"] para calcular el IP.
        private const val SQL_MEDICIONES_CON_CODIGO = """
            SELECT pa.codigo AS codigo, m.valor AS valor
            FROM medicion m
            INNER JOIN parametro pa ON pa.id_parametro = m.id_parametro
            WHERE m.id_prueba = ?
        """

        // Qué valores se han medido en un equipo (tipo de prueba + elemento + parámetro), sin repetir.
        // Sirve para armar la comparación histórica: por cada "serie" se pide después
        // listarHistorialDeParametro. Mismos filtros que SQL_HISTORIAL_PARAMETRO.
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

        // La misma prueba no puede registrarse dos veces en un protocolo (mismo tipo, elemento y momento).
        private const val SQL_PRUEBA_EXISTENTE = """
            SELECT id_prueba FROM prueba
            WHERE id_protocolo = ? AND id_tipo_prueba = ? AND elemento = ? AND momento = ?
        """

        // HISTORIAL DE UN VALOR (HU-06 y RN-09: comparar con la prueba anterior).
        // Ejemplo: la lectura de 1 min de la aislación AT-T del TR-01, a lo largo de los años.
        // Une 5 tablas con INNER JOIN. Solo pruebas realizadas, sin las PREVIAS al mantenimiento
        // y sin protocolos ANULADOS (estado 5). Del más reciente al más antiguo.
        // OJO: no tiene filtro de cliente. Es para el personal de Induztek.
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

    // =================================================================
    //  CREACIÓN DE LA BASE
    // =================================================================

    // Se ejecuta CADA VEZ que se abre la conexión, antes de onCreate.
    // SQLite trae las claves foráneas APAGADAS por defecto: aquí se encienden,
    // para que un id inexistente en un REFERENCES sea rechazado.
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    // Se ejecuta UNA sola vez: cuando induztek.db todavía no existe.
    // Android ya envuelve esto en una transacción: si algo falla, no queda una base a medias.
    override fun onCreate(db: SQLiteDatabase) {
        // 1) Crear las tablas, en orden (primero las "padre").
        Esquema.TABLAS.forEach { (_, sql) -> db.execSQL(sql) }
        Esquema.INDICES.forEach { db.execSQL(it) }

        // 2) Precargar los datos del Anexo 9.
        //    El marcador %HASH% se reemplaza por el hash de la clave de demostración.
        val hashDemo = Seguridad.sha256(CLAVE_DEMO)
        DatosIniciales.SENTENCIAS.forEach { sql ->
            db.execSQL(sql.replace("%HASH%", hashDemo))
        }
    }

    // Se ejecuta cuando VERSION_BD sube. Versión simple, SOLO PARA DESARROLLO (igual que en
    // MiRegistro): borra todo y recrea (se pierden los datos). Orden inverso: primero las tablas
    // "hija". Para la EFT (datos reales en terreno, RNF-03) esto se cambia por migraciones.
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Esquema.TABLAS.map { it.first }.reversed().forEach { nombre ->
            db.execSQL("DROP TABLE IF EXISTS $nombre")
        }
        onCreate(db)
    }

    // =================================================================
    //  USUARIOS Y LOGIN (RF-14)
    // =================================================================

    // Inicia sesión con correo y clave. Se compara el HASH de la clave, nunca el texto.
    // El orden de las comprobaciones es a propósito: primero la clave y DESPUÉS si está
    // activa, para no revelar a un desconocido que una cuenta existe pero está desactivada.
    fun iniciarSesion(correo: String, clave: String): ResultadoLogin {
        val usuario = consultar(SQL_USUARIO_POR_CORREO, listOf(correo.trim()), leer = ::leerUsuario).firstOrNull()
        return when {
            usuario == null || usuario.passwordHash != Seguridad.sha256(clave) ->
                ResultadoLogin.CredencialesInvalidas
            !usuario.activo -> ResultadoLogin.CuentaInactiva
            else -> ResultadoLogin.Exito(usuario)
        }
    }

    // Para restaurar la sesión guardada en SesionPreferencias al abrir la app.
    fun buscarUsuarioPorId(idUsuario: Long): Usuario? =
        consultar(SQL_USUARIO_POR_ID, listOf(idUsuario), leer = ::leerUsuario).firstOrNull()

    // Por ejemplo, para elegir el ingeniero a cargo de una visita.
    fun listarUsuariosActivosPorRol(rol: CodigoRol): List<Usuario> =
        consultar(SQL_USUARIOS_POR_ROL, listOf(rol.codigo), leer = ::leerUsuario)

    // =================================================================
    //  INSTALACIONES (RF-01)
    // =================================================================

    // Las instalaciones que `usuario` tiene permitido ver (RN-18).
    fun listarInstalaciones(usuario: Usuario): List<Instalacion> =
        consultar(SQL_INSTALACIONES, filtroCliente(usuario), leer = ::leerInstalacion)

    // Una instalación, o null si no existe O si pertenece a otro cliente.
    fun buscarInstalacion(idInstalacion: Long, usuario: Usuario): Instalacion? =
        consultar(SQL_INSTALACION_POR_ID, filtroCliente(usuario) + idInstalacion, leer = ::leerInstalacion)
            .firstOrNull()

    // =================================================================
    //  EQUIPOS (RF-02, RN-01, HU-01)
    // =================================================================

    fun listarEquipos(idInstalacion: Long, usuario: Usuario): List<Equipo> =
        consultar(SQL_EQUIPOS_POR_INSTALACION, listOf<Any>(idInstalacion) + filtroCliente(usuario), leer = ::leerEquipo)

    fun buscarEquipo(idEquipo: Long, usuario: Usuario): Equipo? =
        consultar(SQL_EQUIPO_POR_ID, listOf<Any>(idEquipo) + filtroCliente(usuario), leer = ::leerEquipo).firstOrNull()

    // HU-01: "Dado un TAG que ya existe en la misma instalación, cuando intento guardarlo,
    // entonces se me avisa que el equipo ya existe y se me ofrece usar el registrado".
    //  -> Duplicado(idExistente) trae el id del equipo que ya estaba, para ofrecerlo.
    fun insertarEquipo(equipo: Equipo): ResultadoGuardado {
        // "tr-01 " y "TR-01" deben ser el MISMO equipo: se quitan espacios y se pasa a mayúsculas.
        // (La restricción UNIQUE de SQLite distingue mayúsculas de minúsculas.)
        val tag = equipo.tag.trim().uppercase()

        val existente = consultar(SQL_EQUIPO_POR_TAG, listOf(equipo.idInstalacion, tag), leer = ::leerEquipo)
            .firstOrNull()
        if (existente != null) return ResultadoGuardado.Duplicado(existente.idEquipo)

        // insert() devuelve el id nuevo, o -1 si la base rechazó la fila
        // (por ejemplo, si el id_instalacion o el id_tipo_equipo no existen).
        val id = writableDatabase.insert("equipo", null, valoresDe(equipo.copy(tag = tag)))
        return if (id == -1L) ResultadoGuardado.Fallo else ResultadoGuardado.Creado(id)
    }

    // =================================================================
    //  CATÁLOGOS: tipos de equipo, pruebas y parámetros (RN-05)
    //  equipo.idTipoEquipo -> pruebas del tipo (Aislación, TTR...) -> parámetros (Tensión, Lectura 1 min... + unidad)
    //  Los catálogos no contienen datos de ninguna empresa: no llevan filtro de cliente.
    // =================================================================

    fun listarTiposEquipo(): List<TipoEquipo> = consultar(SQL_TIPOS_EQUIPO) { c ->
        TipoEquipo(
            idTipoEquipo = c.largo("id_tipo_equipo"),
            nombre = c.texto("nombre"),
            // El texto guardado ("PRIMARIO") coincide con el nombre del enum.
            clasificacion = ClasificacionEquipo.valueOf(c.texto("clasificacion"))
        )
    }

    // Aclaración nº 2: TODAS estas pruebas deben quedar "realizadas" o "no realizadas con motivo".
    fun listarPruebasDeTipoEquipo(idTipoEquipo: Long): List<TipoPrueba> =
        consultar(SQL_PRUEBAS_DE_TIPO_EQUIPO, listOf(idTipoEquipo)) { c ->
            TipoPrueba(
                idTipoPrueba = c.largo("id_tipo_prueba"),
                codigo = c.texto("codigo"),
                nombre = c.texto("nombre")
            )
        }

    // Los campos a pedir en la pantalla de mediciones, cada uno con su unidad (RNF-05).
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

    // =================================================================
    //  INSTRUMENTOS Y CALIBRACIONES (RF-05, RN-11, HU-04)
    //  RN-11: "No se puede registrar una prueba con un instrumento cuya calibración esté
    //  vencida a la fecha de la prueba."
    // =================================================================

    fun listarInstrumentos(): List<Instrumento> = consultar(SQL_INSTRUMENTOS, leer = ::leerInstrumento)

    // Los instrumentos que SÍ se pueden usar en una prueba hecha en `fecha`.
    fun listarInstrumentosVigentes(fecha: LocalDate): List<Instrumento> =
        consultar(SQL_INSTRUMENTOS_VIGENTES, listOf(fecha.aTexto(), fecha.aTexto()), leer = ::leerInstrumento)

    // null = NO hay calibración vigente en esa fecha (el instrumento no se puede usar).
    fun buscarCalibracionVigente(idInstrumento: Long, fecha: LocalDate): Calibracion? =
        consultar(SQL_CALIBRACION_VIGENTE, listOf(idInstrumento, fecha.aTexto(), fecha.aTexto()), leer = ::leerCalibracion)
            .firstOrNull()

    fun buscarUltimaCalibracion(idInstrumento: Long): Calibracion? =
        consultar(SQL_ULTIMA_CALIBRACION, listOf(idInstrumento), leer = ::leerCalibracion).firstOrNull()

    // =================================================================
    //  VISITAS, CIERRE Y RECEPCIÓN DEL ACTA (RF-07, RF-08, HU-07, HU-08)
    //  OJO: aquí solo se GUARDA. Las reglas de "cuándo se puede cerrar" (RN-13) y de "el RUT
    //  debe ser válido" (RN-03) se validan ANTES, en la capa de lógica (el ViewModel).
    // =================================================================

    // Crea la visita Y sus técnicos participantes en una TRANSACCIÓN: o se guarda todo o nada.
    fun insertarVisita(visita: Visita, idsParticipantes: List<Long>): ResultadoGuardado {
        val numeroOt = visita.numeroOt.trim()
        val existente = consultar(SQL_VISITA_POR_OT, listOf(numeroOt), leer = ::leerVisita).firstOrNull()
        if (existente != null) return ResultadoGuardado.Duplicado(existente.idVisita) // la OT es única

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
                // check(): si la condición es falsa lanza IllegalStateException
                // y la transacción se DESHACE completa.
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

    // Marca la visita como cerrada. Devuelve false si no existe o si YA estaba cerrada
    // (el "AND fecha_hora_cierre IS NULL" evita cerrarla dos veces y cambiar la hora de cierre).
    fun cerrarVisita(idVisita: Long, fechaHora: LocalDateTime): Boolean {
        val valores = ContentValues().apply { put("fecha_hora_cierre", fechaHora.aTexto()) }
        val filas = writableDatabase.update(
            "visita", valores, "id_visita = ? AND fecha_hora_cierre IS NULL", arrayOf(idVisita.toString())
        )
        return filas == 1
    }

    // Constancia de quién recibió el acta. id_visita es PK de recepcion_acta, así que una visita
    // solo puede tener UNA recepción: un segundo intento devuelve false.
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

    // Nombres de los técnicos que participaron (para el acta, RN-14).
    fun listarNombresParticipantes(idVisita: Long): List<String> =
        consultar(SQL_PARTICIPANTES, listOf(idVisita)) { c -> c.texto("nombre") }

    // =================================================================
    //  PROTOCOLOS (RF-03, RF-10, RF-13, RN-02, RNF-07)
    //  Un protocolo = UN equipo en UNA visita. Nace en estado BORRADOR.
    //  La validación de las TRANSICIONES de estado (RN-04) y de los permisos (RN-17) es de la
    //  capa de lógica, ANTES de llamar a cambiarEstadoProtocolo(). Aquí solo se guarda el cambio
    //  y se deja el rastro en historial_estado (quién, cuándo, por qué).
    // =================================================================

    // Historial de un equipo (HU-11 / HU-12). El filtro de cliente va dentro de la consulta.
    fun listarProtocolosPorEquipo(idEquipo: Long, usuario: Usuario): List<ResumenProtocolo> =
        consultar(SQL_RESUMEN_POR_EQUIPO, listOf<Any>(idEquipo) + filtroCliente(usuario), leer = ::leerResumen)

    fun listarProtocolosPorVisita(idVisita: Long, usuario: Usuario): List<ResumenProtocolo> =
        consultar(SQL_RESUMEN_POR_VISITA, listOf<Any>(idVisita) + filtroCliente(usuario), leer = ::leerResumen)

    // Solo personal de Induztek (no lleva filtro de cliente): ayuda en terreno (Aclaración nº 9).
    fun listarUltimosProtocolosDeInstalacion(idInstalacion: Long): List<ResumenProtocolo> =
        consultar(SQL_ULTIMOS_DE_INSTALACION, listOf(idInstalacion), leer = ::leerResumen)

    // El protocolo completo. Solo para el personal: no lleva filtro de cliente.
    fun buscarProtocolo(idProtocolo: Long): Protocolo? =
        consultar(SQL_PROTOCOLO_POR_ID, listOf(idProtocolo), leer = ::leerProtocolo).firstOrNull()

    // HU-02: crea el protocolo en estado BORRADOR y deja la primera fila del historial.
    // Las dos escrituras van en una TRANSACCIÓN (todo o nada).
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
                    // uuid: identificador único generado en el celular (RN-02). Dos celulares sin
                    // señal NUNCA generan el mismo UUID, así que al sincronizar no chocan.
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

                // idEstadoAnterior = null: el protocolo recién nace, no venía de ningún estado.
                insertarHistorial(db, id, null, TipoEstadoProtocolo.BORRADOR.id, idUsuario, ahora, null)
                ResultadoGuardado.Creado(id)
            }
        } catch (e: IllegalStateException) {
            ResultadoGuardado.Fallo
        }
    }

    // Guarda temperatura y humedad (obligatorias para pasar a Registrado en terreno, HU-02).
    // La base rechaza una humedad fuera de 0..100 (CHECK en el esquema): en ese caso, false.
    fun actualizarCondicionesAmbientales(idProtocolo: Long, temperaturaC: Double, humedadPct: Double): Boolean {
        val valores = ContentValues().apply {
            put("temperatura_c", temperaturaC)
            put("humedad_pct", humedadPct)
        }
        return try {
            writableDatabase.update("protocolo", valores, "id_protocolo = ?", arrayOf(idProtocolo.toString())) == 1
        } catch (e: SQLException) {
            false // update() SÍ lanza la excepción cuando falla un CHECK
        }
    }

    // Cambia el estado Y deja constancia en el historial (RNF-07): quién, cuándo y por qué.
    // `motivo` es obligatorio para OBSERVADO y ANULADO, pero eso lo valida la lógica antes.
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
                false // el protocolo no existe
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

    // Número visible del protocolo, p. ej. "PT-2026-T3-0007" = 7º protocolo de 2026 del técnico 3.
    // Llevar el id del usuario en el número evita que dos técnicos sin señal generen el mismo
    // número (RN-02). LIMITACIÓN: supone que cada técnico usa UN solo dispositivo.
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

    // =================================================================
    //  PRUEBAS Y MEDICIONES (RF-04, RF-11, RN-05, RN-09, HU-03, HU-06)
    //
    //      prueba 3 ──┬── medicion (parametro TENSION_PRUEBA,  valor 5)
    //                 ├── medicion (parametro LECTURA_1MIN,    valor 6100)
    //                 └── medicion (parametro LECTURA_10MIN,   valor 10980)
    //
    //  Los valores se guardan SIEMPRE en la unidad base del parámetro (MΩ, mΩ, µΩ, kV, A, ms).
    //  Convertir 5,8 GΩ a 5800 MΩ, calcular el IP, el error TTR y el resultado (RN-06 a RN-08b)
    //  es trabajo de la capa de lógica ANTES de guardar: aquí solo se guarda el idResultado.
    // =================================================================

    // Guarda la prueba Y todas sus mediciones en una TRANSACCIÓN: si falla la tercera medición,
    // la prueba y las dos primeras NO quedan guardadas (RNF-03).
    //  mediciones: mapa  idParametro -> valor (en la unidad base).
    //  Una prueba "No realizada" (Aclaración nº 2) se guarda con un mapa VACÍO y su motivo.
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

    // Para corregir un error mientras el protocolo está en Borrador u Observado (HU-03): se borra la
    // prueba con sus mediciones y se vuelve a guardar. Primero las mediciones (tabla "hija"),
    // después la prueba: al revés, la clave foránea lo impediría. Quién puede hacerlo (RN-17)
    // lo valida la lógica.
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

    // Mapa  código del parámetro -> valor.   Ej.: {"TENSION_PRUEBA": 5.0, "LECTURA_1MIN": 6100.0, ...}
    fun buscarValoresPorCodigo(idPrueba: Long): Map<String, Double> =
        consultar(SQL_MEDICIONES_CON_CODIGO, listOf(idPrueba)) { c ->
            c.texto("codigo") to c.decimal("valor")
        }.toMap()

    // El mismo valor medido en TODOS los protocolos del equipo, del más reciente al más antiguo.
    // La lógica toma el punto anterior al actual y calcula la variación (RN-09):
    //      variación % = (actual - anterior) / anterior * 100     (> 25 % = "Variación significativa")
    //   Ejemplo:  listarHistorialDeParametro(1, "AISLACION", "AT-T", "LECTURA_1MIN")
    //             -> [ PT-2026-0401: 6100,  PT-2025-0288: 9800,  PT-2024-0312: 12500 ]
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

    // Las series medibles de un equipo (ver SQL_SERIES_DE_EQUIPO).
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

    // =================================================================
    //  TRANSACCIÓN: "todo o nada"
    //  Ejemplo: guardar una prueba + sus 3 mediciones. Si falla la tercera medición, NO debe
    //  quedar la prueba guardada a medias (RNF-03: cero pérdida / cero datos rotos).
    //
    //      transaccion { db ->
    //          db.insert(...)   // 1
    //          db.insert(...)   // 2   <- si algo lanza una excepción, se deshace 1 y 2
    //      }
    // =================================================================
    private fun <T> transaccion(bloque: (SQLiteDatabase) -> T): T {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val resultado = bloque(db)
            db.setTransactionSuccessful() // marca "todo salió bien"; sin esto, endTransaction DESHACE todo
            return resultado
        } finally {
            db.endTransaction()
        }
    }

    // =================================================================
    //  AYUDANTES
    // =================================================================

    // Ejecuta un SELECT y convierte CADA fila en un objeto con la función `leer`
    // (es el mismo recorrido del Cursor de MiRegistro, escrito una sola vez).
    //  - rawQuery solo acepta argumentos de tipo texto: por eso se convierten con toString().
    //  - `db` solo se pasa cuando estamos dentro de una transacción.
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

    // RN-18: "un usuario cliente solo puede ver información de SU empresa". El filtro se aplica
    // EN LA CONSULTA, no en la pantalla (si solo estuviera en la UI, un error de código podría
    // mostrarle a un cliente los datos de otro). Devuelve los dos argumentos de la condición
    //      AND (CAST(? AS INTEGER) = 0 OR i.id_usuario_cliente = ?)
    //   - personal Induztek -> (0, 0): el primer "?" es 0, la condición se cumple siempre.
    //   - usuario cliente   -> (1, su id): solo pasan las filas de SUS instalaciones.
    // (CAST es necesario porque los argumentos viajan como texto: '0' = 0 sería falso.)
    private fun filtroCliente(usuario: Usuario): List<Any> =
        if (usuario.esCliente) listOf(1, usuario.idUsuario) else listOf(0, 0)

    // ContentValues = mapa columna -> valor, para INSERT y UPDATE.
    // put(columna, null) guarda NULL en la base (por eso los campos nullable no dan problema).
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
    }

    // Boolean -> 1 / 0 para guardarlo en SQLite.
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

    // ---- Conversión de una fila del Cursor a un objeto del model (como leerUsuario en MiRegistro) ----

    private fun leerUsuario(c: Cursor) = Usuario(
        idUsuario = c.largo("id_usuario"),
        idRol = c.largo("id_rol"),
        codigoRol = CodigoRol.desdeCodigo(c.texto("rol_codigo")), // viene del JOIN con rol
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
        tipoEquipoNombre = c.texto("tipo_equipo_nombre")
    )

    private fun leerInstrumento(c: Cursor) = Instrumento(
        idInstrumento = c.largo("id_instrumento"),
        nombre = c.texto("nombre"),
        marca = c.textoONulo("marca"),
        modelo = c.textoONulo("modelo"),
        numeroSerie = c.texto("numero_serie")
    )

    // Las fechas vienen como texto ("2026-03-15"): .aFecha() las convierte a LocalDate.
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
        // Mientras la visita está abierta, la columna es NULL -> fechaHoraCierre = null
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
        // null si el protocolo todavía no tiene pruebas con resultado
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

package cl.induztek.app.data

object Esquema {

    val TABLAS: List<Pair<String, String>> = listOf(

        // ---------------------------- SEGURIDAD ----------------------------
        "rol" to """
            CREATE TABLE rol (
                id_rol INTEGER PRIMARY KEY AUTOINCREMENT,
                codigo TEXT NOT NULL UNIQUE,
                nombre TEXT NOT NULL
            )
        """,

        "usuario" to """
            CREATE TABLE usuario (
                id_usuario INTEGER PRIMARY KEY AUTOINCREMENT,
                id_rol INTEGER NOT NULL REFERENCES rol(id_rol),
                rut TEXT NOT NULL UNIQUE,
                nombre TEXT NOT NULL,
                correo TEXT NOT NULL UNIQUE,
                telefono TEXT,
                password_hash TEXT NOT NULL,
                activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1)),
                giro TEXT,
                contacto_nombre TEXT,
                contacto_cargo TEXT
            )
        """,

        "region" to """
            CREATE TABLE region (
                id_region INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL UNIQUE
            )
        """,

        "comuna" to """
            CREATE TABLE comuna (
                id_comuna INTEGER PRIMARY KEY AUTOINCREMENT,
                id_region INTEGER NOT NULL REFERENCES region(id_region),
                nombre TEXT NOT NULL
            )
        """,

        "instalacion" to """
            CREATE TABLE instalacion (
                id_instalacion INTEGER PRIMARY KEY AUTOINCREMENT,
                id_usuario_cliente INTEGER NOT NULL REFERENCES usuario(id_usuario),
                nombre TEXT NOT NULL,
                ubicacion TEXT,
                id_comuna INTEGER NOT NULL REFERENCES comuna(id_comuna),
                observaciones_acceso TEXT
            )
        """,

        "tipo_equipo" to """
            CREATE TABLE tipo_equipo (
                id_tipo_equipo INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL UNIQUE,
                clasificacion TEXT NOT NULL CHECK (clasificacion IN ('PRIMARIO', 'SECUNDARIO'))
            )
        """,

        "equipo" to """
            CREATE TABLE equipo (
                id_equipo INTEGER PRIMARY KEY AUTOINCREMENT,
                id_instalacion INTEGER NOT NULL REFERENCES instalacion(id_instalacion),
                tag TEXT NOT NULL,
                id_tipo_equipo INTEGER NOT NULL REFERENCES tipo_equipo(id_tipo_equipo),
                marca TEXT,
                modelo TEXT,
                numero_serie TEXT,
                anio_fabricacion INTEGER,
                potencia_kva REAL,
                tension_primaria_kv REAL,
                tension_secundaria_kv REAL,
                corriente_nominal_a REAL,
                max_contactos_uohm REAL,
                observaciones TEXT,
                UNIQUE (id_instalacion, tag)
            )
        """,

        "tipo_prueba" to """
            CREATE TABLE tipo_prueba (
                id_tipo_prueba INTEGER PRIMARY KEY AUTOINCREMENT,
                codigo TEXT NOT NULL UNIQUE,
                nombre TEXT NOT NULL
            )
        """,

        "tipo_equipo_prueba" to """
            CREATE TABLE tipo_equipo_prueba (
                id_tipo_equipo INTEGER NOT NULL REFERENCES tipo_equipo(id_tipo_equipo),
                id_tipo_prueba INTEGER NOT NULL REFERENCES tipo_prueba(id_tipo_prueba),
                PRIMARY KEY (id_tipo_equipo, id_tipo_prueba)
            )
        """,

        "parametro" to """
            CREATE TABLE parametro (
                id_parametro INTEGER PRIMARY KEY AUTOINCREMENT,
                id_tipo_prueba INTEGER NOT NULL REFERENCES tipo_prueba(id_tipo_prueba),
                codigo TEXT NOT NULL,
                nombre TEXT NOT NULL,
                unidad TEXT NOT NULL,
                UNIQUE (id_tipo_prueba, codigo)
            )
        """,

        "instrumento" to """
            CREATE TABLE instrumento (
                id_instrumento INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                marca TEXT,
                modelo TEXT,
                numero_serie TEXT NOT NULL UNIQUE
            )
        """,

        "calibracion" to """
            CREATE TABLE calibracion (
                id_calibracion INTEGER PRIMARY KEY AUTOINCREMENT,
                id_instrumento INTEGER NOT NULL REFERENCES instrumento(id_instrumento),
                fecha_calibracion TEXT NOT NULL,
                fecha_vencimiento TEXT NOT NULL,
                CHECK (fecha_vencimiento >= fecha_calibracion)
            )
        """,

        "estado_protocolo" to """
            CREATE TABLE estado_protocolo (
                id_estado INTEGER PRIMARY KEY AUTOINCREMENT,
                codigo TEXT NOT NULL UNIQUE
            )
        """,

        "resultado" to """
            CREATE TABLE resultado (
                id_resultado INTEGER PRIMARY KEY AUTOINCREMENT,
                codigo TEXT NOT NULL UNIQUE
            )
        """,

        "visita" to """
            CREATE TABLE visita (
                id_visita INTEGER PRIMARY KEY AUTOINCREMENT,
                numero_ot TEXT NOT NULL UNIQUE,
                id_instalacion INTEGER NOT NULL REFERENCES instalacion(id_instalacion),
                fecha TEXT NOT NULL,
                tipo_visita TEXT NOT NULL CHECK (tipo_visita IN ('SOLO_PRUEBAS', 'PRUEBAS_Y_MANT')),
                id_ingeniero_cargo INTEGER NOT NULL REFERENCES usuario(id_usuario),
                fecha_hora_cierre TEXT
            )
        """,

        "visita_participante" to """
            CREATE TABLE visita_participante (
                id_visita INTEGER NOT NULL REFERENCES visita(id_visita),
                id_usuario INTEGER NOT NULL REFERENCES usuario(id_usuario),
                PRIMARY KEY (id_visita, id_usuario)
            )
        """,

        "recepcion_acta" to """
            CREATE TABLE recepcion_acta (
                id_visita INTEGER PRIMARY KEY REFERENCES visita(id_visita),
                receptor_nombre TEXT NOT NULL,
                receptor_rut TEXT NOT NULL,
                receptor_cargo TEXT NOT NULL,
                fecha_hora_recepcion TEXT NOT NULL
            )
        """,

        "protocolo" to """
            CREATE TABLE protocolo (
                id_protocolo INTEGER PRIMARY KEY AUTOINCREMENT,
                uuid TEXT NOT NULL UNIQUE,
                numero TEXT NOT NULL UNIQUE,
                id_visita INTEGER NOT NULL REFERENCES visita(id_visita),
                id_equipo INTEGER NOT NULL REFERENCES equipo(id_equipo),
                id_usuario_registra INTEGER NOT NULL REFERENCES usuario(id_usuario),
                fecha_hora_prueba TEXT NOT NULL,
                temperatura_c REAL,
                humedad_pct REAL CHECK (humedad_pct IS NULL OR (humedad_pct >= 0 AND humedad_pct <= 100)),
                descripcion_mantenimiento TEXT,
                observaciones TEXT,
                id_estado INTEGER NOT NULL REFERENCES estado_protocolo(id_estado),
                id_protocolo_reemplazado INTEGER REFERENCES protocolo(id_protocolo),
                UNIQUE (id_visita, id_equipo)
            )
        """,

        "historial_estado" to """
            CREATE TABLE historial_estado (
                id_historial INTEGER PRIMARY KEY AUTOINCREMENT,
                id_protocolo INTEGER NOT NULL REFERENCES protocolo(id_protocolo),
                id_estado_anterior INTEGER REFERENCES estado_protocolo(id_estado),
                id_estado_nuevo INTEGER NOT NULL REFERENCES estado_protocolo(id_estado),
                id_usuario INTEGER NOT NULL REFERENCES usuario(id_usuario),
                fecha_hora TEXT NOT NULL,
                motivo TEXT
            )
        """,

        "prueba" to """
            CREATE TABLE prueba (
                id_prueba INTEGER PRIMARY KEY AUTOINCREMENT,
                id_protocolo INTEGER NOT NULL REFERENCES protocolo(id_protocolo),
                id_tipo_prueba INTEGER NOT NULL REFERENCES tipo_prueba(id_tipo_prueba),
                elemento TEXT NOT NULL,
                momento TEXT NOT NULL DEFAULT 'UNICA' CHECK (momento IN ('UNICA', 'PREVIA', 'POSTERIOR')),
                realizada INTEGER NOT NULL DEFAULT 1 CHECK (realizada IN (0, 1)),
                motivo_no_realizada TEXT,
                id_instrumento INTEGER REFERENCES instrumento(id_instrumento),
                id_resultado INTEGER REFERENCES resultado(id_resultado),
                comentario TEXT,
                UNIQUE (id_protocolo, id_tipo_prueba, elemento, momento),
                CHECK (realizada = 1 OR (motivo_no_realizada IS NOT NULL AND length(trim(motivo_no_realizada)) > 0))
            )
        """,

        "medicion" to """
            CREATE TABLE medicion (
                id_medicion INTEGER PRIMARY KEY AUTOINCREMENT,
                id_prueba INTEGER NOT NULL REFERENCES prueba(id_prueba),
                id_parametro INTEGER NOT NULL REFERENCES parametro(id_parametro),
                valor REAL NOT NULL,
                UNIQUE (id_prueba, id_parametro)
            )
        """
    )

    val INDICES: List<String> = listOf(
        "CREATE INDEX idx_protocolo_equipo_fecha ON protocolo (id_equipo, fecha_hora_prueba)",
        "CREATE INDEX idx_protocolo_estado ON protocolo (id_estado)",
        "CREATE INDEX idx_prueba_protocolo ON prueba (id_protocolo)",
        "CREATE INDEX idx_instalacion_cliente ON instalacion (id_usuario_cliente)",
        "CREATE INDEX idx_calibracion_instrumento ON calibracion (id_instrumento)"
    )
}

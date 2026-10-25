package cl.induztek.app.data

object DatosIniciales {

    val SENTENCIAS: List<String> = listOf(

        """
        INSERT INTO rol (id_rol, codigo, nombre) VALUES
            (1, 'TECNICO',      'Técnico de terreno'),
            (2, 'INGENIERO',    'Ingeniero de pruebas'),
            (3, 'JEFE_TECNICO', 'Jefe técnico'),
            (4, 'CLIENTE',      'Usuario cliente')
        """,

        "INSERT INTO region (id_region, nombre) VALUES (1, 'Región Metropolitana de Santiago')",
        """
        INSERT INTO comuna (id_comuna, id_region, nombre) VALUES
            (1, 1, 'Lampa'),
            (2, 1, 'San Bernardo'),
            (3, 1, 'Til Til'),
            (4, 1, 'La Florida'),
            (5, 1, 'Puente Alto'),
            (6, 1, 'Huechuraba')
        """,

        """
        INSERT INTO usuario (id_usuario, id_rol, rut, nombre, correo, password_hash, activo) VALUES
            (1, 1, '17.284.519-3', 'Matías Godoy Herrera',          'mgodoy@induztek.ejemplo',      '%HASH%', 1),
            (2, 1, '19.412.087-7', 'Javiera Tapia Muñoz',           'jtapia@induztek.ejemplo',      '%HASH%', 1),
            (3, 1, '26.104.583-4', 'Carlos Rondón Pérez',           'crondon@induztek.ejemplo',     '%HASH%', 1),
            (4, 1, '20.137.648-3', 'Diego Valenzuela Ríos',         'dvalenzuela@induztek.ejemplo', '%HASH%', 1),
            (5, 2, '15.906.334-8', 'Andrea Contreras Silva',        'acontreras@induztek.ejemplo',  '%HASH%', 1),
            (6, 2, '16.553.270-8', 'Sebastián Morales Gutiérrez',   'smorales@induztek.ejemplo',    '%HASH%', 1),
            (7, 3, '13.871.905-7', 'Ricardo Espinoza Lagos',        'respinoza@induztek.ejemplo',   '%HASH%', 1)
        """,

        """
        INSERT INTO usuario (id_usuario, id_rol, rut, nombre, correo, telefono, password_hash, activo,
                             giro, contacto_nombre, contacto_cargo) VALUES
            (8,  4, '76.482.915-8', 'Envasadora del Pacífico S.A.', 'mfuentes@envpacifico.ejemplo', '+56 9 0000 0101', '%HASH%', 1,
                 'Agroindustria', 'Marcela Fuentes Rojas', 'Jefa de Mantenimiento'),
            (9,  4, '96.731.540-0', 'Sanitaria Valle Central S.A.', 'jarancibia@svcentral.ejemplo', '+56 9 0000 0102', '%HASH%', 0,
                 'Servicios sanitarios', 'Jorge Arancibia Leiva', NULL),
            (10, 4, '77.105.362-9', 'Minera Cerro Alto SpA',        'pnavarro@cerroalto.ejemplo',   '+56 9 0000 0103', '%HASH%', 1,
                 'Minería', 'Patricio Navarro Soto', 'Jefe de Mantenimiento Eléctrico'),
            (11, 4, '76.903.218-5', 'Clínica Los Aromos Ltda.',     'tleiva@losaromos.ejemplo',     '+56 9 0000 0104', '%HASH%', 1,
                 'Salud', 'Tomás Leiva Araya', 'Supervisor Eléctrico'),
            (12, 4, '99.524.170-6', 'Cementos Lomas Blancas S.A.',  'lbravo@lomasblancas.ejemplo',  '+56 9 0000 0105', '%HASH%', 0,
                 'Construcción', 'Luis Bravo Castillo', NULL),
            (13, 4, '78.214.635-1', 'Data Center Andes Norte SpA',  'fordenes@andesnorte.ejemplo',  '+56 9 0000 0106', '%HASH%', 0,
                 'Tecnología', 'Felipe Órdenes Vera', NULL)
        """,

        """
        INSERT INTO instalacion (id_instalacion, id_usuario_cliente, nombre, ubicacion, id_comuna, observaciones_acceso) VALUES
            (1, 8,  'Planta Lampa',          'Sector industrial',        1, 'Inducción obligatoria de 30 min'),
            (2, 9,  'Planta Elevadora Sur',  'Sector rural poniente',    2, 'Sala eléctrica subterránea, sin señal'),
            (3, 10, 'Faena Til Til',         'Acceso por Ruta 5 Norte',  3, 'Sin señal en toda la faena'),
            (4, 11, 'Edificio Clínico',      'Zona urbana',              4, 'Cortes solo domingos 06:00–10:00'),
            (5, 12, 'Planta Molienda',       'Sector precordillera',     5, 'Uso obligatorio de respirador'),
            (6, 13, 'Site Huechuraba',       'Parque empresarial',       6, 'Acompañamiento permanente del cliente')
        """,

        """
        INSERT INTO tipo_equipo (id_tipo_equipo, nombre, clasificacion) VALUES
            (1, 'Transformador',      'PRIMARIO'),
            (2, 'Interruptor',        'PRIMARIO'),
            (3, 'Celda MT',           'PRIMARIO'),
            (4, 'Cable de poder',     'PRIMARIO'),
            (5, 'Relé de protección', 'SECUNDARIO')
        """,

        """
        INSERT INTO equipo (id_equipo, id_instalacion, tag, id_tipo_equipo, marca, modelo, numero_serie, anio_fabricacion,
                            potencia_kva, tension_primaria_kv, tension_secundaria_kv, corriente_nominal_a,
                            max_contactos_uohm, observaciones) VALUES
            (1, 1, 'TR-01',        1, 'Rhona',     NULL, 'RH-18733',      2012, 1500, 12, 0.4, NULL, NULL, NULL),
            (2, 1, 'TR-02',        1, 'Rhona',     NULL, 'RH-21904',      2017, 1000, 12, 0.4, NULL, NULL, NULL),
            (3, 2, 'TR-A',         1, 'Tusan',     NULL, 'TS-5521',       2009, 2500, 23, 0.4, NULL, NULL, NULL),
            (4, 4, 'CEL-MT-03',    3, 'Ormazabal', NULL, 'OR-77812',      2018, NULL, 12, NULL, 630, 60,   NULL),
            (5, 3, 'INT-52-1',     2, 'Schneider', NULL, 'SE-449021',     2015, NULL, 23, NULL, 1250, 45,  NULL),
            (6, 5, 'REL-50/51-A',  5, 'SEL',       NULL, 'SEL-751-30219', 2020, NULL, NULL, NULL, NULL, NULL, 'Sobrecorriente, ajuste 5 A'),
            (7, 6, 'CAB-MT-02',    4, 'Nexans',    NULL, 'NX-2201-B',     2019, NULL, 15, NULL, NULL, NULL, '3×240 mm², 180 m'),
            (8, 3, 'TR-SS-01',     1, 'ABB',       NULL, 'ABB-1180442',   2011, 5000, 23, 6.6, NULL, NULL, NULL)
        """,

        """
        INSERT INTO tipo_prueba (id_tipo_prueba, codigo, nombre) VALUES
            (1, 'AISLACION',      'Resistencia de aislación'),
            (2, 'TTR',            'Relación de transformación (TTR)'),
            (3, 'DEVANADOS',      'Resistencia de devanados'),
            (4, 'CONTACTOS',      'Resistencia de contactos'),
            (5, 'OPERACION_RELE', 'Prueba de operación de relé')
        """,

        """
        INSERT INTO tipo_equipo_prueba (id_tipo_equipo, id_tipo_prueba) VALUES
            (1, 1), (1, 2), (1, 3),
            (2, 1), (2, 4),
            (3, 1), (3, 4),
            (4, 1),
            (5, 5)
        """,

        """
        INSERT INTO parametro (id_parametro, id_tipo_prueba, codigo, nombre, unidad) VALUES
            (1,  1, 'TENSION_PRUEBA',       'Tensión de prueba',                 'kV'),
            (2,  1, 'LECTURA_1MIN',         'Lectura a 1 min',                   'MΩ'),
            (3,  1, 'LECTURA_10MIN',        'Lectura a 10 min',                  'MΩ'),
            (4,  2, 'POSICION_TAP',         'Posición del cambiador',            '—'),
            (5,  2, 'RELACION_TEORICA',     'Relación teórica',                  '—'),
            (6,  2, 'RELACION_MEDIDA',      'Relación medida',                   '—'),
            (7,  3, 'RESISTENCIA',          'Resistencia por fase',              'mΩ'),
            (8,  4, 'RESISTENCIA_CONTACTO', 'Resistencia de contacto por polo',  'µΩ'),
            (9,  5, 'CORRIENTE_AJUSTE',     'Corriente de ajuste',               'A'),
            (10, 5, 'CORRIENTE_OPERACION',  'Corriente medida de operación',     'A'),
            (11, 5, 'TIEMPO_ESPERADO',      'Tiempo esperado',                   'ms'),
            (12, 5, 'TIEMPO_MEDIDO',        'Tiempo medido',                     'ms')
        """,

        """
        INSERT INTO instrumento (id_instrumento, nombre, marca, modelo, numero_serie) VALUES
            (1, 'Megóhmetro 5 kV',                 'Megger', 'MIT525',   'MIT-0045821'),
            (2, 'Megóhmetro 10 kV',                'Megger', 'MIT1025',  'MIT-0071233'),
            (3, 'Medidor de relación (TTR)',       'Megger', 'TTR300',   'TTR-30044'),
            (4, 'Micro-ohmímetro',                 'Megger', 'DLRO10X',  'DLR-10882'),
            (5, 'Equipo de inyección secundaria',  'Omicron', 'CMC 356', 'CMC-HB411')
        """,

        """
        INSERT INTO calibracion (id_calibracion, id_instrumento, fecha_calibracion, fecha_vencimiento) VALUES
            (1, 1, '2026-03-15', '2027-03-15'),
            (2, 2, '2025-10-10', '2026-10-10'),
            (3, 3, '2026-06-02', '2027-06-02'),
            (4, 4, '2025-08-20', '2026-08-20'),
            (5, 5, '2026-01-05', '2027-01-05')
        """,

        """
        INSERT INTO estado_protocolo (id_estado, codigo) VALUES
            (1, 'BORRADOR'), (2, 'REGISTRADO_TERRENO'), (3, 'OBSERVADO'), (4, 'FORMALIZADO'), (5, 'ANULADO')
        """,
        "INSERT INTO resultado (id_resultado, codigo) VALUES (1, 'APROBADO'), (2, 'OBSERVADO'), (3, 'RECHAZADO')",

        """
        INSERT INTO visita (id_visita, numero_ot, id_instalacion, fecha, tipo_visita, id_ingeniero_cargo, fecha_hora_cierre) VALUES
            (1, 'OT-2026-1187',    1, '2026-09-26', 'SOLO_PRUEBAS', 5, '2026-09-26T13:42:00'),
            (2, 'OT-2026-1154',    4, '2026-09-20', 'SOLO_PRUEBAS', 6, '2026-09-20T09:55:00'),
            (3, 'OT-HIST-2024-01', 1, '2024-09-14', 'SOLO_PRUEBAS', 5, '2024-09-14T17:00:00'),
            (4, 'OT-HIST-2025-01', 1, '2025-09-13', 'SOLO_PRUEBAS', 5, '2025-09-13T17:00:00'),
            (5, 'OT-HIST-2026-01', 5, '2026-09-15', 'SOLO_PRUEBAS', 6, '2026-09-15T17:00:00'),
            (6, 'OT-HIST-2026-02', 3, '2026-09-08', 'SOLO_PRUEBAS', 6, '2026-09-08T17:00:00')
        """,

        """
        INSERT INTO visita_participante (id_visita, id_usuario) VALUES
            (1, 1), (2, 3), (3, 1), (4, 2), (5, 4), (6, 2)
        """,

        """
        INSERT INTO recepcion_acta (id_visita, receptor_nombre, receptor_rut, receptor_cargo, fecha_hora_recepcion) VALUES
            (1, 'Marcela Fuentes Rojas', '14.226.791-8', 'Jefa de Mantenimiento', '2026-09-26T13:42:00'),
            (2, 'Tomás Leiva Araya',     '18.390.452-3', 'Supervisor Eléctrico',  '2026-09-20T09:55:00')
        """,

        """
        INSERT INTO protocolo (id_protocolo, uuid, numero, id_visita, id_equipo, id_usuario_registra, fecha_hora_prueba,
                               temperatura_c, humedad_pct, observaciones, id_estado) VALUES
            (1, 'semilla-PT-2024-0312', 'PT-2024-0312', 3, 1, 1, '2024-09-14T09:00:00', 18, 55, NULL, 4),
            (2, 'semilla-PT-2025-0288', 'PT-2025-0288', 4, 1, 2, '2025-09-13T09:00:00', 21, 48, NULL, 4),
            (3, 'semilla-PT-2026-0401', 'PT-2026-0401', 1, 1, 1, '2026-09-26T09:40:00', 19, 60, NULL, 2),
            (4, 'semilla-PT-2026-0402', 'PT-2026-0402', 1, 2, 1, '2026-09-26T09:00:00', 19, 60, NULL, 2),
            (5, 'semilla-PT-2026-0389', 'PT-2026-0389', 2, 4, 3, '2026-09-20T09:00:00', 22, 45, 'Falta nº de serie del instrumento', 3),
            (6, 'semilla-PT-2026-0377', 'PT-2026-0377', 5, 6, 4, '2026-09-15T09:00:00', 24, 35, NULL, 4),
            (7, 'semilla-PT-2026-0360', 'PT-2026-0360', 6, 5, 2, '2026-09-08T09:00:00', 27, 20, NULL, 4)
        """,

        """
        INSERT INTO prueba (id_prueba, id_protocolo, id_tipo_prueba, elemento, momento, realizada, id_instrumento, id_resultado, comentario) VALUES
            (1, 1, 1, 'AT-T',   'UNICA', 1, NULL, 1, 'Sin historial previo'),
            (2, 2, 1, 'AT-T',   'UNICA', 1, NULL, 1, NULL),
            (3, 3, 1, 'AT-T',   'UNICA', 1, 1,    2, 'IP bajo: variación significativa contra la prueba anterior'),
            (4, 4, 1, 'AT-BT',  'UNICA', 1, NULL, 1, 'Resultado de ejemplo del Anexo 9 (sin mediciones detalladas)'),
            (5, 5, 4, 'Polo 1', 'UNICA', 1, NULL, 2, 'Resultado de ejemplo del Anexo 9 (sin mediciones detalladas)'),
            (6, 6, 5, 'Única',  'UNICA', 1, NULL, 1, 'Resultado de ejemplo del Anexo 9 (sin mediciones detalladas)'),
            (7, 7, 4, 'Polo 1', 'UNICA', 1, NULL, 3, 'Resultado de ejemplo del Anexo 9 (sin mediciones detalladas)')
        """,

        """
        INSERT INTO medicion (id_prueba, id_parametro, valor) VALUES
            (1, 1, 5), (1, 2, 12500), (1, 3, 31250),
            (2, 1, 5), (2, 2, 9800),  (2, 3, 21560),
            (3, 1, 5), (3, 2, 6100),  (3, 3, 10980)
        """
    )
}

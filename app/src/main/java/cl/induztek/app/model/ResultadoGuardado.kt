package cl.induztek.app.model

// =====================================================================
//  ResultadoGuardado.kt — qué pasó al intentar GUARDAR algo nuevo
//  ---------------------------------------------------------------------
//  Un INSERT puede fallar por varias razones y la pantalla necesita distinguirlas
//  para mostrar el mensaje correcto (formularios con retroalimentación, RNF-05):
//
//   Creado     -> se guardó; `id` es la clave primaria nueva.
//   Duplicado  -> ya existía (TAG repetido en la instalación, OT repetida, o el equipo
//                 ya tiene protocolo en esa visita). Trae el id del registro EXISTENTE,
//                 para poder ofrecer "usar el que ya está registrado" (HU-01).
//   Fallo      -> cualquier otro error (dato inválido, clave foránea inexistente...).
//
//  `sealed interface` = el compilador conoce TODAS las opciones, y obliga a que un `when`
//  las cubra todas (si se te olvida una, no compila).
//
//      when (val r = db.insertarEquipo(equipo)) {
//          is ResultadoGuardado.Creado   -> ...r.id
//          is ResultadoGuardado.Duplicado -> ...r.idExistente
//          ResultadoGuardado.Fallo       -> ...
//      }
// =====================================================================
sealed interface ResultadoGuardado {
    data class Creado(val id: Long) : ResultadoGuardado
    data class Duplicado(val idExistente: Long) : ResultadoGuardado
    data object Fallo : ResultadoGuardado
}

// Resultado de intentar iniciar sesión. Con 3 casos distintos la pantalla de login puede
// mostrar el mensaje correcto debajo del campo (formulario con retroalimentación visual).
sealed interface ResultadoLogin {
    data class Exito(val usuario: Usuario) : ResultadoLogin
    data object CredencialesInvalidas : ResultadoLogin
    data object CuentaInactiva : ResultadoLogin
}

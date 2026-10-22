package cl.induztek.app.model

sealed interface ResultadoGuardado {
    data class Creado(val id: Long) : ResultadoGuardado
    data class Duplicado(val idExistente: Long) : ResultadoGuardado
    data object Fallo : ResultadoGuardado
}

sealed interface ResultadoLogin {
    data class Exito(val usuario: Usuario) : ResultadoLogin
    data object CredencialesInvalidas : ResultadoLogin
    data object CuentaInactiva : ResultadoLogin
}

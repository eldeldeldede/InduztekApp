package cl.induztek.app.repository

import cl.induztek.app.local.AlmacenLocal
import cl.induztek.app.model.Usuario

// =====================================================================
//  Repositorios.kt — punto único de acceso a los repositorios (se crean una vez y se reutilizan)
//  Los ViewModels usan, por ejemplo, Repositorios.equipos.instalaciones(usuario).
// =====================================================================
object Repositorios {
    val usuarios: UsuarioRepositorio by lazy { UsuarioRepositorio() }
    val equipos: EquipoRepositorio by lazy { EquipoRepositorio(usuarios) }
    val protocolos: ProtocoloRepositorio by lazy { ProtocoloRepositorio(usuarios) }
    val clima: ClimaRepositorio by lazy { ClimaRepositorio() }

    // Quién tiene la sesión ahora (null = nadie). La caché local se separa por usuario.
    @Volatile
    var usuarioActual: Usuario? = null
        private set

    fun establecerUsuario(usuario: Usuario?) {
        usuarioActual = usuario
        AlmacenLocal.usuarioId = usuario?.idUsuario ?: 0
    }
}

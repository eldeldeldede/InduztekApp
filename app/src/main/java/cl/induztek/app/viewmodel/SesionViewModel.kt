package cl.induztek.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.induztek.app.data.SesionPreferencias
import cl.induztek.app.model.Usuario
import cl.induztek.app.network.ResultadoRed
import cl.induztek.app.repository.Repositorios
import cl.induztek.app.repository.Sincronizador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// =====================================================================
//  SesionViewModel.kt — quién tiene la sesión iniciada
//  ---------------------------------------------------------------------
//  Se crea UNA vez en AppNavegacion y se pasa a las pantallas, para que todas sepan qué usuario
//  (y qué rol) está conectado. El usuario viene del servidor (usuarios-service) y se guarda en
//  SharedPreferences (SesionPreferencias), así la sesión sobrevive aunque se cierre la app (RNF-03)
//  y funciona sin conexión.
// =====================================================================
class SesionViewModel(application: Application) : AndroidViewModel(application) {

    private val sesion = SesionPreferencias(application)

    private val _usuarioActual = MutableStateFlow(restaurar())
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    init {
        // Sesión recuperada al abrir la app: se envía lo pendiente y se actualiza la caché (si hay red)
        Repositorios.establecerUsuario(_usuarioActual.value)
        Sincronizador.alVolverConexion(_usuarioActual.value)
        validarSesionGuardada()
    }

    // Con red, la sesión guardada se contrasta con el servidor: si la cuenta ya no existe o fue desactivada se cierra,
    // y si cambió algún dato (por ejemplo el rol) se actualiza. Sin red se mantiene la sesión (modo sin conexión).
    private fun validarSesionGuardada() {
        val guardado = _usuarioActual.value ?: return
        viewModelScope.launch {
            when (val r = Repositorios.usuarios.obtener(guardado.idUsuario)) {
                is ResultadoRed.Exito -> {
                    if (!r.datos.activo) cerrarSesion()
                    else if (r.datos != guardado) {
                        _usuarioActual.value = r.datos
                        sesion.guardar(r.datos)
                        Repositorios.establecerUsuario(r.datos)
                    }
                }
                is ResultadoRed.ErrorServidor -> if (r.codigo == 404 || r.codigo == 403 || r.codigo == 401) cerrarSesion()
                ResultadoRed.SinConexion -> Unit
            }
        }
    }

    private fun restaurar(): Usuario? {
        val usuario = sesion.leer() ?: return null
        // Una cuenta desactivada (RF-22) no puede seguir con la sesión guardada
        if (!usuario.activo) {
            sesion.borrar()
            return null
        }
        return usuario
    }

    fun iniciarSesion(usuario: Usuario) {
        _usuarioActual.value = usuario
        sesion.guardar(usuario)
        Repositorios.establecerUsuario(usuario)
        Sincronizador.alVolverConexion(usuario)
    }

    // Se usa al cambiar la contraseña: el usuario es el mismo pero ya no debe cambiarla
    fun actualizarUsuario(usuario: Usuario) = iniciarSesion(usuario)

    fun cerrarSesion() {
        _usuarioActual.value = null
        sesion.borrar()
        Repositorios.establecerUsuario(null)
    }
}

package cl.induztek.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.induztek.app.data.SesionPreferencias
import cl.induztek.app.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
    }

    // Se usa al cambiar la contraseña: el usuario es el mismo pero ya no debe cambiarla
    fun actualizarUsuario(usuario: Usuario) = iniciarSesion(usuario)

    fun cerrarSesion() {
        _usuarioActual.value = null
        sesion.borrar()
    }
}

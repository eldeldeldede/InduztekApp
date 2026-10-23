package cl.induztek.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.induztek.app.data.InduztekDbHelper
import cl.induztek.app.data.SesionPreferencias
import cl.induztek.app.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// =====================================================================
//  SesionViewModel.kt — quién tiene la sesión iniciada
//  ---------------------------------------------------------------------
//  Es la parte "usuarioActual" del UsuariosViewModel de MiRegistro, separada para que TODAS
//  las pantallas puedan saber qué usuario (y qué rol) está conectado. Se crea UNA vez en
//  AppNavegacion y se pasa a las pantallas.
//
//   - SQLite       -> los datos del usuario (InduztekDbHelper.buscarUsuarioPorId)
//   - SharedPreferences -> solo el ID del usuario conectado (SesionPreferencias), así la sesión
//                     sobrevive aunque se cierre la app (RNF-03)
// =====================================================================
class SesionViewModel(application: Application) : AndroidViewModel(application) {

    private val db = InduztekDbHelper(application)
    private val sesion = SesionPreferencias(application)

    // Al abrir la app: si hay un id guardado, se busca ese usuario en la BD
    private val _usuarioActual = MutableStateFlow(restaurar())
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    private fun restaurar(): Usuario? {
        if (!sesion.hayDatosDeSesion()) return null
        val usuario = db.buscarUsuarioPorId(sesion.leer())
        // Si el usuario ya no existe o fue desactivado (RF-22), la sesión guardada no vale
        if (usuario == null || !usuario.activo) {
            sesion.borrar()
            return null
        }
        return usuario
    }

    fun iniciarSesion(usuario: Usuario) {
        _usuarioActual.value = usuario
        sesion.guardar(usuario.idUsuario)
    }

    fun cerrarSesion() {
        _usuarioActual.value = null
        sesion.borrar()
    }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}

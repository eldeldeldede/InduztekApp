package cl.induztek.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.induztek.app.data.InduztekDbHelper
import cl.induztek.app.data.SesionPreferencias
import cl.induztek.app.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SesionViewModel(application: Application) : AndroidViewModel(application) {

    private val db = InduztekDbHelper(application)
    private val sesion = SesionPreferencias(application)

    private val _usuarioActual = MutableStateFlow(restaurar())
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    private fun restaurar(): Usuario? {
        if (!sesion.hayDatosDeSesion()) return null
        val usuario = db.buscarUsuarioPorId(sesion.leer())

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

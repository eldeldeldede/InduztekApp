package cl.induztek.app.model

// Lista de empresas cliente (cuentas con rol CLIENTE) para el personal de Induztek.
data class ClientesUiState(
    val clientes: List<Usuario> = emptyList(),
    val filtro: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null,
    // Solo el Jefe técnico activa/desactiva cuentas (RF-22); técnico e ingeniero crean y editan.
    val puedeDesactivar: Boolean = false,

    // Diálogo "Editar cliente"
    val clienteEnEdicion: Usuario? = null,
    val nombre: String = "",
    val telefono: String = "",
    val giro: String = "",
    val contactoNombre: String = "",
    val contactoCargo: String = "",
    val errorNombre: String? = null,
    val errorTelefono: String? = null,
    val errorEdicion: String? = null,
    val guardando: Boolean = false
) {
    val visibles: List<Usuario>
        get() = if (filtro.isBlank()) clientes
        else clientes.filter { it.nombre.contains(filtro, true) || it.rut.contains(filtro, true) || it.correo.contains(filtro, true) }
}

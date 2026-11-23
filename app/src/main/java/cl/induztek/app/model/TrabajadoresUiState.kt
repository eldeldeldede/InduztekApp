package cl.induztek.app.model

// Estado de la pantalla "Trabajadores" del Jefe técnico: personal de Induztek (sin clientes).
data class TrabajadoresUiState(
    val trabajadores: List<Usuario> = emptyList(),
    val filtro: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null,
    val idActor: Long = 0,
    // Perfil abierto en el diálogo de solo lectura
    val seleccionado: Usuario? = null
) {
    val visibles: List<Usuario>
        get() = if (filtro.isBlank()) trabajadores
        else trabajadores.filter {
            it.nombre.contains(filtro, true) || it.rut.contains(filtro, true) ||
                it.correo.contains(filtro, true) || it.codigoRol.etiqueta.contains(filtro, true)
        }
}

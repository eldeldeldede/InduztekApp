package cl.induztek.app.model

enum class CodigoRol(val id: Long, val codigo: String, val etiqueta: String) {
    TECNICO(1, "TECNICO", "Técnico de terreno"),
    INGENIERO(2, "INGENIERO", "Ingeniero de pruebas"),
    JEFE_TECNICO(3, "JEFE_TECNICO", "Jefe técnico"),
    CLIENTE(4, "CLIENTE", "Usuario cliente");

    companion object {
        fun desdeCodigo(codigo: String): CodigoRol = entries.first { it.codigo == codigo }
    }
}

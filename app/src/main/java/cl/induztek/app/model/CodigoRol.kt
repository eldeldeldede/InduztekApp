package cl.induztek.app.model

enum class CodigoRol(val id: Long, val codigo: String, val etiqueta: String){
    TECNICO(1,"TECNICO", "Técnico"),
    INGENIERO(2, "INGENIERO", "Ingeniero"),
    JEFE_TECNICO(3, "JEFE_TÉCNICO", "Jefe Técnico"),
    CLIENTE(4, "CLIENTE", "Cliente");

    companion object {
        fun desdeCodigo(codigo: String): CodigoRol =entries.first{ it.codigo == codigo }
    }
}

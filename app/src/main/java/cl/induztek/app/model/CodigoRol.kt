package cl.induztek.app.model

// =====================================================================
//  CodigoRol.kt
//  ---------------------------------------------------------------------
//  Los 4 perfiles del caso (Anexo 2). Cada uno es una fila de la tabla "rol".
//
//  ¿Por qué un enum Y una tabla?
//   - La TABLA guarda los roles en la base de datos (usuario.id_rol apunta a ella).
//   - El ENUM nos deja escribir  if (rol == CodigoRol.CLIENTE)  en el código,
//     en vez de comparar números sueltos como  if (idRol == 4).
//
//  OJO: el `id` de cada valor DEBE coincidir con el id que se inserta en
//  DatosIniciales.kt. Si cambias uno, cambia el otro.
//
//  CORREGIDO: los códigos van SIN tilde ("TECNICO", "JEFE_TECNICO"), como en el MER.
//  Con tilde, el código no coincidiría con lo que se guarda en la base.
// =====================================================================
enum class CodigoRol(val id: Long, val codigo: String, val etiqueta: String) {
    TECNICO(1, "TECNICO", "Técnico de terreno"),
    INGENIERO(2, "INGENIERO", "Ingeniero de pruebas"),
    JEFE_TECNICO(3, "JEFE_TECNICO", "Jefe técnico"),
    CLIENTE(4, "CLIENTE", "Usuario cliente");

    companion object {
        // Convierte el texto guardado en la base ("TECNICO") al enum.
        fun desdeCodigo(codigo: String): CodigoRol = entries.first { it.codigo == codigo }
    }
}

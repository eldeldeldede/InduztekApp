package cl.induztek.app.model

// =====================================================================
//  Usuario.kt
//  ---------------------------------------------------------------------
//  Una fila de la tabla "usuario". Según el MER, aquí viven DOS tipos de personas:
//   - Personal de Induztek (Técnico, Ingeniero, Jefe técnico)
//   - Cuentas de EMPRESAS cliente (rol CLIENTE): una cuenta por empresa.
//
//  Por eso `giro`, `contactoNombre` y `contactoCargo` son nullable (String?):
//  solo se usan en las cuentas CLIENTE y quedan en null para el personal.
//
//  CORREGIDO respecto a la versión anterior:
//   - Faltaba `rut` (es único y debe tener dígito verificador válido, RN-03).
//   - password_hash -> passwordHash (camelCase).
//   - telefono, giro, contactoNombre y contactoCargo pasan a nullable.
//   - Se agrega `codigoRol`: NO es columna de "usuario", llega gracias al JOIN con "rol"
//     (la misma idea que `rolNombre` en MiRegistro).
// =====================================================================
data class Usuario(
    val idUsuario: Long = 0,            // 0 = "todavía no guardado en la BD"
    val idRol: Long,                    // clave foránea -> rol.id_rol
    val codigoRol: CodigoRol,           // viene del JOIN, no es columna de "usuario"
    val rut: String,
    val nombre: String,                 // nombre de la persona o razón social de la empresa
    val correo: String,                 // es la credencial de ingreso (login)
    val telefono: String? = null,
    val passwordHash: String,           // NUNCA la clave en texto plano
    val activo: Boolean = true,         // se desactiva, no se borra (RF-22)
    val giro: String? = null,           // solo CLIENTE
    val contactoNombre: String? = null, // solo CLIENTE
    val contactoCargo: String? = null   // solo CLIENTE
) {
    // Atajo para el código de la UI: if (usuario.esCliente) ...
    val esCliente: Boolean get() = codigoRol == CodigoRol.CLIENTE
}

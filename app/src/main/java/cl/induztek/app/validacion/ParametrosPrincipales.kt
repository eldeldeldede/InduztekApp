package cl.induztek.app.validacion

object ParametrosPrincipales {

    private val PRINCIPAL_POR_TIPO_PRUEBA = mapOf(
        "AISLACION" to "LECTURA_1MIN",
        "TTR" to "RELACION_MEDIDA",
        "DEVANADOS" to "RESISTENCIA",
        "CONTACTOS" to "RESISTENCIA_CONTACTO",
        "OPERACION_RELE" to "TIEMPO_MEDIDO"
    )

    fun esPrincipal(codigoTipoPrueba: String, codigoParametro: String): Boolean =
        PRINCIPAL_POR_TIPO_PRUEBA[codigoTipoPrueba] == codigoParametro
}

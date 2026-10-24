package cl.induztek.app.validacion

object ParametrosPrincipales {

    fun esPrincipal(codigoTipoPrueba: String, codigoParametro: String): Boolean =
        PruebaElectrica.desdeCodigo(codigoTipoPrueba)?.esPrincipal(codigoParametro) ?: false
}

package cl.induztek.app.validacion

import cl.induztek.app.model.TipoResultado

// =====================================================================
//  EvaluadorPrueba.kt — "dados los valores medidos, ¿qué resultado tiene la prueba?"
//  ---------------------------------------------------------------------
//  Junta en UN solo lugar la decisión de qué criterio de CriteriosResultado aplicar a cada
//  tipo de prueba (RN-06 a RN-08b). Así la pantalla de mediciones no tiene ningún `if` de
//  negocio: el ViewModel le pasa los valores (ya en unidad base) y recibe un resultado.
//  Son funciones puras (sin Android): se prueban con tests unitarios.
//
//  Las claves del mapa `valores` son el `codigo` del parámetro (ver DatosIniciales.kt).
// =====================================================================
object EvaluadorPrueba {

    // resultado = null cuando no se puede calcular (faltan datos o el resultado es manual).
    // detalle   = texto corto con el cálculo, para mostrar al técnico ("IP = 1,79").
    data class Evaluacion(val resultado: TipoResultado?, val detalle: String? = null)

    // RN-08c: la resistencia de devanados NO tiene criterio automático: el ingeniero o el
    // técnico elige el resultado. Los contactos también, si el equipo no trae máximo de placa.
    fun requiereResultadoManual(codigoTipoPrueba: String, maxContactosUohm: Double?): Boolean =
        codigoTipoPrueba == "DEVANADOS" || (codigoTipoPrueba == "CONTACTOS" && maxContactosUohm == null)

    // Interruptor y celda MT miden la aislación solo a 1 minuto (RN-05): no hay IP.
    fun aislacionSoloUnMinuto(idTipoEquipo: Long): Boolean = idTipoEquipo == 2L || idTipoEquipo == 3L

    fun evaluar(
        codigoTipoPrueba: String,
        valores: Map<String, Double>,
        maxContactosUohm: Double? = null,
        soloUnMinuto: Boolean = false,
        opero: Boolean = true
    ): Evaluacion = when (codigoTipoPrueba) {

        "AISLACION" -> {
            val l1 = valores["LECTURA_1MIN"]
            val l10 = valores["LECTURA_10MIN"]
            when {
                l1 == null -> Evaluacion(null)
                soloUnMinuto -> Evaluacion(CriteriosResultado.resultadoAislacionSoloUnMinuto(l1))
                l10 == null -> Evaluacion(null)
                else -> {
                    val ip = CriteriosResultado.indicePolarizacion(l1, l10)
                    Evaluacion(
                        CriteriosResultado.resultadoAislacion(l1, l10),
                        ip?.let { "IP = ${Unidades.formatear(it)}" }
                    )
                }
            }
        }

        "TTR" -> {
            val teorica = valores["RELACION_TEORICA"]
            val medida = valores["RELACION_MEDIDA"]
            if (teorica == null || medida == null) Evaluacion(null)
            else Evaluacion(
                CriteriosResultado.resultadoTtr(teorica, medida),
                CriteriosResultado.errorTtr(teorica, medida)?.let { "Error = ${Unidades.formatear(it)} %" }
            )
        }

        "CONTACTOS" -> {
            val valor = valores["RESISTENCIA_CONTACTO"]
            if (valor == null || maxContactosUohm == null) Evaluacion(null)
            else Evaluacion(
                CriteriosResultado.resultadoContactos(valor, maxContactosUohm),
                CriteriosResultado.porcentajeSobreMaximo(valor, maxContactosUohm)
                    ?.let { "${Unidades.formatear(it, 1)} % del máximo de placa" }
            )
        }

        "OPERACION_RELE" -> {
            val esperado = valores["TIEMPO_ESPERADO"]
            val medido = valores["TIEMPO_MEDIDO"]
            when {
                !opero -> Evaluacion(TipoResultado.RECHAZADO, "El relé no operó")
                esperado == null || medido == null -> Evaluacion(null)
                else -> Evaluacion(
                    CriteriosResultado.resultadoRele(esperado, medido, true),
                    CriteriosResultado.diferenciaTiempo(esperado, medido)
                        ?.let { "Diferencia = ${Unidades.formatear(it, 1)} %" }
                )
            }
        }

        // DEVANADOS y cualquier otro: resultado manual
        else -> Evaluacion(null)
    }
}

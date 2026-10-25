package cl.induztek.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.induztek.app.R
import cl.induztek.app.model.Equipo
import cl.induztek.app.model.FormularioPrueba
import cl.induztek.app.model.Instrumento
import cl.induztek.app.model.MedicionesUiState
import cl.induztek.app.model.Momento
import cl.induztek.app.model.Parametro
import cl.induztek.app.model.PruebaRegistrada
import cl.induztek.app.model.TipoEstadoProtocolo
import cl.induztek.app.model.TipoPrueba
import cl.induztek.app.model.TipoResultado
import cl.induztek.app.ui.components.BotonPrincipal
import cl.induztek.app.ui.components.BotonSecundario
import cl.induztek.app.ui.components.CampoTexto
import cl.induztek.app.ui.components.EtiquetaEstado
import cl.induztek.app.ui.components.EtiquetaResultado
import cl.induztek.app.ui.components.ListaDesplegable
import cl.induztek.app.ui.components.MensajeError
import cl.induztek.app.ui.components.TarjetaSeccion
import cl.induztek.app.ui.theme.Advertencia
import cl.induztek.app.ui.theme.AdvertenciaFondo
import cl.induztek.app.ui.theme.Dimens
import cl.induztek.app.ui.theme.Exito
import cl.induztek.app.ui.theme.InduztekAppTheme
import cl.induztek.app.validacion.UnidadAislacion
import cl.induztek.app.validacion.UnidadBaja
import cl.induztek.app.viewmodel.MedicionesViewModel

data class MedicionesAcciones(
    val onTemperaturaCambia: (String) -> Unit = {},
    val onHumedadCambia: (String) -> Unit = {},
    val onGuardarCondiciones: () -> Unit = {},
    val onTipoPruebaSeleccionado: (Long) -> Unit = {},
    val onElementoCambia: (String) -> Unit = {},
    val onMomentoCambia: (Momento) -> Unit = {},
    val onRealizadaCambia: (Boolean) -> Unit = {},
    val onMotivoCambia: (String) -> Unit = {},
    val onValorCambia: (String, String) -> Unit = { _, _ -> },
    val onUnidadAislacionCambia: (UnidadAislacion) -> Unit = {},
    val onUnidadBajaCambia: (UnidadBaja) -> Unit = {},
    val onOperoCambia: (Boolean) -> Unit = {},
    val onInstrumentoSeleccionado: (Long) -> Unit = {},
    val onResultadoManualSeleccionado: (TipoResultado) -> Unit = {},
    val onGuardarPrueba: () -> Unit = {},
    val onEliminarPrueba: (Long) -> Unit = {},
    val onRegistrarEnTerreno: () -> Unit = {},
    val onMotivoRevisionCambia: (String) -> Unit = {},
    val onFormalizar: () -> Unit = {},
    val onObservar: () -> Unit = {},
    val onAnular: () -> Unit = {}
)

@Composable
fun MedicionesScreen(
    onVolver: () -> Unit,
    onCerrarVisita: (Long) -> Unit,
    viewModel: MedicionesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MedicionesContent(
        uiState = uiState,
        onVolver = onVolver,
        onCerrarVisita = onCerrarVisita,
        acciones = MedicionesAcciones(
            onTemperaturaCambia = viewModel::onTemperaturaCambia,
            onHumedadCambia = viewModel::onHumedadCambia,
            onGuardarCondiciones = viewModel::onGuardarCondiciones,
            onTipoPruebaSeleccionado = viewModel::onTipoPruebaSeleccionado,
            onElementoCambia = viewModel::onElementoCambia,
            onMomentoCambia = viewModel::onMomentoCambia,
            onRealizadaCambia = viewModel::onRealizadaCambia,
            onMotivoCambia = viewModel::onMotivoCambia,
            onValorCambia = viewModel::onValorCambia,
            onUnidadAislacionCambia = viewModel::onUnidadAislacionCambia,
            onUnidadBajaCambia = viewModel::onUnidadBajaCambia,
            onOperoCambia = viewModel::onOperoCambia,
            onInstrumentoSeleccionado = viewModel::onInstrumentoSeleccionado,
            onResultadoManualSeleccionado = viewModel::onResultadoManualSeleccionado,
            onGuardarPrueba = viewModel::onGuardarPrueba,
            onEliminarPrueba = viewModel::onEliminarPrueba,
            onRegistrarEnTerreno = viewModel::onRegistrarEnTerreno,
            onMotivoRevisionCambia = viewModel::onMotivoRevisionCambia,
            onFormalizar = viewModel::onFormalizar,
            onObservar = viewModel::onObservar,
            onAnular = viewModel::onAnular
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicionesContent(
    uiState: MedicionesUiState,
    onVolver: () -> Unit,
    acciones: MedicionesAcciones,
    onCerrarVisita: (Long) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.protocolo?.numero ?: stringResource(R.string.titulo_mediciones)) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            painter = painterResource(R.drawable.ic_volver),
                            contentDescription = stringResource(R.string.cd_volver)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        val protocolo = uiState.protocolo
        if (protocolo == null) {
            if (uiState.cargado) {
                Text(
                    text = stringResource(R.string.protocolo_no_encontrado),
                    modifier = Modifier.padding(innerPadding).padding(Dimens.espacioMedio)
                )
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            EncabezadoProtocolo(uiState)

            if (!uiState.puedeModificar) {
                Text(
                    text = stringResource(R.string.protocolo_solo_lectura),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            TarjetaSeccion(titulo = stringResource(R.string.seccion_condiciones)) {
                CampoTexto(
                    valor = uiState.temperatura,
                    onValorCambia = acciones.onTemperaturaCambia,
                    etiqueta = stringResource(R.string.campo_temperatura), icono = R.drawable.ic_termometro,
                    sufijo = "°C",
                    error = uiState.errorTemperatura,
                    tipoTeclado = KeyboardType.Decimal,
                    soloLectura = !uiState.puedeModificar
                )
                CampoTexto(
                    valor = uiState.humedad,
                    onValorCambia = acciones.onHumedadCambia,
                    etiqueta = stringResource(R.string.campo_humedad), icono = R.drawable.ic_gota,
                    sufijo = "%",
                    error = uiState.errorHumedad,
                    tipoTeclado = KeyboardType.Decimal,
                    soloLectura = !uiState.puedeModificar
                )
                if (uiState.puedeModificar) {
                    BotonSecundario(
                        texto = stringResource(R.string.btn_guardar_condiciones),
                        onClick = acciones.onGuardarCondiciones
                    )
                }
            }

            Text(stringResource(R.string.seccion_pruebas_registradas), style = MaterialTheme.typography.titleMedium)
            if (uiState.pruebas.isEmpty()) {
                Text(stringResource(R.string.sin_pruebas), style = MaterialTheme.typography.bodyMedium)
            }
            uiState.pruebas.forEach { prueba ->
                TarjetaPrueba(
                    prueba = prueba,
                    puedeEliminar = uiState.puedeModificar,
                    onEliminar = { acciones.onEliminarPrueba(prueba.idPrueba) }
                )
            }

            if (uiState.tiposSinRegistro.isNotEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = AdvertenciaFondo, contentColor = Advertencia)) {
                    Text(
                        text = stringResource(
                            R.string.pruebas_faltantes,
                            uiState.tiposSinRegistro.joinToString { it.nombre }
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(Dimens.espacioMedio)
                    )
                }
            }

            uiState.motivoUltimoCambio?.let { motivo ->
                Card(colors = CardDefaults.cardColors(containerColor = AdvertenciaFondo, contentColor = Advertencia)) {
                    Text(
                        text = stringResource(R.string.motivo_ultimo_cambio, motivo),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(Dimens.espacioMedio)
                    )
                }
            }

            if (uiState.puedeRevisar || uiState.puedeAnular) {
                TarjetaRevision(uiState, acciones)
            }

            if (uiState.puedeModificar) {
                FormularioNuevaPrueba(uiState, acciones)
            }

            AnimatedVisibility(visible = uiState.mensaje != null) {
                Text(
                    text = uiState.mensaje.orEmpty(),
                    color = Exito,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            uiState.errorGeneral?.let { MensajeError(mensaje = it) }

            val puedeRegistrar = uiState.puedeModificar &&
                (uiState.estado == TipoEstadoProtocolo.BORRADOR || uiState.estado == TipoEstadoProtocolo.OBSERVADO)
            if (puedeRegistrar) {
                BotonPrincipal(
                    texto = stringResource(R.string.btn_registrar_terreno),
                    onClick = acciones.onRegistrarEnTerreno,
                    icono = R.drawable.ic_check
                )
            }

            uiState.visita?.let { visita ->
                BotonSecundario(
                    texto = stringResource(R.string.btn_ir_cierre_visita),
                    onClick = { onCerrarVisita(visita.idVisita) },
                    icono = R.drawable.ic_protocolo,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(Dimens.espacioGrande))
        }
    }
}

@Composable
private fun TarjetaRevision(uiState: MedicionesUiState, acciones: MedicionesAcciones) {
    var confirmando by remember { mutableStateOf<TipoEstadoProtocolo?>(null) }

    TarjetaSeccion(titulo = stringResource(R.string.seccion_revision)) {
        CampoTexto(
            valor = uiState.motivoRevision,
            onValorCambia = acciones.onMotivoRevisionCambia,
            etiqueta = stringResource(R.string.campo_motivo_revision),
            icono = R.drawable.ic_nota,
            error = uiState.errorMotivoRevision,
            lineasMaximas = 3
        )
        if (uiState.puedeRevisar) {
            BotonPrincipal(
                texto = stringResource(R.string.btn_formalizar),
                onClick = { confirmando = TipoEstadoProtocolo.FORMALIZADO },
                icono = R.drawable.ic_check,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)) {
            if (uiState.puedeRevisar) {
                BotonSecundario(
                    texto = stringResource(R.string.btn_observar),
                    onClick = acciones.onObservar,
                    icono = R.drawable.ic_advertencia,
                    modifier = Modifier.weight(1f)
                )
            }
            if (uiState.puedeAnular) {
                BotonSecundario(
                    texto = stringResource(R.string.btn_anular),
                    onClick = { confirmando = TipoEstadoProtocolo.ANULADO },
                    icono = R.drawable.ic_salir,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    confirmando?.let { accion ->
        val formalizar = accion == TipoEstadoProtocolo.FORMALIZADO
        AlertDialog(
            onDismissRequest = { confirmando = null },
            title = {
                Text(stringResource(if (formalizar) R.string.confirmar_formalizar_titulo else R.string.confirmar_anular_titulo))
            },
            text = {
                Text(stringResource(if (formalizar) R.string.confirmar_formalizar_texto else R.string.confirmar_anular_texto))
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmando = null
                    if (formalizar) acciones.onFormalizar() else acciones.onAnular()
                }) { Text(stringResource(R.string.btn_confirmar)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmando = null }) { Text(stringResource(R.string.btn_cancelar)) }
            }
        )
    }
}

@Composable
private fun EncabezadoProtocolo(uiState: MedicionesUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            uiState.equipo?.let {
                Text(it.tipoEquipoNombre, style = MaterialTheme.typography.labelLarge)
                Text(it.tag, style = MaterialTheme.typography.headlineSmall)
            }
            uiState.visita?.let {
                Text(stringResource(R.string.dato_visita, it.numeroOt, it.tipoVisita.etiqueta))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                EtiquetaEstado(uiState.estado, esCliente = false)
                EtiquetaResultado(uiState.resultadoGlobal)
            }
        }
    }
}

@Composable
private fun TarjetaPrueba(prueba: PruebaRegistrada, puedeEliminar: Boolean, onEliminar: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(prueba.nombreTipoPrueba, style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = "${prueba.elemento} · ${prueba.momento.etiqueta}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                if (prueba.realizada) EtiquetaResultado(prueba.resultado)
                else Text(stringResource(R.string.no_realizada), style = MaterialTheme.typography.labelLarge)
            }
            prueba.lineas.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
            prueba.motivoNoRealizada?.let {
                Text(stringResource(R.string.motivo_no_realizada, it), style = MaterialTheme.typography.bodyMedium)
            }
            if (puedeEliminar) {
                TextButton(onClick = onEliminar) { Text(stringResource(R.string.btn_eliminar_prueba)) }
            }
        }
    }
}

@Composable
private fun FormularioNuevaPrueba(uiState: MedicionesUiState, acciones: MedicionesAcciones) {
    val f = uiState.formulario
    val tipo = uiState.tipoPruebaElegido

    TarjetaSeccion(titulo = stringResource(R.string.seccion_nueva_prueba)) {
        ListaDesplegable(
            etiqueta = stringResource(R.string.campo_tipo_prueba), icono = R.drawable.ic_protocolo,
            seleccionado = tipo?.nombre ?: "",
            opciones = uiState.tiposPrueba,
            textoDe = { it.nombre },
            onSeleccionar = { acciones.onTipoPruebaSeleccionado(it.idTipoPrueba) }
        )

        if (tipo != null) {
            CampoTexto(
                valor = f.elemento,
                onValorCambia = acciones.onElementoCambia,
                etiqueta = stringResource(R.string.campo_elemento), icono = R.drawable.ic_buscar,
                error = f.errores["elemento"]
            )
            if (uiState.momentosPermitidos.size > 1) {
                ListaDesplegable(
                    etiqueta = stringResource(R.string.campo_momento), icono = R.drawable.ic_historial,
                    seleccionado = f.momento.etiqueta,
                    opciones = uiState.momentosPermitidos,
                    textoDe = { it.etiqueta },
                    onSeleccionar = acciones.onMomentoCambia
                )
            }

            FilaInterruptor(
                texto = stringResource(R.string.pregunta_realizada),
                marcado = f.realizada,
                onCambia = acciones.onRealizadaCambia
            )

            if (f.realizada) {
                CamposDeMedicion(uiState, acciones)
            } else {

                CampoTexto(
                    valor = f.motivo,
                    onValorCambia = acciones.onMotivoCambia,
                    etiqueta = stringResource(R.string.campo_motivo_no_realizada), icono = R.drawable.ic_nota,
                    error = f.errores["motivo"],
                    lineasMaximas = 3
                )
            }

            BotonPrincipal(
                texto = stringResource(R.string.btn_guardar_prueba),
                onClick = acciones.onGuardarPrueba,
                icono = R.drawable.ic_check
            )
        }
    }
}

@Composable
private fun CamposDeMedicion(uiState: MedicionesUiState, acciones: MedicionesAcciones) {
    val f = uiState.formulario
    val codigoTipo = uiState.tipoPruebaElegido?.codigo

    when (codigoTipo) {
        "AISLACION" -> ListaDesplegable(
            etiqueta = stringResource(R.string.campo_unidad_lectura), icono = R.drawable.ic_medidor,
            seleccionado = f.unidadAislacion.simbolo,
            opciones = UnidadAislacion.entries.toList(),
            textoDe = { it.simbolo },
            onSeleccionar = acciones.onUnidadAislacionCambia
        )
        "DEVANADOS", "CONTACTOS" -> ListaDesplegable(
            etiqueta = stringResource(R.string.campo_unidad_lectura), icono = R.drawable.ic_medidor,
            seleccionado = f.unidadBaja.simbolo,
            opciones = UnidadBaja.entries.toList(),
            textoDe = { it.simbolo },
            onSeleccionar = acciones.onUnidadBajaCambia
        )
        "OPERACION_RELE" -> FilaInterruptor(
            texto = stringResource(R.string.pregunta_opero),
            marcado = f.opero,
            onCambia = acciones.onOperoCambia
        )
    }

    uiState.parametrosVisibles.forEach { parametro ->
        CampoTexto(
            valor = f.valores[parametro.codigo].orEmpty(),
            onValorCambia = { acciones.onValorCambia(parametro.codigo, it) },
            etiqueta = parametro.nombre,
            icono = R.drawable.ic_medidor,
            sufijo = sufijoDe(parametro, f),
            error = f.errores[parametro.codigo],
            tipoTeclado = KeyboardType.Decimal
        )
    }

    if (uiState.instrumentosParaPrueba.isEmpty()) {
        Card(colors = CardDefaults.cardColors(containerColor = AdvertenciaFondo, contentColor = Advertencia)) {
            Text(
                text = stringResource(R.string.sin_instrumentos_vigentes),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(Dimens.espacioMedio)
            )
        }
        f.errores["instrumento"]?.let { MensajeError(mensaje = it) }
    } else {
        ListaDesplegable(
            etiqueta = stringResource(R.string.campo_instrumento), icono = R.drawable.ic_modelo,
            seleccionado = uiState.instrumentosParaPrueba.firstOrNull { it.idInstrumento == f.idInstrumento }?.nombre ?: "",
            opciones = uiState.instrumentosParaPrueba,
            textoDe = { it.nombre },
            onSeleccionar = { acciones.onInstrumentoSeleccionado(it.idInstrumento) },
            error = f.errores["instrumento"]
        )
        Text(stringResource(R.string.nota_instrumentos_vigentes), style = MaterialTheme.typography.bodySmall)
    }

    if (uiState.requiereResultadoManual) {
        ListaDesplegable(
            etiqueta = stringResource(R.string.campo_resultado_manual), icono = R.drawable.ic_check,
            seleccionado = f.resultadoManual?.etiqueta ?: "",
            opciones = TipoResultado.entries.toList(),
            textoDe = { it.etiqueta },
            onSeleccionar = acciones.onResultadoManualSeleccionado,
            error = f.errores["resultado"]
        )
    } else {

        f.vistaPrevia?.let { vista ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
            ) {
                Text(stringResource(R.string.resultado_calculado), style = MaterialTheme.typography.labelLarge)
                EtiquetaResultado(vista.resultado)
                vista.detalle?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            }
        }
        f.errores["resultado"]?.let { MensajeError(mensaje = it) }
    }
}

private fun sufijoDe(parametro: Parametro, f: FormularioPrueba): String? = when (parametro.codigo) {
    "LECTURA_1MIN", "LECTURA_10MIN" -> f.unidadAislacion.simbolo
    "RESISTENCIA", "RESISTENCIA_CONTACTO" -> f.unidadBaja.simbolo
    else -> parametro.unidad.takeIf { it != "—" }
}

@Composable
private fun FilaInterruptor(texto: String, marcado: Boolean, onCambia: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(texto, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = marcado, onCheckedChange = onCambia)
    }
}

private val equipoEjemplo = Equipo(
    1, 1, "TR-01", 1, marca = "Rhona", numeroSerie = "RH-18733", tipoEquipoNombre = "Transformador"
)

@Preview(showBackground = true, heightDp = 1800)
@Composable
private fun MedicionesPreview() {
    InduztekAppTheme {
        MedicionesContent(
            uiState = MedicionesUiState(
                cargado = true,
                protocolo = cl.induztek.app.model.Protocolo(
                    uuid = "x", numero = "PT-2026-T1-0001", idVisita = 1, idEquipo = 1, idUsuario = 1,
                    fechaHoraPrueba = java.time.LocalDateTime.now(), idEstadoProtocolo = 1
                ),
                equipo = equipoEjemplo,
                puedeModificar = true,
                temperatura = "18", humedad = "55",
                tiposPrueba = listOf(
                    TipoPrueba(1, "AISLACION", "Resistencia de aislación"),
                    TipoPrueba(2, "TTR", "Relación de transformación (TTR)")
                ),
                parametros = listOf(
                    Parametro(1, 1, "TENSION_PRUEBA", "Tensión de prueba", "kV"),
                    Parametro(2, 1, "LECTURA_1MIN", "Lectura a 1 min", "MΩ"),
                    Parametro(3, 1, "LECTURA_10MIN", "Lectura a 10 min", "MΩ")
                ),
                instrumentosVigentes = listOf(Instrumento(1, "Megóhmetro 5 kV", "Megger", "MIT525", "MIT-0045821")),
                pruebas = listOf(
                    PruebaRegistrada(
                        1, "Resistencia de aislación", "AT-T", Momento.UNICA, true, null,
                        TipoResultado.OBSERVADO, listOf("Lectura a 1 min: 6100 MΩ", "Lectura a 10 min: 10980 MΩ")
                    )
                ),
                formulario = FormularioPrueba(idTipoPrueba = 1, elemento = "BT-T")
            ),
            onVolver = {},
            acciones = MedicionesAcciones()
        )
    }
}

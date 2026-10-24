package cl.induztek.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import cl.induztek.app.ui.components.FotoPlaca
import cl.induztek.app.util.FotosPlaca
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.induztek.app.R
import cl.induztek.app.model.AltaEquipoUiState
import cl.induztek.app.model.ClasificacionEquipo
import cl.induztek.app.model.TipoEquipo
import cl.induztek.app.ui.components.BotonPrincipal
import cl.induztek.app.ui.components.BotonSecundario
import cl.induztek.app.ui.components.CampoTexto
import cl.induztek.app.ui.components.MensajeError
import cl.induztek.app.ui.components.TarjetaSeccion
import cl.induztek.app.ui.styles.EstilosCampo
import cl.induztek.app.ui.theme.AdvertenciaFondo
import cl.induztek.app.ui.theme.Dimens
import cl.induztek.app.ui.theme.InduztekAppTheme
import cl.induztek.app.viewmodel.AltaEquipoViewModel

// =====================================================================
//  AltaEquipoScreen.kt — HU-01: registrar un equipo en una instalación.
//  Es el "FormularioScreen" de este proyecto: campos validados, lista desplegable,
//  errores debajo de cada campo y botones Limpiar / Guardar.
// =====================================================================
@Composable
fun AltaEquipoScreen(
    idInstalacion: Long,
    onVolver: () -> Unit,
    onGuardado: () -> Unit,
    onVerExistente: (Long) -> Unit,
    viewModel: AltaEquipoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    val mensajePermiso = stringResource(R.string.error_permiso_camara)
    val mensajeFoto = stringResource(R.string.error_foto_no_guardada)

    // Ruta del archivo que la cámara está llenando. rememberSaveable: sobrevive si Android
    // recrea la pantalla (por ejemplo al girar el teléfono) mientras la cámara está abierta.
    var rutaPendiente by rememberSaveable { mutableStateOf<String?>(null) }

    // 2) La cámara toma la foto y la guarda en la Uri que se le entregó
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        val ruta = rutaPendiente
        rutaPendiente = null
        if (ruta != null) {
            if (exito) viewModel.onFotoTomada(ruta) else viewModel.onFotoCancelada(ruta)
        } else if (!exito) {
            viewModel.onErrorFoto(mensajeFoto)
        }
    }

    fun abrirCamara() {
        val archivo = FotosPlaca.crearArchivo(contexto)
        rutaPendiente = archivo.absolutePath
        camara.launch(FotosPlaca.uriDe(contexto, archivo))
    }

    // 1) Primero el permiso de cámara (se pide en pantalla la primera vez)
    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) abrirCamara() else viewModel.onErrorFoto(mensajePermiso)
    }

    val onTomarFoto = {
        val concedido = ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (concedido) abrirCamara() else pedirPermiso.launch(Manifest.permission.CAMERA)
    }

    AltaEquipoContent(
        uiState = uiState,
        onTomarFoto = onTomarFoto,
        onQuitarFoto = viewModel::onQuitarFoto,
        onVolver = onVolver,
        onTagCambia = viewModel::onTagCambia,
        onTipoSeleccionado = viewModel::onTipoSeleccionado,
        onMarcaCambia = viewModel::onMarcaCambia,
        onModeloCambia = viewModel::onModeloCambia,
        onSerieCambia = viewModel::onSerieCambia,
        onAnioCambia = viewModel::onAnioCambia,
        onPotenciaCambia = viewModel::onPotenciaCambia,
        onTensionPrimariaCambia = viewModel::onTensionPrimariaCambia,
        onTensionSecundariaCambia = viewModel::onTensionSecundariaCambia,
        onCorrienteCambia = viewModel::onCorrienteCambia,
        onMaxContactosCambia = viewModel::onMaxContactosCambia,
        onObservacionesCambia = viewModel::onObservacionesCambia,
        onLimpiar = viewModel::onLimpiar,
        // onGuardar devuelve true si quedó guardado: recién ahí se vuelve a la lista
        onGuardar = { if (viewModel.onGuardar(idInstalacion)) onGuardado() },
        onVerExistente = onVerExistente
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AltaEquipoContent(
    uiState: AltaEquipoUiState,
    onTomarFoto: () -> Unit,
    onQuitarFoto: () -> Unit,
    onVolver: () -> Unit,
    onTagCambia: (String) -> Unit,
    onTipoSeleccionado: (Long) -> Unit,
    onMarcaCambia: (String) -> Unit,
    onModeloCambia: (String) -> Unit,
    onSerieCambia: (String) -> Unit,
    onAnioCambia: (String) -> Unit,
    onPotenciaCambia: (String) -> Unit,
    onTensionPrimariaCambia: (String) -> Unit,
    onTensionSecundariaCambia: (String) -> Unit,
    onCorrienteCambia: (String) -> Unit,
    onMaxContactosCambia: (String) -> Unit,
    onObservacionesCambia: (String) -> Unit,
    onLimpiar: () -> Unit,
    onGuardar: () -> Unit,
    onVerExistente: (Long) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_alta_equipo)) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            TarjetaSeccion(titulo = stringResource(R.string.seccion_identificacion)) {
                CampoTexto(
                    valor = uiState.tag,
                    onValorCambia = onTagCambia,
                    etiqueta = stringResource(R.string.campo_tag),
                    icono = R.drawable.ic_rayo,
                    error = uiState.errorTag
                )

                // HU-01: si el TAG ya existe se ofrece usar el equipo que ya está registrado
                AnimatedVisibility(visible = uiState.idEquipoExistente != null) {
                    Card(colors = CardDefaults.cardColors(containerColor = AdvertenciaFondo)) {
                        Column(modifier = Modifier.padding(Dimens.espacioMedio)) {
                            Text(
                                text = stringResource(R.string.equipo_existente_mensaje),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            TextButton(onClick = { uiState.idEquipoExistente?.let(onVerExistente) }) {
                                Text(stringResource(R.string.btn_usar_equipo_existente))
                            }
                        }
                    }
                }

                ListaTipos(
                    tipos = uiState.tipos,
                    seleccionado = uiState.nombreTipoSeleccionado,
                    onSeleccionar = onTipoSeleccionado
                )
                CampoTexto(
                    valor = uiState.marca, onValorCambia = onMarcaCambia,
                    etiqueta = stringResource(R.string.campo_marca)
                )
                CampoTexto(
                    valor = uiState.modelo, onValorCambia = onModeloCambia,
                    etiqueta = stringResource(R.string.campo_modelo)
                )
                CampoTexto(
                    valor = uiState.numeroSerie, onValorCambia = onSerieCambia,
                    etiqueta = stringResource(R.string.campo_serie)
                )
                CampoTexto(
                    valor = uiState.anioFabricacion, onValorCambia = onAnioCambia,
                    etiqueta = stringResource(R.string.campo_anio),
                    error = uiState.errorAnio,
                    tipoTeclado = KeyboardType.Number
                )
            }

            TarjetaSeccion(titulo = stringResource(R.string.seccion_placa)) {
                // Los campos cambian según el tipo de equipo; la unidad queda pegada al campo (RNF-05)
                if (uiState.mostrarPotencia) {
                    CampoTexto(
                        valor = uiState.potenciaKva, onValorCambia = onPotenciaCambia,
                        etiqueta = stringResource(R.string.campo_potencia), sufijo = "kVA",
                        error = uiState.errorPotencia, tipoTeclado = KeyboardType.Decimal
                    )
                }
                if (uiState.mostrarTensionPrimaria) {
                    CampoTexto(
                        valor = uiState.tensionPrimariaKv, onValorCambia = onTensionPrimariaCambia,
                        etiqueta = stringResource(R.string.campo_tension_primaria), sufijo = "kV",
                        error = uiState.errorTensionPrimaria, tipoTeclado = KeyboardType.Decimal
                    )
                }
                if (uiState.mostrarTensionSecundaria) {
                    CampoTexto(
                        valor = uiState.tensionSecundariaKv, onValorCambia = onTensionSecundariaCambia,
                        etiqueta = stringResource(R.string.campo_tension_secundaria), sufijo = "kV",
                        error = uiState.errorTensionSecundaria, tipoTeclado = KeyboardType.Decimal
                    )
                }
                if (uiState.mostrarCorriente) {
                    CampoTexto(
                        valor = uiState.corrienteNominalA, onValorCambia = onCorrienteCambia,
                        etiqueta = stringResource(R.string.campo_corriente), sufijo = "A",
                        error = uiState.errorCorriente, tipoTeclado = KeyboardType.Decimal
                    )
                }
                if (uiState.mostrarMaxContactos) {
                    CampoTexto(
                        valor = uiState.maxContactosUohm, onValorCambia = onMaxContactosCambia,
                        etiqueta = stringResource(R.string.campo_max_contactos), sufijo = "µΩ",
                        error = uiState.errorMaxContactos, tipoTeclado = KeyboardType.Decimal
                    )
                }
                CampoTexto(
                    valor = uiState.observaciones, onValorCambia = onObservacionesCambia,
                    etiqueta = stringResource(R.string.campo_observaciones),
                    lineasMaximas = 3
                )
            }

            // Recurso nativo: cámara. La foto es opcional; sirve para comprobar después los datos de la placa.
            TarjetaSeccion(titulo = stringResource(R.string.seccion_foto_placa)) {
                uiState.fotoPlaca?.let { FotoPlaca(ruta = it) }
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)) {
                    BotonSecundario(
                        texto = stringResource(
                            if (uiState.fotoPlaca == null) R.string.btn_tomar_foto else R.string.btn_repetir_foto
                        ),
                        onClick = onTomarFoto,
                        icono = R.drawable.ic_camara,
                        modifier = Modifier.weight(1f)
                    )
                    AnimatedVisibility(visible = uiState.fotoPlaca != null) {
                        TextButton(onClick = onQuitarFoto) { Text(stringResource(R.string.btn_quitar_foto)) }
                    }
                }
                uiState.errorFoto?.let { MensajeError(mensaje = it) }
            }

            uiState.errorGeneral?.let { MensajeError(mensaje = it) }

            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)) {
                BotonSecundario(
                    texto = stringResource(R.string.btn_limpiar),
                    onClick = onLimpiar,
                    modifier = Modifier.weight(1f)
                )
                BotonPrincipal(
                    texto = stringResource(R.string.btn_guardar_equipo),
                    onClick = onGuardar,
                    icono = R.drawable.ic_check,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(Dimens.espacioGrande))
        }
    }
}

// Lista desplegable de tipos de equipo (mismo ExposedDropdownMenuBox que "Ciudad" en MiRegistro)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListaTipos(
    tipos: List<TipoEquipo>,
    seleccionado: String,
    onSeleccionar: (Long) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = it }
    ) {
        OutlinedTextField(
            value = seleccionado,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.campo_tipo_equipo)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            shape = EstilosCampo.forma,
            colors = EstilosCampo.colores(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            tipos.forEach { tipo ->
                DropdownMenuItem(
                    text = { Text("${tipo.nombre} (${tipo.clasificacion.clasificacion})") },
                    onClick = {
                        onSeleccionar(tipo.idTipoEquipo)
                        expandido = false
                    }
                )
            }
        }
    }
}

private val tiposEjemplo = listOf(
    TipoEquipo(1, "Transformador", ClasificacionEquipo.PRIMARIO),
    TipoEquipo(2, "Interruptor", ClasificacionEquipo.PRIMARIO),
    TipoEquipo(5, "Relé de protección", ClasificacionEquipo.SECUNDARIO)
)

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun AltaEquipoPreview() {
    InduztekAppTheme {
        AltaEquipoContent(
            uiState = AltaEquipoUiState(tipos = tiposEjemplo),
            onTomarFoto = {}, onQuitarFoto = {},
            onVolver = {}, onTagCambia = {}, onTipoSeleccionado = {}, onMarcaCambia = {},
            onModeloCambia = {}, onSerieCambia = {}, onAnioCambia = {}, onPotenciaCambia = {},
            onTensionPrimariaCambia = {}, onTensionSecundariaCambia = {}, onCorrienteCambia = {},
            onMaxContactosCambia = {}, onObservacionesCambia = {}, onLimpiar = {}, onGuardar = {},
            onVerExistente = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 1400, name = "TAG duplicado")
@Composable
private fun AltaEquipoDuplicadoPreview() {
    InduztekAppTheme {
        AltaEquipoContent(
            uiState = AltaEquipoUiState(
                tipos = tiposEjemplo, tag = "TR-01",
                errorTag = "Ya existe un equipo con este TAG en la instalación",
                idEquipoExistente = 1
            ),
            onTomarFoto = {}, onQuitarFoto = {},
            onVolver = {}, onTagCambia = {}, onTipoSeleccionado = {}, onMarcaCambia = {},
            onModeloCambia = {}, onSerieCambia = {}, onAnioCambia = {}, onPotenciaCambia = {},
            onTensionPrimariaCambia = {}, onTensionSecundariaCambia = {}, onCorrienteCambia = {},
            onMaxContactosCambia = {}, onObservacionesCambia = {}, onLimpiar = {}, onGuardar = {},
            onVerExistente = {}
        )
    }
}

package cl.induztek.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.induztek.app.R
import cl.induztek.app.model.ReporteActa
import cl.induztek.app.model.ReportesUiState
import cl.induztek.app.model.Usuario
import cl.induztek.app.ui.components.CampoTexto
import cl.induztek.app.ui.components.FiltrosEstadoReporte
import cl.induztek.app.ui.components.MensajeError
import cl.induztek.app.ui.components.TarjetaReporte
import cl.induztek.app.ui.theme.Dimens
import cl.induztek.app.ui.theme.Exito
import cl.induztek.app.viewmodel.ReportesViewModel

// =====================================================================
//  ReportesScreen.kt — lista de reportes de actas (menú lateral)
//   Cliente: "Mis reportes". Personal: "Reportes de clientes"; el Ingeniero y el Jefe técnico
//   pueden responder cada reporte (En revisión / Resuelto / Rechazado) con un texto para el cliente.
// =====================================================================
@Composable
fun ReportesScreen(
    usuario: Usuario,
    onVolver: () -> Unit,
    onVerActa: (Long) -> Unit,
    onVerProtocolo: (Long) -> Unit,
    viewModel: ReportesViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ReportesContent(
        uiState = uiState,
        onVolver = onVolver,
        onVerActa = onVerActa,
        onVerProtocolo = onVerProtocolo,
        onFiltrar = viewModel::onFiltrar,
        onResponder = viewModel::onResponder,
        onEstadoRespuestaCambia = viewModel::onEstadoRespuestaCambia,
        onTextoRespuestaCambia = viewModel::onTextoRespuestaCambia,
        onCancelarRespuesta = viewModel::onCancelarRespuesta,
        onEnviarRespuesta = viewModel::onEnviarRespuesta
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesContent(
    uiState: ReportesUiState,
    onVolver: () -> Unit,
    onVerActa: (Long) -> Unit,
    onVerProtocolo: (Long) -> Unit,
    onFiltrar: (String?) -> Unit,
    onResponder: (ReporteActa) -> Unit,
    onEstadoRespuestaCambia: (String) -> Unit,
    onTextoRespuestaCambia: (String) -> Unit,
    onCancelarRespuesta: () -> Unit,
    onEnviarRespuesta: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(if (uiState.esCliente) R.string.menu_mis_reportes else R.string.menu_reportes))
                },
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
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            FiltrosEstadoReporte(seleccionado = uiState.filtroEstado, onSeleccionar = onFiltrar)
            uiState.error?.let { MensajeError(mensaje = it) }
            uiState.mensaje?.let { Text(it, color = Exito, style = MaterialTheme.typography.bodyMedium) }

            if (uiState.visibles.isEmpty() && !uiState.cargando) {
                Text(stringResource(R.string.reportes_vacio), style = MaterialTheme.typography.bodyMedium)
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)) {
                items(uiState.visibles, key = { it.idReporte }) { reporte ->
                    TarjetaReporte(
                        reporte = reporte,
                        clienteNombre = if (uiState.esCliente) null else uiState.nombresClientes[reporte.idCliente],
                        acciones = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { onVerActa(reporte.idVisita) }) {
                                    Text(stringResource(R.string.btn_ver_acta))
                                }
                                // El personal puede abrir el protocolo observado para corregirlo o revisarlo
                                if (!uiState.esCliente && reporte.idProtocolo != null) {
                                    TextButton(onClick = { onVerProtocolo(reporte.idProtocolo) }) {
                                        Text(stringResource(R.string.btn_ver_protocolo))
                                    }
                                }
                                if (uiState.puedeResponder && !reporte.estaCerrado) {
                                    TextButton(onClick = { onResponder(reporte) }) {
                                        Text(stringResource(R.string.btn_responder))
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    uiState.reporteEnRespuesta?.let { reporte ->
        AlertDialog(
            onDismissRequest = onCancelarRespuesta,
            title = { Text(stringResource(R.string.titulo_responder_reporte)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                    Text(reporte.texto, style = MaterialTheme.typography.bodySmall)
                    listOf(
                        "EN_REVISION" to "En revisión",
                        "RESUELTO" to "Resuelto",
                        "RECHAZADO" to "Rechazado"
                    ).forEach { (estado, etiqueta) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().selectable(
                                selected = uiState.estadoRespuesta == estado,
                                onClick = { onEstadoRespuestaCambia(estado) },
                                role = Role.RadioButton
                            )
                        ) {
                            RadioButton(selected = uiState.estadoRespuesta == estado, onClick = null)
                            Text(etiqueta, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                    CampoTexto(
                        valor = uiState.textoRespuesta,
                        onValorCambia = onTextoRespuestaCambia,
                        etiqueta = stringResource(R.string.campo_respuesta_reporte),
                        error = uiState.errorRespuesta,
                        lineasMaximas = 4
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onEnviarRespuesta, enabled = !uiState.enviandoRespuesta) {
                    Text(stringResource(R.string.btn_guardar_respuesta))
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelarRespuesta) { Text(stringResource(R.string.dialogo_cancelar)) }
            }
        )
    }
}

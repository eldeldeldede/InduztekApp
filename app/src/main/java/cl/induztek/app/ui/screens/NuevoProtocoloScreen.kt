package cl.induztek.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.induztek.app.R
import cl.induztek.app.model.NuevoProtocoloUiState
import cl.induztek.app.model.TipoVisita
import cl.induztek.app.model.Usuario
import cl.induztek.app.ui.components.BotonPrincipal
import cl.induztek.app.ui.components.CampoTexto
import cl.induztek.app.ui.components.ListaDesplegable
import cl.induztek.app.ui.components.MensajeError
import cl.induztek.app.ui.components.TarjetaSeccion
import cl.induztek.app.ui.theme.Dimens
import cl.induztek.app.ui.theme.InduztekAppTheme
import cl.induztek.app.viewmodel.NuevoProtocoloViewModel

@Composable
fun NuevoProtocoloScreen(
    idEquipo: Long,
    usuario: Usuario,
    onVolver: () -> Unit,
    onProtocoloListo: (Long) -> Unit,
    viewModel: NuevoProtocoloViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(idEquipo) { viewModel.cargar(idEquipo, usuario) }

    LaunchedEffect(uiState.idProtocoloCreado) {
        uiState.idProtocoloCreado?.let {
            onProtocoloListo(it)
            viewModel.onNavegado()
        }
    }

    NuevoProtocoloContent(
        uiState = uiState,
        onVolver = onVolver,
        onOtCambia = viewModel::onOtCambia,
        onTipoVisitaCambia = viewModel::onTipoVisitaCambia,
        onIngenieroSeleccionado = viewModel::onIngenieroSeleccionado,
        onGuardar = { viewModel.onGuardar(usuario) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoProtocoloContent(
    uiState: NuevoProtocoloUiState,
    onVolver: () -> Unit,
    onOtCambia: (String) -> Unit,
    onTipoVisitaCambia: (TipoVisita) -> Unit,
    onIngenieroSeleccionado: (Long) -> Unit,
    onGuardar: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_nuevo_protocolo)) },
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
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            uiState.equipo?.let { equipo ->
                Text(
                    text = stringResource(R.string.protocolo_para_equipo, equipo.tag, equipo.tipoEquipoNombre),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            TarjetaSeccion(titulo = stringResource(R.string.seccion_visita)) {
                CampoTexto(
                    valor = uiState.numeroOt,
                    onValorCambia = onOtCambia,
                    etiqueta = stringResource(R.string.campo_ot),
                    icono = R.drawable.ic_protocolo,
                    error = uiState.errorOt
                )
                ListaDesplegable(
                    etiqueta = stringResource(R.string.campo_tipo_visita), icono = R.drawable.ic_lista,
                    seleccionado = uiState.tipoVisita.etiqueta,
                    opciones = TipoVisita.entries.toList(),
                    textoDe = { it.etiqueta },
                    onSeleccionar = onTipoVisitaCambia
                )
                ListaDesplegable(
                    etiqueta = stringResource(R.string.campo_ingeniero), icono = R.drawable.ic_persona,
                    seleccionado = uiState.nombreIngeniero,
                    opciones = uiState.ingenieros,
                    textoDe = { it.nombre },
                    onSeleccionar = { onIngenieroSeleccionado(it.idUsuario) },
                    error = uiState.errorIngeniero
                )
            }

            uiState.errorGeneral?.let { MensajeError(mensaje = it) }

            BotonPrincipal(
                texto = stringResource(R.string.btn_iniciar_protocolo),
                onClick = onGuardar,
                icono = R.drawable.ic_check,
                modifier = Modifier.padding(bottom = Dimens.espacioGrande)
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun NuevoProtocoloPreview() {
    InduztekAppTheme {
        NuevoProtocoloContent(
            uiState = NuevoProtocoloUiState(numeroOt = "OT-2026-1187", errorOt = null),
            onVolver = {}, onOtCambia = {}, onTipoVisitaCambia = {},
            onIngenieroSeleccionado = {}, onGuardar = {}
        )
    }
}

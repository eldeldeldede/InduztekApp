package cl.induztek.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.induztek.app.R
import cl.induztek.app.ui.components.BotonPrincipal
import cl.induztek.app.ui.components.CampoTexto
import cl.induztek.app.ui.components.ListaDesplegable
import cl.induztek.app.ui.components.MensajeError
import cl.induztek.app.ui.components.TarjetaSeccion
import cl.induztek.app.ui.theme.Dimens
import cl.induztek.app.viewmodel.InstalacionFormViewModel

// =====================================================================
//  InstalacionFormScreen.kt — crear o editar una instalación (planta o sitio) de un cliente
// =====================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstalacionFormScreen(
    onVolver: () -> Unit,
    viewModel: InstalacionFormViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Al guardar con éxito se vuelve a la lista (que se recarga sola)
    LaunchedEffect(uiState.guardado) { if (uiState.guardado) onVolver() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (uiState.esNueva) R.string.titulo_nueva_instalacion else R.string.titulo_editar_instalacion)) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(painterResource(R.drawable.ic_volver), contentDescription = stringResource(R.string.cd_volver))
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
            modifier = Modifier.fillMaxSize().padding(innerPadding).imePadding()
                .verticalScroll(rememberScrollState()).padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            TarjetaSeccion(titulo = stringResource(R.string.seccion_datos_instalacion)) {
                if (uiState.esNueva) {
                    ListaDesplegable(
                        etiqueta = stringResource(R.string.campo_cliente),
                        seleccionado = uiState.cliente?.nombre ?: "",
                        opciones = uiState.clientes,
                        textoDe = { it.nombre },
                        onSeleccionar = viewModel::onClienteCambia,
                        error = uiState.errorCliente
                    )
                } else {
                    Text(uiState.clienteNombreFijo.orEmpty(), style = MaterialTheme.typography.titleSmall)
                }
                CampoTexto(
                    valor = uiState.nombre, onValorCambia = viewModel::onNombreCambia,
                    etiqueta = stringResource(R.string.campo_nombre_instalacion),
                    icono = R.drawable.ic_instalacion, error = uiState.errorNombre
                )
                CampoTexto(
                    valor = uiState.ubicacion, onValorCambia = viewModel::onUbicacionCambia,
                    etiqueta = stringResource(R.string.campo_ubicacion), icono = R.drawable.ic_ubicacion
                )
                ListaDesplegable(
                    etiqueta = stringResource(R.string.campo_comuna),
                    seleccionado = uiState.comuna?.nombre ?: "",
                    opciones = uiState.comunas,
                    textoDe = { it.nombre },
                    onSeleccionar = viewModel::onComunaCambia,
                    error = uiState.errorComuna
                )
                CampoTexto(
                    valor = uiState.observaciones, onValorCambia = viewModel::onObservacionesCambia,
                    etiqueta = stringResource(R.string.campo_observaciones_acceso),
                    icono = R.drawable.ic_nota, lineasMaximas = 3
                )
            }
            uiState.errorGeneral?.let { MensajeError(mensaje = it) }
            BotonPrincipal(
                texto = stringResource(if (uiState.guardando) R.string.btn_guardando else R.string.btn_guardar),
                onClick = viewModel::onGuardar,
                icono = R.drawable.ic_check,
                habilitado = !uiState.guardando && !uiState.cargando,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

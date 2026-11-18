package cl.induztek.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.induztek.app.R
import cl.induztek.app.model.CambiarClaveUiState
import cl.induztek.app.model.Usuario
import cl.induztek.app.ui.components.BotonPrincipal
import cl.induztek.app.ui.components.BotonSecundario
import cl.induztek.app.ui.components.CampoContrasena
import cl.induztek.app.ui.components.EncabezadoMarca
import cl.induztek.app.ui.components.MensajeError
import cl.induztek.app.ui.components.TarjetaSeccion
import cl.induztek.app.ui.theme.Dimens
import cl.induztek.app.ui.theme.InduztekAppTheme
import cl.induztek.app.viewmodel.CambiarClaveViewModel

// =====================================================================
//  CambiarClaveScreen.kt — pantalla OBLIGATORIA tras el primer ingreso con la clave temporal.
//  No tiene botón "volver": la única salida alternativa es cerrar sesión.
//    CambiarClaveScreen  -> conecta con el ViewModel
//    CambiarClaveContent -> solo dibuja el uiState (por eso tiene @Preview)
// =====================================================================
@Composable
fun CambiarClaveScreen(
    usuario: Usuario,
    onClaveCambiada: (Usuario) -> Unit,
    onCerrarSesion: () -> Unit,
    // obligatorio = true: primer ingreso con clave temporal (sin botón volver).
    // obligatorio = false: el usuario la cambia cuando quiere desde "Mi perfil" (con flecha para volver).
    obligatorio: Boolean = true,
    onVolver: () -> Unit = {},
    viewModel: CambiarClaveViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CambiarClaveContent(
        uiState = uiState,
        onActualCambia = viewModel::onActualCambia,
        onNuevaCambia = viewModel::onNuevaCambia,
        onConfirmacionCambia = viewModel::onConfirmacionCambia,
        onAlternarVisibilidad = viewModel::onAlternarVisibilidad,
        onGuardar = { viewModel.onGuardar(usuario, onClaveCambiada) },
        onCerrarSesion = onCerrarSesion,
        obligatorio = obligatorio,
        onVolver = onVolver
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CambiarClaveContent(
    uiState: CambiarClaveUiState,
    onActualCambia: (String) -> Unit,
    onNuevaCambia: (String) -> Unit,
    onConfirmacionCambia: (String) -> Unit,
    onAlternarVisibilidad: () -> Unit,
    onGuardar: () -> Unit,
    onCerrarSesion: () -> Unit,
    obligatorio: Boolean = true,
    onVolver: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            if (!obligatorio) {
                TopAppBar(
                    title = { Text(stringResource(R.string.titulo_cambiar_clave_voluntaria)) },
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
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            if (obligatorio) {
                EncabezadoMarca(
                    titulo = stringResource(R.string.app_name),
                    subtitulo = stringResource(R.string.titulo_cambiar_clave)
                )
            }
            Column(
                modifier = Modifier.padding(Dimens.espacioPantalla),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
            ) {
                Text(
                    text = stringResource(
                        if (obligatorio) R.string.cambiar_clave_ayuda else R.string.cambiar_clave_ayuda_voluntaria
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
                TarjetaSeccion(titulo = stringResource(R.string.titulo_cambiar_clave)) {
                    CampoContrasena(
                        valor = uiState.actual,
                        onValorCambia = onActualCambia,
                        etiqueta = stringResource(
                            if (obligatorio) R.string.campo_clave_actual else R.string.campo_clave_actual_voluntaria
                        ),
                        visible = uiState.clavesVisibles,
                        onAlternarVisible = onAlternarVisibilidad,
                        error = uiState.errorActual
                    )
                    CampoContrasena(
                        valor = uiState.nueva,
                        onValorCambia = onNuevaCambia,
                        etiqueta = stringResource(R.string.campo_clave_nueva),
                        visible = uiState.clavesVisibles,
                        onAlternarVisible = onAlternarVisibilidad,
                        error = uiState.errorNueva
                    )
                    CampoContrasena(
                        valor = uiState.confirmacion,
                        onValorCambia = onConfirmacionCambia,
                        etiqueta = stringResource(R.string.campo_clave_confirmar),
                        visible = uiState.clavesVisibles,
                        onAlternarVisible = onAlternarVisibilidad,
                        error = uiState.errorConfirmacion
                    )
                    AnimatedVisibility(visible = uiState.errorGeneral != null) {
                        MensajeError(mensaje = uiState.errorGeneral ?: "")
                    }
                    BotonPrincipal(
                        texto = stringResource(if (uiState.cargando) R.string.btn_guardando else R.string.btn_guardar_clave),
                        onClick = onGuardar,
                        icono = R.drawable.ic_check,
                        habilitado = !uiState.cargando,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                // Solo en el cambio obligatorio: la única otra salida es cerrar sesión
                if (obligatorio) {
                    BotonSecundario(
                        texto = stringResource(R.string.menu_cerrar_sesion),
                        onClick = onCerrarSesion,
                        icono = R.drawable.ic_salir,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CambiarClavePreview() {
    InduztekAppTheme {
        CambiarClaveContent(
            uiState = CambiarClaveUiState(errorNueva = "Debe tener al menos 6 caracteres"),
            onActualCambia = {}, onNuevaCambia = {}, onConfirmacionCambia = {},
            onAlternarVisibilidad = {}, onGuardar = {}, onCerrarSesion = {}
        )
    }
}

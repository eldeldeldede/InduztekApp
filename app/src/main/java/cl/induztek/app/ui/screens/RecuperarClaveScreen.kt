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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.induztek.app.R
import cl.induztek.app.model.PasoRecuperacion
import cl.induztek.app.model.RecuperarClaveUiState
import cl.induztek.app.ui.components.BotonPrincipal
import cl.induztek.app.ui.components.CampoContrasena
import cl.induztek.app.ui.components.CampoTexto
import cl.induztek.app.ui.components.EncabezadoMarca
import cl.induztek.app.ui.components.MensajeError
import cl.induztek.app.ui.components.TarjetaSeccion
import cl.induztek.app.ui.theme.Dimens
import cl.induztek.app.ui.theme.InduztekAppTheme
import cl.induztek.app.viewmodel.RecuperarClaveViewModel

// =====================================================================
//  RecuperarClaveScreen.kt — "Olvidé mi contraseña" (pantalla pública, se abre desde el login)
//  Dos pasos en la misma pantalla: pedir el código al correo y elegir la contraseña nueva.
// =====================================================================
@Composable
fun RecuperarClaveScreen(
    onVolver: () -> Unit,
    onClaveCambiada: () -> Unit,
    viewModel: RecuperarClaveViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RecuperarClaveContent(
        uiState = uiState,
        onCorreoCambia = viewModel::onCorreoCambia,
        onCodigoCambia = viewModel::onCodigoCambia,
        onNuevaCambia = viewModel::onNuevaCambia,
        onConfirmacionCambia = viewModel::onConfirmacionCambia,
        onAlternarVisibilidad = viewModel::onAlternarVisibilidad,
        onSolicitarCodigo = viewModel::onSolicitarCodigo,
        onConfirmar = { viewModel.onConfirmar(onClaveCambiada) },
        onPedirOtroCodigo = viewModel::onPedirOtroCodigo,
        onVolver = onVolver
    )
}

@Composable
fun RecuperarClaveContent(
    uiState: RecuperarClaveUiState,
    onCorreoCambia: (String) -> Unit,
    onCodigoCambia: (String) -> Unit,
    onNuevaCambia: (String) -> Unit,
    onConfirmacionCambia: (String) -> Unit,
    onAlternarVisibilidad: () -> Unit,
    onSolicitarCodigo: () -> Unit,
    onConfirmar: () -> Unit,
    onPedirOtroCodigo: () -> Unit,
    onVolver: () -> Unit
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            EncabezadoMarca(
                titulo = stringResource(R.string.app_name),
                subtitulo = stringResource(R.string.titulo_recuperar_clave)
            )
            Column(
                modifier = Modifier.padding(Dimens.espacioPantalla),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
            ) {
                if (uiState.paso == PasoRecuperacion.SOLICITAR) {
                    Text(stringResource(R.string.recuperar_ayuda_paso1), style = MaterialTheme.typography.bodyMedium)
                    TarjetaSeccion(titulo = stringResource(R.string.titulo_recuperar_clave)) {
                        CampoTexto(
                            valor = uiState.correo,
                            onValorCambia = onCorreoCambia,
                            etiqueta = stringResource(R.string.campo_correo),
                            icono = R.drawable.ic_correo,
                            error = uiState.errorCorreo,
                            tipoTeclado = KeyboardType.Email
                        )
                        AnimatedVisibility(visible = uiState.errorGeneral != null) {
                            MensajeError(mensaje = uiState.errorGeneral ?: "")
                        }
                        BotonPrincipal(
                            texto = stringResource(if (uiState.cargando) R.string.btn_enviando else R.string.btn_enviar_codigo),
                            onClick = onSolicitarCodigo,
                            icono = R.drawable.ic_correo,
                            habilitado = !uiState.cargando,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Text(
                        text = uiState.informacion ?: stringResource(R.string.recuperar_ayuda_paso2),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TarjetaSeccion(titulo = stringResource(R.string.btn_cambiar_clave_recuperada)) {
                        CampoTexto(
                            valor = uiState.codigo,
                            onValorCambia = onCodigoCambia,
                            etiqueta = stringResource(R.string.campo_codigo),
                            icono = R.drawable.ic_candado,
                            error = uiState.errorCodigo,
                            tipoTeclado = KeyboardType.Number
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
                            texto = stringResource(if (uiState.cargando) R.string.btn_guardando else R.string.btn_cambiar_clave_recuperada),
                            onClick = onConfirmar,
                            icono = R.drawable.ic_check,
                            habilitado = !uiState.cargando,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    TextButton(onClick = onPedirOtroCodigo, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.btn_pedir_otro_codigo))
                    }
                }
                TextButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.btn_volver_login))
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RecuperarPaso1Preview() {
    InduztekAppTheme {
        RecuperarClaveContent(
            uiState = RecuperarClaveUiState(),
            onCorreoCambia = {}, onCodigoCambia = {}, onNuevaCambia = {}, onConfirmacionCambia = {},
            onAlternarVisibilidad = {}, onSolicitarCodigo = {}, onConfirmar = {}, onPedirOtroCodigo = {}, onVolver = {}
        )
    }
}

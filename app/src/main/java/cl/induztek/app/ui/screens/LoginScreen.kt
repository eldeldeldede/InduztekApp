package cl.induztek.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.induztek.app.R
import cl.induztek.app.model.LoginUiState
import cl.induztek.app.model.Usuario
import cl.induztek.app.ui.components.BotonPrincipal
import cl.induztek.app.ui.components.CampoContrasena
import cl.induztek.app.ui.components.CampoTexto
import cl.induztek.app.ui.components.EncabezadoMarca
import cl.induztek.app.ui.components.MensajeError
import cl.induztek.app.ui.components.TarjetaSeccion
import cl.induztek.app.ui.theme.Dimens
import cl.induztek.app.ui.theme.InduztekAppTheme
import cl.induztek.app.viewmodel.LoginViewModel

// =====================================================================
//  LoginScreen.kt — RF-14: ingreso por correo y contraseña.
//  Misma estructura que FormularioScreen de MiRegistro:
//    LoginScreen  -> conecta con el ViewModel (viewModel(), collectAsStateWithLifecycle)
//    LoginContent -> SOLO dibuja el uiState y avisa con funciones (por eso tiene @Preview)
// =====================================================================
@Composable
fun LoginScreen(
    onIngresoExitoso: (Usuario) -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LoginContent(
        uiState = uiState,
        onCorreoCambia = viewModel::onCorreoCambia,
        onClaveCambia = viewModel::onClaveCambia,
        onAlternarVisibilidad = viewModel::onAlternarVisibilidad,
        // Si las credenciales son correctas, onIngresar() devuelve el usuario y se avisa hacia arriba
        onIngresar = { viewModel.onIngresar()?.let { usuario -> onIngresoExitoso(usuario) } }
    )
}

@Composable
fun LoginContent(
    uiState: LoginUiState,
    onCorreoCambia: (String) -> Unit,
    onClaveCambia: (String) -> Unit,
    onAlternarVisibilidad: () -> Unit,
    onIngresar: () -> Unit
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            EncabezadoMarca(
                titulo = stringResource(R.string.app_name),
                subtitulo = stringResource(R.string.login_subtitulo)
            )

            Column(
                modifier = Modifier.padding(Dimens.espacioPantalla),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
            ) {
                TarjetaSeccion(titulo = stringResource(R.string.login_titulo)) {
                    CampoTexto(
                        valor = uiState.correo,
                        onValorCambia = onCorreoCambia,
                        etiqueta = stringResource(R.string.campo_correo),
                        icono = R.drawable.ic_correo,
                        error = uiState.errorCorreo,
                        tipoTeclado = KeyboardType.Email
                    )
                    CampoContrasena(
                        valor = uiState.clave,
                        onValorCambia = onClaveCambia,
                        etiqueta = stringResource(R.string.campo_contrasena),
                        visible = uiState.claveVisible,
                        onAlternarVisible = onAlternarVisibilidad,
                        error = uiState.errorClave
                    )

                    // ANIMACIÓN: el mensaje de error aparece y desaparece suavemente
                    AnimatedVisibility(visible = uiState.errorGeneral != null) {
                        MensajeError(mensaje = uiState.errorGeneral ?: "")
                    }

                    BotonPrincipal(
                        texto = stringResource(R.string.btn_ingresar),
                        onClick = onIngresar,
                        icono = R.drawable.ic_check,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Ayuda para probar la app (datos de demostración). Se puede borrar al terminar.
                Text(
                    text = stringResource(R.string.login_demo),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Dimens.espacioGrande))
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginVacioPreview() {
    InduztekAppTheme {
        LoginContent(
            uiState = LoginUiState(),
            onCorreoCambia = {}, onClaveCambia = {}, onAlternarVisibilidad = {}, onIngresar = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Con errores")
@Composable
private fun LoginConErroresPreview() {
    InduztekAppTheme {
        LoginContent(
            uiState = LoginUiState(
                correo = "mgodoy@induztek.ejemplo",
                errorClave = "La contraseña es obligatoria",
                errorGeneral = "Correo o contraseña incorrectos"
            ),
            onCorreoCambia = {}, onClaveCambia = {}, onAlternarVisibilidad = {}, onIngresar = {}
        )
    }
}

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.induztek.app.R
import cl.induztek.app.model.PerfilUiState
import cl.induztek.app.model.Usuario
import cl.induztek.app.ui.components.BotonPrincipal
import cl.induztek.app.ui.components.BotonSecundario
import cl.induztek.app.ui.components.CampoTexto
import cl.induztek.app.ui.components.MensajeError
import cl.induztek.app.ui.components.TarjetaSeccion
import cl.induztek.app.ui.theme.Dimens
import cl.induztek.app.ui.theme.InduztekAppTheme
import cl.induztek.app.viewmodel.PerfilViewModel

// =====================================================================
//  PerfilScreen.kt — "Mi perfil": editar nombre y teléfono (las empresas, también giro y contacto)
//  y llegar al cambio de contraseña voluntario. Requiere conexión para guardar.
// =====================================================================
@Composable
fun PerfilScreen(
    usuario: Usuario,
    onVolver: () -> Unit,
    onUsuarioActualizado: (Usuario) -> Unit,
    onCambiarClave: () -> Unit,
    viewModel: PerfilViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(usuario.idUsuario) { viewModel.cargar(usuario) }

    PerfilContent(
        uiState = uiState,
        onVolver = onVolver,
        onNombreCambia = viewModel::onNombreCambia,
        onTelefonoCambia = viewModel::onTelefonoCambia,
        onGiroCambia = viewModel::onGiroCambia,
        onContactoNombreCambia = viewModel::onContactoNombreCambia,
        onContactoCargoCambia = viewModel::onContactoCargoCambia,
        onGuardar = { viewModel.onGuardar(usuario, onUsuarioActualizado) },
        onCambiarClave = onCambiarClave
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilContent(
    uiState: PerfilUiState,
    onVolver: () -> Unit,
    onNombreCambia: (String) -> Unit,
    onTelefonoCambia: (String) -> Unit,
    onGiroCambia: (String) -> Unit,
    onContactoNombreCambia: (String) -> Unit,
    onContactoCargoCambia: (String) -> Unit,
    onGuardar: () -> Unit,
    onCambiarClave: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_perfil)) },
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
            TarjetaSeccion(titulo = stringResource(R.string.perfil_seccion_cuenta)) {
                CampoTexto(
                    valor = uiState.rut, onValorCambia = {}, etiqueta = stringResource(R.string.campo_rut),
                    icono = R.drawable.ic_credencial, soloLectura = true
                )
                CampoTexto(
                    valor = uiState.correo, onValorCambia = {}, etiqueta = stringResource(R.string.campo_correo),
                    icono = R.drawable.ic_correo, soloLectura = true
                )
                CampoTexto(
                    valor = uiState.rolEtiqueta, onValorCambia = {}, etiqueta = "Rol",
                    icono = R.drawable.ic_persona, soloLectura = true
                )
                Text(
                    text = stringResource(R.string.perfil_solo_lectura),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TarjetaSeccion(titulo = stringResource(R.string.perfil_seccion_datos)) {
                CampoTexto(
                    valor = uiState.nombre, onValorCambia = onNombreCambia,
                    etiqueta = stringResource(R.string.campo_nombre_usuario),
                    icono = R.drawable.ic_persona, error = uiState.errorNombre
                )
                CampoTexto(
                    valor = uiState.telefono, onValorCambia = onTelefonoCambia,
                    etiqueta = stringResource(R.string.campo_telefono),
                    icono = R.drawable.ic_credencial, error = uiState.errorTelefono,
                    tipoTeclado = KeyboardType.Phone
                )
                if (uiState.esCliente) {
                    CampoTexto(
                        valor = uiState.giro, onValorCambia = onGiroCambia,
                        etiqueta = stringResource(R.string.campo_giro), icono = R.drawable.ic_instalacion
                    )
                    CampoTexto(
                        valor = uiState.contactoNombre, onValorCambia = onContactoNombreCambia,
                        etiqueta = stringResource(R.string.campo_contacto_nombre), icono = R.drawable.ic_persona
                    )
                    CampoTexto(
                        valor = uiState.contactoCargo, onValorCambia = onContactoCargoCambia,
                        etiqueta = stringResource(R.string.campo_contacto_cargo), icono = R.drawable.ic_cargo
                    )
                }
                AnimatedVisibility(visible = uiState.errorGeneral != null) {
                    MensajeError(mensaje = uiState.errorGeneral ?: "")
                }
                AnimatedVisibility(visible = uiState.exito != null) {
                    Text(
                        text = uiState.exito ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                BotonPrincipal(
                    texto = stringResource(if (uiState.guardando) R.string.btn_guardando else R.string.btn_guardar_perfil),
                    onClick = onGuardar,
                    icono = R.drawable.ic_check,
                    habilitado = !uiState.guardando,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            BotonSecundario(
                texto = stringResource(R.string.btn_cambiar_clave_voluntaria),
                onClick = onCambiarClave,
                icono = R.drawable.ic_candado,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PerfilPreview() {
    InduztekAppTheme {
        PerfilContent(
            uiState = PerfilUiState(
                rut = "76.123.456-7", correo = "contacto@empresa.cl", rolEtiqueta = "Cliente",
                esCliente = true, nombre = "Envasados del Pacífico", telefono = "+56 9 1234 5678",
                giro = "Alimentos", contactoNombre = "María Fuentes", contactoCargo = "Jefa de planta"
            ),
            onVolver = {}, onNombreCambia = {}, onTelefonoCambia = {}, onGiroCambia = {},
            onContactoNombreCambia = {}, onContactoCargoCambia = {}, onGuardar = {}, onCambiarClave = {}
        )
    }
}

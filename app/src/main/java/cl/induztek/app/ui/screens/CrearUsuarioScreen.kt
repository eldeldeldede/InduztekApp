package cl.induztek.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.induztek.app.R
import cl.induztek.app.model.CrearUsuarioUiState
import cl.induztek.app.model.Usuario
import cl.induztek.app.network.RolDto
import cl.induztek.app.ui.components.BotonPrincipal
import cl.induztek.app.ui.components.BotonSecundario
import cl.induztek.app.ui.components.CampoTexto
import cl.induztek.app.ui.components.ListaDesplegable
import cl.induztek.app.ui.components.MensajeError
import cl.induztek.app.ui.components.TarjetaSeccion
import cl.induztek.app.ui.theme.Dimens
import cl.induztek.app.ui.theme.InduztekAppTheme
import cl.induztek.app.viewmodel.CrearUsuarioViewModel

// =====================================================================
//  CrearUsuarioScreen.kt — el Jefe técnico crea usuarios (técnicos, ingenieros, otros jefes y
//  cuentas de empresas cliente). Elige el rol pero NO la contraseña: el servidor genera una
//  temporal que llega al correo del nuevo usuario, quien debe cambiarla en su primer ingreso.
// =====================================================================
@Composable
fun CrearUsuarioScreen(
    jefe: Usuario,
    onVolver: () -> Unit,
    viewModel: CrearUsuarioViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CrearUsuarioContent(
        uiState = uiState,
        onVolver = onVolver,
        onRutCambia = viewModel::onRutCambia,
        onNombreCambia = viewModel::onNombreCambia,
        onCorreoCambia = viewModel::onCorreoCambia,
        onTelefonoCambia = viewModel::onTelefonoCambia,
        onRolCambia = viewModel::onRolCambia,
        onGiroCambia = viewModel::onGiroCambia,
        onContactoNombreCambia = viewModel::onContactoNombreCambia,
        onContactoCargoCambia = viewModel::onContactoCargoCambia,
        onLimpiar = viewModel::onLimpiar,
        onCrear = { viewModel.onCrear(jefe) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearUsuarioContent(
    uiState: CrearUsuarioUiState,
    onVolver: () -> Unit,
    onRutCambia: (String) -> Unit,
    onNombreCambia: (String) -> Unit,
    onCorreoCambia: (String) -> Unit,
    onTelefonoCambia: (String) -> Unit,
    onRolCambia: (RolDto) -> Unit,
    onGiroCambia: (String) -> Unit,
    onContactoNombreCambia: (String) -> Unit,
    onContactoCargoCambia: (String) -> Unit,
    onLimpiar: () -> Unit,
    onCrear: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_crear_usuario)) },
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
            Text(
                text = stringResource(R.string.crear_usuario_ayuda),
                style = MaterialTheme.typography.bodyMedium
            )

            TarjetaSeccion(titulo = stringResource(R.string.seccion_datos_usuario)) {
                CampoTexto(
                    valor = uiState.rut,
                    onValorCambia = onRutCambia,
                    etiqueta = stringResource(R.string.campo_rut),
                    icono = R.drawable.ic_credencial,
                    error = uiState.errorRut
                )
                CampoTexto(
                    valor = uiState.nombre,
                    onValorCambia = onNombreCambia,
                    etiqueta = stringResource(R.string.campo_nombre_usuario),
                    icono = R.drawable.ic_persona,
                    error = uiState.errorNombre
                )
                CampoTexto(
                    valor = uiState.correo,
                    onValorCambia = onCorreoCambia,
                    etiqueta = stringResource(R.string.campo_correo),
                    icono = R.drawable.ic_correo,
                    error = uiState.errorCorreo,
                    tipoTeclado = KeyboardType.Email
                )
                CampoTexto(
                    valor = uiState.telefono,
                    onValorCambia = onTelefonoCambia,
                    etiqueta = stringResource(R.string.campo_telefono),
                    error = uiState.errorTelefono,
                    tipoTeclado = KeyboardType.Phone
                )
                ListaDesplegable(
                    etiqueta = stringResource(R.string.campo_rol),
                    seleccionado = uiState.rol?.nombre ?: "",
                    opciones = uiState.roles,
                    textoDe = { it.nombre },
                    onSeleccionar = onRolCambia,
                    error = uiState.errorRol
                )
            }

            // Los datos de empresa solo aparecen si el rol elegido es CLIENTE
            AnimatedVisibility(visible = uiState.esCliente) {
                TarjetaSeccion(titulo = stringResource(R.string.seccion_datos_cliente)) {
                    CampoTexto(
                        valor = uiState.giro,
                        onValorCambia = onGiroCambia,
                        etiqueta = stringResource(R.string.campo_giro)
                    )
                    CampoTexto(
                        valor = uiState.contactoNombre,
                        onValorCambia = onContactoNombreCambia,
                        etiqueta = stringResource(R.string.campo_contacto_nombre),
                        icono = R.drawable.ic_persona
                    )
                    CampoTexto(
                        valor = uiState.contactoCargo,
                        onValorCambia = onContactoCargoCambia,
                        etiqueta = stringResource(R.string.campo_contacto_cargo),
                        icono = R.drawable.ic_cargo
                    )
                }
            }

            AnimatedVisibility(visible = uiState.errorGeneral != null) {
                MensajeError(mensaje = uiState.errorGeneral ?: "")
            }
            AnimatedVisibility(visible = uiState.exito != null) {
                Text(
                    text = uiState.exito ?: "",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)) {
                BotonSecundario(
                    texto = stringResource(R.string.btn_limpiar),
                    onClick = onLimpiar,
                    modifier = Modifier.weight(1f)
                )
                BotonPrincipal(
                    texto = stringResource(if (uiState.cargando) R.string.btn_creando else R.string.btn_crear_usuario),
                    onClick = onCrear,
                    icono = R.drawable.ic_agregar,
                    habilitado = !uiState.cargando,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CrearUsuarioPreview() {
    InduztekAppTheme {
        CrearUsuarioContent(
            uiState = CrearUsuarioUiState(errorRut = "RUT inválido (revisa el dígito verificador)"),
            onVolver = {}, onRutCambia = {}, onNombreCambia = {}, onCorreoCambia = {}, onTelefonoCambia = {},
            onRolCambia = {}, onGiroCambia = {}, onContactoNombreCambia = {}, onContactoCargoCambia = {},
            onLimpiar = {}, onCrear = {}
        )
    }
}

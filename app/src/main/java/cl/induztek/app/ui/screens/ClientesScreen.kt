package cl.induztek.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.induztek.app.R
import cl.induztek.app.model.ClientesUiState
import cl.induztek.app.model.Usuario
import cl.induztek.app.ui.components.BotonPrincipal
import cl.induztek.app.ui.components.CampoTexto
import cl.induztek.app.ui.components.Etiqueta
import cl.induztek.app.ui.components.MensajeError
import cl.induztek.app.ui.theme.Dimens
import cl.induztek.app.ui.theme.Exito
import cl.induztek.app.ui.theme.ExitoFondo
import cl.induztek.app.ui.theme.Neutro
import cl.induztek.app.ui.theme.NeutroFondo
import cl.induztek.app.viewmodel.ClientesViewModel

// =====================================================================
//  ClientesScreen.kt — empresas cliente (personal de Induztek)
//   Todos: buscar, crear y editar. Solo el Jefe técnico: activar / desactivar la cuenta.
// =====================================================================
@Composable
fun ClientesScreen(
    onVolver: () -> Unit,
    onNuevoCliente: () -> Unit,
    viewModel: ClientesViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ClientesContent(
        uiState = uiState,
        onVolver = onVolver,
        onNuevoCliente = onNuevoCliente,
        onBuscar = viewModel::onBuscar,
        onEditar = viewModel::onEditar,
        onCambiarEstado = viewModel::onCambiarEstado,
        onNombreCambia = viewModel::onNombreCambia,
        onTelefonoCambia = viewModel::onTelefonoCambia,
        onGiroCambia = viewModel::onGiroCambia,
        onContactoNombreCambia = viewModel::onContactoNombreCambia,
        onContactoCargoCambia = viewModel::onContactoCargoCambia,
        onGuardar = viewModel::onGuardarEdicion,
        onCancelar = viewModel::onCancelarEdicion
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientesContent(
    uiState: ClientesUiState,
    onVolver: () -> Unit,
    onNuevoCliente: () -> Unit,
    onBuscar: (String) -> Unit,
    onEditar: (Usuario) -> Unit,
    onCambiarEstado: (Usuario) -> Unit,
    onNombreCambia: (String) -> Unit,
    onTelefonoCambia: (String) -> Unit,
    onGiroCambia: (String) -> Unit,
    onContactoNombreCambia: (String) -> Unit,
    onContactoCargoCambia: (String) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.menu_clientes)) },
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
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            BotonPrincipal(
                texto = stringResource(R.string.btn_nuevo_cliente),
                onClick = onNuevoCliente,
                icono = R.drawable.ic_agregar,
                modifier = Modifier.fillMaxWidth()
            )
            CampoTexto(
                valor = uiState.filtro,
                onValorCambia = onBuscar,
                etiqueta = stringResource(R.string.campo_buscar_cliente),
                icono = R.drawable.ic_buscar
            )
            uiState.error?.let { MensajeError(mensaje = it) }
            uiState.mensaje?.let { Text(it, color = Exito, style = MaterialTheme.typography.bodyMedium) }
            if (uiState.visibles.isEmpty() && !uiState.cargando) {
                Text(stringResource(R.string.clientes_vacio), style = MaterialTheme.typography.bodyMedium)
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                items(uiState.visibles, key = { it.idUsuario }) { c ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(Modifier.padding(Dimens.espacioMedio), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(c.nombre, style = MaterialTheme.typography.titleSmall)
                                if (c.activo) Etiqueta("Activo", Exito, ExitoFondo) else Etiqueta("Inactivo", Neutro, NeutroFondo)
                            }
                            Text("${c.rut} · ${c.correo}", style = MaterialTheme.typography.bodySmall)
                            if (!c.giro.isNullOrBlank()) Text(c.giro, style = MaterialTheme.typography.bodySmall)
                            if (!c.contactoNombre.isNullOrBlank()) {
                                Text(
                                    "Contacto: ${c.contactoNombre}" + (c.contactoCargo?.let { " ($it)" } ?: ""),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Row {
                                TextButton(onClick = { onEditar(c) }) { Text(stringResource(R.string.btn_editar)) }
                                if (uiState.puedeDesactivar) {
                                    TextButton(onClick = { onCambiarEstado(c) }) {
                                        Text(stringResource(if (c.activo) R.string.btn_desactivar else R.string.btn_activar))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    uiState.clienteEnEdicion?.let { cliente ->
        AlertDialog(
            onDismissRequest = onCancelar,
            title = { Text(stringResource(R.string.titulo_editar_cliente)) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                ) {
                    Text("${cliente.rut} · ${cliente.correo}", style = MaterialTheme.typography.bodySmall)
                    CampoTexto(uiState.nombre, onNombreCambia, stringResource(R.string.campo_nombre_usuario), error = uiState.errorNombre)
                    CampoTexto(
                        uiState.telefono, onTelefonoCambia, stringResource(R.string.campo_telefono),
                        error = uiState.errorTelefono, tipoTeclado = KeyboardType.Phone
                    )
                    CampoTexto(uiState.giro, onGiroCambia, stringResource(R.string.campo_giro))
                    CampoTexto(uiState.contactoNombre, onContactoNombreCambia, stringResource(R.string.campo_contacto_nombre))
                    CampoTexto(uiState.contactoCargo, onContactoCargoCambia, stringResource(R.string.campo_contacto_cargo))
                    uiState.errorEdicion?.let { MensajeError(mensaje = it) }
                }
            },
            confirmButton = {
                TextButton(onClick = onGuardar, enabled = !uiState.guardando) { Text(stringResource(R.string.btn_guardar)) }
            },
            dismissButton = { TextButton(onClick = onCancelar) { Text(stringResource(R.string.dialogo_cancelar)) } }
        )
    }
}

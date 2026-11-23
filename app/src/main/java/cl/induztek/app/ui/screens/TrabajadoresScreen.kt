package cl.induztek.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.induztek.app.R
import cl.induztek.app.model.TrabajadoresUiState
import cl.induztek.app.model.Usuario
import cl.induztek.app.ui.components.CampoTexto
import cl.induztek.app.ui.components.Etiqueta
import cl.induztek.app.ui.components.MensajeError
import cl.induztek.app.ui.theme.Dimens
import cl.induztek.app.ui.theme.Exito
import cl.induztek.app.ui.theme.ExitoFondo
import cl.induztek.app.ui.theme.Neutro
import cl.induztek.app.ui.theme.NeutroFondo
import cl.induztek.app.viewmodel.TrabajadoresViewModel

@Composable
fun TrabajadoresScreen(
    onVolver: () -> Unit,
    viewModel: TrabajadoresViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TrabajadoresContent(
        uiState = uiState,
        onVolver = onVolver,
        onBuscar = viewModel::onBuscar,
        onVerPerfil = viewModel::onVerPerfil,
        onCerrarPerfil = viewModel::onCerrarPerfil,
        onCambiarEstado = viewModel::onCambiarEstado
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrabajadoresContent(
    uiState: TrabajadoresUiState,
    onVolver: () -> Unit,
    onBuscar: (String) -> Unit,
    onVerPerfil: (Usuario) -> Unit,
    onCerrarPerfil: () -> Unit,
    onCambiarEstado: (Usuario) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.menu_trabajadores)) },
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
            CampoTexto(
                valor = uiState.filtro,
                onValorCambia = onBuscar,
                etiqueta = stringResource(R.string.campo_buscar_trabajador),
                icono = R.drawable.ic_buscar
            )
            uiState.error?.let { MensajeError(mensaje = it) }
            uiState.mensaje?.let { Text(it, color = Exito, style = MaterialTheme.typography.bodyMedium) }
            if (uiState.visibles.isEmpty() && !uiState.cargando) {
                Text(stringResource(R.string.trabajadores_vacio), style = MaterialTheme.typography.bodyMedium)
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                items(uiState.visibles, key = { it.idUsuario }) { t ->
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
                                Text(t.nombre, style = MaterialTheme.typography.titleSmall)
                                if (t.activo) Etiqueta("Activo", Exito, ExitoFondo) else Etiqueta("Inactivo", Neutro, NeutroFondo)
                            }
                            Text(t.codigoRol.etiqueta, style = MaterialTheme.typography.labelLarge)
                            Text("${t.rut} · ${t.correo}", style = MaterialTheme.typography.bodySmall)
                            Row {
                                TextButton(onClick = { onVerPerfil(t) }) { Text(stringResource(R.string.btn_ver_perfil)) }
                                if (t.idUsuario != uiState.idActor) {
                                    TextButton(onClick = { onCambiarEstado(t) }) {
                                        Text(stringResource(if (t.activo) R.string.btn_desactivar else R.string.btn_activar))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    uiState.seleccionado?.let { t ->
        AlertDialog(
            onDismissRequest = onCerrarPerfil,
            title = { Text(t.nombre) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                    Text(stringResource(R.string.perfil_dato_rol, t.codigoRol.etiqueta))
                    Text(stringResource(R.string.perfil_dato_rut, t.rut))
                    Text(stringResource(R.string.perfil_dato_correo, t.correo))
                    Text(stringResource(R.string.perfil_dato_telefono, t.telefono?.takeIf { it.isNotBlank() } ?: "—"))
                    Text(stringResource(R.string.perfil_dato_estado, if (t.activo) "Activo" else "Inactivo"))
                }
            },
            confirmButton = { TextButton(onClick = onCerrarPerfil) { Text(stringResource(R.string.btn_cerrar)) } }
        )
    }
}

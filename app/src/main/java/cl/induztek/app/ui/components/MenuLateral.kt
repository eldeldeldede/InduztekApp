package cl.induztek.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import cl.induztek.app.R
import cl.induztek.app.model.Usuario
import cl.induztek.app.ui.theme.Dimens
import kotlinx.coroutines.launch

@Composable
fun MenuLateral(
    usuario: Usuario,
    estadoMenu: DrawerState,
    onCerrarSesion: () -> Unit,
    contenido: @Composable (abrirMenu: () -> Unit) -> Unit
) {
    val scope = rememberCoroutineScope()
    var mostrarDialogo by remember { mutableStateOf(false) }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text(stringResource(R.string.dialogo_titulo)) },
            text = { Text(stringResource(R.string.dialogo_mensaje)) },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogo = false
                    onCerrarSesion()
                }) { Text(stringResource(R.string.menu_cerrar_sesion)) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text(stringResource(R.string.dialogo_cancelar))
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = estadoMenu,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = stringResource(R.string.saludo, usuario.nombre),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(Dimens.espacioMedio)
                )
                Text(
                    text = usuario.codigoRol.etiqueta,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = Dimens.espacioMedio, bottom = Dimens.espacioMedio)
                )
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.menu_cerrar_sesion)) },
                    icon = { Icon(painterResource(R.drawable.ic_salir), contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { estadoMenu.close() }
                        mostrarDialogo = true
                    }
                )
            }
        }
    ) {
        contenido { scope.launch { estadoMenu.open() } }
    }
}

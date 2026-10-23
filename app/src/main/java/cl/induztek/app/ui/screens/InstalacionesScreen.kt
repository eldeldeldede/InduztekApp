package cl.induztek.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.induztek.app.R
import cl.induztek.app.model.CodigoRol
import cl.induztek.app.model.Instalacion
import cl.induztek.app.model.InstalacionesUiState
import cl.induztek.app.model.Usuario
import cl.induztek.app.ui.components.CampoTexto
import cl.induztek.app.ui.components.MenuLateral
import cl.induztek.app.ui.theme.Dimens
import cl.induztek.app.ui.theme.InduztekAppTheme
import cl.induztek.app.viewmodel.InstalacionesViewModel

// =====================================================================
//  InstalacionesScreen.kt — pantalla de inicio después del login (RF-01).
//  Equivale a UsuariosScreen de MiRegistro: buscador arriba + lista con LazyColumn.
//  Para el cliente la lista trae solo las instalaciones de su empresa (RN-18).
//  La carga de datos la dispara AppNavegacion con AlReanudar { viewModel.cargar(usuario) }.
// =====================================================================
@Composable
fun InstalacionesScreen(
    usuario: Usuario,
    onVerEquipos: (Long) -> Unit,
    onCerrarSesion: () -> Unit,
    viewModel: InstalacionesViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val estadoMenu = rememberDrawerState(DrawerValue.Closed)

    MenuLateral(usuario = usuario, estadoMenu = estadoMenu, onCerrarSesion = onCerrarSesion) { abrirMenu ->
        InstalacionesContent(
            uiState = uiState,
            usuario = usuario,
            onBuscar = viewModel::buscar,
            onAbrirMenu = abrirMenu,
            onVerEquipos = onVerEquipos
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstalacionesContent(
    uiState: InstalacionesUiState,
    usuario: Usuario,
    onBuscar: (String) -> Unit,
    onAbrirMenu: () -> Unit,
    onVerEquipos: (Long) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_instalaciones)) },
                navigationIcon = {
                    IconButton(onClick = onAbrirMenu) {
                        Icon(
                            painter = painterResource(R.drawable.ic_menu),
                            contentDescription = stringResource(R.string.cd_menu)
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
        ) {
            CampoTexto(
                valor = uiState.filtro,
                onValorCambia = onBuscar,
                etiqueta = stringResource(R.string.campo_buscar_instalacion),
                icono = R.drawable.ic_buscar,
                modifier = Modifier.padding(horizontal = Dimens.espacioMedio, vertical = Dimens.espacioChico)
            )

            // Mensaje cuando el SELECT (o el filtro) no devuelve filas
            if (uiState.instalaciones.isEmpty()) {
                Text(
                    text = stringResource(R.string.lista_vacia_instalaciones),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(Dimens.espacioMedio)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Dimens.espacioMedio),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
            ) {
                // key = id: Compose identifica cada tarjeta por su clave primaria
                items(uiState.instalaciones, key = { it.idInstalacion }) { instalacion ->
                    // El cliente solo ve su propia empresa: el nombre del cliente sobra
                    val sobreTitulo: (@Composable () -> Unit)? =
                        if (usuario.esCliente) null else ({ Text(instalacion.clienteNombre) })
                    Card(onClick = { onVerEquipos(instalacion.idInstalacion) }) {
                        ListItem(
                            overlineContent = sobreTitulo,
                            leadingContent = {
                                Icon(painterResource(R.drawable.ic_instalacion), contentDescription = null)
                            },
                            headlineContent = { Text(instalacion.nombre) },
                            supportingContent = {
                                Text("${instalacion.comunaNombre} · ${instalacion.ubicacion}")
                            }
                        )
                    }
                }
            }
        }
    }
}

// ---- Previews: se ven en Android Studio sin ejecutar la app ----
private val instalacionesEjemplo = listOf(
    Instalacion(1, 8, "Planta Lampa", "Sector industrial", 1, null, "Lampa", "Envasadora del Pacífico S.A."),
    Instalacion(2, 9, "Planta Elevadora Sur", "Sector rural poniente", 2, null, "Buin", "Sanitaria Valle Central S.A.")
)

private val tecnicoEjemplo = Usuario(
    idUsuario = 1, idRol = 1, codigoRol = CodigoRol.TECNICO, rut = "12.345.678-5",
    nombre = "M. Godoy", correo = "mgodoy@induztek.ejemplo", passwordHash = ""
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun InstalacionesPreview() {
    InduztekAppTheme {
        InstalacionesContent(
            uiState = InstalacionesUiState(instalaciones = instalacionesEjemplo),
            usuario = tecnicoEjemplo,
            onBuscar = {}, onAbrirMenu = {}, onVerEquipos = {}
        )
    }
}

@Preview(showBackground = true, name = "Sin resultados")
@Composable
private fun InstalacionesVaciaPreview() {
    InduztekAppTheme {
        InstalacionesContent(
            uiState = InstalacionesUiState(filtro = "zzz"),
            usuario = tecnicoEjemplo,
            onBuscar = {}, onAbrirMenu = {}, onVerEquipos = {}
        )
    }
}

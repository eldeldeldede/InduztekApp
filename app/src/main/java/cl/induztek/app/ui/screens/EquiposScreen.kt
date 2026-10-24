package cl.induztek.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.induztek.app.R
import cl.induztek.app.model.CodigoRol
import cl.induztek.app.model.Equipo
import cl.induztek.app.model.EquiposUiState
import cl.induztek.app.model.Instalacion
import cl.induztek.app.model.Usuario
import cl.induztek.app.ui.components.CampoTexto
import cl.induztek.app.ui.theme.Dimens
import cl.induztek.app.ui.theme.InduztekAppTheme
import cl.induztek.app.viewmodel.EquiposViewModel

// =====================================================================
//  EquiposScreen.kt — equipos de una instalación (RF-02).
//  El botón "+" solo lo ve el personal de Induztek: el cliente consulta pero no crea equipos.
// =====================================================================
@Composable
fun EquiposScreen(
    usuario: Usuario,
    onVolver: () -> Unit,
    onAltaEquipo: () -> Unit,
    onVerHistorial: (Long) -> Unit,
    onNuevoProtocolo: (Long) -> Unit,
    viewModel: EquiposViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    EquiposContent(
        uiState = uiState,
        usuario = usuario,
        onBuscar = viewModel::buscar,
        onVolver = onVolver,
        onAltaEquipo = onAltaEquipo,
        onVerHistorial = onVerHistorial,
        onNuevoProtocolo = onNuevoProtocolo
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquiposContent(
    uiState: EquiposUiState,
    usuario: Usuario,
    onBuscar: (String) -> Unit,
    onVolver: () -> Unit,
    onAltaEquipo: () -> Unit,
    onVerHistorial: (Long) -> Unit,
    onNuevoProtocolo: (Long) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.instalacion?.nombre ?: stringResource(R.string.titulo_equipos)) },
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
        floatingActionButton = {
            if (!usuario.esCliente && uiState.instalacion != null) {
                FloatingActionButton(onClick = onAltaEquipo) {
                    Icon(
                        painter = painterResource(R.drawable.ic_agregar),
                        contentDescription = stringResource(R.string.cd_nuevo_equipo)
                    )
                }
            }
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
                etiqueta = stringResource(R.string.campo_buscar_equipo),
                icono = R.drawable.ic_buscar,
                modifier = Modifier.padding(horizontal = Dimens.espacioMedio, vertical = Dimens.espacioChico)
            )

            if (uiState.equipos.isEmpty()) {
                Text(
                    text = stringResource(R.string.lista_vacia_equipos),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(Dimens.espacioMedio)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Dimens.espacioMedio),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
            ) {
                items(uiState.equipos, key = { it.idEquipo }) { equipo ->
                    Card(onClick = { onVerHistorial(equipo.idEquipo) }) {
                        ListItem(
                            overlineContent = { Text(equipo.tipoEquipoNombre) },
                            leadingContent = {
                                Icon(painterResource(R.drawable.ic_rayo), contentDescription = null)
                            },
                            headlineContent = { Text(equipo.tag) },
                            supportingContent = {
                                // Solo se muestran los datos que existen (marca y serie son opcionales)
                                val detalle = listOfNotNull(equipo.marca, equipo.modelo, equipo.numeroSerie)
                                    .joinToString(" · ")
                                Text(detalle.ifEmpty { stringResource(R.string.sin_datos_placa) })
                            },
                            trailingContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Solo el personal inicia protocolos (el cliente solo consulta)
                                    if (!usuario.esCliente) {
                                        IconButton(onClick = { onNuevoProtocolo(equipo.idEquipo) }) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_protocolo),
                                                contentDescription = stringResource(R.string.cd_nuevo_protocolo)
                                            )
                                        }
                                    }
                                    Icon(
                                        painter = painterResource(R.drawable.ic_historial),
                                        contentDescription = stringResource(R.string.cd_ver_historial)
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

private val usuarioEjemplo = Usuario(
    idUsuario = 1, idRol = 1, codigoRol = CodigoRol.TECNICO, rut = "12.345.678-5",
    nombre = "M. Godoy", correo = "mgodoy@induztek.ejemplo", passwordHash = ""
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun EquiposPreview() {
    InduztekAppTheme {
        EquiposContent(
            uiState = EquiposUiState(
                instalacion = Instalacion(1, 8, "Planta Lampa", "Sector industrial", 1, null, "Lampa", "Envasadora del Pacífico S.A."),
                equipos = listOf(
                    Equipo(1, 1, "TR-01", 1, marca = "Rhona", numeroSerie = "RH-18733", tipoEquipoNombre = "Transformador"),
                    Equipo(2, 1, "TR-02", 1, marca = "Rhona", numeroSerie = "RH-21904", tipoEquipoNombre = "Transformador")
                )
            ),
            usuario = usuarioEjemplo,
            onBuscar = {}, onVolver = {}, onAltaEquipo = {}, onVerHistorial = {}, onNuevoProtocolo = {}
        )
    }
}

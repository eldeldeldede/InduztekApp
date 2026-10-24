package cl.induztek.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.induztek.app.R
import cl.induztek.app.data.aTextoLegible
import cl.induztek.app.model.ActasUiState
import cl.induztek.app.model.TipoVisita
import cl.induztek.app.model.Visita
import cl.induztek.app.ui.components.Etiqueta
import cl.induztek.app.ui.theme.Advertencia
import cl.induztek.app.ui.theme.AdvertenciaFondo
import cl.induztek.app.ui.theme.Dimens
import cl.induztek.app.ui.theme.Exito
import cl.induztek.app.ui.theme.ExitoFondo
import cl.induztek.app.ui.theme.InduztekAppTheme
import cl.induztek.app.viewmodel.ActasViewModel
import java.time.LocalDate
import java.time.LocalDateTime

@Composable
fun ActasScreen(
    onVolver: () -> Unit,
    onAbrirVisita: (Long) -> Unit,
    viewModel: ActasViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ActasContent(uiState = uiState, onVolver = onVolver, onAbrirVisita = onAbrirVisita)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActasContent(
    uiState: ActasUiState,
    onVolver: () -> Unit,
    onAbrirVisita: (Long) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.instalacion?.nombre ?: stringResource(R.string.titulo_actas)) },
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
        ) {
            if (uiState.cargado && uiState.visitas.isEmpty()) {
                Text(
                    text = stringResource(
                        if (uiState.esCliente) R.string.lista_vacia_actas_cliente else R.string.lista_vacia_visitas
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(Dimens.espacioMedio)
                )
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Dimens.espacioMedio),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
            ) {
                items(uiState.visitas, key = { it.idVisita }) { visita ->
                    TarjetaVisita(visita = visita, onClick = { onAbrirVisita(visita.idVisita) })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TarjetaVisita(visita: Visita, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(visita.numeroOt, style = MaterialTheme.typography.titleMedium)
                if (visita.estaCerrada) {
                    Etiqueta(stringResource(R.string.visita_estado_cerrada), Exito, ExitoFondo)
                } else {
                    Etiqueta(stringResource(R.string.visita_estado_abierta), Advertencia, AdvertenciaFondo)
                }
            }
            Text(stringResource(R.string.dato_fecha, visita.fecha.aTextoLegible()), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.dato_tipo_visita, visita.tipoVisita.etiqueta), style = MaterialTheme.typography.bodyMedium)
            visita.fechaHoraCierre?.let {
                Text(stringResource(R.string.visita_cerrada_el, it.aTextoLegible()), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ActasPreview() {
    InduztekAppTheme {
        ActasContent(
            uiState = ActasUiState(
                cargado = true,
                visitas = listOf(
                    Visita(1, "OT-2026-1187", 1, LocalDate.now(), TipoVisita.SOLO_PRUEBAS, 2, LocalDateTime.now()),
                    Visita(2, "OT-2026-1201", 1, LocalDate.now(), TipoVisita.PRUEBAS_Y_MANT, 2)
                )
            ),
            onVolver = {},
            onAbrirVisita = {}
        )
    }
}

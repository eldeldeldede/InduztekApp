package cl.induztek.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.induztek.app.R
import cl.induztek.app.data.aTextoLegible
import cl.induztek.app.model.ActaProtocolo
import cl.induztek.app.model.CierreVisitaUiState
import cl.induztek.app.model.Instalacion
import cl.induztek.app.model.RecepcionActa
import cl.induztek.app.model.TipoResultado
import cl.induztek.app.model.TipoVisita
import cl.induztek.app.model.Visita
import cl.induztek.app.ui.components.BotonPrincipal
import cl.induztek.app.ui.components.CampoTexto
import cl.induztek.app.ui.components.Etiqueta
import cl.induztek.app.ui.components.EtiquetaResultado
import cl.induztek.app.ui.components.MensajeError
import cl.induztek.app.ui.components.TarjetaSeccion
import cl.induztek.app.ui.theme.Advertencia
import cl.induztek.app.ui.theme.AdvertenciaFondo
import cl.induztek.app.ui.theme.Dimens
import cl.induztek.app.ui.theme.Exito
import cl.induztek.app.ui.theme.InduztekAppTheme
import cl.induztek.app.viewmodel.CierreVisitaViewModel
import java.time.LocalDate
import java.time.LocalDateTime

@Composable
fun CierreVisitaScreen(
    onVolver: () -> Unit,
    onAbrirProtocolo: (Long) -> Unit,
    viewModel: CierreVisitaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CierreVisitaContent(
        uiState = uiState,
        onVolver = onVolver,
        onAbrirProtocolo = onAbrirProtocolo,
        onNombreCambia = viewModel::onNombreCambia,
        onRutCambia = viewModel::onRutCambia,
        onCargoCambia = viewModel::onCargoCambia,
        onCerrarVisita = viewModel::onCerrarVisita,
        onRegistrarRecepcion = viewModel::onRegistrarRecepcion
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CierreVisitaContent(
    uiState: CierreVisitaUiState,
    onVolver: () -> Unit,
    onNombreCambia: (String) -> Unit,
    onRutCambia: (String) -> Unit,
    onCargoCambia: (String) -> Unit,
    onCerrarVisita: () -> Unit,
    onRegistrarRecepcion: () -> Unit,
    onAbrirProtocolo: (Long) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_cierre_visita)) },
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
        val visita = uiState.visita
        if (visita == null) {
            if (uiState.cargado) {
                Text(
                    text = stringResource(R.string.visita_no_encontrada),
                    modifier = Modifier.padding(innerPadding).padding(Dimens.espacioMedio)
                )
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            ActaDeTerreno(uiState, visita)

            if (!uiState.estaCerrada && uiState.pendientes.isNotEmpty()) {
                TarjetaSeccion(titulo = stringResource(R.string.seccion_pendientes)) {
                    uiState.pendientes.forEach { pendiente ->
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "${pendiente.numero} · ${pendiente.tagEquipo} (${pendiente.estado.etiqueta})",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = if (pendiente.pruebasFaltantes.isEmpty())
                                    stringResource(R.string.solo_falta_registrar)
                                else stringResource(R.string.pruebas_faltantes_cierre, pendiente.pruebasFaltantes.joinToString()),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            TextButton(onClick = { onAbrirProtocolo(pendiente.idProtocolo) }) {
                                Text(stringResource(R.string.btn_ir_protocolo))
                            }
                        }
                    }
                }
            }

            TarjetaSeccion(titulo = stringResource(R.string.seccion_cierre)) {
                if (uiState.estaCerrada) {
                    Text(
                        text = stringResource(
                            R.string.visita_cerrada_el,
                            visita.fechaHoraCierre?.aTextoLegible().orEmpty()
                        ),
                        color = Exito,
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    Text(stringResource(R.string.cierre_explicacion), style = MaterialTheme.typography.bodyMedium)
                    BotonPrincipal(
                        texto = stringResource(R.string.btn_cerrar_visita),
                        onClick = onCerrarVisita,
                        icono = R.drawable.ic_check
                    )
                }
            }

            TarjetaSeccion(titulo = stringResource(R.string.seccion_recepcion)) {
                val recepcion = uiState.recepcion
                when {
                    recepcion != null -> DatosRecepcion(recepcion)
                    !uiState.estaCerrada ->
                        Text(stringResource(R.string.recepcion_requiere_cierre), style = MaterialTheme.typography.bodyMedium)
                    else -> {
                        CampoTexto(
                            valor = uiState.receptorNombre,
                            onValorCambia = onNombreCambia,
                            etiqueta = stringResource(R.string.campo_receptor_nombre),
                            icono = R.drawable.ic_persona,
                            error = uiState.errorNombre
                        )
                        CampoTexto(
                            valor = uiState.receptorRut,
                            onValorCambia = onRutCambia,
                            etiqueta = stringResource(R.string.campo_receptor_rut), icono = R.drawable.ic_credencial,
                            error = uiState.errorRut
                        )
                        CampoTexto(
                            valor = uiState.receptorCargo,
                            onValorCambia = onCargoCambia,
                            etiqueta = stringResource(R.string.campo_receptor_cargo), icono = R.drawable.ic_cargo,
                            error = uiState.errorCargo
                        )
                        BotonPrincipal(
                            texto = stringResource(R.string.btn_registrar_recepcion),
                            onClick = onRegistrarRecepcion,
                            icono = R.drawable.ic_check
                        )
                    }
                }
            }

            AnimatedVisibility(visible = uiState.mensaje != null) {
                Text(text = uiState.mensaje.orEmpty(), color = Exito, style = MaterialTheme.typography.bodyMedium)
            }
            uiState.errorGeneral?.let { MensajeError(mensaje = it) }
            Spacer(Modifier.height(Dimens.espacioGrande))
        }
    }
}

@Composable
private fun ActaDeTerreno(uiState: CierreVisitaUiState, visita: Visita) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(stringResource(R.string.acta_titulo), style = MaterialTheme.typography.titleLarge)

            Etiqueta(stringResource(R.string.acta_pendiente), Advertencia, AdvertenciaFondo)

            Text(stringResource(R.string.dato_ot, visita.numeroOt), style = MaterialTheme.typography.bodyMedium)
            uiState.instalacion?.let {
                Text(stringResource(R.string.dato_cliente, it.clienteNombre), style = MaterialTheme.typography.bodyMedium)
                Text(stringResource(R.string.dato_instalacion, it.nombre), style = MaterialTheme.typography.bodyMedium)
            }
            Text(stringResource(R.string.dato_fecha, visita.fecha.aTextoLegible()), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.dato_tipo_visita, visita.tipoVisita.etiqueta), style = MaterialTheme.typography.bodyMedium)
            if (uiState.participantes.isNotEmpty()) {
                Text(
                    stringResource(R.string.dato_tecnicos, uiState.participantes.joinToString()),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(stringResource(R.string.dato_ingeniero, uiState.ingenieroNombre), style = MaterialTheme.typography.bodyMedium)

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            if (uiState.acta.isEmpty()) {
                Text(stringResource(R.string.acta_sin_protocolos), style = MaterialTheme.typography.bodyMedium)
            }
            uiState.acta.forEach { protocolo ->
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${protocolo.numero} · ${protocolo.tagEquipo}",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        EtiquetaResultado(protocolo.resultado)
                    }
                    protocolo.lineas.forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
                }
            }

            uiState.resultadoGlobal?.let {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.acta_resultado_visita), style = MaterialTheme.typography.labelLarge)
                    EtiquetaResultado(it)
                }
            }
        }
    }
}

@Composable
private fun DatosRecepcion(recepcion: RecepcionActa) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(R.string.recepcion_registrada), color = Exito, style = MaterialTheme.typography.labelLarge)
        Text(stringResource(R.string.dato_receptor, recepcion.receptorNombre), style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(R.string.dato_receptor_rut, recepcion.receptorRut), style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(R.string.dato_receptor_cargo, recepcion.receptorCargo), style = MaterialTheme.typography.bodyMedium)
        Text(
            stringResource(R.string.dato_recepcion_fecha, recepcion.fechaHoraRecepcion.aTextoLegible()),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private val visitaEjemplo = Visita(
    1, "OT-2026-1187", 1, LocalDate.now(), TipoVisita.SOLO_PRUEBAS, 2
)

@Preview(showBackground = true, heightDp = 1500)
@Composable
private fun CierreVisitaPreview() {
    InduztekAppTheme {
        CierreVisitaContent(
            uiState = CierreVisitaUiState(
                cargado = true,
                visita = visitaEjemplo,
                instalacion = Instalacion(1, 8, "Planta Lampa", "Sector industrial", 1, null, "Lampa", "Envasadora del Pacífico S.A."),
                ingenieroNombre = "A. Contreras",
                participantes = listOf("M. Godoy"),
                acta = listOf(
                    ActaProtocolo(
                        "PT-2026-T1-0001", "TR-01", TipoResultado.OBSERVADO,
                        listOf("Resistencia de aislación · AT-T · Observado", "    Lectura a 1 min: 6100 MΩ")
                    )
                )
            ),
            onVolver = {}, onNombreCambia = {}, onRutCambia = {}, onCargoCambia = {},
            onCerrarVisita = {}, onRegistrarRecepcion = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 1500, name = "Visita cerrada")
@Composable
private fun CierreVisitaCerradaPreview() {
    InduztekAppTheme {
        CierreVisitaContent(
            uiState = CierreVisitaUiState(
                cargado = true,
                visita = visitaEjemplo.copy(fechaHoraCierre = LocalDateTime.now()),
                ingenieroNombre = "A. Contreras",
                receptorRut = "11.111.111-2",
                errorRut = "RUT inválido (revisa el dígito verificador)"
            ),
            onVolver = {}, onNombreCambia = {}, onRutCambia = {}, onCargoCambia = {},
            onCerrarVisita = {}, onRegistrarRecepcion = {}
        )
    }
}

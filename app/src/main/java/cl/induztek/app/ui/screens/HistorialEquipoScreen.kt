package cl.induztek.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import cl.induztek.app.model.CodigoRol
import cl.induztek.app.model.ComparacionValor
import cl.induztek.app.model.Equipo
import cl.induztek.app.model.HistorialEquipoUiState
import cl.induztek.app.model.PuntoHistorial
import cl.induztek.app.model.ResumenProtocolo
import cl.induztek.app.model.SerieMedicion
import cl.induztek.app.model.TipoEstadoProtocolo
import cl.induztek.app.model.TipoResultado
import cl.induztek.app.model.Usuario
import cl.induztek.app.ui.components.Etiqueta
import cl.induztek.app.ui.components.EtiquetaEstado
import cl.induztek.app.ui.components.EtiquetaResultado
import cl.induztek.app.ui.theme.Advertencia
import cl.induztek.app.ui.theme.AdvertenciaFondo
import cl.induztek.app.ui.theme.Dimens
import cl.induztek.app.ui.theme.InduztekAppTheme
import cl.induztek.app.ui.theme.Peligro
import cl.induztek.app.ui.theme.PeligroFondo
import cl.induztek.app.validacion.ReglasProtocolo
import cl.induztek.app.validacion.Unidades
import cl.induztek.app.viewmodel.HistorialEquipoViewModel
import java.time.LocalDate
import java.time.LocalDateTime

@Composable
fun HistorialEquipoScreen(
    usuario: Usuario,
    onVolver: () -> Unit,
    viewModel: HistorialEquipoViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HistorialEquipoContent(uiState = uiState, usuario = usuario, onVolver = onVolver)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialEquipoContent(
    uiState: HistorialEquipoUiState,
    usuario: Usuario,
    onVolver: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.equipo?.tag ?: stringResource(R.string.titulo_historial)) },
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
        val equipo = uiState.equipo
        if (equipo == null) {
            Text(
                text = stringResource(R.string.equipo_no_encontrado),
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(Dimens.espacioMedio)
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            item { DatosEquipo(equipo) }

            if (uiState.comparaciones.isNotEmpty()) {
                item { Seccion(stringResource(R.string.seccion_comparacion)) }
                items(uiState.comparaciones, key = { "${it.serie.elemento}-${it.serie.codigoTipoPrueba}-${it.serie.codigoParametro}" }) {
                    TarjetaComparacion(it)
                }
            }

            item { Seccion(stringResource(R.string.seccion_protocolos)) }

            if (uiState.protocolos.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.lista_vacia_protocolos),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            items(uiState.protocolos, key = { it.idProtocolo }) { protocolo ->
                TarjetaProtocolo(protocolo, usuario, uiState.hoy)
            }
        }
    }
}

@Composable
private fun Seccion(titulo: String) {
    Text(
        text = titulo,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = Dimens.espacioChico)
    )
}

@Composable
private fun DatosEquipo(equipo: Equipo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(equipo.tipoEquipoNombre, style = MaterialTheme.typography.labelLarge)
            Text(equipo.tag, style = MaterialTheme.typography.headlineSmall)

            val marcaModelo = listOfNotNull(equipo.marca, equipo.modelo).joinToString(" ")
            if (marcaModelo.isNotEmpty()) Text(marcaModelo)
            equipo.numeroSerie?.let { Text(stringResource(R.string.dato_serie, it)) }
            equipo.anioFabricacion?.let { Text(stringResource(R.string.dato_anio, it)) }
            equipo.potenciaKva?.let { Text(stringResource(R.string.dato_potencia, Unidades.formatear(it))) }
            equipo.tensionPrimariaKv?.let { Text(stringResource(R.string.dato_tension_primaria, Unidades.formatear(it))) }
            equipo.tensionSecundariaKv?.let { Text(stringResource(R.string.dato_tension_secundaria, Unidades.formatear(it))) }
            equipo.corrienteNominalA?.let { Text(stringResource(R.string.dato_corriente, Unidades.formatear(it))) }
            equipo.maxContactosUohm?.let { Text(stringResource(R.string.dato_max_contactos, Unidades.formatear(it))) }
            equipo.observaciones?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun TarjetaComparacion(comparacion: ComparacionValor) {
    val colorFondo = if (comparacion.significativa) AdvertenciaFondo else MaterialTheme.colorScheme.surface
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorFondo)
    ) {
        Column(
            modifier = Modifier.padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val serie = comparacion.serie
            Text(
                text = "${serie.nombreTipoPrueba} · ${serie.elemento}",
                style = MaterialTheme.typography.labelLarge
            )
            Text(text = serie.nombreParametro, style = MaterialTheme.typography.bodySmall)
            Text(
                text = "${Unidades.formatear(comparacion.actual.valor)} ${serie.unidad}",
                style = MaterialTheme.typography.titleLarge
            )

            val anterior = comparacion.anterior
            if (anterior == null || comparacion.variacionPct == null) {
                Text(stringResource(R.string.primera_medicion), style = MaterialTheme.typography.bodySmall)
            } else {
                Text(
                    text = stringResource(
                        R.string.comparacion_anterior,
                        Unidades.formatear(anterior.valor), serie.unidad,
                        Unidades.formatearPorcentaje(comparacion.variacionPct),
                        anterior.numeroProtocolo
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (comparacion.significativa) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_advertencia),
                        contentDescription = null,
                        tint = Advertencia,
                        modifier = Modifier.size(Dimens.tamanoIcono)
                    )
                    Text(
                        text = stringResource(R.string.variacion_significativa),
                        style = MaterialTheme.typography.labelLarge,
                        color = Advertencia,
                        modifier = Modifier.padding(start = Dimens.espacioChico)
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaProtocolo(protocolo: ResumenProtocolo, usuario: Usuario, hoy: LocalDate) {
    val atrasado = !usuario.esCliente &&
            ReglasProtocolo.estaAtrasado(protocolo.fechaHoraPrueba.toLocalDate(), hoy, protocolo.estado)

    Card(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            overlineContent = { Text(protocolo.fechaHoraPrueba.aTextoLegible()) },
            headlineContent = { Text(protocolo.numero) },
            supportingContent = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.tecnico_responsable, protocolo.tecnicoNombre))
                    if (atrasado) Etiqueta(stringResource(R.string.atrasado), Peligro, PeligroFondo)
                }
            },
            trailingContent = {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    EtiquetaResultado(protocolo.resultadoGlobal)
                    EtiquetaEstado(protocolo.estado, usuario.esCliente)
                }
            }
        )
    }
}

private val usuarioIngeniero = Usuario(
    idUsuario = 2, idRol = 2, codigoRol = CodigoRol.INGENIERO, rut = "12.345.678-5",
    nombre = "A. Contreras", correo = "acontreras@induztek.ejemplo", passwordHash = ""
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HistorialPreview() {
    val serie = SerieMedicion("AISLACION", "Resistencia de aislación", "AT-T", "LECTURA_1MIN", "Lectura a 1 min", "MΩ")
    InduztekAppTheme {
        HistorialEquipoContent(
            uiState = HistorialEquipoUiState(
                equipo = Equipo(
                    1, 1, "TR-01", 1, marca = "Rhona", numeroSerie = "RH-18733", anioFabricacion = 2012,
                    potenciaKva = 1500.0, tensionPrimariaKv = 12.0, tensionSecundariaKv = 0.4,
                    tipoEquipoNombre = "Transformador"
                ),
                comparaciones = listOf(
                    ComparacionValor(
                        serie = serie,
                        actual = PuntoHistorial(3, "PT-2026-0401", LocalDateTime.of(2026, 9, 26, 9, 40), 6100.0),
                        anterior = PuntoHistorial(2, "PT-2025-0288", LocalDateTime.of(2025, 9, 13, 9, 0), 9800.0),
                        variacionPct = -37.755, significativa = true
                    )
                ),
                protocolos = listOf(
                    ResumenProtocolo(
                        3, "PT-2026-0401", 1, "TR-01", LocalDateTime.of(2026, 9, 26, 9, 40),
                        TipoEstadoProtocolo.REGISTRADO_TERRENO, TipoResultado.OBSERVADO, "M. Godoy"
                    ),
                    ResumenProtocolo(
                        2, "PT-2025-0288", 1, "TR-01", LocalDateTime.of(2025, 9, 13, 9, 0),
                        TipoEstadoProtocolo.FORMALIZADO, TipoResultado.APROBADO, "M. Godoy"
                    )
                ),
                hoy = LocalDate.of(2026, 10, 20)
            ),
            usuario = usuarioIngeniero,
            onVolver = {}
        )
    }
}

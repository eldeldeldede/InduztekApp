package cl.induztek.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cl.induztek.app.R
import cl.induztek.app.data.aFechaHora
import cl.induztek.app.data.aTextoLegible
import cl.induztek.app.model.ReporteActa
import cl.induztek.app.model.ReportesActaUiState
import cl.induztek.app.model.ResumenProtocolo
import cl.induztek.app.model.TipoEstadoProtocolo
import cl.induztek.app.ui.theme.Advertencia
import cl.induztek.app.ui.theme.AdvertenciaFondo
import cl.induztek.app.ui.theme.Dimens
import cl.induztek.app.ui.theme.Exito
import cl.induztek.app.ui.theme.ExitoFondo
import cl.induztek.app.ui.theme.Neutro
import cl.induztek.app.ui.theme.NeutroFondo
import cl.induztek.app.ui.theme.Peligro
import cl.induztek.app.ui.theme.PeligroFondo

// =====================================================================
//  ReportesActa.kt — piezas de pantalla para los reportes de actas
//   EtiquetaReporte  -> el estado (Abierto, En revisión, Resuelto, Rechazado) con su color
//   TarjetaReporte   -> un reporte con su respuesta (la usan el cliente y el personal)
//   SeccionReportesCliente -> dentro del acta: formulario "Reportar un problema" + seguimiento
// =====================================================================
@Composable
fun EtiquetaReporte(reporte: ReporteActa, modifier: Modifier = Modifier) {
    when (reporte.estado) {
        "ABIERTO" -> Etiqueta(reporte.etiquetaEstado, Advertencia, AdvertenciaFondo, modifier)
        "RESUELTO" -> Etiqueta(reporte.etiquetaEstado, Exito, ExitoFondo, modifier)
        "RECHAZADO" -> Etiqueta(reporte.etiquetaEstado, Peligro, PeligroFondo, modifier)
        else -> Etiqueta(reporte.etiquetaEstado, Neutro, NeutroFondo, modifier)
    }
}

@Composable
fun TarjetaReporte(
    reporte: ReporteActa,
    modifier: Modifier = Modifier,
    clienteNombre: String? = null,
    acciones: @Composable () -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Acta ${reporte.numeroOt}" + (reporte.numeroProtocolo?.let { " · $it" } ?: ""),
                    style = MaterialTheme.typography.titleSmall
                )
                EtiquetaReporte(reporte)
            }
            if (clienteNombre != null) {
                Text(clienteNombre, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(reporte.texto, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = runCatching { reporte.fechaHoraCreacion.aFechaHora().aTextoLegible() }.getOrDefault(reporte.fechaHoraCreacion),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!reporte.respuesta.isNullOrBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(stringResource(R.string.reporte_respuesta), style = MaterialTheme.typography.labelMedium)
                    Text(reporte.respuesta, style = MaterialTheme.typography.bodyMedium)
                }
            }
            acciones()
        }
    }
}

// Los filtros de estado de la lista (Todos / Abierto / En revisión / Resuelto / Rechazado)
@Composable
fun FiltrosEstadoReporte(seleccionado: String?, onSeleccionar: (String?) -> Unit) {
    val opciones = listOf<Pair<String?, String>>(
        null to "Todos", "ABIERTO" to "Abiertos", "EN_REVISION" to "En revisión",
        "RESUELTO" to "Resueltos", "RECHAZADO" to "Rechazados"
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(opciones) { (estado, etiqueta) ->
            FilterChip(
                selected = seleccionado == estado,
                onClick = { onSeleccionar(estado) },
                label = { Text(etiqueta) }
            )
        }
    }
}

@Composable
fun SeccionReportesCliente(
    uiState: ReportesActaUiState,
    protocolos: List<ResumenProtocolo>,
    onTextoCambia: (String) -> Unit,
    onProtocoloSeleccionado: (Long?) -> Unit,
    onEnviar: () -> Unit
) {
    TarjetaSeccion(titulo = stringResource(R.string.seccion_reportar_acta)) {
        Text(stringResource(R.string.reportar_ayuda), style = MaterialTheme.typography.bodyMedium)

        // Protocolo observado (opcional): solo los que no están anulados
        val candidatos = protocolos.filter { it.estado != TipoEstadoProtocolo.ANULADO }
        if (candidatos.isNotEmpty()) {
            Text(stringResource(R.string.reportar_protocolo), style = MaterialTheme.typography.labelMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = uiState.idProtocolo == null,
                        onClick = { onProtocoloSeleccionado(null) },
                        label = { Text(stringResource(R.string.reportar_acta_completa)) }
                    )
                }
                items(candidatos) { p ->
                    FilterChip(
                        selected = uiState.idProtocolo == p.idProtocolo,
                        onClick = { onProtocoloSeleccionado(p.idProtocolo) },
                        label = { Text(p.tagEquipo.ifEmpty { p.numero }) }
                    )
                }
            }
        }
        CampoTexto(
            valor = uiState.texto,
            onValorCambia = onTextoCambia,
            etiqueta = stringResource(R.string.campo_reporte_texto),
            icono = R.drawable.ic_nota,
            error = uiState.errorTexto,
            lineasMaximas = 4
        )
        uiState.errorGeneral?.let { MensajeError(mensaje = it) }
        uiState.exito?.let { Text(it, color = Exito, style = MaterialTheme.typography.bodyMedium) }
        BotonPrincipal(
            texto = stringResource(if (uiState.enviando) R.string.btn_enviando else R.string.btn_enviar_reporte),
            onClick = onEnviar,
            icono = R.drawable.ic_check,
            habilitado = !uiState.enviando,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (uiState.reportes.isNotEmpty()) {
        TarjetaSeccion(titulo = stringResource(R.string.seccion_seguimiento_reportes)) {
            uiState.reportes.forEach { TarjetaReporte(reporte = it) }
        }
    }
}

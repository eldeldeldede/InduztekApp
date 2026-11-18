package cl.induztek.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.induztek.app.local.AlmacenLocal
import cl.induztek.app.local.EstadoOperacion
import cl.induztek.app.repository.Sincronizador
import cl.induztek.app.ui.theme.Advertencia
import cl.induztek.app.ui.theme.AdvertenciaFondo
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

// =====================================================================
//  Sincronizacion.kt — barra "N cambios pendientes de sincronizar" (RF de modo sin conexión)
//  ---------------------------------------------------------------------
//  Lee la cola de AlmacenLocal. Si hay cambios hechos sin conexión, muestra cuántos son y permite
//  verlos, enviarlos ahora (si hay red) o descartar los que fallaron.
// =====================================================================
@Composable
fun BarraSincronizacion(hayRed: Boolean, modifier: Modifier = Modifier) {
    val cola by AlmacenLocal.cola.collectAsStateWithLifecycle()
    val sincronizando by Sincronizador.sincronizando.collectAsStateWithLifecycle()
    var detalle by remember { mutableStateOf(false) }
    val alcance = rememberCoroutineScope()

    if (cola.isEmpty()) return
    val fallidas = cola.count { it.estado == EstadoOperacion.FALLIDA }
    val pendientes = cola.size - fallidas

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .background(if (fallidas > 0) MaterialTheme.colorScheme.errorContainer else AdvertenciaFondo)
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text(
            text = when {
                sincronizando -> "Sincronizando…"
                fallidas > 0 -> "$pendientes pendiente(s) y $fallidas con error"
                else -> "$pendientes cambio(s) pendiente(s) de sincronizar"
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (fallidas > 0) MaterialTheme.colorScheme.onErrorContainer else Advertencia
        )
        TextButton(onClick = { detalle = true }) { Text("Ver") }
    }

    if (detalle) {
        AlertDialog(
            onDismissRequest = { detalle = false },
            title = { Text("Cambios sin sincronizar") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(cola, key = { it.id }) { op ->
                        Column {
                            Text(op.descripcion, style = MaterialTheme.typography.bodyMedium)
                            if (op.estado == EstadoOperacion.FALLIDA) {
                                Text(
                                    "No se pudo enviar: ${op.error ?: "error"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Text("Esperando conexión", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = hayRed && !sincronizando,
                    onClick = {
                        Sincronizador.reintentarFallidas()
                        alcance.launch { Sincronizador.sincronizar() }
                    }
                ) { Text("Sincronizar ahora") }
            },
            dismissButton = {
                Row {
                    if (fallidas > 0) TextButton(onClick = { Sincronizador.descartarFallidas() }) { Text("Descartar con error") }
                    TextButton(onClick = { detalle = false }) { Text("Cerrar") }
                }
            }
        )
    }
}

package cl.induztek.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

@Composable
fun AlReanudar(propietario: LifecycleOwner, accion: () -> Unit) {

    val accionActual by rememberUpdatedState(accion)
    DisposableEffect(propietario) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) accionActual()
        }
        propietario.lifecycle.addObserver(observador)

        onDispose { propietario.lifecycle.removeObserver(observador) }
    }
}

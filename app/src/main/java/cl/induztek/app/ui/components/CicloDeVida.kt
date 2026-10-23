package cl.induztek.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

// =====================================================================
//  CicloDeVida.kt — "ejecuta esto cada vez que la pantalla vuelva a mostrarse"
//  ---------------------------------------------------------------------
//  Problema: al guardar un equipo nuevo se vuelve a la lista de equipos. Esa pantalla
//  ya estaba creada (solo estaba tapada), así que NO vuelve a leer la base de datos sola
//  y el equipo recién creado no aparecería.
//  Solución: esta función ejecuta `accion` cada vez que la pantalla pasa a estar visible
//  (evento ON_RESUME del ciclo de vida), incluida la primera vez.
//
//  En AppNavegacion se usa así:   AlReanudar(entrada) { viewModel.cargar(...) }
//  (la `entrada` de la navegación es un LifecycleOwner).
// =====================================================================
@Composable
fun AlReanudar(propietario: LifecycleOwner, accion: () -> Unit) {
    // rememberUpdatedState: el observador siempre llama a la ÚLTIMA versión de `accion`
    val accionActual by rememberUpdatedState(accion)
    DisposableEffect(propietario) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) accionActual()
        }
        propietario.lifecycle.addObserver(observador)
        // onDispose: al salir de la pantalla se quita el observador (si no, quedaría "colgado")
        onDispose { propietario.lifecycle.removeObserver(observador) }
    }
}

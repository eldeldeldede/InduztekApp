package cl.induztek.app.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

// =====================================================================
//  AppNavegacion.kt — el mapa de pantallas
//  ---------------------------------------------------------------------
//  NavHost = el "contenedor" que muestra UNA pantalla a la vez según la ruta actual.
//  navController.navigate("ruta") cambia de pantalla.
//
//  ESQUELETO: cada pantalla es por ahora un PantallaPendiente (un texto y botones para probar
//  la navegación). A medida que Pedro termine cada pantalla, se reemplaza la línea
//  `PantallaPendiente(...)` por la pantalla real, por ejemplo:
//
//      composable(Rutas.LOGIN) { LoginScreen(onIngresar = { navController.navigate(Rutas.INSTALACIONES) }) }
//
//  Las pantallas NO reciben el navController: reciben funciones (onIngresar, onVolver...).
//  Así no dependen de la navegación y se pueden previsualizar solas (@Preview).
// =====================================================================
@Composable
fun AppNavegacion() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Rutas.LOGIN) {

        composable(Rutas.LOGIN) {
            PantallaPendiente(
                titulo = "Login por rol",
                destinos = listOf("Ir a instalaciones" to { navController.navigate(Rutas.INSTALACIONES) })
            )
        }

        composable(Rutas.INSTALACIONES) {
            PantallaPendiente(
                titulo = "Instalaciones",
                destinos = listOf("Ver equipos de la instalación 1" to { navController.navigate(Rutas.equipos(1)) })
            )
        }

        // Pantallas con ARGUMENTO: se declara su nombre y tipo, y se lee desde la entrada.
        composable(
            route = Rutas.EQUIPOS,
            arguments = listOf(navArgument(Rutas.ARG_ID_INSTALACION) { type = NavType.LongType })
        ) { entrada ->
            val idInstalacion = entrada.arguments?.getLong(Rutas.ARG_ID_INSTALACION) ?: 0L
            PantallaPendiente(
                titulo = "Equipos de la instalación $idInstalacion",
                destinos = listOf(
                    "Alta de equipo" to { navController.navigate(Rutas.altaEquipo(idInstalacion)) },
                    "Historial del equipo 1 (TR-01)" to { navController.navigate(Rutas.historialEquipo(1)) },
                    "Volver" to { navController.popBackStack() }
                )
            )
        }

        composable(
            route = Rutas.ALTA_EQUIPO,
            arguments = listOf(navArgument(Rutas.ARG_ID_INSTALACION) { type = NavType.LongType })
        ) { entrada ->
            val idInstalacion = entrada.arguments?.getLong(Rutas.ARG_ID_INSTALACION) ?: 0L
            PantallaPendiente(
                titulo = "Alta de equipo (instalación $idInstalacion)",
                destinos = listOf("Volver" to { navController.popBackStack() })
            )
        }

        composable(
            route = Rutas.HISTORIAL_EQUIPO,
            arguments = listOf(navArgument(Rutas.ARG_ID_EQUIPO) { type = NavType.LongType })
        ) { entrada ->
            val idEquipo = entrada.arguments?.getLong(Rutas.ARG_ID_EQUIPO) ?: 0L
            PantallaPendiente(
                titulo = "Historial y comparación del equipo $idEquipo",
                destinos = listOf("Volver" to { navController.popBackStack() })
            )
        }

        composable(
            route = Rutas.MEDICIONES,
            arguments = listOf(navArgument(Rutas.ARG_ID_PROTOCOLO) { type = NavType.LongType })
        ) { entrada ->
            val idProtocolo = entrada.arguments?.getLong(Rutas.ARG_ID_PROTOCOLO) ?: 0L
            PantallaPendiente(
                titulo = "Mediciones del protocolo $idProtocolo",
                destinos = listOf("Volver" to { navController.popBackStack() })
            )
        }

        composable(
            route = Rutas.CIERRE_VISITA,
            arguments = listOf(navArgument(Rutas.ARG_ID_VISITA) { type = NavType.LongType })
        ) { entrada ->
            val idVisita = entrada.arguments?.getLong(Rutas.ARG_ID_VISITA) ?: 0L
            PantallaPendiente(
                titulo = "Cierre de visita $idVisita y recepción del acta",
                destinos = listOf("Volver" to { navController.popBackStack() })
            )
        }
    }
}

// Pantalla provisoria: título + un botón por cada destino. BORRAR cuando todas estén hechas.
@Composable
private fun PantallaPendiente(titulo: String, destinos: List<Pair<String, () -> Unit>>) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = titulo, style = MaterialTheme.typography.titleLarge)
            Text(text = "Pantalla pendiente", style = MaterialTheme.typography.bodyMedium)
            destinos.forEach { (texto, accion) ->
                Button(onClick = accion) { Text(texto) }
            }
        }
    }
}

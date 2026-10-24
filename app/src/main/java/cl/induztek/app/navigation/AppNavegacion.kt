package cl.induztek.app.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.induztek.app.ui.components.AlReanudar
import cl.induztek.app.ui.components.AvisoSinSenal
import cl.induztek.app.ui.components.rememberHayRed
import cl.induztek.app.ui.screens.AltaEquipoScreen
import cl.induztek.app.ui.screens.EquiposScreen
import cl.induztek.app.ui.screens.HistorialEquipoScreen
import cl.induztek.app.ui.screens.InstalacionesScreen
import cl.induztek.app.ui.screens.LoginScreen
import cl.induztek.app.viewmodel.EquiposViewModel
import cl.induztek.app.viewmodel.HistorialEquipoViewModel
import cl.induztek.app.viewmodel.InstalacionesViewModel
import cl.induztek.app.viewmodel.SesionViewModel

// =====================================================================
//  AppNavegacion.kt — el mapa de pantallas (como AppNavegacion de MiRegistro)
//  ---------------------------------------------------------------------
//  NavHost = el "contenedor" que muestra UNA pantalla a la vez según la ruta actual.
//  navController.navigate("ruta") cambia de pantalla.
//
//  - Las pantallas NO reciben el navController: reciben funciones (onVolver, onVerEquipos...).
//    Así no dependen de la navegación y se pueden previsualizar solas (@Preview).
//  - SesionViewModel se crea UNA vez aquí y guarda quién inició sesión.
//  - Todas las pantallas, salvo el login, exigen un usuario con sesión: si no hay, vuelven al login.
//  - Los ViewModels de lista se crean AQUÍ (viewModel()) para poder pedirles que recarguen los
//    datos cada vez que la pantalla vuelve a mostrarse (AlReanudar).
//
//  Pantallas que aún son PantallaPendiente (las irá reemplazando el equipo):
//  mediciones y cierre de visita.
// =====================================================================
@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    val sesionViewModel: SesionViewModel = viewModel()
    val usuarioActual by sesionViewModel.usuarioActual.collectAsStateWithLifecycle()

    // Si hay sesión guardada se entra directo; si no, al login.
    // remember { }: se calcula UNA sola vez al abrir la app. Si cambiara en cada recomposición
    // (al iniciar o cerrar sesión), el NavHost reiniciaría todo el recorrido de pantallas.
    val destinoInicial = remember {
        if (sesionViewModel.usuarioActual.value != null) Rutas.INSTALACIONES else Rutas.LOGIN
    }

    // Al cerrar sesión (usuarioActual pasa a null) se vuelve al login y se borra el historial de pantallas
    val entradaActual by navController.currentBackStackEntryAsState()
    LaunchedEffect(usuarioActual, entradaActual?.destination?.route) {
        val ruta = entradaActual?.destination?.route
        if (usuarioActual == null && ruta != null && ruta != Rutas.LOGIN) irAlLogin(navController)
    }

    // Recurso nativo: estado de la red. El aviso se dibuja encima de TODAS las pantallas (abajo).
    val hayRed by rememberHayRed()

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = destinoInicial) {

            composable(Rutas.LOGIN) {
                LoginScreen(
                    onIngresoExitoso = { usuario ->
                        sesionViewModel.iniciarSesion(usuario)
                        // popUpTo: el login sale del historial, así "atrás" no vuelve a él
                        navController.navigate(Rutas.INSTALACIONES) {
                            popUpTo(Rutas.LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            composable(Rutas.INSTALACIONES) { entrada ->
                val usuario = usuarioActual ?: return@composable
                val viewModel: InstalacionesViewModel = viewModel()
                AlReanudar(entrada) { viewModel.cargar(usuario) }
                InstalacionesScreen(
                    usuario = usuario,
                    onVerEquipos = { idInstalacion -> navController.navigate(Rutas.equipos(idInstalacion)) },
                    onCerrarSesion = { sesionViewModel.cerrarSesion() },
                    viewModel = viewModel
                )
            }

            // Pantallas con ARGUMENTO: se declara su nombre y tipo, y se lee desde la entrada.
            composable(
                route = Rutas.EQUIPOS,
                arguments = listOf(navArgument(Rutas.ARG_ID_INSTALACION) { type = NavType.LongType })
            ) { entrada ->
                val usuario = usuarioActual ?: return@composable
                val idInstalacion = entrada.arguments?.getLong(Rutas.ARG_ID_INSTALACION) ?: 0L
                val viewModel: EquiposViewModel = viewModel()
                AlReanudar(entrada) { viewModel.cargar(idInstalacion, usuario) }
                EquiposScreen(
                    usuario = usuario,
                    onVolver = { navController.popBackStack() },
                    onAltaEquipo = { navController.navigate(Rutas.altaEquipo(idInstalacion)) },
                    onVerHistorial = { idEquipo -> navController.navigate(Rutas.historialEquipo(idEquipo)) },
                    viewModel = viewModel
                )
            }

            composable(
                route = Rutas.ALTA_EQUIPO,
                arguments = listOf(navArgument(Rutas.ARG_ID_INSTALACION) { type = NavType.LongType })
            ) { entrada ->
                usuarioActual ?: return@composable
                val idInstalacion = entrada.arguments?.getLong(Rutas.ARG_ID_INSTALACION) ?: 0L
                AltaEquipoScreen(
                    idInstalacion = idInstalacion,
                    onVolver = { navController.popBackStack() },
                    onGuardado = { navController.popBackStack() },
                    // HU-01: el TAG ya existía -> se abre el historial del equipo ya registrado
                    onVerExistente = { idEquipo ->
                        navController.navigate(Rutas.historialEquipo(idEquipo)) {
                            popUpTo(Rutas.ALTA_EQUIPO) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = Rutas.HISTORIAL_EQUIPO,
                arguments = listOf(navArgument(Rutas.ARG_ID_EQUIPO) { type = NavType.LongType })
            ) { entrada ->
                val usuario = usuarioActual ?: return@composable
                val idEquipo = entrada.arguments?.getLong(Rutas.ARG_ID_EQUIPO) ?: 0L
                val viewModel: HistorialEquipoViewModel = viewModel()
                AlReanudar(entrada) { viewModel.cargar(idEquipo, usuario) }
                HistorialEquipoScreen(
                    usuario = usuario,
                    onVolver = { navController.popBackStack() },
                    viewModel = viewModel
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
        AvisoSinSenal(visible = !hayRed, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

// Vuelve al login y vacía el historial de pantallas (popUpTo(0) = hasta el principio)
private fun irAlLogin(navController: NavHostController) {
    navController.navigate(Rutas.LOGIN) {
        popUpTo(0) { inclusive = true }
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

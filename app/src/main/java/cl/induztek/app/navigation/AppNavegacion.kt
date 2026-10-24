package cl.induztek.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import cl.induztek.app.ui.screens.ActasScreen
import cl.induztek.app.ui.screens.AltaEquipoScreen
import cl.induztek.app.ui.screens.CierreVisitaScreen
import cl.induztek.app.viewmodel.ActasViewModel
import cl.induztek.app.viewmodel.CierreVisitaViewModel
import cl.induztek.app.ui.screens.EquiposScreen
import cl.induztek.app.ui.screens.HistorialEquipoScreen
import cl.induztek.app.ui.screens.InstalacionesScreen
import cl.induztek.app.ui.screens.LoginScreen
import cl.induztek.app.ui.screens.MedicionesScreen
import cl.induztek.app.ui.screens.NuevoProtocoloScreen
import cl.induztek.app.viewmodel.MedicionesViewModel
import cl.induztek.app.viewmodel.EquiposViewModel
import cl.induztek.app.viewmodel.HistorialEquipoViewModel
import cl.induztek.app.viewmodel.InstalacionesViewModel
import cl.induztek.app.viewmodel.SesionViewModel

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    val sesionViewModel: SesionViewModel = viewModel()
    val usuarioActual by sesionViewModel.usuarioActual.collectAsStateWithLifecycle()

    val destinoInicial = remember {
        if (sesionViewModel.usuarioActual.value != null) Rutas.INSTALACIONES else Rutas.LOGIN
    }

    val entradaActual by navController.currentBackStackEntryAsState()
    LaunchedEffect(usuarioActual, entradaActual?.destination?.route) {
        val ruta = entradaActual?.destination?.route
        if (usuarioActual == null && ruta != null && ruta != Rutas.LOGIN) irAlLogin(navController)
    }

    val hayRed by rememberHayRed()

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = destinoInicial,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(350)) + fadeIn(tween(350))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(350)) + fadeOut(tween(350))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(350)) + fadeIn(tween(350))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(350)) + fadeOut(tween(350))
            }
        ) {

            composable(Rutas.LOGIN) {
                LoginScreen(
                    onIngresoExitoso = { usuario ->
                        sesionViewModel.iniciarSesion(usuario)

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
                    onNuevoProtocolo = { idEquipo -> navController.navigate(Rutas.nuevoProtocolo(idEquipo)) },
                    onVerActas = { navController.navigate(Rutas.actas(idInstalacion)) },
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

                    onAbrirProtocolo = { idProtocolo -> navController.navigate(Rutas.mediciones(idProtocolo)) },
                    viewModel = viewModel
                )
            }

            composable(
                route = Rutas.NUEVO_PROTOCOLO,
                arguments = listOf(navArgument(Rutas.ARG_ID_EQUIPO) { type = NavType.LongType })
            ) { entrada ->
                val usuario = usuarioActual ?: return@composable
                val idEquipo = entrada.arguments?.getLong(Rutas.ARG_ID_EQUIPO) ?: 0L
                NuevoProtocoloScreen(
                    idEquipo = idEquipo,
                    usuario = usuario,
                    onVolver = { navController.popBackStack() },

                    onProtocoloListo = { idProtocolo ->
                        navController.navigate(Rutas.mediciones(idProtocolo)) {
                            popUpTo(Rutas.NUEVO_PROTOCOLO) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = Rutas.MEDICIONES,
                arguments = listOf(navArgument(Rutas.ARG_ID_PROTOCOLO) { type = NavType.LongType })
            ) { entrada ->
                val usuario = usuarioActual ?: return@composable
                val idProtocolo = entrada.arguments?.getLong(Rutas.ARG_ID_PROTOCOLO) ?: 0L
                val viewModel: MedicionesViewModel = viewModel()
                AlReanudar(entrada) { viewModel.cargar(idProtocolo, usuario) }
                MedicionesScreen(
                    onVolver = { navController.popBackStack() },
                    onCerrarVisita = { idVisita -> navController.navigate(Rutas.cierreVisita(idVisita)) },
                    viewModel = viewModel
                )
            }

            composable(
                route = Rutas.ACTAS,
                arguments = listOf(navArgument(Rutas.ARG_ID_INSTALACION) { type = NavType.LongType })
            ) { entrada ->
                val usuario = usuarioActual ?: return@composable
                val idInstalacion = entrada.arguments?.getLong(Rutas.ARG_ID_INSTALACION) ?: 0L
                val viewModel: ActasViewModel = viewModel()
                AlReanudar(entrada) { viewModel.cargar(idInstalacion, usuario) }
                ActasScreen(
                    onVolver = { navController.popBackStack() },
                    onAbrirVisita = { idVisita -> navController.navigate(Rutas.cierreVisita(idVisita)) },
                    viewModel = viewModel
                )
            }

            composable(
                route = Rutas.CIERRE_VISITA,
                arguments = listOf(navArgument(Rutas.ARG_ID_VISITA) { type = NavType.LongType })
            ) { entrada ->
                val usuario = usuarioActual ?: return@composable
                val idVisita = entrada.arguments?.getLong(Rutas.ARG_ID_VISITA) ?: 0L
                val viewModel: CierreVisitaViewModel = viewModel()
                AlReanudar(entrada) { viewModel.cargar(idVisita, usuario) }
                CierreVisitaScreen(
                    onVolver = { navController.popBackStack() },
                    onAbrirProtocolo = { idProtocolo -> navController.navigate(Rutas.mediciones(idProtocolo)) },
                    viewModel = viewModel
                )
            }
        }
        AvisoSinSenal(visible = !hayRed, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

private fun irAlLogin(navController: NavHostController) {
    navController.navigate(Rutas.LOGIN) {
        popUpTo(0) { inclusive = true }
    }
}

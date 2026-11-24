package cl.induztek.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.induztek.app.local.AlmacenLocal
import cl.induztek.app.navigation.AppNavegacion
import cl.induztek.app.util.MonitorConexion
import java.io.File
import cl.induztek.app.ui.theme.EscalaFuente
import cl.induztek.app.ui.theme.InduztekAppTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Lo único que se guarda en el teléfono: cola de cambios sin conexión y caché (ver AlmacenLocal)
        AlmacenLocal.iniciar(File(filesDir, "local"))
        MonitorConexion.iniciar(this)
        EscalaFuente.cargar(this)
        enableEdgeToEdge()
        setContent {
            val nivel by EscalaFuente.nivel.collectAsStateWithLifecycle()
            val densidad = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(densidad.density, densidad.fontScale * nivel.factor)
            ) {
                InduztekAppTheme {
                    AppNavegacion()
                }
            }
        }
    }
}

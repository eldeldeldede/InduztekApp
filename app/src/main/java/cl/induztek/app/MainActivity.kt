package cl.induztek.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.induztek.app.local.AlmacenLocal
import cl.induztek.app.navigation.AppNavegacion
import cl.induztek.app.util.MonitorConexion
import java.io.File
import cl.induztek.app.ui.theme.InduztekAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Lo único que se guarda en el teléfono: cola de cambios sin conexión y caché (ver AlmacenLocal)
        AlmacenLocal.iniciar(File(filesDir, "local"))
        MonitorConexion.iniciar(this)
        enableEdgeToEdge()
        setContent {
            InduztekAppTheme {

                AppNavegacion()
            }
        }
    }
}

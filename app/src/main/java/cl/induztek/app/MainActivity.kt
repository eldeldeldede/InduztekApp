package cl.induztek.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.induztek.app.navigation.AppNavegacion
import cl.induztek.app.ui.theme.InduztekAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InduztekAppTheme {

                AppNavegacion()
            }
        }
    }
}

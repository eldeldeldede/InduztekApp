package cl.induztek.app.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import cl.induztek.app.repository.Repositorios
import cl.induztek.app.repository.Sincronizador
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// =====================================================================
//  MonitorConexion.kt — avisa a la lógica de la app cuando vuelve la red
//  ---------------------------------------------------------------------
//  Android llama a onAvailable cada vez que aparece una red. En ese momento se envían los cambios
//  hechos sin conexión (cola) y se actualiza la caché. Esto es "usar la conectividad en la lógica":
//  no es solo un cartel, la app decide qué hacer según haya o no red.
// =====================================================================
object MonitorConexion {

    private var registrado = false

    fun iniciar(contexto: Context) {
        if (registrado) return
        registrado = true
        val administrador = contexto.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        administrador.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Sincronizador.alcance.launch {
                    delay(1000) // la red recién apareció: se le da un momento para estabilizarse
                    Sincronizador.alVolverConexion(Repositorios.usuarioActual)
                }
            }
        })
    }
}

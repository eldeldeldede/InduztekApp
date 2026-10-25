package cl.induztek.app.ui.components

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.induztek.app.R
import cl.induztek.app.ui.theme.Advertencia
import cl.induztek.app.ui.theme.AdvertenciaFondo
import cl.induztek.app.ui.theme.InduztekAppTheme

@Composable
fun rememberHayRed(): State<Boolean> {
    val contexto = LocalContext.current
    val hayRed = remember { mutableStateOf(redDisponible(contexto)) }

    DisposableEffect(contexto) {
        val administrador = contexto.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val oyente = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { hayRed.value = true }
            override fun onLost(network: Network) { hayRed.value = redDisponible(contexto) }
        }
        administrador.registerDefaultNetworkCallback(oyente)
        onDispose { administrador.unregisterNetworkCallback(oyente) }
    }
    return hayRed
}

private fun redDisponible(contexto: Context): Boolean {
    val administrador = contexto.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capacidades = administrador.getNetworkCapabilities(administrador.activeNetwork)
    return capacidades?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
}

@Composable
fun AvisoSinSenal(visible: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(AdvertenciaFondo)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_advertencia),
                contentDescription = null,
                tint = Advertencia,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.aviso_sin_senal),
                style = MaterialTheme.typography.bodySmall,
                color = Advertencia
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AvisoSinSenalPreview() {
    InduztekAppTheme { AvisoSinSenal(visible = true) }
}

package cl.induztek.app.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cl.induztek.app.model.TipoEstadoProtocolo
import cl.induztek.app.model.TipoResultado
import cl.induztek.app.ui.theme.Advertencia
import cl.induztek.app.ui.theme.AdvertenciaFondo
import cl.induztek.app.ui.theme.Dimens
import cl.induztek.app.ui.theme.Exito
import cl.induztek.app.ui.theme.ExitoFondo
import cl.induztek.app.ui.theme.Neutro
import cl.induztek.app.ui.theme.NeutroFondo
import cl.induztek.app.ui.theme.Peligro
import cl.induztek.app.ui.theme.PeligroFondo

@Composable
fun TarjetaSeccion(
    titulo: String,
    modifier: Modifier = Modifier,
    contenido: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth().animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            Text(text = titulo, style = MaterialTheme.typography.titleMedium)
            contenido()
        }
    }
}

@Composable
fun Etiqueta(texto: String, colorTexto: Color, colorFondo: Color, modifier: Modifier = Modifier) {
    val fondoAnimado by animateColorAsState(colorFondo, tween(400), label = "fondoEtiqueta")
    val textoAnimado by animateColorAsState(colorTexto, tween(400), label = "textoEtiqueta")
    Surface(
        color = fondoAnimado,
        contentColor = textoAnimado,
        shape = RoundedCornerShape(50),
        modifier = modifier.animateContentSize()
    ) {
        Crossfade(targetState = texto, animationSpec = tween(300), label = "textoEtiquetaCambio") { valor ->
            Text(
                text = valor,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(PaddingValues(horizontal = 10.dp, vertical = 4.dp))
            )
        }
    }
}

@Composable
fun EtiquetaResultado(resultado: TipoResultado?, modifier: Modifier = Modifier) {
    when (resultado) {
        TipoResultado.APROBADO -> Etiqueta(resultado.etiqueta, Exito, ExitoFondo, modifier)
        TipoResultado.OBSERVADO -> Etiqueta(resultado.etiqueta, Advertencia, AdvertenciaFondo, modifier)
        TipoResultado.RECHAZADO -> Etiqueta(resultado.etiqueta, Peligro, PeligroFondo, modifier)
        null -> Etiqueta("Sin resultado", Neutro, NeutroFondo, modifier)
    }
}

@Composable
fun EtiquetaEstado(estado: TipoEstadoProtocolo, esCliente: Boolean, modifier: Modifier = Modifier) {
    val texto = estado.etiquetaPara(esCliente)
    when (estado) {
        TipoEstadoProtocolo.FORMALIZADO -> Etiqueta(texto, Exito, ExitoFondo, modifier)
        TipoEstadoProtocolo.OBSERVADO -> Etiqueta(texto, Advertencia, AdvertenciaFondo, modifier)
        TipoEstadoProtocolo.ANULADO -> Etiqueta(texto, Peligro, PeligroFondo, modifier)
        else -> Etiqueta(texto, Neutro, NeutroFondo, modifier)
    }
}

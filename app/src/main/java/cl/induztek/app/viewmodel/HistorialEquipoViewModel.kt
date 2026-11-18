package cl.induztek.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.induztek.app.model.ComparacionValor
import cl.induztek.app.model.HistorialEquipoUiState
import cl.induztek.app.model.PuntoHistorial
import cl.induztek.app.model.SerieMedicion
import cl.induztek.app.model.Usuario
import cl.induztek.app.network.ResultadoRed
import cl.induztek.app.network.mensajeError
import cl.induztek.app.repository.Repositorios
import cl.induztek.app.validacion.CriteriosResultado
import cl.induztek.app.validacion.ParametrosPrincipales
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

// HistorialEquipoViewModel — protocolos de un equipo y comparación del valor actual con el anterior.
class HistorialEquipoViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(HistorialEquipoUiState())
    val uiState: StateFlow<HistorialEquipoUiState> = _uiState.asStateFlow()

    fun cargar(idEquipo: Long, usuario: Usuario) {
        _uiState.value = _uiState.value.copy(cargando = true, error = null)
        viewModelScope.launch {
            val equipoRes = Repositorios.equipos.equipo(idEquipo, usuario)
            val equipo = (equipoRes as? ResultadoRed.Exito)?.datos
            if (equipo == null) {
                _uiState.value = HistorialEquipoUiState(error = equipoRes.mensajeError())
                return@launch
            }
            val protocolos = Repositorios.protocolos.protocolosPorEquipo(idEquipo, usuario, equipo.tag)
            // El cliente no ve comparaciones técnicas, solo el listado de protocolos formalizados.
            val comparaciones = if (usuario.esCliente) emptyList() else armarComparaciones(idEquipo)
            _uiState.value = HistorialEquipoUiState(
                equipo = equipo,
                protocolos = (protocolos as? ResultadoRed.Exito)?.datos ?: emptyList(),
                comparaciones = comparaciones,
                hoy = LocalDate.now(),
                error = protocolos.mensajeError()
            )
        }
    }

    private suspend fun armarComparaciones(idEquipo: Long): List<ComparacionValor> {
        val series: List<Pair<SerieMedicion, List<PuntoHistorial>>> =
            (Repositorios.protocolos.seriesDeEquipo(idEquipo) as? ResultadoRed.Exito)?.datos ?: return emptyList()
        return series
            .filter { (serie, _) -> ParametrosPrincipales.esPrincipal(serie.codigoTipoPrueba, serie.codigoParametro) }
            .mapNotNull { (serie, puntos) ->
                val actual = puntos.firstOrNull() ?: return@mapNotNull null
                val anterior = puntos.getOrNull(1)
                val variacion = anterior?.let { CriteriosResultado.variacionPorcentual(it.valor, actual.valor) }
                ComparacionValor(
                    serie = serie,
                    actual = actual,
                    anterior = anterior,
                    variacionPct = variacion,
                    significativa = anterior != null &&
                        CriteriosResultado.esVariacionSignificativa(anterior.valor, actual.valor)
                )
            }
    }
}

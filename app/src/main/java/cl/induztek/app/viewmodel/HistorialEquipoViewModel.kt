package cl.induztek.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.induztek.app.data.InduztekDbHelper
import cl.induztek.app.model.ComparacionValor
import cl.induztek.app.model.HistorialEquipoUiState
import cl.induztek.app.model.Usuario
import cl.induztek.app.validacion.CriteriosResultado
import cl.induztek.app.validacion.ParametrosPrincipales
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

class HistorialEquipoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = InduztekDbHelper(application)

    private val _uiState = MutableStateFlow(HistorialEquipoUiState())
    val uiState: StateFlow<HistorialEquipoUiState> = _uiState.asStateFlow()

    fun cargar(idEquipo: Long, usuario: Usuario) {
        val equipo = db.buscarEquipo(idEquipo, usuario)
        if (equipo == null) {
            _uiState.value = HistorialEquipoUiState()
            return
        }

        val protocolos = db.listarProtocolosPorEquipo(idEquipo, usuario)

        val comparaciones = if (usuario.esCliente) emptyList() else armarComparaciones(idEquipo)

        _uiState.value = HistorialEquipoUiState(
            equipo = equipo,
            protocolos = protocolos,
            comparaciones = comparaciones,
            hoy = LocalDate.now()
        )
    }

    private fun armarComparaciones(idEquipo: Long): List<ComparacionValor> =
        db.listarSeriesDeEquipo(idEquipo)
            .filter { ParametrosPrincipales.esPrincipal(it.codigoTipoPrueba, it.codigoParametro) }
            .mapNotNull { serie ->
                val puntos = db.listarHistorialDeParametro(
                    idEquipo, serie.codigoTipoPrueba, serie.elemento, serie.codigoParametro
                )
                val actual = puntos.firstOrNull() ?: return@mapNotNull null
                val anterior = puntos.getOrNull(1)
                val variacion = anterior?.let { CriteriosResultado.variacionPorcentual(it.valor, actual.valor) }
                ComparacionValor(
                    serie = serie,
                    actual = actual,
                    anterior = anterior,
                    variacionPct = variacion,
                    significativa = anterior != null && CriteriosResultado.esVariacionSignificativa(anterior.valor, actual.valor)
                )
            }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}

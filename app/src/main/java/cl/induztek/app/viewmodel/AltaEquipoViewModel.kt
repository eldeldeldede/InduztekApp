package cl.induztek.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.induztek.app.model.AltaEquipoUiState
import cl.induztek.app.model.Equipo
import cl.induztek.app.network.ResultadoRed
import cl.induztek.app.repository.Repositorios
import cl.induztek.app.util.FotosPlaca
import cl.induztek.app.validacion.Unidades
import cl.induztek.app.validacion.ValidacionesFormulario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AltaEquipoViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AltaEquipoUiState())
    val uiState: StateFlow<AltaEquipoUiState> = _uiState.asStateFlow()

    init {
        // Los tipos de equipo vienen del servidor (equipos-service).
        viewModelScope.launch {
            when (val r = Repositorios.equipos.tiposEquipo()) {
                is ResultadoRed.Exito -> _uiState.update {
                    it.copy(tipos = r.datos, idTipoEquipo = if (it.idTipoEquipo == 0L) r.datos.firstOrNull()?.idTipoEquipo ?: 0L else it.idTipoEquipo)
                }
                else -> _uiState.update { it.copy(errorGeneral = "No se pudieron cargar los tipos de equipo. Revisa tu conexión.") }
            }
        }
    }

    fun onTagCambia(valor: String) = _uiState.update {
        it.copy(tag = valor.uppercase(), errorTag = null, idEquipoExistente = null, errorGeneral = null)
    }
    fun onTipoSeleccionado(id: Long) = _uiState.update { it.copy(idTipoEquipo = id, errorGeneral = null) }
    fun onMarcaCambia(valor: String) = _uiState.update { it.copy(marca = valor) }
    fun onModeloCambia(valor: String) = _uiState.update { it.copy(modelo = valor) }
    fun onSerieCambia(valor: String) = _uiState.update { it.copy(numeroSerie = valor) }
    fun onAnioCambia(valor: String) = _uiState.update { it.copy(anioFabricacion = valor, errorAnio = null) }
    fun onPotenciaCambia(valor: String) = _uiState.update { it.copy(potenciaKva = valor, errorPotencia = null) }
    fun onTensionPrimariaCambia(valor: String) =
        _uiState.update { it.copy(tensionPrimariaKv = valor, errorTensionPrimaria = null) }
    fun onTensionSecundariaCambia(valor: String) =
        _uiState.update { it.copy(tensionSecundariaKv = valor, errorTensionSecundaria = null) }
    fun onCorrienteCambia(valor: String) = _uiState.update { it.copy(corrienteNominalA = valor, errorCorriente = null) }
    fun onMaxContactosCambia(valor: String) = _uiState.update { it.copy(maxContactosUohm = valor, errorMaxContactos = null) }
    fun onObservacionesCambia(valor: String) = _uiState.update { it.copy(observaciones = valor) }

    fun onFotoTomada(ruta: String) {
        FotosPlaca.borrar(_uiState.value.fotoPlaca)
        _uiState.update { it.copy(fotoPlaca = ruta, errorFoto = null) }
    }
    fun onFotoCancelada(ruta: String) = FotosPlaca.borrar(ruta)
    fun onQuitarFoto() {
        FotosPlaca.borrar(_uiState.value.fotoPlaca)
        _uiState.update { it.copy(fotoPlaca = null, errorFoto = null) }
    }
    fun onErrorFoto(mensaje: String) = _uiState.update { it.copy(errorFoto = mensaje) }

    fun onLimpiar() {
        FotosPlaca.borrar(_uiState.value.fotoPlaca)
        _uiState.update { AltaEquipoUiState(tipos = it.tipos, idTipoEquipo = it.idTipoEquipo) }
    }

    fun onGuardar(idInstalacion: Long, alGuardar: () -> Unit) {
        val e = _uiState.value

        val errorTag = ValidacionesFormulario.tag(e.tag)
        val errorAnio = ValidacionesFormulario.anioFabricacion(e.anioFabricacion)
        val errorPotencia = if (e.mostrarPotencia) ValidacionesFormulario.numeroPositivoOpcional(e.potenciaKva) else null
        val errorTensionP = if (e.mostrarTensionPrimaria) ValidacionesFormulario.numeroPositivoOpcional(e.tensionPrimariaKv) else null
        val errorTensionS = if (e.mostrarTensionSecundaria) ValidacionesFormulario.numeroPositivoOpcional(e.tensionSecundariaKv) else null
        val errorCorriente = if (e.mostrarCorriente) ValidacionesFormulario.numeroPositivoOpcional(e.corrienteNominalA) else null
        val errorMaxContactos = if (e.mostrarMaxContactos) ValidacionesFormulario.numeroPositivoOpcional(e.maxContactosUohm) else null

        val hayErrores = listOf(
            errorTag, errorAnio, errorPotencia, errorTensionP, errorTensionS, errorCorriente, errorMaxContactos
        ).any { it != null }

        if (hayErrores) {
            _uiState.update {
                it.copy(
                    errorTag = errorTag, errorAnio = errorAnio, errorPotencia = errorPotencia,
                    errorTensionPrimaria = errorTensionP, errorTensionSecundaria = errorTensionS,
                    errorCorriente = errorCorriente, errorMaxContactos = errorMaxContactos
                )
            }
            return
        }

        val equipo = Equipo(
            idInstalacion = idInstalacion,
            tag = e.tag,
            idTipoEquipo = e.idTipoEquipo,
            marca = e.marca.trim().ifEmpty { null },
            modelo = e.modelo.trim().ifEmpty { null },
            numeroSerie = e.numeroSerie.trim().ifEmpty { null },
            anioFabricacion = e.anioFabricacion.trim().toIntOrNull(),
            potenciaKva = if (e.mostrarPotencia) Unidades.parsearDecimal(e.potenciaKva) else null,
            tensionPrimariaKv = if (e.mostrarTensionPrimaria) Unidades.parsearDecimal(e.tensionPrimariaKv) else null,
            tensionSecundariaKv = if (e.mostrarTensionSecundaria) Unidades.parsearDecimal(e.tensionSecundariaKv) else null,
            corrienteNominalA = if (e.mostrarCorriente) Unidades.parsearDecimal(e.corrienteNominalA) else null,
            maxContactosUohm = if (e.mostrarMaxContactos) Unidades.parsearDecimal(e.maxContactosUohm) else null,
            observaciones = e.observaciones.trim().ifEmpty { null },
            fotoPlaca = e.fotoPlaca
        )

        _uiState.update { it.copy(guardando = true, errorGeneral = null) }
        viewModelScope.launch {
            when (val r = Repositorios.equipos.crearEquipo(equipo)) {
                is ResultadoRed.Exito -> {
                    _uiState.update { it.copy(guardando = false, guardadoExitoso = true, errorGeneral = null) }
                    alGuardar()
                }
                is ResultadoRed.ErrorServidor ->
                    if (r.codigo == 409) {
                        // TAG repetido en la instalación: se busca el equipo existente para poder abrirlo.
                        val existente = Repositorios.equipos.idEquipoPorTag(idInstalacion, equipo.tag)
                        _uiState.update {
                            it.copy(
                                guardando = false,
                                errorTag = "Ya existe un equipo con este TAG en la instalación",
                                idEquipoExistente = existente
                            )
                        }
                    } else {
                        _uiState.update { it.copy(guardando = false, errorGeneral = r.mensaje) }
                    }
                ResultadoRed.SinConexion -> _uiState.update {
                    it.copy(guardando = false, errorGeneral = "Sin conexión con el servidor. Revisa tu red e intenta de nuevo.")
                }
            }
        }
    }
}

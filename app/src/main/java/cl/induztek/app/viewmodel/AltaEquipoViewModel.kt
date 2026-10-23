package cl.induztek.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.induztek.app.data.InduztekDbHelper
import cl.induztek.app.model.AltaEquipoUiState
import cl.induztek.app.model.Equipo
import cl.induztek.app.model.ResultadoGuardado
import cl.induztek.app.validacion.Unidades
import cl.induztek.app.validacion.ValidacionesFormulario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// =====================================================================
//  AltaEquipoViewModel.kt — HU-01: registrar un equipo nuevo en una instalación.
//  Estructura idéntica a FormularioViewModel: un onXxxCambia por campo + onGuardar().
//  RN-01 (TAG único en la instalación) la hace cumplir la base de datos; aquí solo se
//  convierte su respuesta (ResultadoGuardado.Duplicado) en un mensaje para el usuario.
// =====================================================================
class AltaEquipoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = InduztekDbHelper(application)

    // Los tipos de equipo no cambian mientras la app corre: se leen una sola vez (como `roles` en MiRegistro)
    private val _uiState = MutableStateFlow(AltaEquipoUiState(tipos = db.listarTiposEquipo()))
    val uiState: StateFlow<AltaEquipoUiState> = _uiState.asStateFlow()

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

    fun onLimpiar() {
        _uiState.update { AltaEquipoUiState(tipos = it.tipos, idTipoEquipo = it.idTipoEquipo) }
    }

    // Valida todo y, si está bien, guarda. Devuelve true cuando el equipo quedó guardado.
    fun onGuardar(idInstalacion: Long): Boolean {
        val e = _uiState.value

        // Los errores solo se calculan para los campos que están visibles
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
            return false
        }

        // Los campos ocultos NO se guardan aunque tengan texto (si cambió de tipo de equipo)
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
            observaciones = e.observaciones.trim().ifEmpty { null }
        )

        return when (val resultado = db.insertarEquipo(equipo)) {
            is ResultadoGuardado.Creado -> {
                _uiState.update { it.copy(guardadoExitoso = true, errorGeneral = null) }
                true
            }
            is ResultadoGuardado.Duplicado -> {
                // HU-01: se avisa que el equipo ya existe y se ofrece usar el registrado
                _uiState.update {
                    it.copy(
                        errorTag = "Ya existe un equipo con este TAG en la instalación",
                        idEquipoExistente = resultado.idExistente
                    )
                }
                false
            }
            ResultadoGuardado.Fallo -> {
                _uiState.update { it.copy(errorGeneral = "No se pudo guardar el equipo. Intenta de nuevo.") }
                false
            }
        }
    }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}

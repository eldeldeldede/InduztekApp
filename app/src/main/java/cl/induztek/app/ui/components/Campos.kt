package cl.induztek.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import cl.induztek.app.R
import cl.induztek.app.ui.styles.EstilosCampo
import cl.induztek.app.ui.theme.Dimens

@Composable
fun CampoTexto(
    valor: String,
    onValorCambia: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    icono: Int? = null,
    error: String? = null,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    sufijo: String? = null,
    soloLectura: Boolean = false,
    lineasMaximas: Int = 1
) {
    var ultimoError by remember { mutableStateOf("") }
    if (error != null) ultimoError = error
    OutlinedTextField(
        value = valor,
        onValueChange = onValorCambia,
        label = { Text(etiqueta) },
        leadingIcon = icono?.let { id ->
            { Icon(painter = painterResource(id), contentDescription = null) }
        },
        suffix = sufijo?.let { texto -> { Text(texto) } },
        isError = error != null,
        supportingText = {
            AnimatedVisibility(
                visible = error != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) { Text(ultimoError) }
        },
        readOnly = soloLectura,
        singleLine = lineasMaximas == 1,
        maxLines = lineasMaximas,
        keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),
        shape = EstilosCampo.forma,
        colors = EstilosCampo.colores(),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun CampoContrasena(
    valor: String,
    onValorCambia: (String) -> Unit,
    etiqueta: String,
    visible: Boolean,
    onAlternarVisible: () -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null
) {
    var ultimoError by remember { mutableStateOf("") }
    if (error != null) ultimoError = error
    OutlinedTextField(
        value = valor,
        onValueChange = onValorCambia,
        label = { Text(etiqueta) },
        leadingIcon = { Icon(painter = painterResource(R.drawable.ic_candado), contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = onAlternarVisible) {
                Icon(
                    painter = painterResource(if (visible) R.drawable.ic_no_visible else R.drawable.ic_visible),
                    contentDescription = stringResource(
                        if (visible) R.string.cd_ocultar_contrasena else R.string.cd_mostrar_contrasena
                    )
                )
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        isError = error != null,
        supportingText = {
            AnimatedVisibility(
                visible = error != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) { Text(ultimoError) }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        shape = EstilosCampo.forma,
        colors = EstilosCampo.colores(),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun MensajeError(mensaje: String, modifier: Modifier = Modifier) {
    val estadoVisible = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(
        visibleState = estadoVisible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Column(modifier = modifier.fillMaxWidth()) {
            Text(
                text = mensaje,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = Dimens.espacioChico)
            )
        }
    }
}

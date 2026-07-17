package com.tvrgs.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.tvrgs.app.device.rememberIsTelevisionDevice
import com.tvrgs.app.ui.interaction.mouseClickable
import com.tvrgs.app.ui.theme.FocusBorder
import com.tvrgs.app.ui.theme.OnSurface
import com.tvrgs.app.ui.theme.OnSurfaceDim
import com.tvrgs.app.ui.theme.Primary
import com.tvrgs.app.ui.theme.SurfaceElevated
import com.tvrgs.app.ui.theme.SurfaceHighlight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A TV-optimized single-line text field for simple forms like the quick-login screen.
 * The shell is focusable for D-pad navigation and only enters edit mode on click/select,
 * matching the interaction pattern used by [SearchInput].
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() },
    isPassword: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    enabled: Boolean = true
) {
    val isTelevisionDevice = rememberIsTelevisionDevice()
    var hasContainerFocus by remember { mutableStateOf(false) }
    var hasInputFocus by remember { mutableStateOf(false) }
    var acceptsInput by remember(isTelevisionDevice) { mutableStateOf(!isTelevisionDevice) }
    var pendingInputActivation by remember { mutableStateOf(false) }
    var isPasswordVisible by rememberSaveable(isPassword) { mutableStateOf(false) }
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length)))
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    val inputFocusRequester = remember { FocusRequester() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val isFocused = enabled && (hasContainerFocus || hasInputFocus)

    fun requestBringIntoView(delayMillis: Long = 0L) {
        coroutineScope.launch {
            if (delayMillis > 0) delay(delayMillis)
            runCatching { bringIntoViewRequester.bringIntoView() }
        }
    }

    fun activateInput() {
        if (!enabled) return
        if (!isTelevisionDevice) {
            acceptsInput = true
            inputFocusRequester.requestFocus()
            keyboardController?.show()
            requestBringIntoView()
            requestBringIntoView(180)
            return
        }
        acceptsInput = true
        pendingInputActivation = true
        requestBringIntoView()
    }

    LaunchedEffect(value) {
        if (value != textFieldValue.text) {
            val start = textFieldValue.selection.start.coerceIn(0, value.length)
            val end = textFieldValue.selection.end.coerceIn(0, value.length)
            textFieldValue = textFieldValue.copy(text = value, selection = TextRange(start, end))
        }
    }

    LaunchedEffect(acceptsInput, pendingInputActivation) {
        if (!isTelevisionDevice || !acceptsInput || !pendingInputActivation) return@LaunchedEffect
        inputFocusRequester.requestFocus()
        keyboardController?.show()
        requestBringIntoView(120)
        pendingInputActivation = false
    }

    val borderColor = if (isFocused) FocusBorder else OnSurfaceDim.copy(alpha = 0.5f)
    val backgroundColor = if (isFocused) SurfaceHighlight else SurfaceElevated
    val borderWidth = if (isFocused) 2.dp else 1.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .focusRequester(focusRequester)
            .bringIntoViewRequester(bringIntoViewRequester)
            .background(backgroundColor, RoundedCornerShape(10.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(10.dp))
            .semantics(mergeDescendants = true) {
                contentDescription = placeholder
                stateDescription = if (isPassword) "" else value.ifBlank { placeholder }
            }
            .onFocusChanged { hasContainerFocus = enabled && it.isFocused }
            .focusProperties { canFocus = enabled }
            .mouseClickable(enabled = enabled, focusRequester = focusRequester) { activateInput() }
            .clickable(enabled = enabled) { activateInput() }
            .focusable(enabled = enabled)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty() && !isFocused) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = OnSurfaceDim
                    )
                }

                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { updated ->
                        textFieldValue = updated
                        if (updated.text != value) onValueChange(updated.text)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(inputFocusRequester)
                        .focusProperties {
                            canFocus = enabled && (!isTelevisionDevice || acceptsInput)
                            if (isTelevisionDevice && acceptsInput) {
                                left = FocusRequester.Cancel
                                right = FocusRequester.Cancel
                            }
                        }
                        .onFocusChanged {
                            hasInputFocus = enabled && it.hasFocus
                            if (it.hasFocus) {
                                requestBringIntoView(120)
                            } else {
                                if (isTelevisionDevice) acceptsInput = false
                                keyboardController?.hide()
                            }
                        },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = OnSurface),
                    singleLine = true,
                    cursorBrush = SolidColor(Primary),
                    enabled = enabled,
                    readOnly = isTelevisionDevice && !acceptsInput,
                    visualTransformation = if (isPassword && !isPasswordVisible) {
                        PasswordVisualTransformation()
                    } else {
                        VisualTransformation.None
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = imeAction,
                        keyboardType = if (isPassword) {
                            androidx.compose.ui.text.input.KeyboardType.Password
                        } else {
                            androidx.compose.ui.text.input.KeyboardType.Text
                        }
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { onImeAction(); acceptsInput = false; keyboardController?.hide() },
                        onDone = { onImeAction(); acceptsInput = false; keyboardController?.hide() },
                        onGo = { onImeAction(); acceptsInput = false; keyboardController?.hide() }
                    )
                )
            }

            if (isPassword && value.isNotEmpty()) {
                Icon(
                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null,
                    tint = if (isFocused) Primary else OnSurfaceDim,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clickable(enabled = enabled) { isPasswordVisible = !isPasswordVisible }
                )
            }
        }
    }
}

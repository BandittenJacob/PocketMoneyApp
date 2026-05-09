package com.jmp.pocketmoneyapp.ui.common

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.text.input.VisualTransformation

/**
 * TextField with autofill hints for Google Password Manager.
 * This enables "Use Strong Password" feature during signup.
 * 
 * Credential Manager (backend) handles save/retrieve.
 * AutofillNode (UI) triggers "Use Strong Password" button.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AutofillTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit,
    autofillTypes: List<AutofillType>,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    onAutofilled: (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false
) {
    val autofillNode = AutofillNode(
        autofillTypes = autofillTypes,
        onFill = {
            onValueChange(it)
            onAutofilled?.invoke()
        }
    )
    val autofill = LocalAutofill.current
    
    LocalAutofillTree.current += autofillNode
    
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        trailingIcon = trailingIcon,
        supportingText = supportingText,
        isError = isError,
        singleLine = true,
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                autofillNode.boundingBox = coordinates.boundsInWindow()
            }
            .onFocusChanged { focusState ->
                autofill?.run {
                    if (focusState.isFocused) {
                        requestAutofillForNode(autofillNode)
                    } else {
                        cancelAutofillForNode(autofillNode)
                    }
                }
            }
    )
}

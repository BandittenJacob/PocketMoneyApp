package com.jmp.pocketmoneyapp.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jmp.pocketmoneyapp.R

@Composable
fun PinEntryScreen(
    onPinEntered: (String) -> Unit,
    onUseBiometric: () -> Unit,
    showBiometricOption: Boolean = true,
    errorMessage: String? = null
) {
    var pin by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.enter_pin_title),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.enter_pin_description),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // PIN input
        OutlinedTextField(
            value = pin,
            onValueChange = {
                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                    pin = it
                }
            },
            label = { Text(stringResource(R.string.enter_pin_label)) },
            placeholder = { Text("••••") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            isError = errorMessage != null
        )
        
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Unlock button
        Button(
            onClick = {
                if (pin.length >= 4) {
                    onPinEntered(pin)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = pin.length >= 4
        ) {
            Text(stringResource(R.string.enter_pin_button))
        }
        
        if (showBiometricOption) {
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = onUseBiometric,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.enter_pin_use_biometric))
            }
        }
    }
}

package com.jmp.pocketmoneyapp.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.jmp.pocketmoneyapp.ui.components.AppTopBar
import com.jmp.pocketmoneyapp.utils.PinUtils
import com.jmp.pocketmoneyapp.viewmodel.AuthState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupPinScreen(
    onPinSet: (String) -> Unit,
    onSkip: () -> Unit = {},
    authState: AuthState? = null,
    onBack: (() -> Unit)? = null
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Update error message from auth state
    LaunchedEffect(authState?.error) {
        if (authState?.error != null) {
            errorMessage = authState.error
        }
    }
    
    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            if (onBack != null) {
                AppTopBar(
                    title = { Text(stringResource(R.string.setup_pin_title)) },
                    onNavigateBack = onBack
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
            
            // Only show title if there's no top bar (onBack is null)
            if (onBack == null) {
                Text(
                    text = stringResource(R.string.setup_pin_title),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Text(
                text = stringResource(R.string.setup_pin_description),
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
                    errorMessage = null
                }
            },
            label = { Text(stringResource(R.string.setup_pin_enter)) },
            placeholder = { Text(stringResource(R.string.setup_pin_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            isError = errorMessage != null
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Confirm PIN input
        OutlinedTextField(
            value = confirmPin,
            onValueChange = {
                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                    confirmPin = it
                    errorMessage = null
                }
            },
            label = { Text(stringResource(R.string.setup_pin_confirm)) },
            placeholder = { Text(stringResource(R.string.setup_pin_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            isError = errorMessage != null
        )
        
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.setup_pin_hint),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Set PIN button
        Button(
            onClick = {
                when {
                    !PinUtils.isValidPin(pin) -> {
                        errorMessage = "PIN must be 4-6 digits"
                    }
                    pin != confirmPin -> {
                        errorMessage = "PINs do not match"
                    }
                    else -> {
                        onPinSet(pin)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = pin.isNotEmpty() && confirmPin.isNotEmpty() && authState?.isLoading != true
        ) {
            if (authState?.isLoading == true) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(stringResource(R.string.setup_pin_button))
            }
        }
        }
    }
}

package com.jmp.pocketmoneyapp.ui.auth

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jmp.pocketmoneyapp.R
import com.jmp.pocketmoneyapp.ui.components.AppTopBar
import com.jmp.pocketmoneyapp.ui.common.AutofillTextField
import com.jmp.pocketmoneyapp.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    onSignUpSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    invitationCode: String? = null,
    memberName: String? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var name by remember { mutableStateOf(memberName ?: "") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showPinSetup by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var createdFirebaseUserId by remember { mutableStateOf<String?>(null) }
    var savedPassword by remember { mutableStateOf("") } // Store password for later
    val nameFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }
    
    val authState by viewModel.authState.collectAsState()
    
    // Clear error when screen is first opened
    LaunchedEffect(Unit) {
        viewModel.clearError()
    }
    
    // Auto-fill confirm password when password is generated/autofilled
    LaunchedEffect(password) {
        // If password is long (likely auto-generated), auto-fill confirm field
        if (password.length >= 12 && confirmPassword.isEmpty()) {
            confirmPassword = password
        }
    }
    
    LaunchedEffect(authState.isAuthenticated, authState.needsFamilySetup) {
        if (authState.isAuthenticated && authState.needsFamilySetup) {
            onSignUpSuccess()
        }
    }
    
    val passwordsMatch = password == confirmPassword
    val canSignUp = name.isNotBlank() && 
                    email.isNotBlank() && 
                    password.length >= 6 && 
                    passwordsMatch &&
                    emailError == null
    
    // If there's an auth error related to email, go back to form
    LaunchedEffect(authState.error) {
        if (authState.error != null && showPinSetup) {
            val errorLower = authState.error!!.lowercase()
            if (errorLower.contains("email") || errorLower.contains("already") || 
                errorLower.contains("in use") || errorLower.contains("exists")) {
                showPinSetup = false
            }
        }
    }
    
    if (showPinSetup) {
        // Show PIN setup screen
        SetupPinScreen(
            authState = authState,
            onBack = { 
                viewModel.clearError()
                showPinSetup = false
                // Delete the created Firebase Auth account if going back
                if (createdFirebaseUserId != null) {
                    viewModel.deleteAuthAccount()
                    createdFirebaseUserId = null
                }
            },
            onPinSet = { pin ->
                if (invitationCode != null && createdFirebaseUserId != null) {
                    // Sign up with invitation - create Firestore document
                    viewModel.completeSignUpWithInvitation(
                        createdFirebaseUserId!!,
                        email.trim(), 
                        name.trim(), 
                        pin, 
                        invitationCode,
                        savedPassword, 
                        context
                    ) { success, error ->
                        if (success) {
                            onSignUpSuccess()
                        }
                    }
                } else if (createdFirebaseUserId != null) {
                    // Regular sign up - create Firestore document
                    viewModel.completeSignUp(createdFirebaseUserId!!, email.trim(), name.trim(), pin, savedPassword, context)
                }
            }
        )
    } else {
        // Show signup form
        Scaffold(
            modifier = Modifier.imePadding(),
            topBar = {
                AppTopBar(
                    title = { Text(stringResource(R.string.sign_up_title)) },
                    onNavigateBack = onNavigateBack
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = stringResource(R.string.sign_up_create_account),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Start by creating your account",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Email field (username for password manager) - FIRST
            AutofillTextField(
                value = email,
                onValueChange = { 
                    email = it
                    emailError = null // Clear error when user modifies email
                },
                label = { Text(stringResource(R.string.email)) },
                autofillTypes = listOf(AutofillType.Username, AutofillType.EmailAddress),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    autoCorrectEnabled = false
                ),
                keyboardActions = KeyboardActions(
                    onNext = { nameFocusRequester.requestFocus() }
                ),
                modifier = Modifier.fillMaxWidth(),
                isError = emailError != null,
                supportingText = if (emailError != null) {
                    { Text(emailError!!, color = MaterialTheme.colorScheme.error) }
                } else null
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.name)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { passwordFocusRequester.requestFocus() }
                ),
                modifier = Modifier.fillMaxWidth().focusRequester(nameFocusRequester),
                singleLine = true,
                enabled = memberName == null, // Disable if joining via invitation
                colors = if (memberName != null) {
                    OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    OutlinedTextFieldDefaults.colors()
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Password field (with "Use Strong Password" support)
            AutofillTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.password)) },
                autofillTypes = listOf(AutofillType.NewPassword),
                visualTransformation = if (passwordVisible) 
                    VisualTransformation.None 
                else 
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next,
                    autoCorrectEnabled = false
                ),
                keyboardActions = KeyboardActions(
                    onNext = { confirmPasswordFocusRequester.requestFocus() }
                ),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            "Toggle password visibility"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().focusRequester(passwordFocusRequester),
                supportingText = {
                    Text(stringResource(R.string.sign_up_password_hint), fontSize = 12.sp)
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Confirm password field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text(stringResource(R.string.confirm_password)) },
                visualTransformation = if (passwordVisible) 
                    VisualTransformation.None 
                else 
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    autoCorrectEnabled = false
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (canSignUp && !authState.isLoading) {
                            emailError = null
                            coroutineScope.launch {
                                viewModel.createAuthAccount(email.trim(), password) { success, userId, error ->
                                    if (success && userId != null) {
                                        createdFirebaseUserId = userId
                                        savedPassword = password
                                        showPinSetup = true
                                    } else {
                                        emailError = if (error != null && error.lowercase().let {
                                            it.contains("email") || it.contains("already") ||
                                            it.contains("in use") || it.contains("exists")
                                        }) "This email is already in use. Please use a different email or sign in."
                                        else error ?: "Failed to create account"
                                    }
                                }
                            }
                        }
                    }
                ),
                modifier = Modifier.fillMaxWidth().focusRequester(confirmPasswordFocusRequester),
                singleLine = true,
                isError = confirmPassword.isNotEmpty() && !passwordsMatch,
                supportingText = {
                    if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                        Text(stringResource(R.string.sign_up_password_mismatch), color = MaterialTheme.colorScheme.error)
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Sign up button
            Button(
                onClick = {
                    if (canSignUp && !authState.isLoading) {
                        emailError = null
                        coroutineScope.launch {
                            // Try to create Firebase Auth account first
                            viewModel.createAuthAccount(email.trim(), password) { success, userId, error ->
                                if (success && userId != null) {
                                    // Success - save userId and password, proceed to PIN
                                    createdFirebaseUserId = userId
                                    savedPassword = password
                                    showPinSetup = true
                                } else {
                                    // Failed - show error on form
                                    if (error != null && error.lowercase().let { 
                                        it.contains("email") || it.contains("already") || 
                                        it.contains("in use") || it.contains("exists")
                                    }) {
                                        emailError = "This email is already in use. Please use a different email or sign in."
                                    } else {
                                        emailError = error ?: "Failed to create account"
                                    }
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !authState.isLoading && canSignUp
            ) {
                if (authState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.create_account), fontSize = 15.sp)
                }
            }
            
            // Error message (for auth errors, not email validation)
            if (authState.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = authState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
    }
}

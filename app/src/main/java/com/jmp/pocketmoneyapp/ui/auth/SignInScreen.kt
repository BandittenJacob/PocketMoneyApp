package com.jmp.pocketmoneyapp.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jmp.pocketmoneyapp.R
import com.jmp.pocketmoneyapp.ui.components.AppTopBar
import com.jmp.pocketmoneyapp.ui.common.AutofillTextField
import com.jmp.pocketmoneyapp.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class SignInContext {
    NORMAL,           // Normal sign-in from welcome screen
    CREATE_FAMILY,    // Sign in to create a new family
    JOIN_INVITATION   // Sign in to accept an invitation
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SignInScreen(
    viewModel: AuthViewModel,
    onSignInSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    onForgotPasswordClick: () -> Unit = {},
    signInContext: SignInContext = SignInContext.NORMAL,
    invitationCode: String? = null,
    familyName: String? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var hadOldFamily by remember { mutableStateOf(false) }
    var autofillSubmitPending by remember { mutableStateOf(false) }

    // Track if credentials came from password manager and haven't been modified
    var credentialsAutoLoaded by remember { mutableStateOf(false) }
    var credentialsModified by remember { mutableStateOf(false) }
    val passwordFocusRequester = remember { FocusRequester() }
    
    val authState by viewModel.authState.collectAsState()

    // Auto-submit when password manager fills both fields
    LaunchedEffect(autofillSubmitPending) {
        if (autofillSubmitPending) {
            delay(200)
            if (email.isNotBlank() && password.isNotBlank() && !authState.isLoading) {
                when (signInContext) {
                    SignInContext.NORMAL, SignInContext.CREATE_FAMILY ->
                        viewModel.signIn(email.trim(), password, context, true)
                    SignInContext.JOIN_INVITATION -> {
                        if (invitationCode != null) {
                            viewModel.signInWithInvitation(email.trim(), password, invitationCode, context) { success, _, wasInOldFamily ->
                                if (success) { hadOldFamily = wasInOldFamily; showSuccessDialog = true }
                            }
                        }
                    }
                }
            }
            autofillSubmitPending = false
        }
    }

    // Clear error and try to load saved credentials when screen appears
    LaunchedEffect(Unit) {
        viewModel.clearError()
        scope.launch {
            val credentials = viewModel.getSavedCredentials(context)
            credentials?.let { (savedEmail, savedPassword) ->
                email = savedEmail
                password = savedPassword
                credentialsAutoLoaded = true
            }
        }
    }
    
    // Handle successful authentication based on context
    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            when (signInContext) {
                SignInContext.NORMAL -> onSignInSuccess()
                SignInContext.CREATE_FAMILY -> {
                    // Check if user had an old family
                    hadOldFamily = authState.user?.familyId != null
                    showSuccessDialog = true
                }
                SignInContext.JOIN_INVITATION -> {
                    // Already handled in button click, just show success
                }
            }
        }
    }
    
    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            AppTopBar(
                title = { Text(stringResource(R.string.sign_in_title)) },
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.sign_in_welcome),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Context-specific information
            when (signInContext) {
                SignInContext.CREATE_FAMILY -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(text = "ℹ️", fontSize = 12.sp)
                            Text(
                                text = "Sign in with your existing account to create a new family. If you're already in a family, you'll be disconnected from it.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
                SignInContext.JOIN_INVITATION -> {
                    if (familyName != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "You're joining:",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = familyName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
                SignInContext.NORMAL -> {}
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Email field (with autofill support)
            AutofillTextField(
                value = email,
                onValueChange = { 
                    email = it
                    credentialsModified = true
                },
                label = { Text(stringResource(R.string.email)) },
                autofillTypes = listOf(AutofillType.Username, AutofillType.EmailAddress),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    autoCorrectEnabled = false
                ),
                keyboardActions = KeyboardActions(
                    onNext = { passwordFocusRequester.requestFocus() }
                ),
                onAutofilled = { autofillSubmitPending = true },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Password field (with autofill support)
            AutofillTextField(
                value = password,
                onValueChange = { 
                    password = it
                    credentialsModified = true
                },
                label = { Text(stringResource(R.string.password)) },
                autofillTypes = listOf(AutofillType.Password),
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
                        if (email.isNotBlank() && password.isNotBlank() && !authState.isLoading) {
                            val fromPasswordManager = credentialsAutoLoaded && !credentialsModified
                            when (signInContext) {
                                SignInContext.NORMAL, SignInContext.CREATE_FAMILY ->
                                    viewModel.signIn(email.trim(), password, context, fromPasswordManager)
                                SignInContext.JOIN_INVITATION -> {
                                    if (invitationCode != null) {
                                        viewModel.signInWithInvitation(email.trim(), password, invitationCode, context) { success, _, wasInOldFamily ->
                                            if (success) { hadOldFamily = wasInOldFamily; showSuccessDialog = true }
                                        }
                                    }
                                }
                            }
                        }
                    }
                ),
                onAutofilled = { autofillSubmitPending = true },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            "Toggle password visibility"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().focusRequester(passwordFocusRequester)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Sign in button
            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        when (signInContext) {
                            SignInContext.NORMAL, SignInContext.CREATE_FAMILY -> {
                                // Normal sign-in
                                val fromPasswordManager = credentialsAutoLoaded && !credentialsModified
                                viewModel.signIn(email.trim(), password, context, fromPasswordManager)
                            }
                            SignInContext.JOIN_INVITATION -> {
                                // Sign in with invitation
                                if (invitationCode != null) {
                                    viewModel.signInWithInvitation(
                                        email.trim(),
                                        password,
                                        invitationCode,
                                        context
                                    ) { success, error, wasInOldFamily ->
                                        if (success) {
                                            hadOldFamily = wasInOldFamily
                                            showSuccessDialog = true
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !authState.isLoading && email.isNotBlank() && password.isNotBlank()
            ) {
                if (authState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.sign_in_button), fontSize = 15.sp)
                }
            }
            
            // Forgot password link
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = onForgotPasswordClick,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Forgot Password?",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Error message
            if (authState.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = authState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }
        }
    }
    
    // Success dialog for CREATE_FAMILY and JOIN_INVITATION contexts
    if (showSuccessDialog) {
        val (title, message) = when (signInContext) {
            SignInContext.CREATE_FAMILY -> {
                val msg = if (hadOldFamily) {
                    "You will be disconnected from your previous family when you create the new family."
                } else {
                    "You can now proceed to create your family."
                }
                "Signed In Successfully" to msg
            }
            SignInContext.JOIN_INVITATION -> {
                val msg = if (hadOldFamily) {
                    "You have been disconnected from your previous family and connected to ${familyName ?: "the new family"}."
                } else {
                    "You have successfully joined ${familyName ?: "the family"}!"
                }
                "Welcome!" to msg
            }
            else -> "" to ""
        }
        
        AlertDialog(
            onDismissRequest = { },
            title = { Text(title, textAlign = TextAlign.Center) },
            text = { Text(message, textAlign = TextAlign.Center) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        onSignInSuccess()
                    }
                ) {
                    Text(stringResource(R.string.continue_button))
                }
            }
        )
    }
}

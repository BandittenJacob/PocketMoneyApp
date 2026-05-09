package com.jmp.pocketmoneyapp.ui.settings

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.jmp.pocketmoneyapp.R
import com.jmp.pocketmoneyapp.ui.components.AppTopBar
import com.jmp.pocketmoneyapp.data.model.UserRole
import com.jmp.pocketmoneyapp.data.repository.BiometricAuthManager
import com.jmp.pocketmoneyapp.data.repository.LocaleManager
import com.jmp.pocketmoneyapp.data.repository.PreferencesManager
import com.jmp.pocketmoneyapp.ui.auth.SetupPinScreen
import com.jmp.pocketmoneyapp.ui.family.FamilyDeletionApprovalScreen
import com.jmp.pocketmoneyapp.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val authViewModel: AuthViewModel = viewModel()
    
    val biometricManager = remember { BiometricAuthManager(context) }
    val preferencesManager = remember { PreferencesManager(context) }
    val localeManager = remember { LocaleManager(context) }
    
    val authState by authViewModel.authState.collectAsState()
    val isParent = authState.user?.role == com.jmp.pocketmoneyapp.data.model.UserRole.PARENT
    
    val hasPendingDeletion = authState.family?.deletionRequest?.active == true
    
    // Show deletion approval screen INSTEAD of normal settings for parents
    if (isParent && hasPendingDeletion && authState.family != null && authState.user != null) {
        FamilyDeletionApprovalScreen(
            deletionRequest = authState.family!!.deletionRequest!!,
            familyName = authState.family!!.name,
            currentUserId = authState.user!!.id,
            viewModel = authViewModel,
            onDismiss = {
                // After dismissal (cancel or complete), navigate back
                if (!authState.isAuthenticated) {
                    // User was signed out, let NavGraph handle redirect
                } else {
                    onNavigateBack()
                }
            }
        )
        return  // Don't show normal settings content
    }
    
    var biometricEnabled by remember { mutableStateOf(preferencesManager.isBiometricEnabled) }
    var showBiometricInfo by remember { mutableStateOf(false) }
    var biometricInfoMessage by remember { mutableStateOf("") }
    var showChangePinDialog by remember { mutableStateOf(false) }
    var showPinUpdatedMessage by remember { mutableStateOf(false) }
    var showResetToursDialog by remember { mutableStateOf(false) }
    var toursResetDone by remember { mutableStateOf(false) }

    var selectedLanguage by remember { mutableStateOf(localeManager.selectedLanguage) }
    var showLanguageMenu by remember { mutableStateOf(false) }
    
    val biometricAvailability = remember { biometricManager.isBiometricAvailable() }
    
    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            AppTopBar(
                title = { Text(stringResource(R.string.settings_title)) },
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // General Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {

                    // Language
                    ExposedDropdownMenuBox(
                        expanded = showLanguageMenu,
                        onExpandedChange = { showLanguageMenu = it }
                    ) {
                        OutlinedTextField(
                            value = if (selectedLanguage == LocaleManager.LANGUAGE_DANISH)
                                stringResource(R.string.language_danish)
                            else
                                stringResource(R.string.language_english),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.settings_language)) },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Dropdown") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = showLanguageMenu,
                            onDismissRequest = { showLanguageMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.language_danish)) },
                                onClick = {
                                    selectedLanguage = LocaleManager.LANGUAGE_DANISH
                                    localeManager.setLocale(LocaleManager.LANGUAGE_DANISH)
                                    showLanguageMenu = false
                                    (context as? Activity)?.recreate()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.language_english)) },
                                onClick = {
                                    selectedLanguage = LocaleManager.LANGUAGE_ENGLISH
                                    localeManager.setLocale(LocaleManager.LANGUAGE_ENGLISH)
                                    showLanguageMenu = false
                                    (context as? Activity)?.recreate()
                                }
                            )
                        }
                    }

                    // Family Name + Proposals (Parents only)
                    if (isParent) {
                        var familyNameInput by remember { mutableStateOf(authState.family?.name ?: "") }
                        var familyNameSaved by remember { mutableStateOf(false) }
                        LaunchedEffect(authState.family?.name) {
                            familyNameInput = authState.family?.name ?: ""
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        OutlinedTextField(
                            value = familyNameInput,
                            onValueChange = {
                                familyNameInput = it
                                familyNameSaved = false
                            },
                            label = { Text(stringResource(R.string.settings_family_name_label)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (familyNameInput.isNotBlank()) {
                                    authViewModel.updateFamilyName(familyNameInput) { success ->
                                        if (success) familyNameSaved = true
                                    }
                                }
                            },
                            enabled = familyNameInput.isNotBlank() && familyNameInput.trim() != authState.family?.name,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.settings_family_name_save))
                        }
                        if (familyNameSaved) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.settings_family_name_saved),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        val proposalRepository = remember { com.jmp.pocketmoneyapp.data.repository.ChoreProposalRepository() }
                        var proposalsEnabled by remember { mutableStateOf(authState.family?.choreProposalsEnabled ?: false) }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.settings_proposals_toggle),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = stringResource(R.string.settings_proposals_description),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = proposalsEnabled,
                                onCheckedChange = { enabled ->
                                    proposalsEnabled = enabled
                                    authState.family?.id?.let { familyId ->
                                        kotlinx.coroutines.MainScope().launch {
                                            proposalRepository.setProposalsEnabled(familyId, enabled)
                                        }
                                    }
                                }
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        // Due date indicators toggle
                        var dueDateIndicatorsEnabled by remember { mutableStateOf(authState.family?.dueDateIndicatorsEnabled ?: false) }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.settings_due_date_toggle),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = stringResource(R.string.settings_due_date_description),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = dueDateIndicatorsEnabled,
                                onCheckedChange = { enabled ->
                                    dueDateIndicatorsEnabled = enabled
                                    authState.family?.id?.let { familyId ->
                                        kotlinx.coroutines.MainScope().launch {
                                            proposalRepository.setDueDateIndicatorsEnabled(familyId, enabled)
                                        }
                                    }
                                }
                            )
                        }                    }

                    // Biometric
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.settings_biometric),
                                fontSize = 12.sp
                            )
                            Text(
                                text = stringResource(R.string.settings_biometric_description),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = biometricEnabled,
                            onCheckedChange = { enabled ->
                                if (!biometricAvailability.isAvailable) {
                                    biometricInfoMessage = biometricAvailability.getMessage()
                                    showBiometricInfo = true
                                } else if (enabled && activity != null) {
                                    biometricManager.authenticate(
                                        activity = activity,
                                        title = "Enable Biometric Login",
                                        subtitle = "Authenticate to enable biometric login",
                                        negativeButtonText = "Cancel",
                                        onSuccess = {
                                            biometricEnabled = true
                                            preferencesManager.isBiometricEnabled = true
                                        },
                                        onError = { error ->
                                            biometricInfoMessage = error
                                            showBiometricInfo = true
                                        },
                                        onFailed = {
                                            biometricInfoMessage = "Authentication failed"
                                            showBiometricInfo = true
                                        }
                                    )
                                } else {
                                    biometricEnabled = false
                                    preferencesManager.isBiometricEnabled = false
                                }
                            },
                            enabled = biometricAvailability.isAvailable
                        )
                    }
                    if (!biometricAvailability.isAvailable) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = biometricAvailability.getMessage(),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    // Change PIN
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    OutlinedButton(
                        onClick = { showChangePinDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.settings_change_pin))
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    OutlinedButton(
                        onClick = { showResetToursDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.settings_reset_tours))
                    }
                    if (toursResetDone) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.settings_reset_tours_done),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Danger Zone (Parents only)
            if (isParent) {
                val deletionRequest = authState.family?.deletionRequest
                val hasPendingDeletion = deletionRequest?.active == true
                val currentUserId = authState.user?.id ?: ""
                val hasCurrentUserApproved = deletionRequest?.approvedBy?.contains(currentUserId) == true
                val requiredApprovals = deletionRequest?.requiredApprovals?.size ?: 0
                val receivedApprovals = deletionRequest?.approvedBy?.size ?: 0

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.settings_delete_family),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (hasPendingDeletion) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = stringResource(R.string.settings_deletion_pending),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.settings_deletion_initiated, deletionRequest?.initiatedByName ?: ""),
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = stringResource(R.string.settings_deletion_approvals, receivedApprovals, requiredApprovals),
                                        fontSize = 12.sp
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (!hasCurrentUserApproved) {
                                            Button(
                                                onClick = {
                                                    authViewModel.approveFamilyDeletion { success, allApproved, error ->
                                                        if (success && allApproved) { }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.error
                                                ),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(stringResource(R.string.settings_deletion_approve))
                                            }
                                        } else {
                                            Text(
                                                text = stringResource(R.string.settings_deletion_approved),
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.weight(1f).padding(8.dp)
                                            )
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                authViewModel.cancelFamilyDeletion { success, error -> }
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(stringResource(R.string.add_chore_cancel))
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.settings_deletion_description),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            var showDeleteConfirmation by remember { mutableStateOf(false) }

                            OutlinedButton(
                                onClick = { showDeleteConfirmation = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                            ) {
                                Text(stringResource(R.string.settings_deletion_request))
                            }

                            if (showDeleteConfirmation) {
                                AlertDialog(
                                    onDismissRequest = { showDeleteConfirmation = false },
                                    title = { Text(stringResource(R.string.settings_deletion_dialog_title)) },
                                    text = {
                                        Text(
                                            stringResource(R.string.settings_deletion_dialog_message, authState.family?.name ?: "")
                                        )
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                showDeleteConfirmation = false
                                                authViewModel.requestFamilyDeletion { success, wasDeleted, error ->
                                                    if (success && wasDeleted) { }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.error
                                            )
                                        ) {
                                            Text(stringResource(R.string.settings_deletion_request_button))
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showDeleteConfirmation = false }) {
                                            Text(stringResource(R.string.add_chore_cancel))
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (showBiometricInfo) {
                Spacer(modifier = Modifier.height(16.dp))
                AlertDialog(
                    onDismissRequest = { showBiometricInfo = false },
                    title = { Text(stringResource(R.string.settings_biometric)) },
                    text = { Text(biometricInfoMessage) },
                    confirmButton = {
                        TextButton(onClick = { showBiometricInfo = false }) {
                            Text(stringResource(R.string.ok))
                        }
                    }
                )
            }
            
            // Change PIN Dialog
            if (showChangePinDialog) {
                AlertDialog(
                    onDismissRequest = { showChangePinDialog = false },
                    properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
                    modifier = Modifier.fillMaxWidth(0.95f),
                    title = { Text(stringResource(R.string.settings_change_pin)) },
                    text = {
                        SetupPinScreen(
                            onPinSet = { newPin ->
                                authViewModel.updatePin(newPin) { success ->
                                    if (success) {
                                        showChangePinDialog = false
                                        showPinUpdatedMessage = true
                                    }
                                }
                            }
                        )
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showChangePinDialog = false }) {
                            Text(stringResource(R.string.dismiss))
                        }
                    }
                )
            }
            
            // PIN Updated Snackbar
            if (showPinUpdatedMessage) {
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2000)
                    showPinUpdatedMessage = false
                }
                Snackbar(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(stringResource(R.string.settings_pin_updated))
                }
            }

            // Reset Tours Dialog
            if (showResetToursDialog) {
                var resetChores by remember { mutableStateOf(false) }
                var resetDashboard by remember { mutableStateOf(false) }
                var resetLibrary by remember { mutableStateOf(false) }
                var resetFamily by remember { mutableStateOf(false) }
                val anySelected = resetChores || resetDashboard || resetLibrary || resetFamily

                AlertDialog(
                    onDismissRequest = { showResetToursDialog = false },
                    title = { Text(stringResource(R.string.settings_reset_tours_dialog_title)) },
                    text = {
                        Column {
                            Text(
                                text = stringResource(R.string.settings_reset_tours_dialog_body),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(checked = resetDashboard, onCheckedChange = { resetDashboard = it })
                                Text(stringResource(R.string.settings_reset_tours_screen_dashboard))
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(checked = resetChores, onCheckedChange = { resetChores = it })
                                Text(stringResource(R.string.settings_reset_tours_screen_chores))
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(checked = resetLibrary, onCheckedChange = { resetLibrary = it })
                                Text(stringResource(R.string.settings_reset_tours_screen_library))
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(checked = resetFamily, onCheckedChange = { resetFamily = it })
                                Text(stringResource(R.string.settings_reset_tours_screen_family))
                            }
                        }
                    },
                    confirmButton = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (resetChores) preferencesManager.hasSeenChoresTour = false
                                    if (resetDashboard) preferencesManager.hasSeenDashboardTour = false
                                    if (resetLibrary) preferencesManager.hasSeenLibraryTour = false
                                    if (resetFamily) preferencesManager.hasSeenFamilyMembersTour = false
                                    showResetToursDialog = false
                                    toursResetDone = true
                                },
                                enabled = anySelected,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.settings_reset_tours_reset_selected))
                            }
                            OutlinedButton(
                                onClick = {
                                    preferencesManager.resetAllTours()
                                    showResetToursDialog = false
                                    toursResetDone = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.settings_reset_tours_reset_all))
                            }
                            TextButton(
                                onClick = { showResetToursDialog = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.dismiss))
                            }
                        }
                    },
                    dismissButton = null
                )
            }
        }
    }
}

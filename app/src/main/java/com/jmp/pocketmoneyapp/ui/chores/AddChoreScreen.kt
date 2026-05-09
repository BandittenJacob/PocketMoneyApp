package com.jmp.pocketmoneyapp.ui.chores

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.jmp.pocketmoneyapp.R
import com.jmp.pocketmoneyapp.ui.components.AppTopBar
import com.jmp.pocketmoneyapp.data.model.Chore
import com.jmp.pocketmoneyapp.data.model.ChoreProposal
import com.jmp.pocketmoneyapp.data.model.ChoreStatus
import com.jmp.pocketmoneyapp.viewmodel.AuthViewModel
import com.jmp.pocketmoneyapp.viewmodel.ChoreViewModel
import com.jmp.pocketmoneyapp.viewmodel.FamilyMemberViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChoreScreen(
    authViewModel: AuthViewModel,
    choreViewModel: ChoreViewModel,
    memberViewModel: FamilyMemberViewModel,
    choreToEdit: Chore? = null,
    proposalToAccept: ChoreProposal? = null,
    onChoreCreated: () -> Unit,
    onProposalAccepted: ((proposalId: String) -> Unit)? = null,
    onNavigateBack: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val memberState by memberViewModel.memberState.collectAsState()
    val isEditMode = choreToEdit != null
    
    // Load family members when family is available
    LaunchedEffect(authState.family?.id) {
        authState.family?.id?.let { familyId ->
            memberViewModel.loadFamilyMembers(familyId)
        }
    }
    
    var choreName by remember { mutableStateOf(choreToEdit?.name ?: proposalToAccept?.name ?: "") }
    var choreDescription by remember { mutableStateOf(choreToEdit?.description ?: proposalToAccept?.description ?: "") }
    var choreValue by remember { mutableStateOf(choreToEdit?.value?.toString() ?: proposalToAccept?.suggestedReward?.let { if (it > 0) it.toString() else "" } ?: "") }
    
    // Due date state
    var dueDate by remember { mutableStateOf(choreToEdit?.dueDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Delete confirmation
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val isParent = authState.user?.role == com.jmp.pocketmoneyapp.data.model.UserRole.PARENT
    
    // Assignment state - using real family members (children only)
    val unassignedText = stringResource(R.string.chores_unassigned)
    val assignmentOptions = remember(memberState.members) {
        val children = memberState.members.filter { it.role == com.jmp.pocketmoneyapp.data.model.MemberRole.CHILD }
        listOf(unassignedText) + children.map { "${it.avatarEmoji} ${it.name}" }
    }
    var assignedPerson by remember(unassignedText) { 
        mutableStateOf(choreToEdit?.assignedTo?.ifEmpty { unassignedText } ?: unassignedText) 
    }
    var showAssignmentMenu by remember { mutableStateOf(false) }
    
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val focusManager = LocalFocusManager.current
    val descriptionFocusRequester = remember { FocusRequester() }
    val valueFocusRequester = remember { FocusRequester() }
    
    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            AppTopBar(
                title = { Text(if (isEditMode) stringResource(R.string.edit_chore_title) else stringResource(R.string.add_chore_title)) },
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isEditMode) stringResource(R.string.edit_chore_header) else stringResource(R.string.add_chore_header),
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = choreName,
                onValueChange = { choreName = it },
                label = { Text(stringResource(R.string.chore_name)) },
                placeholder = { Text(stringResource(R.string.chore_name_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { descriptionFocusRequester.requestFocus() })
            )
            
            OutlinedTextField(
                value = choreDescription,
                onValueChange = { choreDescription = it },
                label = { Text(stringResource(R.string.chore_description)) },
                placeholder = { Text(stringResource(R.string.chore_description_placeholder)) },
                modifier = Modifier.fillMaxWidth().focusRequester(descriptionFocusRequester),
                minLines = 3,
                maxLines = 5
            )
            
            OutlinedTextField(
                value = choreValue,
                onValueChange = { 
                    // Only allow numbers and decimal point
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                        choreValue = it
                    }
                },
                label = { Text(stringResource(R.string.chore_value)) },
                placeholder = { Text(stringResource(R.string.chore_value_placeholder)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                modifier = Modifier.fillMaxWidth().focusRequester(valueFocusRequester),
                singleLine = true,
                suffix = { Text(stringResource(R.string.add_chore_kr_suffix)) }
            )
            
            // Assignment dropdown
            ExposedDropdownMenuBox(
                expanded = showAssignmentMenu,
                onExpandedChange = { showAssignmentMenu = it }
            ) {
                OutlinedTextField(
                    value = assignedPerson,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.chore_assign_to)) },
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, "Dropdown")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                
                ExposedDropdownMenu(
                    expanded = showAssignmentMenu,
                    onDismissRequest = { showAssignmentMenu = false }
                ) {
                    assignmentOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                assignedPerson = option
                                showAssignmentMenu = false
                            }
                        )
                    }
                }
            }
            
            // Due Date Picker
            OutlinedTextField(
                value = dueDate?.let { dateFormat.format(it.toDate()) } ?: "No due date",
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.chore_due_date)) },
                trailingIcon = {
                    Row {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, "Select date")
                        }
                        if (dueDate != null) {
                            TextButton(onClick = { dueDate = null }) {
                                Text(stringResource(R.string.clear))
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
            )
            
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text(stringResource(R.string.ok))
                        }
                    }
                ) {
                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = dueDate?.toDate()?.time ?: System.currentTimeMillis()
                    )
                    DatePicker(state = datePickerState)
                    
                    LaunchedEffect(datePickerState.selectedDateMillis) {
                        datePickerState.selectedDateMillis?.let { millis ->
                            dueDate = Timestamp(Date(millis))
                        }
                    }
                }
            }
            
            // Info about recurring chores
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ℹ️",
                        fontSize = 12.sp
                    )
                    Text(
                        text = stringResource(R.string.add_chore_recurring_info),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Delete button (only in edit mode and for parents)
            if (isEditMode && isParent && choreToEdit != null) {
                OutlinedButton(
                    onClick = { showDeleteConfirmation = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.add_chore_delete))
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            Button(
                onClick = {
                    val value = choreValue.toDoubleOrNull() ?: 0.0
                    if (choreName.isNotBlank() && authState.user != null && authState.family != null) {
                        if (isEditMode) {
                            // Update existing chore (choreToEdit is guaranteed non-null here)
                            val assignedDisplayName = if (assignedPerson == unassignedText) "" else assignedPerson
                            val assignedUserId = memberState.members
                                .find { "${it.avatarEmoji} ${it.name}" == assignedDisplayName }?.userId ?: ""
                            val updatedChore = choreToEdit!!.copy(
                                name = choreName.trim(),
                                description = choreDescription.trim(),
                                value = value,
                                assignedTo = assignedDisplayName,
                                assignedToUserId = assignedUserId,
                                dueDate = dueDate
                            )
                            choreViewModel.updateChore(updatedChore)
                        } else {
                            // Create new chore (one-off, not recurring)
                            val assignedDisplayName = if (assignedPerson == unassignedText) "" else assignedPerson
                            val assignedUserId = memberState.members
                                .find { "${it.avatarEmoji} ${it.name}" == assignedDisplayName }?.userId ?: ""
                            val chore = Chore(
                                name = choreName.trim(),
                                description = choreDescription.trim(),
                                value = value,
                                assignedTo = assignedDisplayName,
                                assignedToUserId = assignedUserId,
                                createdBy = authState.user!!.id,
                                familyId = authState.family!!.id,
                                status = ChoreStatus.PENDING,
                                dueDate = dueDate,
                                createdAt = Timestamp.now()
                            )
                            choreViewModel.createChore(chore)
                            proposalToAccept?.let { onProposalAccepted?.invoke(it.id) }
                        }
                        onChoreCreated()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = choreName.isNotBlank() && choreValue.isNotBlank()
            ) {
                Text(
                    text = if (isEditMode) stringResource(R.string.save_changes) else stringResource(R.string.create_chore),
                    fontSize = 15.sp
                )
            }
        }        
        // Delete confirmation dialog
        if (showDeleteConfirmation && choreToEdit != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text(stringResource(R.string.add_chore_delete_title)) },
                text = { Text(stringResource(R.string.add_chore_delete_message, choreToEdit.name)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            authState.family?.id?.let { familyId ->
                                choreViewModel.deleteChore(choreToEdit.id, familyId)
                            }
                            showDeleteConfirmation = false
                            onChoreCreated()
                        }
                    ) {
                        Text(stringResource(R.string.add_chore_delete_button))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmation = false }) {
                        Text(stringResource(R.string.add_chore_cancel))
                    }
                }
            )
        }    }
}

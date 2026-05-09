package com.jmp.pocketmoneyapp.ui.chores

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.jmp.pocketmoneyapp.data.model.MemberRole
import com.jmp.pocketmoneyapp.data.model.RecurrenceType
import com.jmp.pocketmoneyapp.data.model.ChoreLibraryTemplate
import com.jmp.pocketmoneyapp.R
import com.jmp.pocketmoneyapp.ui.components.AppTopBar
import com.jmp.pocketmoneyapp.viewmodel.AuthViewModel
import com.jmp.pocketmoneyapp.viewmodel.FamilyMemberViewModel
import com.jmp.pocketmoneyapp.viewmodel.ChoreLibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChoreLibraryItemScreen(
    authViewModel: AuthViewModel,
    recurringViewModel: ChoreLibraryViewModel,
    memberViewModel: FamilyMemberViewModel,
    templateToEdit: ChoreLibraryTemplate? = null,
    onTemplateCreated: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val memberState by memberViewModel.memberState.collectAsState()
    val isEditMode = templateToEdit != null

    val isParent = authState.user?.role == com.jmp.pocketmoneyapp.data.model.UserRole.PARENT
    LaunchedEffect(isParent) {
        if (!isParent) onNavigateBack()
    }

    LaunchedEffect(authState.family?.id) {
        authState.family?.id?.let { familyId ->
            memberViewModel.loadFamilyMembers(familyId)
        }
    }

    var templateName by remember { mutableStateOf(templateToEdit?.name ?: "") }
    var templateDescription by remember { mutableStateOf(templateToEdit?.description ?: "") }
    var templateValue by remember { mutableStateOf(templateToEdit?.value?.toString() ?: "") }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val unassignedText = stringResource(R.string.chores_unassigned)
    val assignmentOptions = remember(memberState.members) {
        val children = memberState.members.filter { it.role == MemberRole.CHILD }
        listOf(unassignedText) + children.map { "${it.avatarEmoji} ${it.name}" }
    }
    var defaultAssignedTo by remember(unassignedText) {
        mutableStateOf(templateToEdit?.defaultAssignedTo?.ifEmpty { unassignedText } ?: unassignedText)
    }
    var showAssignmentMenu by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val descriptionFocusRequester = remember { FocusRequester() }
    val valueFocusRequester = remember { FocusRequester() }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            AppTopBar(
                title = { Text(stringResource(if (isEditMode) R.string.edit_recurring_chore_title else R.string.add_recurring_chore_title)) },
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
                text = stringResource(R.string.recurring_chore_template_info),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = templateName,
                onValueChange = { templateName = it },
                label = { Text(stringResource(R.string.chore_name)) },
                placeholder = { Text(stringResource(R.string.recurring_chore_name_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { descriptionFocusRequester.requestFocus() })
            )

            OutlinedTextField(
                value = templateDescription,
                onValueChange = { templateDescription = it },
                label = { Text(stringResource(R.string.chore_description)) },
                placeholder = { Text(stringResource(R.string.recurring_chore_description_placeholder)) },
                modifier = Modifier.fillMaxWidth().focusRequester(descriptionFocusRequester),
                minLines = 2,
                maxLines = 4
            )

            OutlinedTextField(
                value = templateValue,
                onValueChange = { templateValue = it },
                label = { Text(stringResource(R.string.chore_value)) },
                placeholder = { Text(stringResource(R.string.recurring_chore_value_placeholder)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth().focusRequester(valueFocusRequester),
                singleLine = true,
                suffix = { Text(stringResource(R.string.add_chore_kr_suffix)) }
            )

            HorizontalDivider()

            Text(
                text = stringResource(R.string.recurring_chore_assignment),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            ExposedDropdownMenuBox(
                expanded = showAssignmentMenu,
                onExpandedChange = { showAssignmentMenu = it }
            ) {
                OutlinedTextField(
                    value = defaultAssignedTo,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.recurring_chore_assigned_to)) },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Dropdown") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = showAssignmentMenu,
                    onDismissRequest = { showAssignmentMenu = false }
                ) {
                    assignmentOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                defaultAssignedTo = option
                                showAssignmentMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (isEditMode && isParent && templateToEdit != null) {
                OutlinedButton(
                    onClick = { showDeleteConfirmation = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.recurring_chore_delete))
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = {
                    val value = templateValue.toDoubleOrNull() ?: 0.0
                    if (templateName.isNotBlank() && authState.user != null && authState.family != null) {
                        val assignedTo = if (defaultAssignedTo == unassignedText) "" else defaultAssignedTo
                        if (isEditMode && templateToEdit != null) {
                            val updated = templateToEdit.copy(
                                name = templateName.trim(),
                                description = templateDescription.trim(),
                                value = value,
                                defaultAssignedTo = assignedTo,
                                recurrenceType = RecurrenceType.NONE,
                                timeOfDay = null,
                                dayOfWeek = null,
                                dayOfMonth = null
                            )
                            recurringViewModel.updateTemplate(updated)
                        } else {
                            val template = ChoreLibraryTemplate(
                                name = templateName.trim(),
                                description = templateDescription.trim(),
                                value = value,
                                familyId = authState.family!!.id,
                                createdBy = authState.user!!.id,
                                defaultAssignedTo = assignedTo,
                                recurrenceType = RecurrenceType.NONE,
                                isActive = true,
                                createdAt = Timestamp.now()
                            )
                            recurringViewModel.createTemplate(template)
                        }
                        onTemplateCreated()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = templateName.isNotBlank() && templateValue.isNotBlank()
            ) {
                Text(
                    text = stringResource(if (isEditMode) R.string.save_changes else R.string.recurring_chore_create),
                    fontSize = 15.sp
                )
            }
        }
    }

    if (showDeleteConfirmation && templateToEdit != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.recurring_chore_delete_title)) },
            text = { Text(stringResource(R.string.recurring_chore_delete_message, templateToEdit.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        authState.family?.id?.let { familyId ->
                            recurringViewModel.deleteTemplate(templateToEdit.id, familyId)
                        }
                        showDeleteConfirmation = false
                        onTemplateCreated()
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
    }
}

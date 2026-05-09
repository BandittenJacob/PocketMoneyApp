package com.jmp.pocketmoneyapp.ui.familymembers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.jmp.pocketmoneyapp.R
import com.jmp.pocketmoneyapp.ui.components.AppTopBar
import com.jmp.pocketmoneyapp.data.model.FamilyMember
import com.jmp.pocketmoneyapp.data.model.MemberRole
import com.jmp.pocketmoneyapp.viewmodel.AuthViewModel
import com.jmp.pocketmoneyapp.viewmodel.FamilyMemberViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFamilyMemberScreen(
    authViewModel: AuthViewModel,
    memberViewModel: FamilyMemberViewModel,
    memberToEdit: FamilyMember? = null,
    onNavigateBack: () -> Unit,
    onInviteClick: (FamilyMember) -> Unit = {}
) {
    val authState by authViewModel.authState.collectAsState()
    
    var name by remember { mutableStateOf(memberToEdit?.name ?: "") }
    var selectedRole by remember { mutableStateOf(memberToEdit?.role ?: MemberRole.CHILD) }
    var selectedEmoji by remember { mutableStateOf(memberToEdit?.avatarEmoji ?: "👤") }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    val isEditMode = memberToEdit != null
    val focusManager = LocalFocusManager.current
    
    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            AppTopBar(
                title = {
                    Text(
                        if (isEditMode)
                            stringResource(R.string.edit_member_title)
                        else
                            stringResource(R.string.add_member_title)
                    )
                },
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Name input
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.member_name)) },
                placeholder = { Text(stringResource(R.string.member_name_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )
            
            // Role selector
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.member_role),
                    style = MaterialTheme.typography.labelLarge
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        selected = selectedRole == MemberRole.PARENT,
                        onClick = { selectedRole = MemberRole.PARENT },
                        label = { Text(stringResource(R.string.member_role_parent)) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedRole == MemberRole.CHILD,
                        onClick = { selectedRole = MemberRole.CHILD },
                        label = { Text(stringResource(R.string.member_role_child)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Avatar/emoji selector
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.member_avatar),
                    style = MaterialTheme.typography.labelLarge
                )
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showEmojiPicker = !showEmojiPicker }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.member_avatar_hint))
                        Text(
                            text = selectedEmoji,
                            fontSize = 32.sp
                        )
                    }
                }
                
                if (showEmojiPicker) {
                    EmojiPicker(
                        selectedEmoji = selectedEmoji,
                        onEmojiSelected = {
                            selectedEmoji = it
                            showEmojiPicker = false
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Invite button (only in edit mode and if member has no linked user)
            if (isEditMode && memberToEdit != null && memberToEdit.userId == null) {
                Button(
                    onClick = { onInviteClick(memberToEdit) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("📱 Generate Invite QR Code")
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Delete button (only in edit mode)
            if (isEditMode && memberToEdit != null) {
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
                    Text(stringResource(R.string.member_delete))
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Save button
            Button(
                onClick = {
                    authState.family?.id?.let { familyId ->
                        if (isEditMode && memberToEdit != null) {
                            memberViewModel.updateMember(
                                memberToEdit.copy(
                                    name = name,
                                    role = selectedRole,
                                    avatarEmoji = selectedEmoji
                                )
                            )
                        } else {
                            memberViewModel.createMember(
                                FamilyMember(
                                    familyId = familyId,
                                    name = name,
                                    role = selectedRole,
                                    avatarEmoji = selectedEmoji,
                                    createdAt = Timestamp.now()
                                )
                            )
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) {
                Text(
                    if (isEditMode)
                        stringResource(R.string.update_member)
                    else
                        stringResource(R.string.create_member)
                )
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirmation && memberToEdit != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.member_delete_confirm, memberToEdit.name)) },
            text = { Text(stringResource(R.string.member_delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        memberViewModel.deleteMember(memberToEdit.id)
                        showDeleteConfirmation = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.member_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(R.string.member_delete_cancel))
                }
            }
        )
    }
}

@Composable
fun EmojiPicker(
    selectedEmoji: String,
    onEmojiSelected: (String) -> Unit
) {
    val emojis = listOf(
        "👨", "👩", "🧑", "👦", "👧", "🧒",
        "👨‍🦱", "👩‍🦱", "👨‍🦰", "👩‍🦰", "👨‍🦳", "👩‍🦳",
        "👶", "🧓", "👴", "👵",
        "😀", "😊", "😎", "🤓", "🥳", "😇",
        "🐶", "🐱", "🐭", "🐹", "🐰", "🦊",
        "🐻", "🐼", "🐨", "🐯", "🦁", "🐮",
        "⚽", "🏀", "⚾", "🎮", "🎨", "🎵",
        "⭐", "✨", "🌟", "💫", "🔥", "💎"
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier.height(200.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(emojis) { emoji ->
                    Card(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { onEmojiSelected(emoji) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (emoji == selectedEmoji)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

package com.jmp.pocketmoneyapp.ui.family

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jmp.pocketmoneyapp.data.model.DeletionRequest
import com.jmp.pocketmoneyapp.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyDeletionApprovalScreen(
    deletionRequest: DeletionRequest,
    familyName: String,
    currentUserId: String,
    viewModel: AuthViewModel,
    onDismiss: () -> Unit = {}
) {
    val authState by viewModel.authState.collectAsState()
    
    // Monitor for family deletion - if family becomes null, it was deleted
    LaunchedEffect(authState.family) {
        if (authState.family == null && authState.user == null) {
            // Family and user deleted, navigation will happen automatically
            onDismiss()
        }
    }
    
    val hasCurrentUserApproved = deletionRequest.approvedBy.contains(currentUserId)
    val requiredApprovals = deletionRequest.requiredApprovals.size
    val receivedApprovals = deletionRequest.approvedBy.size
    val allApproved = receivedApprovals == requiredApprovals
    
    var isProcessing by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    // Full-screen dialog
    AlertDialog(
        onDismissRequest = { /* Cannot dismiss */ },
        modifier = Modifier.fillMaxSize(),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "⚠️ Family Deletion Request",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Family info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Family: $familyName",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Initiated by: ${deletionRequest.initiatedByName}",
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Approvals: $receivedApprovals of $requiredApprovals",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Show who has approved
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Approval Status:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        deletionRequest.requiredApprovals.forEach { userId ->
                            val hasApproved = deletionRequest.approvedBy.contains(userId)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (hasApproved) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (hasApproved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = if (userId == currentUserId) {
                                        if (hasApproved) "You (Approved)" else "You (Pending)"
                                    } else {
                                        if (hasApproved) "Parent (Approved)" else "Parent (Pending)"
                                    },
                                    fontSize = 12.sp,
                                    color = if (hasApproved) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // Warning text
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "⚠️ WARNING",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This will permanently delete:\n\n" +
                                    "• The family \"$familyName\"\n" +
                                    "• All family members\n" +
                                    "• All chores and templates\n" +
                                    "• All transactions and balances\n" +
                                    "• All user accounts\n\n" +
                                    "This action CANNOT be undone!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                
                if (isProcessing) {
                    CircularProgressIndicator()
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!hasCurrentUserApproved) {
                    Button(
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        enabled = !isProcessing
                    ) {
                        Text("Approve Deletion")
                    }
                } else {
                    Text(
                        text = "✓ You have approved this deletion",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        dismissButton = {
            if (!hasCurrentUserApproved) {
                OutlinedButton(
                    onClick = {
                        isProcessing = true
                        viewModel.cancelFamilyDeletion { success, error ->
                            isProcessing = false
                            if (success) {
                                onDismiss()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isProcessing
                ) {
                    Text("Reject & Cancel Request")
                }
            }
        }
    )
    
    // Confirmation dialog for approval
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Deletion Approval") },
            text = { 
                Text(
                    "Are you ABSOLUTELY SURE you want to approve the deletion of \"$familyName\"?\n\n" +
                    "This will delete all family data and user accounts permanently."
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        isProcessing = true
                        viewModel.approveFamilyDeletion { success, allApproved, error ->
                            isProcessing = false
                            if (success) {
                                if (allApproved) {
                                    // Family was deleted, user will be signed out automatically
                                    onDismiss()
                                }
                                // If not all approved yet, dialog stays open showing updated status
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Yes, Approve Deletion")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

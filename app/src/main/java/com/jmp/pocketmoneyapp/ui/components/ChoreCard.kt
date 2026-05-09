package com.jmp.pocketmoneyapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jmp.pocketmoneyapp.R
import com.jmp.pocketmoneyapp.data.model.Chore
import com.jmp.pocketmoneyapp.data.model.ChoreStatus
import com.jmp.pocketmoneyapp.data.model.FamilyMember
import com.jmp.pocketmoneyapp.ui.theme.AppText
import java.text.SimpleDateFormat
import java.util.*

private enum class DueDateUrgency { NORMAL, DUE_SOON, DUE_TODAY, OVERDUE }

private fun getDueDateUrgency(dueDate: com.google.firebase.Timestamp): DueDateUrgency {
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val dueDay = Calendar.getInstance().apply {
        time = dueDate.toDate()
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val diffDays = ((dueDay.timeInMillis - today.timeInMillis) / (1000L * 60 * 60 * 24)).toInt()
    return when {
        diffDays < 0  -> DueDateUrgency.OVERDUE
        diffDays == 0 -> DueDateUrgency.DUE_TODAY
        diffDays <= 2 -> DueDateUrgency.DUE_SOON
        else          -> DueDateUrgency.NORMAL
    }
}

/**
 * Standardized chore card component.
 * 
 * Displays all chore information in a consistent format with proper styling,
 * status badges, and action buttons based on user role and chore state.
 * 
 * @param chore The chore data to display
 * @param members List of family members (for assignment dropdown)
 * @param onMarkComplete Callback when chore is marked complete (child action)
 * @param onEdit Callback on long press (parent action)
 * @param onRevert Callback when reverting chore status (parent action)
 * @param onApprove Callback when approving completed chore (parent action)
 * @param onAssign Callback when assigning/reassigning chore (parent/child action)
 * @param isParent True if current user is a parent
 * @param modifier Optional modifier for the card
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChoreCard(
    chore: Chore,
    members: List<FamilyMember>,
    onMarkComplete: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    onRevert: (() -> Unit)? = null,
    onApprove: (() -> Unit)? = null,
    onAssign: ((String) -> Unit)? = null,
    isParent: Boolean,
    isExpanded: Boolean = true,
    onExpand: (() -> Unit)? = null,
    showDueDateIndicator: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showAssignmentMenu by remember { mutableStateOf(false) }
    val unassignedText = stringResource(R.string.chores_unassigned)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onExpand != null || onEdit != null) Modifier.combinedClickable(
                    onClick = { onExpand?.invoke() },
                    onLongClick = if (onEdit != null) ({ onEdit() }) else null
                ) else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = when (chore.status) {
                ChoreStatus.COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer
                ChoreStatus.APPROVED -> MaterialTheme.colorScheme.primaryContainer
                ChoreStatus.PAID -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Row 1: Title + type badge on left, value on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppText.CardTitle(text = chore.name)
                }
                AppText.CardValue(text = String.format("%.2f kr.", chore.value))
            }

            // Collapsed summary: status chip + assignee (only shown when collapsed)
            AnimatedVisibility(visible = !isExpanded) {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusChip(status = chore.status)
                    AppText.CardLabel(
                        text = if (chore.assignedTo.isEmpty())
                            "\uD83D\uDC64 ${stringResource(R.string.chores_unassigned)}"
                        else
                            chore.assignedTo
                    )
                    // Overdue / due-today badge in collapsed view
                    if (showDueDateIndicator && chore.dueDate != null && chore.status == ChoreStatus.PENDING) {
                        when (getDueDateUrgency(chore.dueDate)) {
                            DueDateUrgency.OVERDUE   -> Text("⚠️", fontSize = 13.sp)
                            DueDateUrgency.DUE_TODAY -> Text("🔔", fontSize = 13.sp)
                            else -> {}
                        }
                    }
                }
            }

            // Expanded content: description + full status/action row
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    // Row 2: Description (only if present)
                    if (chore.description.isNotEmpty()) {
                        AppText.BodySecondary(
                            text = chore.description,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // Row 3: Status chip + all action buttons in one horizontal row
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Status chip + assignment label
                        Column {
                            StatusChip(status = chore.status)
                            AppText.CardLabel(
                                text = if (chore.assignedTo.isEmpty())
                                    "\uD83D\uDC64 ${stringResource(R.string.chores_unassigned)}"
                                else
                                    chore.assignedTo,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            chore.dueDate?.let { dueDate ->
                                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                if (showDueDateIndicator && chore.status == ChoreStatus.PENDING) {
                                    val urgency = getDueDateUrgency(dueDate)
                                    val dueDateColor = when (urgency) {
                                        DueDateUrgency.OVERDUE   -> MaterialTheme.colorScheme.error
                                        DueDateUrgency.DUE_TODAY -> Color(0xFFE65100)
                                        DueDateUrgency.DUE_SOON  -> Color(0xFFF57F17)
                                        DueDateUrgency.NORMAL    -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                    val prefix = when (urgency) {
                                        DueDateUrgency.OVERDUE   -> "⚠️ "
                                        DueDateUrgency.DUE_TODAY -> "🔔 "
                                        DueDateUrgency.DUE_SOON  -> "⏰ "
                                        DueDateUrgency.NORMAL    -> "📅 "
                                    }
                                    AppText.CardLabel(
                                        text = "$prefix${stringResource(R.string.chores_due_date, dateFormat.format(dueDate.toDate()))}",
                                        color = dueDateColor,
                                        modifier = Modifier.padding(top = 1.dp)
                                    )
                                } else {
                                    AppText.CardLabel(
                                        text = "\uD83D\uDCC5 ${stringResource(R.string.chores_due_date, dateFormat.format(dueDate.toDate()))}",
                                        modifier = Modifier.padding(top = 1.dp)
                                    )
                                }
                            }
                        }

                        // Right: All action buttons side by side (no vertical stacking)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (onRevert != null) {
                                OutlinedButton(
                                    onClick = onRevert,
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    AppText.ButtonSecondary(stringResource(R.string.chores_not_done))
                                }
                            }
                            if (chore.status == ChoreStatus.PENDING && chore.assignedTo.isNotEmpty() && onMarkComplete != null) {
                                Button(
                                    onClick = onMarkComplete,
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    AppText.ButtonPrimary(stringResource(R.string.chores_complete))
                                }
                            }
                            if (chore.status == ChoreStatus.COMPLETED && onApprove != null) {
                                Button(
                                    onClick = onApprove,
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    AppText.ButtonPrimary("\u2713 Approve")
                                }
                            }
                            if (chore.status == ChoreStatus.PENDING && onAssign != null) {
                                AssignmentButtons(
                                    chore = chore,
                                    members = members,
                                    isParent = isParent,
                                    onAssign = onAssign,
                                    showMenu = showAssignmentMenu,
                                    onShowMenuChange = { showAssignmentMenu = it },
                                    unassignedText = unassignedText
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Handles Claim/Assign/Reassign button logic with dropdown menus.
 */
@Composable
private fun AssignmentButtons(
    chore: Chore,
    members: List<FamilyMember>,
    isParent: Boolean,
    onAssign: (String) -> Unit,
    showMenu: Boolean,
    onShowMenuChange: (Boolean) -> Unit,
    unassignedText: String
) {
    if (chore.assignedTo.isEmpty()) {
        // Unassigned chore
        if (!isParent && members.isNotEmpty()) {
            // Child: Direct claim button
            Button(
                onClick = {
                    val member = members.first()
                    onAssign("${member.avatarEmoji} ${member.name}")
                },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                AppText.ButtonPrimary(stringResource(R.string.chores_claim))
            }
        } else if (isParent && members.isNotEmpty()) {
            // Parent: Assign dropdown
            Box {
                Button(
                    onClick = { onShowMenuChange(true) },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    AppText.ButtonPrimary(stringResource(R.string.chores_assign))
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { onShowMenuChange(false) }
                ) {
                    members.forEach { member ->
                        DropdownMenuItem(
                            text = { Text("${member.avatarEmoji} ${member.name}") },
                            onClick = {
                                onAssign("${member.avatarEmoji} ${member.name}")
                                onShowMenuChange(false)
                            }
                        )
                    }
                }
            }
        }
    } else {
        // Assigned chore - Reassign dropdown (parents only)
        if (isParent && members.isNotEmpty()) {
            Box {
                OutlinedButton(
                    onClick = { onShowMenuChange(true) },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    AppText.ButtonSecondary(stringResource(R.string.chores_reassign))
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { onShowMenuChange(false) }
                ) {
                    DropdownMenuItem(
                        text = { Text(unassignedText) },
                        onClick = {
                            onAssign("")
                            onShowMenuChange(false)
                        }
                    )
                    members.forEach { member ->
                        DropdownMenuItem(
                            text = { Text("${member.avatarEmoji} ${member.name}") },
                            onClick = {
                                onAssign("${member.avatarEmoji} ${member.name}")
                                onShowMenuChange(false)
                            }
                        )
                    }
                }
            }
        }
    }
}

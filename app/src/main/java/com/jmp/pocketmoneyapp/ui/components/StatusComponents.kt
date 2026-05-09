package com.jmp.pocketmoneyapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jmp.pocketmoneyapp.R
import com.jmp.pocketmoneyapp.data.model.ChoreStatus

/**
 * Reusable status chip component with proper contrast colors.
 * 
 * Displays the current status of a chore (PENDING, COMPLETED, APPROVED, PAID)
 * with accessibility-compliant color contrast.
 * 
 * @param status The current chore status
 * @param modifier Optional modifier for the chip
 */
@Composable
fun StatusChip(
    status: ChoreStatus,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = {},
        label = {
            Text(
                when (status) {
                    ChoreStatus.PENDING -> stringResource(R.string.status_pending)
                    ChoreStatus.COMPLETED -> stringResource(R.string.status_completed)
                    ChoreStatus.APPROVED -> stringResource(R.string.status_approved)
                    ChoreStatus.PAID -> stringResource(R.string.status_paid)
                    else -> status.name
                }
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = when (status) {
                ChoreStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant
                ChoreStatus.COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer
                ChoreStatus.APPROVED -> MaterialTheme.colorScheme.primaryContainer
                ChoreStatus.PAID -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            labelColor = when (status) {
                ChoreStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
                ChoreStatus.COMPLETED -> MaterialTheme.colorScheme.onTertiaryContainer
                ChoreStatus.APPROVED -> MaterialTheme.colorScheme.onPrimaryContainer
                ChoreStatus.PAID -> MaterialTheme.colorScheme.onSecondaryContainer
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        ),
        modifier = modifier
    )
}

package com.jmp.pocketmoneyapp.ui.chores

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jmp.pocketmoneyapp.R
import com.jmp.pocketmoneyapp.data.model.ChoreStatus
import com.jmp.pocketmoneyapp.data.model.UserRole
import com.jmp.pocketmoneyapp.ui.components.ChoreCard
import com.jmp.pocketmoneyapp.ui.components.AppTopBar
import com.jmp.pocketmoneyapp.ui.components.EmptyState
import com.jmp.pocketmoneyapp.viewmodel.AuthViewModel
import com.jmp.pocketmoneyapp.viewmodel.ChoreViewModel
import com.jmp.pocketmoneyapp.viewmodel.FamilyMemberViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoreHistoryScreen(
    authViewModel: AuthViewModel,
    choreViewModel: ChoreViewModel,
    memberViewModel: FamilyMemberViewModel,
    onNavigateBack: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val choreState by choreViewModel.choreState.collectAsState()
    val memberState by memberViewModel.memberState.collectAsState()

    val isParent = authState.user?.role == UserRole.PARENT
    val currentMember = memberState.members.find { it.userId == authState.user?.id }
    val currentMemberName = currentMember?.let { "${it.avatarEmoji} ${it.name}" } ?: ""

    // Only show APPROVED and PAID chores
    val historyChores = choreState.chores.filter { chore ->
        val isApprovedOrPaid = chore.status == ChoreStatus.APPROVED || chore.status == ChoreStatus.PAID
        if (isParent) {
            isApprovedOrPaid
        } else {
            isApprovedOrPaid && chore.assignedTo == currentMemberName
        }
    }.sortedByDescending { it.createdAt }

    Scaffold(
        topBar = {
            AppTopBar(
                title = { Text(stringResource(R.string.chore_history_title)) },
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = choreState.isLoading,
            onRefresh = {
                authState.family?.id?.let { familyId ->
                    choreViewModel.loadFamilyChores(familyId)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (historyChores.isEmpty()) {
                EmptyState(
                    icon = "✅",
                    title = stringResource(R.string.chore_history_empty),
                    hint = stringResource(R.string.chore_history_empty_hint)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = stringResource(R.string.chore_history_count, historyChores.size),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(historyChores, key = { it.id }) { chore ->
                        ChoreCard(
                            chore = chore,
                            members = memberState.members.filter {
                                it.role == com.jmp.pocketmoneyapp.data.model.MemberRole.CHILD
                            },
                            onMarkComplete = null,
                            onEdit = null,
                            onRevert = if (isParent) {
                                {
                                    authState.family?.id?.let { familyId ->
                                        choreViewModel.updateChoreStatus(
                                            chore.id,
                                            ChoreStatus.PENDING,
                                            familyId
                                        )
                                        onNavigateBack()
                                    }
                                }
                            } else null,
                            onApprove = null,
                            onAssign = null,
                            isParent = isParent
                        )
                    }
                }
            }
        }
    }
}

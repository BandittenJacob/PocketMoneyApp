package com.jmp.pocketmoneyapp.ui.chores

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopas.lib.showcase.IntroShowcase
import com.canopas.lib.showcase.component.ShowcaseStyle
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import com.jmp.pocketmoneyapp.R
import com.jmp.pocketmoneyapp.data.model.Chore
import com.jmp.pocketmoneyapp.data.model.ChoreStatus
import com.jmp.pocketmoneyapp.data.model.ProposalStatus
import com.jmp.pocketmoneyapp.data.repository.PreferencesManager
import com.jmp.pocketmoneyapp.ui.components.ChoreCard
import com.jmp.pocketmoneyapp.ui.components.AppTopBar
import com.jmp.pocketmoneyapp.ui.components.EmptyState
import com.jmp.pocketmoneyapp.ui.components.SectionHeader
import com.jmp.pocketmoneyapp.ui.theme.AppText
import com.jmp.pocketmoneyapp.ui.family.FamilyDeletionApprovalScreen
import com.jmp.pocketmoneyapp.viewmodel.AuthViewModel
import com.jmp.pocketmoneyapp.viewmodel.ChoreProposalViewModel
import com.jmp.pocketmoneyapp.viewmodel.ChoreViewModel
import com.jmp.pocketmoneyapp.viewmodel.FamilyMemberViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoresScreen(
    authViewModel: AuthViewModel,
    choreViewModel: ChoreViewModel,
    memberViewModel: FamilyMemberViewModel,
    proposalViewModel: ChoreProposalViewModel,
    onAddChoreClick: () -> Unit,
    onEditChore: (Chore) -> Unit,
    onChoreLibraryClick: () -> Unit,
    onChoreHistoryClick: () -> Unit,
    onSuggestChoreClick: () -> Unit,
    onAcceptWithEdits: () -> Unit,
    onSignOut: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val choreState by choreViewModel.choreState.collectAsState()
    val memberState by memberViewModel.memberState.collectAsState()
    val proposalState by proposalViewModel.state.collectAsState()

    val proposalsEnabled = authState.family?.choreProposalsEnabled ?: false
    val dueDateIndicatorsEnabled = authState.family?.dueDateIndicatorsEnabled ?: false
    val isParent = authState.user?.role == com.jmp.pocketmoneyapp.data.model.UserRole.PARENT
    
    val hasPendingDeletion = authState.family?.deletionRequest?.active == true
    
    // Show deletion approval screen INSTEAD of normal chores for parents
    if (isParent && hasPendingDeletion && authState.family != null && authState.user != null) {
        FamilyDeletionApprovalScreen(
            deletionRequest = authState.family!!.deletionRequest!!,
            familyName = authState.family!!.name,
            currentUserId = authState.user!!.id,
            viewModel = authViewModel,
            onDismiss = {
                // After dismissal (cancel or complete), may need to sign out
                if (!authState.isAuthenticated) {
                    onSignOut()
                }
            }
        )
        return  // Don't show normal chores content
    }
    
    // Get current user's family member (for children)
    val currentMember = memberState.members.find { it.userId == authState.user?.id }
    val currentMemberName = currentMember?.let { "${it.avatarEmoji} ${it.name}" } ?: ""
    
    // Filter chores based on user role
    val visibleChores = if (isParent) {
        choreState.chores
    } else {
        // Children only see: their own chores + unassigned chores (PENDING/IN_PROGRESS/COMPLETED only, not APPROVED/PAID)
        choreState.chores.filter { chore ->
            val isOwnChore = chore.assignedTo == currentMemberName
            val isUnassigned = chore.assignedTo.isEmpty()
            val isNotApproved = chore.status != ChoreStatus.APPROVED && chore.status != ChoreStatus.PAID
            (isOwnChore || isUnassigned) && isNotApproved
        }
    }
    
    // Split chores into categories
    val incompleteChores = visibleChores.filter { 
        it.status == ChoreStatus.PENDING || it.status == ChoreStatus.IN_PROGRESS 
    }
    val pendingApprovalChores = visibleChores.filter { 
        it.status == ChoreStatus.COMPLETED 
    }
    // APPROVED/PAID chores moved to ChoreHistoryScreen
    
    // For children: split incomplete chores into "My Chores" and "Unassigned"
    val myChores = if (!isParent) {
        incompleteChores.filter { it.assignedTo == currentMemberName }
    } else emptyList()
    
    val unassignedChores = if (!isParent) {
        incompleteChores.filter { it.assignedTo.isEmpty() }
    } else emptyList()
    
    // The draggable section is incompleteChores for parents, myChores for children
    val activeReorderableChores = if (isParent) incompleteChores else myChores
    val reorderableChores = remember { mutableStateListOf<com.jmp.pocketmoneyapp.data.model.Chore>() }
    var hasDragged by remember { mutableStateOf(false) }
    var isReorderMode by remember { mutableStateOf(false) }
    var expandedChoreId by remember { mutableStateOf<String?>(null) }
    val onToggleExpanded: (String) -> Unit = { id ->
        expandedChoreId = if (expandedChoreId == id) null else id
    }

    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val fromIndex = reorderableChores.indexOfFirst { "chore_${it.id}" == from.key }
        val toIndex = reorderableChores.indexOfFirst { "chore_${it.id}" == to.key }
        if (fromIndex >= 0 && toIndex >= 0) {
            reorderableChores.add(toIndex, reorderableChores.removeAt(fromIndex))
            hasDragged = true
        }
    }

    // Tour state — shown once on first visit
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }
    var showTour by remember { mutableStateOf(!prefs.hasSeenChoresTour) }

    // Sync local reorderable list when chores load/refresh
    LaunchedEffect(activeReorderableChores) {
        reorderableChores.clear()
        reorderableChores.addAll(activeReorderableChores)
    }

    // Save order to DataStore when drag ends
    LaunchedEffect(reorderState.isAnyItemDragging) {
        if (!reorderState.isAnyItemDragging && hasDragged) {
            hasDragged = false
            val userId = authState.user?.id ?: return@LaunchedEffect
            choreViewModel.saveChoreOrder(userId, reorderableChores.map { it.id })
        }
    }

    // Load chores when family is available
    LaunchedEffect(authState.family?.id) {
        authState.family?.id?.let { familyId ->
            val userId = authState.user?.id ?: ""
            choreViewModel.loadFamilyChores(familyId, userId)
            memberViewModel.loadFamilyMembers(familyId)
            if (isParent) {
                proposalViewModel.loadForFamily(familyId)
            }
        }
    }

    // Load child proposals in a separate effect — waits until members are available
    LaunchedEffect(memberState.members, authState.family?.id) {
        if (!isParent) {
            val familyId = authState.family?.id ?: return@LaunchedEffect
            val userId = authState.user?.id ?: return@LaunchedEffect
            val memberId = memberState.members.find { it.userId == userId }?.id ?: return@LaunchedEffect
            proposalViewModel.loadForMember(familyId, memberId)
        }
    }
    
    IntroShowcase(
        showIntroShowCase = showTour,
        dismissOnClickOutside = false,
        onShowCaseCompleted = {
            prefs.hasSeenChoresTour = true
            showTour = false
        }
    ) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = { Text(stringResource(R.string.chores_title)) },
                onNavigateBack = onNavigateBack,
                actions = {
                    // 💡 Suggest button — children only, when feature is enabled
                    if (!isParent && proposalsEnabled) {
                        IconButton(
                            onClick = onSuggestChoreClick,
                            modifier = Modifier.introShowCaseTarget(
                                index = 0,
                                style = ShowcaseStyle.Default.copy(
                                    backgroundColor = Color(0xFF1A237E),
                                    backgroundAlpha = 0.95f,
                                    targetCircleColor = Color.White
                                ),
                                content = {
                                    Column {
                                        Text(
                                            text = stringResource(R.string.tour_chores_suggest_title),
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = stringResource(R.string.tour_chores_suggest_body),
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            )
                        ) {
                            AppText.TopBarIcon(stringResource(R.string.proposals_suggest_icon))
                        }
                    }
                    // 📚 Chore Library — parents only
                    if (isParent) {
                        IconButton(
                            onClick = onChoreLibraryClick,
                            modifier = Modifier.introShowCaseTarget(
                                index = 0,
                                style = ShowcaseStyle.Default.copy(
                                    backgroundColor = Color(0xFF1A237E),
                                    backgroundAlpha = 0.95f,
                                    targetCircleColor = Color.White
                                ),
                                content = {
                                    Column {
                                        Text(
                                            text = stringResource(R.string.tour_chores_library_title),
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = stringResource(R.string.tour_chores_library_body),
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            )
                        ) {
                            AppText.TopBarIcon(stringResource(R.string.chores_recurring_icon))
                        }
                    }
                    IconButton(
                        onClick = onChoreHistoryClick,
                        modifier = Modifier.introShowCaseTarget(
                            index = 1,
                            style = ShowcaseStyle.Default.copy(
                                backgroundColor = Color(0xFF1A237E),
                                backgroundAlpha = 0.95f,
                                targetCircleColor = Color.White
                            ),
                            content = {
                                Column {
                                    Text(
                                        text = stringResource(R.string.tour_chores_history_title),
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = stringResource(R.string.tour_chores_history_body),
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        )
                    ) {
                        AppText.TopBarIcon(stringResource(R.string.chore_history_icon))
                    }
                    // Divider separating navigation from inline action
                    VerticalDivider(
                        modifier = Modifier
                            .height(24.dp)
                            .padding(horizontal = 4.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
                    IconButton(
                        onClick = { isReorderMode = !isReorderMode },
                        modifier = Modifier.introShowCaseTarget(
                            index = 2,
                            style = ShowcaseStyle.Default.copy(
                                backgroundColor = Color(0xFF1A237E),
                                backgroundAlpha = 0.95f,
                                targetCircleColor = Color.White
                            ),
                            content = {
                                Column {
                                    Text(
                                        text = stringResource(R.string.tour_chores_reorder_title),
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = stringResource(R.string.tour_chores_reorder_body),
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        )
                    ) {
                        Icon(
                            imageVector = if (isReorderMode) Icons.Default.CheckCircle else Icons.Default.DragHandle,
                            contentDescription = stringResource(
                                if (isReorderMode) R.string.chore_reorder_done else R.string.chore_reorder_enable
                            ),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // Only parents can add new chores
            if (isParent) {
                FloatingActionButton(
                    onClick = onAddChoreClick,
                    modifier = Modifier.introShowCaseTarget(
                        index = 4,
                        style = ShowcaseStyle.Default.copy(
                            backgroundColor = Color(0xFF1A237E),
                            backgroundAlpha = 0.95f,
                            targetCircleColor = Color.White
                        ),
                        content = {
                            Column {
                                Text(
                                    text = stringResource(R.string.tour_chores_fab_title),
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.tour_chores_fab_body),
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    )
                ) {
                    Icon(Icons.Default.Add, stringResource(R.string.chores_add_chore))
                }
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = choreState.isLoading,
            onRefresh = {
                authState.family?.id?.let { familyId ->
                    val userId = authState.user?.id ?: ""
                    choreViewModel.loadFamilyChores(familyId, userId)
                    memberViewModel.loadFamilyMembers(familyId)
                    if (isParent) {
                        proposalViewModel.loadForFamily(familyId)
                    } else {
                        currentMember?.id?.let { memberId ->
                            proposalViewModel.loadForMember(familyId, memberId)
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Family info header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = authState.family?.name ?: "Loading...",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = stringResource(R.string.chores_welcome, authState.user?.name ?: ""),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Chore list
            if (choreState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (visibleChores.isEmpty()) {
                EmptyState(
                    icon = stringResource(R.string.chores_empty_icon),
                    title = if (isParent) 
                        stringResource(R.string.chores_no_chores) 
                    else 
                        stringResource(R.string.chores_empty_child),
                    hint = if (isParent) 
                        stringResource(R.string.chores_add_hint) 
                    else 
                        stringResource(R.string.chores_empty_child_hint)
                )
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // PARENT VIEW: Pending proposals section
                    val pendingProposals = proposalState.proposals.filter { it.status == ProposalStatus.PENDING }
                    if (isParent && proposalsEnabled && pendingProposals.isNotEmpty()) {
                        item(key = "header_proposals") {
                            SectionHeader(
                                text = stringResource(R.string.proposals_section_parent, pendingProposals.size),
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        items(pendingProposals, key = { "proposal_${it.id}" }) { proposal ->
                            var showRejectField by remember { mutableStateOf(false) }
                            var rejectReason by remember { mutableStateOf("") }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(proposal.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text(String.format("%.2f kr.", proposal.suggestedReward), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                    if (proposal.description.isNotEmpty()) {
                                        Text(proposal.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(stringResource(R.string.proposals_by, proposal.proposedByName), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (!showRejectField) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Button(
                                                onClick = {
                                                    val familyId = authState.family?.id ?: return@Button
                                                    val userId = authState.user?.id ?: return@Button
                                                    choreViewModel.createChore(
                                                        com.jmp.pocketmoneyapp.data.model.Chore(
                                                            name = proposal.name,
                                                            description = proposal.description,
                                                            value = proposal.suggestedReward,
                                                            assignedTo = proposal.proposedByName,
                                                            assignedToUserId = memberState.members
                                                                .find { it.id == proposal.proposedByMemberId }?.userId ?: "",
                                                            createdBy = userId,
                                                            familyId = familyId,
                                                            status = com.jmp.pocketmoneyapp.data.model.ChoreStatus.PENDING,
                                                            createdAt = com.google.firebase.Timestamp.now()
                                                        )
                                                    )
                                                    proposalViewModel.accept(proposal, familyId)
                                                },
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                            ) { Text(stringResource(R.string.proposals_accept), style = MaterialTheme.typography.labelSmall) }
                                            OutlinedButton(
                                                onClick = {
                                                    proposalViewModel.acceptWithEdits(proposal)
                                                    onAcceptWithEdits()
                                                },
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                            ) { Text(stringResource(R.string.proposals_accept_edit), style = MaterialTheme.typography.labelSmall) }
                                            OutlinedButton(
                                                onClick = { showRejectField = true },
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                            ) { Text(stringResource(R.string.proposals_reject_confirm), style = MaterialTheme.typography.labelSmall) }
                                        }
                                    } else {
                                        OutlinedTextField(
                                            value = rejectReason,
                                            onValueChange = { rejectReason = it },
                                            label = { Text(stringResource(R.string.proposals_reject_reason)) },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Button(
                                                onClick = {
                                                    authState.family?.id?.let { proposalViewModel.reject(proposal, rejectReason, it) }
                                                },
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                            ) { Text(stringResource(R.string.proposals_reject_confirm), style = MaterialTheme.typography.labelSmall) }
                                            OutlinedButton(
                                                onClick = { showRejectField = false; rejectReason = "" },
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                            ) { Text(stringResource(R.string.dismiss), style = MaterialTheme.typography.labelSmall) }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // CHILD VIEW: My Suggestions section
                    if (!isParent && proposalsEnabled && proposalState.proposals.isNotEmpty()) {
                        item(key = "header_my_suggestions") {
                            SectionHeader(text = stringResource(R.string.proposals_section_child))
                        }
                        items(proposalState.proposals, key = { "myproposal_${it.id}" }) { proposal ->
                            val statusText = when (proposal.status) {
                                ProposalStatus.PENDING -> stringResource(R.string.proposals_status_pending)
                                ProposalStatus.ACCEPTED -> stringResource(R.string.proposals_status_accepted)
                                ProposalStatus.ACCEPTED_WITH_EDITS -> stringResource(R.string.proposals_status_accepted_edited)
                                ProposalStatus.REJECTED -> stringResource(R.string.proposals_status_rejected)
                            }
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(proposal.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text(String.format("%.2f kr.", proposal.suggestedReward), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Text(statusText, style = MaterialTheme.typography.labelSmall)
                                    if (proposal.status == ProposalStatus.REJECTED && proposal.parentNote.isNotEmpty()) {
                                        Text("\"${proposal.parentNote}\"", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (proposal.status != ProposalStatus.PENDING) {
                                        TextButton(
                                            onClick = {
                                                val familyId = authState.family?.id ?: return@TextButton
                                                val memberId = currentMember?.id ?: return@TextButton
                                                proposalViewModel.dismiss(proposal, familyId, memberId)
                                            },
                                            contentPadding = PaddingValues(0.dp)
                                        ) { Text(stringResource(R.string.proposals_dismiss), style = MaterialTheme.typography.labelSmall) }
                                    }
                                }
                            }
                        }
                    }

                    // PARENT VIEW: Show all incomplete chores (drag-to-reorder enabled)
                    if (isParent && reorderableChores.isNotEmpty()) {
                        item(key = "header_todo") {
                            SectionHeader(
                                text = stringResource(R.string.chores_todo_section, reorderableChores.size)
                            )
                        }

                        itemsIndexed(reorderableChores, key = { _, chore -> "chore_${chore.id}" }) { choreIndex, chore ->
                            ReorderableItem(reorderState, key = "chore_${chore.id}") {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (isReorderMode) {
                                        Icon(
                                            imageVector = Icons.Default.DragHandle,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .draggableHandle(),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    ChoreCard(
                                        modifier = if (choreIndex == 0) Modifier
                                            .weight(1f)
                                            .introShowCaseTarget(
                                                index = 3,
                                                style = ShowcaseStyle.Default.copy(
                                                    backgroundColor = Color(0xFF1A237E),
                                                    backgroundAlpha = 0.95f,
                                                    targetCircleColor = Color.White
                                                ),
                                                content = {
                                                    Column {
                                                        Text(
                                                            text = stringResource(R.string.tour_chores_card_title),
                                                            color = Color.White,
                                                            fontSize = 18.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        Text(
                                                            text = stringResource(R.string.tour_chores_card_body),
                                                            color = Color.White,
                                                            fontSize = 14.sp
                                                        )
                                                    }
                                                }
                                            )
                                        else Modifier.weight(1f),
                                        chore = chore,
                                        members = memberState.members.filter { it.role == com.jmp.pocketmoneyapp.data.model.MemberRole.CHILD },
                                        onMarkComplete = {
                                            authState.family?.id?.let { familyId ->
                                                choreViewModel.updateChoreStatus(
                                                    chore.id,
                                                    ChoreStatus.COMPLETED,
                                                    familyId
                                                )
                                            }
                                        },
                                        onEdit = { onEditChore(chore) },
                                        onRevert = null,
                                        onApprove = null,
                                        onAssign = { assignTo ->
                                            authState.family?.id?.let { familyId ->
                                                val assignedUserId = memberState.members
                                                    .find { "${it.avatarEmoji} ${it.name}" == assignTo }?.userId ?: ""
                                                val updatedChore = chore.copy(assignedTo = assignTo, assignedToUserId = assignedUserId)
                                                choreViewModel.updateChore(updatedChore)
                                            }
                                        },
                                        isParent = isParent,
                                        isExpanded = chore.id == expandedChoreId,
                                        onExpand = { onToggleExpanded(chore.id) },
                                        showDueDateIndicator = dueDateIndicatorsEnabled
                                    )
                                }
                            }
                        }
                    }
                    
                    // CHILD VIEW: My Chores Section (drag-to-reorder enabled)
                    if (!isParent && reorderableChores.isNotEmpty()) {
                        item(key = "header_my_chores") {
                            SectionHeader(text = "📝 My Chores (${reorderableChores.size})")
                        }

                        itemsIndexed(reorderableChores, key = { _, chore -> "chore_${chore.id}" }) { choreIndex, chore ->
                            ReorderableItem(reorderState, key = "chore_${chore.id}") {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (isReorderMode) {
                                        Icon(
                                            imageVector = Icons.Default.DragHandle,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .draggableHandle(),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    ChoreCard(
                                        modifier = if (choreIndex == 0) Modifier
                                            .weight(1f)
                                            .introShowCaseTarget(
                                                index = 3,
                                                style = ShowcaseStyle.Default.copy(
                                                    backgroundColor = Color(0xFF1A237E),
                                                    backgroundAlpha = 0.95f,
                                                    targetCircleColor = Color.White
                                                ),
                                                content = {
                                                    Column {
                                                        Text(
                                                            text = stringResource(R.string.tour_chores_card_title),
                                                            color = Color.White,
                                                            fontSize = 18.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        Text(
                                                            text = stringResource(R.string.tour_chores_card_body),
                                                            color = Color.White,
                                                            fontSize = 14.sp
                                                        )
                                                    }
                                                }
                                            )
                                        else Modifier.weight(1f),
                                        chore = chore,
                                        members = emptyList(),
                                        onMarkComplete = {
                                            expandedChoreId = null
                                            authState.family?.id?.let { familyId ->
                                                choreViewModel.updateChoreStatus(
                                                    chore.id,
                                                    ChoreStatus.COMPLETED,
                                                    familyId
                                                )
                                            }
                                        },
                                        onEdit = null,
                                        onRevert = null,
                                        onApprove = null,
                                        onAssign = null,
                                        isParent = isParent,
                                        isExpanded = chore.id == expandedChoreId,
                                        onExpand = { onToggleExpanded(chore.id) },
                                        showDueDateIndicator = dueDateIndicatorsEnabled
                                    )
                                }
                            }
                        }
                    }
                    
                    // CHILD VIEW: Unassigned Chores Section
                    if (!isParent && unassignedChores.isNotEmpty()) {
                        item(key = "header_unassigned") {
                            SectionHeader(
                                text = "🎯 Unassigned Chores (${unassignedChores.size})",
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        
                        items(unassignedChores, key = { "unassigned_${it.id}" }) { chore ->
                            ChoreCard(
                                chore = chore,
                                members = if (currentMember != null) listOf(currentMember) else emptyList(),
                                onMarkComplete = null,
                                onEdit = null,
                                onRevert = null,
                                onApprove = null,
                                onAssign = { assignTo ->
                                    authState.family?.id?.let { familyId ->
                                        val assignedUserId = memberState.members
                                            .find { "${it.avatarEmoji} ${it.name}" == assignTo }?.userId ?: ""
                                        val updatedChore = chore.copy(assignedTo = assignTo, assignedToUserId = assignedUserId)
                                        choreViewModel.updateChore(updatedChore)
                                    }
                                },
                                isParent = isParent,
                                isExpanded = chore.id == expandedChoreId,
                                onExpand = { onToggleExpanded(chore.id) },
                                showDueDateIndicator = dueDateIndicatorsEnabled
                            )
                        }
                    }
                    
                    // Pending Approval Section
                    if (pendingApprovalChores.isNotEmpty()) {
                        item(key = "header_pending") {
                            SectionHeader(
                                text = "⏳ Pending Approval (${pendingApprovalChores.size})",
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        
                        items(pendingApprovalChores, key = { "pending_${it.id}" }) { chore ->
                            ChoreCard(
                                chore = chore,
                                members = memberState.members.filter { it.role == com.jmp.pocketmoneyapp.data.model.MemberRole.CHILD },
                                onMarkComplete = null,
                                onEdit = if (isParent) { { onEditChore(chore) } } else null,
                                onRevert = if (isParent) {
                                    {
                                        authState.family?.id?.let { familyId ->
                                            choreViewModel.updateChoreStatus(
                                                chore.id,
                                                ChoreStatus.PENDING,
                                                familyId
                                            )
                                        }
                                    }
                                } else null,
                                onApprove = if (isParent) {
                                    {
                                        authState.family?.id?.let { familyId ->
                                            choreViewModel.updateChoreStatus(
                                                chore.id,
                                                ChoreStatus.APPROVED,
                                                familyId
                                            )
                                        }
                                    }
                                } else null,
                                onAssign = null,
                                isParent = isParent,
                                isExpanded = chore.id == expandedChoreId,
                                onExpand = { onToggleExpanded(chore.id) },
                                showDueDateIndicator = dueDateIndicatorsEnabled
                            )
                        }
                    }
                }
            }
            
            // Error message
            choreState.error?.let { error ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { choreViewModel.clearError() }) {
                            Text(stringResource(R.string.dismiss))
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
        }
    }
    } // end IntroShowcase
}

package com.jmp.pocketmoneyapp.ui.familymembers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import com.jmp.pocketmoneyapp.R
import com.jmp.pocketmoneyapp.data.repository.PreferencesManager
import com.jmp.pocketmoneyapp.ui.components.AppTopBar
import kotlinx.coroutines.launch
import com.jmp.pocketmoneyapp.data.model.FamilyMember
import com.jmp.pocketmoneyapp.data.model.MemberRole
import com.jmp.pocketmoneyapp.data.model.Transaction
import com.jmp.pocketmoneyapp.data.model.TransactionType
import com.jmp.pocketmoneyapp.data.repository.TransactionRepository
import com.jmp.pocketmoneyapp.viewmodel.AuthViewModel
import com.jmp.pocketmoneyapp.viewmodel.BalanceViewModel
import com.jmp.pocketmoneyapp.viewmodel.FamilyMemberViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyMembersScreen(
    authViewModel: AuthViewModel,
    memberViewModel: FamilyMemberViewModel,
    balanceViewModel: BalanceViewModel,
    onAddMemberClick: () -> Unit,
    onAddTransactionClick: () -> Unit,
    onEditMember: (FamilyMember) -> Unit,
    onViewMemberTransactions: (FamilyMember) -> Unit,
    onNavigateBack: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val memberState by memberViewModel.memberState.collectAsState()
    val balanceState by balanceViewModel.balanceState.collectAsState()

    val isParent = authState.user?.role == com.jmp.pocketmoneyapp.data.model.UserRole.PARENT

    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }
    var showTour by remember { mutableStateOf(!prefs.hasSeenFamilyMembersTour) }

    // Filter members based on user role
    val visibleMembers = if (isParent) {
        memberState.members
    } else {
        // Children only see themselves
        memberState.members.filter { it.userId == authState.user?.id }
    }
    
    var expandedMemberId by remember { mutableStateOf<String?>(null) }
    var memberBalances by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
    val transactionRepository = remember { TransactionRepository() }
    
    // Load family members when family is available
    LaunchedEffect(authState.family?.id) {
        authState.family?.id?.let { familyId ->
            memberViewModel.loadFamilyMembers(familyId)
        }
    }
    
    // Load balance for each member individually with real-time updates
    LaunchedEffect(visibleMembers.map { it.id }) {
        visibleMembers.forEach { member ->
            this.launch {
                // Listen to transactions and calculate balance in real-time
                transactionRepository.getMemberTransactions(member.id).collect { transactions ->
                    // Calculate balance from transactions
                    val balance = transactions.sumOf { it.amount }
                    memberBalances = memberBalances + (member.id to balance)
                }
            }
        }
    }
    
    IntroShowcase(
        showIntroShowCase = showTour && isParent,
        onShowCaseCompleted = {
            prefs.hasSeenFamilyMembersTour = true
            showTour = false
        }
    ) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = { Text(stringResource(R.string.family_members_title)) },
                onNavigateBack = onNavigateBack,
                actions = {
                    // Only parents can add transactions
                    if (isParent) {
                        IconButton(onClick = onAddTransactionClick) {
                            Icon(
                                Icons.Default.AttachMoney,
                                stringResource(R.string.balance_add_transaction),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.introShowCaseTarget(
                                    index = 0,
                                    style = ShowcaseStyle.Default.copy(
                                        backgroundColor = Color(0xFF1B5E20),
                                        backgroundAlpha = 0.95f,
                                        targetCircleColor = Color.White
                                    ),
                                    content = {
                                        Column {
                                            Text(
                                                text = stringResource(R.string.tour_family_members_money_title),
                                                color = Color.White,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = stringResource(R.string.tour_family_members_money_body),
                                                color = Color.White,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                )
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            // Only parents can add family members
            if (isParent) {
                FloatingActionButton(onClick = onAddMemberClick) {
                    Icon(Icons.Default.Add, "Add member")
                }
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = memberState.isLoading,
            onRefresh = {
                authState.family?.id?.let { familyId ->
                    memberViewModel.loadFamilyMembers(familyId)
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
            if (memberState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (visibleMembers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "👥",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isParent) stringResource(R.string.family_members_empty) else "Loading...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isParent) stringResource(R.string.family_members_add_hint) else "Please wait",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Parents section
                    val parents = visibleMembers.filter { it.role == MemberRole.PARENT }
                    if (parents.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.member_section_parents),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(parents) { member ->
                            MemberCardWithBalance(
                                member = member,
                                isExpanded = expandedMemberId == member.id,
                                currentBalance = memberBalances[member.id] ?: 0.0,
                                detailedBalance = if (expandedMemberId == member.id) balanceState.balance else 0.0,
                                breakdown = if (expandedMemberId == member.id) balanceState.breakdown else null,
                                transactions = if (expandedMemberId == member.id) balanceState.transactions else emptyList(),
                                onToggleExpand = {
                                    if (expandedMemberId == member.id) {
                                        expandedMemberId = null
                                    } else {
                                        expandedMemberId = member.id
                                        balanceViewModel.loadMemberBalance(member.id)
                                    }
                                },
                                onEdit = if (isParent) { { onEditMember(member) } } else null,
                                onViewAllTransactions = { onViewMemberTransactions(member) }
                            )
                        }
                    }
                    
                    // Children section
                    val children = visibleMembers.filter { it.role == MemberRole.CHILD }
                    if (children.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.member_section_children),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                        items(children) { member ->
                            MemberCardWithBalance(
                                member = member,
                                isExpanded = expandedMemberId == member.id,
                                currentBalance = memberBalances[member.id] ?: 0.0,
                                detailedBalance = if (expandedMemberId == member.id) balanceState.balance else 0.0,
                                breakdown = if (expandedMemberId == member.id) balanceState.breakdown else null,
                                transactions = if (expandedMemberId == member.id) balanceState.transactions else emptyList(),
                                onToggleExpand = {
                                    if (expandedMemberId == member.id) {
                                        expandedMemberId = null
                                    } else {
                                        expandedMemberId = member.id
                                        balanceViewModel.loadMemberBalance(member.id)
                                    }
                                },
                                onEdit = if (isParent) { { onEditMember(member) } } else null,
                                onViewAllTransactions = { onViewMemberTransactions(member) }
                            )
                        }
                    }
                }
            }
        }
        }
    }
    } // end IntroShowcase
}

@Composable
fun MemberCardWithBalance(
    member: FamilyMember,
    isExpanded: Boolean,
    currentBalance: Double,
    detailedBalance: Double,
    breakdown: com.jmp.pocketmoneyapp.data.repository.EarningsBreakdown?,
    transactions: List<Transaction>,
    onToggleExpand: () -> Unit,
    onEdit: (() -> Unit)?,
    onViewAllTransactions: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (member.role == MemberRole.PARENT)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Member header (always visible)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (member.role == MemberRole.CHILD)
                            Modifier.clickable { onToggleExpand() }
                        else
                            Modifier
                    )
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = member.avatarEmoji,
                        fontSize = 32.sp
                    )
                    Column {
                        Text(
                            text = member.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (member.role == MemberRole.PARENT)
                                stringResource(R.string.member_role_parent)
                            else
                                stringResource(R.string.member_role_child),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Balance display - only for children
                    if (member.role == MemberRole.CHILD) {
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = String.format("%.2f kr.", currentBalance),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.balance_current),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Only show edit button for parents
                    if (onEdit != null) {
                        IconButton(onClick = onEdit) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = stringResource(R.string.edit)
                            )
                        }
                    }
                    
                    // Only show expand/collapse for children (who have balances)
                    if (member.role == MemberRole.CHILD) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse" else "Expand"
                        )
                    }
                }
            }
            
            // Expandable balance section - only for children
            AnimatedVisibility(
                visible = isExpanded && member.role == MemberRole.CHILD,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(top = 0.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))
                    
                    // Balance
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.balance_current),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format("%.2f kr.", detailedBalance),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                    
                    // Breakdown
                    if (breakdown != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.balance_breakdown),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        BreakdownRow(
                            label = stringResource(R.string.balance_chore_earnings),
                            amount = breakdown.choreEarnings,
                            color = MaterialTheme.colorScheme.primary
                        )
                        BreakdownRow(
                            label = stringResource(R.string.balance_allowance),
                            amount = breakdown.allowanceEarnings,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        if (breakdown.bonusEarnings != 0.0) {
                            BreakdownRow(
                                label = stringResource(R.string.balance_bonus),
                                amount = breakdown.bonusEarnings,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        if (breakdown.spending != 0.0) {
                            BreakdownRow(
                                label = stringResource(R.string.balance_spending),
                                amount = breakdown.spending,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        if (breakdown.adjustments != 0.0) {
                            BreakdownRow(
                                label = stringResource(R.string.balance_adjustments),
                                amount = breakdown.adjustments,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Recent transactions
                    if (transactions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.balance_transaction_history),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        transactions.take(5).forEach { transaction ->
                            TransactionRow(transaction)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        
                        if (transactions.size > 5) {
                            Text(
                                text = "... and ${transactions.size - 5} more",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        
                        // View All button
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onViewAllTransactions,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.balance_view_all_transactions))
                        }
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.balance_no_transactions),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BreakdownRow(label: String, amount: Double, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp)
        Text(
            text = String.format("%.2f kr.", amount),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun TransactionRow(transaction: Transaction) {
    val dateFormat = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }
    
    val typeString = when (transaction.type) {
        TransactionType.CHORE_APPROVED -> stringResource(R.string.transaction_type_chore_approved)
        TransactionType.CHORE_PAID -> stringResource(R.string.transaction_type_chore_paid)
        TransactionType.ALLOWANCE -> stringResource(R.string.transaction_type_allowance)
        TransactionType.SPENDING -> stringResource(R.string.transaction_type_spending)
        TransactionType.ADJUSTMENT -> stringResource(R.string.transaction_type_adjustment)
        TransactionType.BONUS -> stringResource(R.string.transaction_type_bonus)
        TransactionType.BANK_TRANSFER -> stringResource(R.string.transaction_type_bank_transfer)
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = typeString,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            if (transaction.description.isNotEmpty()) {
                Text(
                    text = transaction.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = dateFormat.format(transaction.createdAt.toDate()),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Text(
            text = String.format("%+.2f kr.", transaction.amount),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (transaction.amount >= 0) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.error
        )
    }
}

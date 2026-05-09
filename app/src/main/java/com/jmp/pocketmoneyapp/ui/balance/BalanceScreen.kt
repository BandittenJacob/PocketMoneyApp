package com.jmp.pocketmoneyapp.ui.balance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jmp.pocketmoneyapp.R
import com.jmp.pocketmoneyapp.ui.components.AppTopBar
import com.jmp.pocketmoneyapp.data.model.Transaction
import com.jmp.pocketmoneyapp.data.model.TransactionType
import com.jmp.pocketmoneyapp.viewmodel.AuthViewModel
import com.jmp.pocketmoneyapp.viewmodel.BalanceViewModel
import com.jmp.pocketmoneyapp.viewmodel.FamilyMemberViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceScreen(
    authViewModel: AuthViewModel,
    memberViewModel: FamilyMemberViewModel,
    balanceViewModel: BalanceViewModel,
    onAddTransactionClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val memberState by memberViewModel.memberState.collectAsState()
    val balanceState by balanceViewModel.balanceState.collectAsState()
    
    var selectedMemberId by remember { mutableStateOf<String?>(null) }
    var showMemberMenu by remember { mutableStateOf(false) }
    
    // Load family members
    LaunchedEffect(authState.family?.id) {
        authState.family?.id?.let { familyId ->
            memberViewModel.loadFamilyMembers(familyId)
        }
    }
    
    // Load balance when member is selected
    LaunchedEffect(selectedMemberId) {
        selectedMemberId?.let { memberId ->
            balanceViewModel.loadMemberBalance(memberId)
        }
    }
    
    val selectedMember = memberState.members.find { it.id == selectedMemberId }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = { Text(stringResource(R.string.balance_title)) },
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            if (selectedMemberId != null) {
                FloatingActionButton(onClick = onAddTransactionClick) {
                    Icon(Icons.Default.Add, stringResource(R.string.balance_add_transaction))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Member selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showMemberMenu = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedMember?.let { "${it.avatarEmoji} ${it.name}" } 
                            ?: stringResource(R.string.balance_select_member),
                        fontSize = 15.sp
                    )
                    Icon(Icons.Default.ArrowDropDown, null)
                }
                
                DropdownMenu(
                    expanded = showMemberMenu,
                    onDismissRequest = { showMemberMenu = false }
                ) {
                    memberState.members.forEach { member ->
                        DropdownMenuItem(
                            text = { Text("${member.avatarEmoji} ${member.name}") },
                            onClick = {
                                selectedMemberId = member.id
                                showMemberMenu = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (selectedMemberId != null) {
                // Balance card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.balance_current),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = String.format("%.2f kr.", balanceState.balance),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Earnings breakdown
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.balance_breakdown),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        BreakdownRow(
                            label = stringResource(R.string.balance_chore_earnings),
                            amount = balanceState.breakdown.choreEarnings,
                            color = MaterialTheme.colorScheme.primary
                        )
                        BreakdownRow(
                            label = stringResource(R.string.balance_allowance),
                            amount = balanceState.breakdown.allowanceEarnings,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        BreakdownRow(
                            label = stringResource(R.string.balance_bonus),
                            amount = balanceState.breakdown.bonusEarnings,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        if (balanceState.breakdown.spending != 0.0) {
                            BreakdownRow(
                                label = stringResource(R.string.balance_spending),
                                amount = balanceState.breakdown.spending,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        if (balanceState.breakdown.adjustments != 0.0) {
                            BreakdownRow(
                                label = stringResource(R.string.balance_adjustments),
                                amount = balanceState.breakdown.adjustments,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Transaction history
                Text(
                    text = stringResource(R.string.balance_transaction_history),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (balanceState.transactions.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "💰",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.balance_no_transactions),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.balance_empty_hint),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(balanceState.transactions) { transaction ->
                            TransactionCard(transaction)
                        }
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
            .padding(vertical = 4.dp),
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
fun TransactionCard(transaction: Transaction) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    
    val typeString = when (transaction.type) {
        TransactionType.CHORE_APPROVED -> stringResource(R.string.transaction_type_chore_approved)
        TransactionType.CHORE_PAID -> stringResource(R.string.transaction_type_chore_paid)
        TransactionType.ALLOWANCE -> stringResource(R.string.transaction_type_allowance)
        TransactionType.SPENDING -> stringResource(R.string.transaction_type_spending)
        TransactionType.ADJUSTMENT -> stringResource(R.string.transaction_type_adjustment)
        TransactionType.BONUS -> stringResource(R.string.transaction_type_bonus)
        TransactionType.BANK_TRANSFER -> stringResource(R.string.transaction_type_bank_transfer)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (transaction.type) {
                TransactionType.CHORE_APPROVED, TransactionType.CHORE_PAID -> 
                    MaterialTheme.colorScheme.primaryContainer
                TransactionType.ALLOWANCE -> 
                    MaterialTheme.colorScheme.secondaryContainer
                TransactionType.SPENDING, TransactionType.BANK_TRANSFER -> 
                    MaterialTheme.colorScheme.errorContainer
                else -> 
                    MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = typeString,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                if (transaction.description.isNotEmpty()) {
                    Text(
                        text = transaction.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                transaction.choreName?.let { choreName ->
                    Text(
                        text = "📝 $choreName",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = dateFormat.format(transaction.createdAt.toDate()),
                    fontSize = 12.sp,
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
}

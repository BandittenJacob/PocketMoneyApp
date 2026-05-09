package com.jmp.pocketmoneyapp.ui.balance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.jmp.pocketmoneyapp.data.model.FamilyMember
import com.jmp.pocketmoneyapp.data.model.Transaction
import com.jmp.pocketmoneyapp.data.model.TransactionType
import com.jmp.pocketmoneyapp.viewmodel.BalanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    member: FamilyMember,
    balanceViewModel: BalanceViewModel,
    onNavigateBack: () -> Unit
) {
    val balanceState by balanceViewModel.balanceState.collectAsState()
    
    // Load transactions when screen opens
    LaunchedEffect(member.id) {
        balanceViewModel.loadMemberBalance(member.id)
    }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = member.avatarEmoji,
                            fontSize = 12.sp
                        )
                        Text(stringResource(R.string.transaction_history_title, member.name))
                    }
                },
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Balance header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
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
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Breakdown summary
            if (balanceState.breakdown != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.balance_breakdown),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        BreakdownRow(
                            label = stringResource(R.string.balance_chore_earnings),
                            amount = balanceState.breakdown!!.choreEarnings,
                            color = MaterialTheme.colorScheme.primary
                        )
                        BreakdownRow(
                            label = stringResource(R.string.balance_allowance),
                            amount = balanceState.breakdown!!.allowanceEarnings,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        if (balanceState.breakdown!!.bonusEarnings != 0.0) {
                            BreakdownRow(
                                label = stringResource(R.string.balance_bonus),
                                amount = balanceState.breakdown!!.bonusEarnings,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        if (balanceState.breakdown!!.spending != 0.0) {
                            BreakdownRow(
                                label = stringResource(R.string.balance_spending),
                                amount = balanceState.breakdown!!.spending,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        if (balanceState.breakdown!!.adjustments != 0.0) {
                            BreakdownRow(
                                label = stringResource(R.string.balance_adjustments),
                                amount = balanceState.breakdown!!.adjustments,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Transactions list
            if (balanceState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (balanceState.transactions.isEmpty()) {
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
                            text = "📝",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.balance_no_transactions),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.balance_all_transactions, balanceState.transactions.size),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(balanceState.transactions) { transaction ->
                            TransactionCard(transaction)
                        }
                        
                        // Bottom spacing
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

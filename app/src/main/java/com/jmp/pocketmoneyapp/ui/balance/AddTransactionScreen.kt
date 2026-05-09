package com.jmp.pocketmoneyapp.ui.balance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import com.jmp.pocketmoneyapp.R
import com.jmp.pocketmoneyapp.ui.components.AppTopBar
import com.jmp.pocketmoneyapp.data.model.Transaction
import com.jmp.pocketmoneyapp.data.model.TransactionType
import com.jmp.pocketmoneyapp.viewmodel.AuthViewModel
import com.jmp.pocketmoneyapp.viewmodel.BalanceViewModel
import com.jmp.pocketmoneyapp.viewmodel.FamilyMemberViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    authViewModel: AuthViewModel,
    memberViewModel: FamilyMemberViewModel,
    balanceViewModel: BalanceViewModel,
    onNavigateBack: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val memberState by memberViewModel.memberState.collectAsState()

    var selectedMemberId by remember { mutableStateOf<String?>(null) }
    var showMemberMenu by remember { mutableStateOf(false) }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.BONUS) }
    var showTypeMenu by remember { mutableStateOf(false) }
    // For Correction type: whether it adds (+) or removes (−) from balance
    var correctionIsPositive by remember { mutableStateOf(true) }

    LaunchedEffect(authState.family?.id) {
        authState.family?.id?.let { familyId ->
            memberViewModel.loadFamilyMembers(familyId)
        }
    }

    val selectedMember = memberState.members.find { it.id == selectedMemberId }
    val focusManager = LocalFocusManager.current
    val descriptionFocusRequester = remember { FocusRequester() }

    val manualTransactionTypes = listOf(
        TransactionType.BONUS,
        TransactionType.ADJUSTMENT,
        TransactionType.BANK_TRANSFER
    )

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            AppTopBar(
                title = { Text(stringResource(R.string.add_transaction_title)) },
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
            // Member selector
            Text(
                text = stringResource(R.string.transaction_member),
                style = MaterialTheme.typography.labelLarge
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showMemberMenu = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = selectedMember?.let { "${it.avatarEmoji} ${it.name}" }
                            ?: stringResource(R.string.balance_select_member)
                    )
                    Icon(Icons.Default.ArrowDropDown, null)
                }
                DropdownMenu(
                    expanded = showMemberMenu,
                    onDismissRequest = { showMemberMenu = false }
                ) {
                    memberState.members.filter { it.role == com.jmp.pocketmoneyapp.data.model.MemberRole.CHILD }.forEach { member ->
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

            // Transaction type selector
            Text(
                text = stringResource(R.string.transaction_type),
                style = MaterialTheme.typography.labelLarge
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showTypeMenu = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = getTransactionTypeString(selectedType))
                    Icon(Icons.Default.ArrowDropDown, null)
                }
                DropdownMenu(
                    expanded = showTypeMenu,
                    onDismissRequest = { showTypeMenu = false }
                ) {
                    manualTransactionTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(getTransactionTypeString(type)) },
                            onClick = {
                                selectedType = type
                                showTypeMenu = false
                            }
                        )
                    }
                }
            }

            // Effect on balance indicator
            when (selectedType) {
                TransactionType.BONUS -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("＋", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = stringResource(R.string.transaction_effect_adds),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                TransactionType.BANK_TRANSFER -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("－", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error)
                            Text(
                                text = stringResource(R.string.transaction_effect_removes),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                TransactionType.ADJUSTMENT -> {
                    // Toggle: adds or removes
                    Text(
                        text = stringResource(R.string.transaction_effect_label),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilterChip(
                            selected = correctionIsPositive,
                            onClick = { correctionIsPositive = true },
                            label = {
                                Text("＋  ${stringResource(R.string.transaction_effect_adds)}")
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        FilterChip(
                            selected = !correctionIsPositive,
                            onClick = { correctionIsPositive = false },
                            label = {
                                Text("－  ${stringResource(R.string.transaction_effect_removes)}")
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        )
                    }
                }
                else -> {}
            }

            // Amount input
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text(stringResource(R.string.transaction_amount)) },
                placeholder = { Text(stringResource(R.string.transaction_amount_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { descriptionFocusRequester.requestFocus() }
                ),
                suffix = { Text("kr.") }
            )

            // Description input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.transaction_description)) },
                placeholder = { Text(stringResource(R.string.transaction_description_hint)) },
                modifier = Modifier.fillMaxWidth().focusRequester(descriptionFocusRequester),
                minLines = 2,
                maxLines = 4
            )

            Spacer(modifier = Modifier.weight(1f))

            // Save button
            Button(
                onClick = {
                    val member = selectedMember
                    val familyId = authState.family?.id
                    val userId = authState.user?.id

                    if (member != null && familyId != null && amount.isNotBlank()) {
                        val amountValue = amount.toDoubleOrNull() ?: 0.0
                        val finalAmount = when (selectedType) {
                            TransactionType.BONUS -> amountValue
                            TransactionType.BANK_TRANSFER -> -amountValue
                            TransactionType.ADJUSTMENT -> if (correctionIsPositive) amountValue else -amountValue
                            else -> amountValue
                        }

                        val transaction = Transaction(
                            familyId = familyId,
                            memberId = member.id,
                            memberName = member.name,
                            amount = finalAmount,
                            type = selectedType,
                            description = description,
                            createdAt = Timestamp.now(),
                            createdBy = userId ?: ""
                        )

                        balanceViewModel.createTransaction(transaction)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedMemberId != null && amount.isNotBlank()
            ) {
                Text(stringResource(R.string.create_transaction))
            }
        }
    }
}

@Composable
private fun getTransactionTypeString(type: TransactionType): String {
    return when (type) {
        TransactionType.ALLOWANCE -> stringResource(R.string.transaction_type_allowance)
        TransactionType.SPENDING -> stringResource(R.string.transaction_type_spending)
        TransactionType.ADJUSTMENT -> stringResource(R.string.transaction_type_adjustment)
        TransactionType.BONUS -> stringResource(R.string.transaction_type_bonus)
        TransactionType.BANK_TRANSFER -> stringResource(R.string.transaction_type_bank_transfer)
        else -> type.name
    }
}

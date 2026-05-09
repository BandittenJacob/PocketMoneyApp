package com.jmp.pocketmoneyapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jmp.pocketmoneyapp.data.model.Transaction
import com.jmp.pocketmoneyapp.data.repository.EarningsBreakdown
import com.jmp.pocketmoneyapp.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BalanceState(
    val transactions: List<Transaction> = emptyList(),
    val balance: Double = 0.0,
    val breakdown: EarningsBreakdown = EarningsBreakdown(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class BalanceViewModel : ViewModel() {
    private val repository = TransactionRepository()
    
    private val _balanceState = MutableStateFlow(BalanceState())
    val balanceState: StateFlow<BalanceState> = _balanceState.asStateFlow()
    
    private var currentMemberId: String? = null
    
    // Load transactions and balance for a family member
    fun loadMemberBalance(memberId: String) {
        if (currentMemberId == memberId && _balanceState.value.transactions.isNotEmpty()) {
            // Already loaded for this member
            return
        }
        
        currentMemberId = memberId
        _balanceState.value = _balanceState.value.copy(isLoading = true)
        
        // Load transactions (real-time)
        viewModelScope.launch {
            repository.getMemberTransactions(memberId).collect { transactions ->
                _balanceState.value = _balanceState.value.copy(
                    transactions = transactions,
                    isLoading = false
                )
                
                // Calculate balance and breakdown
                loadBreakdown(memberId)
            }
        }
    }
    
    // Load all transactions for a family
    fun loadFamilyTransactions(familyId: String) {
        _balanceState.value = _balanceState.value.copy(isLoading = true)
        
        viewModelScope.launch {
            repository.getFamilyTransactions(familyId).collect { transactions ->
                _balanceState.value = _balanceState.value.copy(
                    transactions = transactions,
                    isLoading = false
                )
            }
        }
    }
    
    // Load earnings breakdown
    private fun loadBreakdown(memberId: String) {
        viewModelScope.launch {
            val result = repository.getEarningsBreakdown(memberId)
            result.fold(
                onSuccess = { breakdown ->
                    _balanceState.value = _balanceState.value.copy(
                        breakdown = breakdown,
                        balance = breakdown.totalBalance
                    )
                },
                onFailure = { error ->
                    _balanceState.value = _balanceState.value.copy(
                        error = error.message
                    )
                }
            )
        }
    }
    
    // Create a new transaction
    fun createTransaction(transaction: Transaction) {
        viewModelScope.launch {
            _balanceState.value = _balanceState.value.copy(isLoading = true)
            val result = repository.createTransaction(transaction)
            result.fold(
                onSuccess = {
                    _balanceState.value = _balanceState.value.copy(isLoading = false)
                    // Transactions will update via real-time listener
                },
                onFailure = { error ->
                    _balanceState.value = _balanceState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }
    
    // Delete a transaction
    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            _balanceState.value = _balanceState.value.copy(isLoading = true)
            val result = repository.deleteTransaction(transactionId)
            result.fold(
                onSuccess = {
                    _balanceState.value = _balanceState.value.copy(isLoading = false)
                    // Transactions will update via real-time listener
                },
                onFailure = { error ->
                    _balanceState.value = _balanceState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }
    
    fun clearError() {
        _balanceState.value = _balanceState.value.copy(error = null)
    }
}

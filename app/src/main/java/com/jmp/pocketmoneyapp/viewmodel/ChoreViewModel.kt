package com.jmp.pocketmoneyapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.jmp.pocketmoneyapp.data.model.Chore
import com.jmp.pocketmoneyapp.data.model.ChoreStatus
import com.jmp.pocketmoneyapp.data.model.Transaction
import com.jmp.pocketmoneyapp.data.model.TransactionType
import com.jmp.pocketmoneyapp.data.repository.ChoreOrderRepository
import com.jmp.pocketmoneyapp.data.repository.ChoreRepository
import com.jmp.pocketmoneyapp.data.repository.FamilyMemberRepository
import com.jmp.pocketmoneyapp.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ChoreState(
    val chores: List<Chore> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ChoreViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ChoreRepository()
    private val memberRepository = FamilyMemberRepository()
    private val transactionRepository = TransactionRepository()
    private val choreOrderRepository = ChoreOrderRepository(application)

    private val _choreState = MutableStateFlow(ChoreState())
    val choreState: StateFlow<ChoreState> = _choreState.asStateFlow()

    // Remembered across internal reloads (create/update/delete) so the order is preserved
    private var lastUserId: String = ""
    
    // Store chore being edited temporarily
    private var _choreToEdit: Chore? = null
    val choreToEdit: Chore?
        get() = _choreToEdit
    
    fun setChoreToEdit(chore: Chore?) {
        _choreToEdit = chore
    }
    
    /**
     * Load chores for a family, applying the user's local sort order to incomplete chores.
     * [userId] is used to look up the saved order; if empty, the last known userId is used.
     */
    fun loadFamilyChores(familyId: String, userId: String = lastUserId) {
        if (userId.isNotBlank()) lastUserId = userId
        viewModelScope.launch {
            _choreState.value = _choreState.value.copy(isLoading = true, error = null)

            val result = repository.getFamilyChores(familyId)
            result.fold(
                onSuccess = { chores ->
                    val incompleteStatuses = setOf(ChoreStatus.PENDING, ChoreStatus.IN_PROGRESS)
                    val incompleteChores = chores.filter { it.status in incompleteStatuses }
                    val otherChores = chores
                        .filter { it.status !in incompleteStatuses }
                        .sortedBy { it.createdAt }

                    val orderedIncomplete = if (lastUserId.isNotBlank()) {
                        val savedOrder = choreOrderRepository.getOrderForUser(lastUserId)
                        val orderedIds = choreOrderRepository.reconcile(
                            savedOrder,
                            incompleteChores.map { it.id }.toSet()
                        )
                        orderedIds.mapNotNull { id -> incompleteChores.find { it.id == id } }
                    } else {
                        incompleteChores.sortedBy { it.createdAt }
                    }

                    _choreState.value = _choreState.value.copy(
                        chores = orderedIncomplete + otherChores,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _choreState.value = _choreState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }

    /**
     * Persist the user's preferred order for incomplete chores to local DataStore.
     */
    fun saveChoreOrder(userId: String, orderedIds: List<String>) {
        viewModelScope.launch {
            choreOrderRepository.saveOrderForUser(userId, orderedIds)
        }
    }
    
    /**
     * Load chores for a specific user
     */
    fun loadUserChores(userId: String) {
        viewModelScope.launch {
            _choreState.value = _choreState.value.copy(isLoading = true, error = null)

            val result = repository.getUserChores(userId)
            result.fold(
                onSuccess = { chores ->
                    _choreState.value = _choreState.value.copy(
                        chores = chores.sortedBy { it.createdAt },
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _choreState.value = _choreState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }
    
    /**
     * Create a new chore
     */
    fun createChore(chore: Chore) {
        viewModelScope.launch {
            _choreState.value = _choreState.value.copy(isLoading = true, error = null)
            
            val result = repository.createChore(chore)
            result.fold(
                onSuccess = {
                    // Reload chores after creating
                    loadFamilyChores(chore.familyId)
                },
                onFailure = { error ->
                    _choreState.value = _choreState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }
    
    /**
     * Update an existing chore
     */
    fun updateChore(chore: Chore) {
        viewModelScope.launch {
            _choreState.value = _choreState.value.copy(isLoading = true, error = null)
            
            val result = repository.updateChore(chore)
            result.fold(
                onSuccess = {
                    // Reload chores after updating
                    loadFamilyChores(chore.familyId)
                },
                onFailure = { error ->
                    _choreState.value = _choreState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }
    
    /**
     * Update chore status
     */
    fun updateChoreStatus(choreId: String, status: ChoreStatus, familyId: String) {
        viewModelScope.launch {
            // First, get the chore to access its details
            val chore = _choreState.value.chores.find { it.id == choreId }
            
            // Check if we're reverting an approved chore (going from APPROVED/PAID to PENDING)
            val isRevertingApprovedChore = chore != null && 
                (chore.status == ChoreStatus.APPROVED || 
                 chore.status == ChoreStatus.PAID) &&
                status == ChoreStatus.PENDING
            
            val result = repository.updateChoreStatus(choreId, status)
            result.fold(
                onSuccess = {
                    // If reverting an approved chore, delete the transaction
                    if (isRevertingApprovedChore) {
                        deleteChoreTransaction(choreId)
                    }
                    
                    // If status is APPROVED and chore has assignedTo, create a transaction
                    // (Transaction is only created when parent approves, not when child completes)
                    if (status == ChoreStatus.APPROVED && chore != null && chore.assignedTo.isNotEmpty()) {
                        createChoreTransaction(chore, familyId)
                    }
                    // Reload chores after update
                    loadFamilyChores(familyId)
                },
                onFailure = { error ->
                    _choreState.value = _choreState.value.copy(
                        error = error.message
                    )
                }
            )
        }
    }
    
    /**
     * Create a transaction when a chore is completed
     */
    private suspend fun createChoreTransaction(chore: Chore, familyId: String) {
        try {
            // Get family members to find the member ID
            val members = memberRepository.getFamilyMembers(familyId).first()
            
            // Find the member by matching the assignedTo string (format: "👧 Emma")
            val member = members.find { member ->
                "${member.avatarEmoji} ${member.name}" == chore.assignedTo
            }
            
            if (member != null) {
                // Create transaction
                val transaction = Transaction(
                    familyId = familyId,
                    memberId = member.id,
                    memberName = member.name,
                    amount = chore.value,
                    type = TransactionType.CHORE_APPROVED,
                    description = chore.name,
                    choreId = chore.id,
                    choreName = chore.name,
                    createdAt = Timestamp.now(),
                    createdBy = "" // Could be set to parent's userId if needed
                )
                
                transactionRepository.createTransaction(transaction)
            }
        } catch (e: Exception) {
            // Log error but don't fail the chore update
            _choreState.value = _choreState.value.copy(
                error = "Chore completed but failed to create transaction: ${e.message}"
            )
        }
    }
    
    /**
     * Delete transaction(s) when a chore is reverted
     */
    private suspend fun deleteChoreTransaction(choreId: String) {
        try {
            transactionRepository.deleteTransactionsByChoreId(choreId)
        } catch (e: Exception) {
            // Log error but don't fail the chore update
            _choreState.value = _choreState.value.copy(
                error = "Chore reverted but failed to delete transaction: ${e.message}"
            )
        }
    }
    
    /**
     * Delete a chore
     */
    fun deleteChore(choreId: String, familyId: String) {
        viewModelScope.launch {
            // First, delete any associated transactions
            transactionRepository.deleteTransactionsByChoreId(choreId)
            
            val result = repository.deleteChore(choreId)
            result.fold(
                onSuccess = {
                    // Reload chores after deletion
                    loadFamilyChores(familyId)
                },
                onFailure = { error ->
                    _choreState.value = _choreState.value.copy(
                        error = error.message
                    )
                }
            )
        }
    }
    
    fun clearError() {
        _choreState.value = _choreState.value.copy(error = null)
    }
}

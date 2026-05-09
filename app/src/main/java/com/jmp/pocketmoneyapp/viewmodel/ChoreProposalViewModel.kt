package com.jmp.pocketmoneyapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jmp.pocketmoneyapp.data.model.ChoreProposal
import com.jmp.pocketmoneyapp.data.model.ProposalStatus
import com.jmp.pocketmoneyapp.data.repository.ChoreProposalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProposalState(
    val proposals: List<ChoreProposal> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ChoreProposalViewModel : ViewModel() {
    private val repository = ChoreProposalRepository()

    private val _state = MutableStateFlow(ProposalState())
    val state: StateFlow<ProposalState> = _state.asStateFlow()

    // Holds a proposal being accepted-with-edits so NavGraph can pass it to AddChoreScreen
    private var _proposalToAccept: ChoreProposal? = null
    val proposalToAccept: ChoreProposal? get() = _proposalToAccept
    fun setProposalToAccept(p: ChoreProposal?) { _proposalToAccept = p }

    fun loadForFamily(familyId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            repository.getProposalsForFamily(familyId).fold(
                onSuccess = { list ->
                    _state.value = _state.value.copy(
                        proposals = list.sortedByDescending { it.createdAt },
                        isLoading = false
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun loadForMember(familyId: String, memberId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            repository.getProposalsForMember(familyId, memberId).fold(
                onSuccess = { list ->
                    _state.value = _state.value.copy(
                        proposals = list.sortedByDescending { it.createdAt },
                        isLoading = false
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun submitProposal(proposal: ChoreProposal, familyId: String) {
        viewModelScope.launch {
            repository.createProposal(proposal).fold(
                onSuccess = { loadForMember(familyId, proposal.proposedByMemberId) },
                onFailure = { e -> _state.value = _state.value.copy(error = e.message) }
            )
        }
    }

    fun accept(proposal: ChoreProposal, familyId: String) {
        viewModelScope.launch {
            repository.resolveProposal(proposal.id, ProposalStatus.ACCEPTED).fold(
                onSuccess = { loadForFamily(familyId) },
                onFailure = { e -> _state.value = _state.value.copy(error = e.message) }
            )
        }
    }

    fun acceptWithEdits(proposal: ChoreProposal) {
        // Just store the proposal — caller navigates to AddChoreScreen pre-filled
        _proposalToAccept = proposal
    }

    fun markAcceptedWithEdits(proposalId: String, familyId: String) {
        viewModelScope.launch {
            repository.resolveProposal(proposalId, ProposalStatus.ACCEPTED_WITH_EDITS).fold(
                onSuccess = { loadForFamily(familyId) },
                onFailure = { e -> _state.value = _state.value.copy(error = e.message) }
            )
        }
    }

    fun reject(proposal: ChoreProposal, reason: String, familyId: String) {
        viewModelScope.launch {
            repository.resolveProposal(proposal.id, ProposalStatus.REJECTED, reason).fold(
                onSuccess = { loadForFamily(familyId) },
                onFailure = { e -> _state.value = _state.value.copy(error = e.message) }
            )
        }
    }

    fun dismiss(proposal: ChoreProposal, familyId: String, memberId: String) {
        viewModelScope.launch {
            repository.deleteProposal(proposal.id).fold(
                onSuccess = { loadForMember(familyId, memberId) },
                onFailure = { e -> _state.value = _state.value.copy(error = e.message) }
            )
        }
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }
}

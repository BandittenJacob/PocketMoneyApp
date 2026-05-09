package com.jmp.pocketmoneyapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jmp.pocketmoneyapp.data.model.FamilyMember
import com.jmp.pocketmoneyapp.data.repository.FamilyMemberRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FamilyMemberState(
    val members: List<FamilyMember> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FamilyMemberViewModel : ViewModel() {
    private val repository = FamilyMemberRepository()
    
    private val _memberState = MutableStateFlow(FamilyMemberState())
    val memberState: StateFlow<FamilyMemberState> = _memberState.asStateFlow()
    
    private var loadJob: Job? = null

    var memberToEdit: FamilyMember? = null
        private set
    
    fun setMemberToEdit(member: FamilyMember?) {
        memberToEdit = member
    }
    
    fun loadFamilyMembers(familyId: String) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _memberState.value = _memberState.value.copy(isLoading = true)
            repository.getFamilyMembers(familyId).collect { members ->
                _memberState.value = FamilyMemberState(
                    members = members,
                    isLoading = false
                )
            }
        }
    }
    
    fun createMember(member: FamilyMember) {
        viewModelScope.launch {
            _memberState.value = _memberState.value.copy(isLoading = true)
            val result = repository.createMember(member)
            result.fold(
                onSuccess = {
                    _memberState.value = _memberState.value.copy(isLoading = false)
                },
                onFailure = { error ->
                    _memberState.value = _memberState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }
    
    fun updateMember(member: FamilyMember) {
        viewModelScope.launch {
            _memberState.value = _memberState.value.copy(isLoading = true)
            val result = repository.updateMember(member)
            result.fold(
                onSuccess = {
                    _memberState.value = _memberState.value.copy(isLoading = false)
                },
                onFailure = { error ->
                    _memberState.value = _memberState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }
    
    fun deleteMember(memberId: String) {
        viewModelScope.launch {
            _memberState.value = _memberState.value.copy(isLoading = true)
            val result = repository.deleteMember(memberId)
            result.fold(
                onSuccess = {
                    _memberState.value = _memberState.value.copy(isLoading = false)
                },
                onFailure = { error ->
                    _memberState.value = _memberState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }
    
    fun clearError() {
        _memberState.value = _memberState.value.copy(error = null)
    }
}

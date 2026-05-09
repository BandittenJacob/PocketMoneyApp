package com.jmp.pocketmoneyapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jmp.pocketmoneyapp.data.model.ChoreLibraryTemplate
import com.jmp.pocketmoneyapp.data.repository.ChoreLibraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChoreLibraryState(
    val templates: List<ChoreLibraryTemplate> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class ChoreLibraryViewModel : ViewModel() {
    private val repository = ChoreLibraryRepository()
    
    private val _templateState = MutableStateFlow(ChoreLibraryState())
    val templateState: StateFlow<ChoreLibraryState> = _templateState.asStateFlow()
    
    // For editing templates
    var templateToEdit: ChoreLibraryTemplate? = null
        private set
    
    fun setTemplateToEdit(template: ChoreLibraryTemplate?) {
        templateToEdit = template
    }
    
    /**
     * Load all recurring chore templates for a family
     */
    fun loadFamilyTemplates(familyId: String) {
        viewModelScope.launch {
            _templateState.value = _templateState.value.copy(isLoading = true, error = null)
            
            val result = repository.getFamilyTemplates(familyId)
            result.fold(
                onSuccess = { templates ->
                    _templateState.value = _templateState.value.copy(
                        templates = templates.sortedBy { it.createdAt },
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _templateState.value = _templateState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }
    
    /**
     * Create a new recurring chore template
     */
    fun createTemplate(template: ChoreLibraryTemplate) {
        viewModelScope.launch {
            _templateState.value = _templateState.value.copy(isLoading = true, error = null)
            
            val result = repository.createTemplate(template)
            result.fold(
                onSuccess = {
                    // Reload templates after creating
                    loadFamilyTemplates(template.familyId)
                    _templateState.value = _templateState.value.copy(
                        successMessage = "Recurring chore template created!"
                    )
                },
                onFailure = { error ->
                    _templateState.value = _templateState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }
    
    /**
     * Update an existing recurring chore template
     */
    fun updateTemplate(template: ChoreLibraryTemplate) {
        viewModelScope.launch {
            _templateState.value = _templateState.value.copy(isLoading = true, error = null)
            
            val result = repository.updateTemplate(template)
            result.fold(
                onSuccess = {
                    // Reload templates after update
                    loadFamilyTemplates(template.familyId)
                    _templateState.value = _templateState.value.copy(
                        successMessage = "Template updated!"
                    )
                },
                onFailure = { error ->
                    _templateState.value = _templateState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }
    
    /**
     * Delete a recurring chore template
     */
    fun deleteTemplate(templateId: String, familyId: String) {
        viewModelScope.launch {
            val result = repository.deleteTemplate(templateId)
            result.fold(
                onSuccess = {
                    // Reload templates after deletion
                    loadFamilyTemplates(familyId)
                    _templateState.value = _templateState.value.copy(
                        successMessage = "Template deleted"
                    )
                },
                onFailure = { error ->
                    _templateState.value = _templateState.value.copy(
                        error = error.message
                    )
                }
            )
        }
    }
    
    /**
     * Toggle template active/inactive status
     */
    fun toggleTemplateActive(templateId: String, isActive: Boolean, familyId: String) {
        viewModelScope.launch {
            // Optimistic update - update local state immediately
            val updatedTemplates = _templateState.value.templates.map { template ->
                if (template.id == templateId) {
                    template.copy(isActive = isActive)
                } else {
                    template
                }
            }
            _templateState.value = _templateState.value.copy(
                templates = updatedTemplates,
                successMessage = if (isActive) "Template activated" else "Template paused"
            )
            
            // Then update Firestore in background
            val result = repository.toggleTemplateActive(templateId, isActive)
            result.fold(
                onSuccess = {
                    // Don't reload - trust our optimistic update
                },
                onFailure = { error ->
                    // Revert optimistic update on failure by reloading from Firestore
                    loadFamilyTemplates(familyId)
                    _templateState.value = _templateState.value.copy(
                        error = error.message,
                        successMessage = null
                    )
                }
            )
        }
    }
    
    /**
     * Generate a chore instance from a template
     */
    fun generateInstance(template: ChoreLibraryTemplate) {
        viewModelScope.launch {
            // First check if there's already an active instance
            val hasActiveResult = repository.hasActiveInstance(template.id)
            hasActiveResult.fold(
                onSuccess = { hasActive ->
                    if (hasActive) {
                        _templateState.value = _templateState.value.copy(
                            error = "This chore already has an active instance"
                        )
                    } else {
                        // Generate the instance
                        val result = repository.generateInstance(template)
                        result.fold(
                            onSuccess = {
                                _templateState.value = _templateState.value.copy(
                                    successMessage = "Chore instance created!"
                                )
                                // Reload templates to update lastInstanceCreated
                                loadFamilyTemplates(template.familyId)
                            },
                            onFailure = { error ->
                                _templateState.value = _templateState.value.copy(
                                    error = error.message
                                )
                            }
                        )
                    }
                },
                onFailure = { error ->
                    _templateState.value = _templateState.value.copy(
                        error = error.message
                    )
                }
            )
        }
    }
    
    fun clearError() {
        _templateState.value = _templateState.value.copy(error = null)
    }
    
    fun clearSuccessMessage() {
        _templateState.value = _templateState.value.copy(successMessage = null)
    }
}

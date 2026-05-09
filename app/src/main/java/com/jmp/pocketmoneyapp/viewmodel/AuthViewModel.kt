package com.jmp.pocketmoneyapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.jmp.pocketmoneyapp.data.model.Family
import com.jmp.pocketmoneyapp.data.model.User
import com.jmp.pocketmoneyapp.data.repository.AuthRepository
import com.jmp.pocketmoneyapp.data.repository.CredentialManagerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val user: User? = null,
    val family: Family? = null,
    val error: String? = null,
    val needsFamilySetup: Boolean = false,
    val needsProfileCompletion: Boolean = false  // Firebase Auth exists but no Firestore user document
)

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()
    
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private var familyListener: com.google.firebase.firestore.ListenerRegistration? = null

    init {
        checkAuthState()
    }
    
    override fun onCleared() {
        super.onCleared()
        familyListener?.remove()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            val currentUser = repository.getCurrentUser()
            if (currentUser != null) {
                loadUserData(currentUser.uid)
            }
        }
    }

    private suspend fun loadUserData(userId: String) {
        _authState.value = _authState.value.copy(isLoading = true)
        
        val userResult = repository.getUserDocument(userId)
        userResult.fold(
            onSuccess = { user ->
                if (user != null) {
                    if (user.familyId.isEmpty()) {
                        // User exists but no family - need to create one
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            user = user,
                            needsFamilySetup = true,
                            needsProfileCompletion = false  // Profile is now complete
                        )
                    } else {
                        // Load family data
                        loadFamilyData(user)
                    }
                } else {
                    // User has Firebase Auth account but no Firestore document
                    // This happens if they created account but never completed PIN setup
                    // Or if their Firestore document was deleted
                    // Keep them authenticated but flag that they need to complete profile
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        needsProfileCompletion = true,
                        error = null
                    )
                }
            },
            onFailure = { error ->
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = error.message
                )
            }
        )
    }

    private suspend fun loadFamilyData(user: User) {
        val familyResult = repository.getFamily(user.familyId)
        familyResult.fold(
            onSuccess = { family ->
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    isAuthenticated = true,
                    user = user,
                    family = family,
                    needsFamilySetup = false,
                    needsProfileCompletion = false  // Profile is now complete
                )
                
                // Set up real-time listener for family changes
                familyListener?.remove()  // Remove old listener if exists
                familyListener = repository.listenToFamily(user.familyId) { updatedFamily ->
                    if (updatedFamily == null) {
                        // Family was deleted, sign out the user
                        android.util.Log.d("AuthViewModel", "Family deleted, signing out user")
                        viewModelScope.launch {
                            signOut()
                        }
                    } else {
                        // Update family data in state
                        _authState.value = _authState.value.copy(family = updatedFamily)
                    }
                }

                // Save FCM token so Cloud Functions can send notifications to this device
                viewModelScope.launch {
                    try {
                        val token = FirebaseMessaging.getInstance().token.await()
                        repository.saveFcmToken(user.id, token)
                    } catch (_: Exception) { /* Non-critical — notifications simply won't work until token is saved */ }
                }
            },
            onFailure = { error ->
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = error.message
                )
            }
        )
    }

    // Step 1: Create Firebase Auth account only (before PIN)
    fun createAuthAccount(email: String, password: String, onResult: (Boolean, String?, String?) -> Unit) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            val result = repository.signUp(email, password, "")
            result.fold(
                onSuccess = { firebaseUser ->
                    _authState.value = _authState.value.copy(isLoading = false, error = null)
                    onResult(true, firebaseUser.uid, null)
                },
                onFailure = { error ->
                    _authState.value = _authState.value.copy(isLoading = false, error = error.message)
                    onResult(false, null, error.message)
                }
            )
        }
    }

    // Step 2: Complete signup by creating Firestore document (after PIN)
    fun completeSignUp(userId: String, email: String, name: String, pin: String, password: String, context: Context) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            val createUserResult = repository.createUserDocument(
                userId = userId,
                email = email,
                name = name,
                pin = pin
            )
            createUserResult.fold(
                onSuccess = {
                    // Save credentials to Google Password Manager
                    val credentialService = CredentialManagerService(context)
                    credentialService.saveCredentials(email, password)
                    
                    loadUserData(userId)
                },
                onFailure = { error ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }

    // Complete profile for existing Firebase Auth account (no Firestore document)
    fun completeProfile(name: String, pin: String) {
        viewModelScope.launch {
            val currentUser = repository.getCurrentUser()
            if (currentUser == null) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = "Not signed in"
                )
                return@launch
            }
            
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            val createUserResult = repository.createUserDocument(
                userId = currentUser.uid,
                email = currentUser.email ?: "",
                name = name,
                pin = pin
            )
            createUserResult.fold(
                onSuccess = {
                    loadUserData(currentUser.uid)
                },
                onFailure = { error ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }

    // Complete signup with invitation
    fun completeSignUpWithInvitation(
        userId: String,
        email: String,
        name: String,
        pin: String,
        invitationCode: String,
        password: String,
        context: Context,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            // Accept invitation first
            val memberRepository = com.jmp.pocketmoneyapp.data.repository.FamilyMemberRepository()
            val acceptResult = memberRepository.acceptInvitation(invitationCode, userId)
            
            acceptResult.fold(
                onSuccess = { member ->
                    // Map MemberRole to UserRole
                    val userRole = when (member.role) {
                        com.jmp.pocketmoneyapp.data.model.MemberRole.PARENT -> com.jmp.pocketmoneyapp.data.model.UserRole.PARENT
                        com.jmp.pocketmoneyapp.data.model.MemberRole.CHILD -> com.jmp.pocketmoneyapp.data.model.UserRole.CHILD
                    }
                    
                    // Create user document with the family ID and role from the member
                    val createUserResult = repository.createUserDocument(
                        userId = userId,
                        email = email,
                        name = name,
                        pin = pin,
                        familyId = member.familyId,
                        userRole = userRole
                    )
                    createUserResult.fold(
                        onSuccess = {
                            // Save credentials
                            val credentialService = CredentialManagerService(context)
                            credentialService.saveCredentials(email, password)
                            
                            loadUserData(userId)
                            onResult(true, null)
                        },
                        onFailure = { error ->
                            _authState.value = _authState.value.copy(
                                isLoading = false,
                                error = error.message
                            )
                            onResult(false, error.message)
                        }
                    )
                },
                onFailure = { error ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                    onResult(false, error.message)
                }
            )
        }
    }

    // Delete auth account (if user goes back from PIN screen)
    fun deleteAuthAccount() {
        viewModelScope.launch {
            try {
                repository.deleteCurrentAuthUser()
            } catch (e: Exception) {
                // Silent fail - account will be orphaned but that's OK
            }
        }
    }

    suspend fun checkEmailExists(email: String): Boolean {
        val result = repository.checkEmailExists(email)
        return result.getOrDefault(false)
    }

    fun signUp(email: String, password: String, name: String, pin: String, context: Context) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            val result = repository.signUp(email, password, name)
            result.fold(
                onSuccess = { firebaseUser ->
                    // Create user document in Firestore
                    val createUserResult = repository.createUserDocument(
                        userId = firebaseUser.uid,
                        email = email,
                        name = name,
                        pin = pin
                    )
                    createUserResult.fold(
                        onSuccess = {
                            // Save credentials to Google Password Manager
                            val credentialService = CredentialManagerService(context)
                            credentialService.saveCredentials(email, password)
                            
                            loadUserData(firebaseUser.uid)
                        },
                        onFailure = { error ->
                            _authState.value = _authState.value.copy(
                                isLoading = false,
                                error = error.message
                            )
                        }
                    )
                },
                onFailure = { error ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }

    fun signIn(email: String, password: String, context: Context, credentialsFromPasswordManager: Boolean = false) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            val result = repository.signIn(email, password)
            result.fold(
                onSuccess = { firebaseUser ->
                    // Only save credentials if they weren't just retrieved from password manager
                    if (!credentialsFromPasswordManager) {
                        val credentialService = CredentialManagerService(context)
                        credentialService.saveCredentials(email, password)
                    }
                    
                    loadUserData(firebaseUser.uid)
                },
                onFailure = { error ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }
    
    /**
     * Retrieve saved credentials from Google Password Manager
     * Returns Pair of (email, password) or null if not found
     */
    suspend fun getSavedCredentials(context: Context): Pair<String, String>? {
        val credentialService = CredentialManagerService(context)
        return credentialService.getSavedCredentials()
    }

    fun createFamily(familyName: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            val userId = repository.getCurrentUser()?.uid ?: return@launch
            val result = repository.createFamily(familyName, userId)
            
            result.fold(
                onSuccess = { familyId ->
                    loadUserData(userId)
                },
                onFailure = { error ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }
    
    // Verify user's PIN
    fun verifyPin(pin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val userId = repository.getCurrentUser()?.uid
            if (userId != null) {
                val result = repository.verifyPin(userId, pin)
                result.fold(
                    onSuccess = { isValid ->
                        onResult(isValid)
                    },
                    onFailure = {
                        onResult(false)
                    }
                )
            } else {
                onResult(false)
            }
        }
    }
    
    fun updateFamilyName(newName: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val familyId = _authState.value.family?.id ?: return@launch onResult(false)
            repository.updateFamilyName(familyId, newName).fold(
                onSuccess = { onResult(true) },
                onFailure = { onResult(false) }
            )
        }
    }

    // Update user's PIN
    fun updatePin(newPin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val userId = repository.getCurrentUser()?.uid
            if (userId != null) {
                val result = repository.updatePin(userId, newPin)
                result.fold(
                    onSuccess = {
                        onResult(true)
                    },
                    onFailure = {
                        onResult(false)
                    }
                )
            } else {
                onResult(false)
            }
        }
    }

    fun signOut() {
        familyListener?.remove()  // Clean up listener
        familyListener = null
        repository.signOut()
        _authState.value = AuthState()
    }

    fun sendPasswordResetEmail(email: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            val result = repository.sendPasswordResetEmail(email)
            
            result.fold(
                onSuccess = {
                    _authState.value = _authState.value.copy(isLoading = false)
                    onResult(true, null)
                },
                onFailure = { error ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                    onResult(false, error.message)
                }
            )
        }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
    
    /**
     * Request family deletion
     */
    fun requestFamilyDeletion(onResult: (Boolean, Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val user = _authState.value.user
            val family = _authState.value.family
            
            if (user == null || family == null) {
                android.util.Log.e("AuthViewModel", "requestFamilyDeletion: user or family is null")
                onResult(false, false, "User or family not found")
                return@launch
            }
            
            android.util.Log.d("AuthViewModel", "Requesting deletion for family: ${family.id}")
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            val result = repository.requestFamilyDeletion(family.id, user.id, user.name)
            result.fold(
                onSuccess = {
                    android.util.Log.d("AuthViewModel", "Deletion request created successfully")
                    // Check if family still exists (multi-parent) or was deleted (single parent)
                    val familyCheckResult = repository.getFamily(family.id)
                    val familyDeleted = familyCheckResult.getOrNull() == null
                    
                    if (familyDeleted) {
                        // Family was deleted immediately (single parent)
                        android.util.Log.d("AuthViewModel", "Family was deleted immediately (single parent)")
                        _authState.value = AuthState()
                        onResult(true, true, null)
                    } else {
                        // Family still exists, reload data to show deletion request
                        android.util.Log.d("AuthViewModel", "Family still exists, reloading data")
                        loadFamilyData(user)
                        android.util.Log.d("AuthViewModel", "After reload - deletionRequest: ${_authState.value.family?.deletionRequest}")
                        onResult(true, false, null)
                    }
                },
                onFailure = { error ->
                    android.util.Log.e("AuthViewModel", "Failed to request deletion: ${error.message}")
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                    onResult(false, false, error.message)
                }
            )
        }
    }
    
    /**
     * Approve family deletion
     */
    fun approveFamilyDeletion(onResult: (Boolean, Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val user = _authState.value.user
            val family = _authState.value.family
            
            if (user == null || family == null) {
                onResult(false, false, "User or family not found")
                return@launch
            }
            
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            val result = repository.approveFamilyDeletion(family.id, user.id)
            result.fold(
                onSuccess = { allApproved ->
                    if (allApproved) {
                        // Family was deleted, clear state
                        _authState.value = AuthState()
                        onResult(true, true, null)
                    } else {
                        // Reload family data to get updated deletion request
                        loadFamilyData(user)
                        onResult(true, false, null)
                    }
                },
                onFailure = { error ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                    onResult(false, false, error.message)
                }
            )
        }
    }
    
    /**
     * Cancel family deletion request
     */
    fun cancelFamilyDeletion(onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val user = _authState.value.user
            val family = _authState.value.family
            
            if (user == null || family == null) {
                onResult(false, "User or family not found")
                return@launch
            }
            
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            val result = repository.cancelFamilyDeletion(family.id)
            result.fold(
                onSuccess = {
                    // Reload family data to clear deletion request
                    loadFamilyData(user)
                    onResult(true, null)
                },
                onFailure = { error ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                    onResult(false, error.message)
                }
            )
        }
    }
    
    /**
     * Sign up with an invitation code to link to an existing family member
     */
    fun signUpWithInvitation(
        email: String, 
        password: String, 
        name: String, 
        pin: String, 
        invitationCode: String, 
        context: Context,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            // First, create the Firebase auth account
            val signUpResult = repository.signUp(email, password, name)
            signUpResult.fold(
                onSuccess = { firebaseUser ->
                    val userId = firebaseUser.uid
                    
                    // Accept the invitation (links user to family member)
                    val memberRepository = com.jmp.pocketmoneyapp.data.repository.FamilyMemberRepository()
                    val acceptResult = memberRepository.acceptInvitation(invitationCode, userId)
                    
                    acceptResult.fold(
                        onSuccess = { linkedMember ->
                            // Create user document with the family ID from the invitation
                            val createUserResult = repository.createUserDocument(
                                userId = userId,
                                email = email,
                                name = name,
                                pin = pin,
                                familyId = linkedMember.familyId
                            )
                            
                            createUserResult.fold(
                                onSuccess = {
                                    // Save credentials
                                    val credentialService = CredentialManagerService(context)
                                    credentialService.saveCredentials(email, password)
                                    
                                    // Load user data
                                    loadUserData(userId)
                                    onResult(true, null)
                                },
                                onFailure = { error ->
                                    _authState.value = _authState.value.copy(
                                        isLoading = false,
                                        error = error.message
                                    )
                                    onResult(false, error.message)
                                }
                            )
                        },
                        onFailure = { error ->
                            // Failed to accept invitation, delete the auth account
                            firebaseUser.delete()
                            _authState.value = _authState.value.copy(
                                isLoading = false,
                                error = error.message
                            )
                            onResult(false, error.message)
                        }
                    )
                },
                onFailure = { error ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                    onResult(false, error.message)
                }
            )
        }
    }
    
    /**
     * Sign in with an existing account and link to a family member via invitation
     */
    fun signInWithInvitation(
        email: String,
        password: String,
        invitationCode: String,
        context: Context,
        onResult: (Boolean, String?, Boolean) -> Unit  // success, error, hadOldFamily
    ) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            // Sign in with existing credentials
            val signInResult = repository.signIn(email, password)
            signInResult.fold(
                onSuccess = { firebaseUser ->
                    val userId = firebaseUser.uid
                    
                    // Get current user data to check if already in a family
                    val userResult = repository.getUserDocument(userId)
                    userResult.fold(
                        onSuccess = { user ->
                            val oldFamilyId = user?.familyId
                            val hadOldFamily = !oldFamilyId.isNullOrEmpty()
                            
                            // Accept invitation to get new family member details
                            val memberRepository = com.jmp.pocketmoneyapp.data.repository.FamilyMemberRepository()
                            val invitationResult = memberRepository.getInvitation(invitationCode)
                            
                            invitationResult.fold(
                                onSuccess = { invitation ->
                                    if (invitation == null) {
                                        _authState.value = _authState.value.copy(
                                            isLoading = false,
                                            error = "Invalid or expired invitation"
                                        )
                                        onResult(false, "Invalid or expired invitation", false)
                                        return@launch
                                    }
                                    
                                    // Switch user to new family
                                    val switchResult = repository.switchUserToNewFamily(
                                        userId = userId,
                                        newFamilyId = invitation.familyId,
                                        newMemberId = invitation.memberId,
                                        oldFamilyId = oldFamilyId
                                    )
                                    
                                    switchResult.fold(
                                        onSuccess = {
                                            // Delete the invitation
                                            memberRepository.acceptInvitation(invitationCode, userId)
                                            
                                            // Save credentials
                                            val credentialService = CredentialManagerService(context)
                                            credentialService.saveCredentials(email, password)
                                            
                                            // Reload user data
                                            loadUserData(userId)
                                            onResult(true, null, hadOldFamily)
                                        },
                                        onFailure = { error ->
                                            _authState.value = _authState.value.copy(
                                                isLoading = false,
                                                error = error.message
                                            )
                                            onResult(false, error.message, hadOldFamily)
                                        }
                                    )
                                },
                                onFailure = { error ->
                                    _authState.value = _authState.value.copy(
                                        isLoading = false,
                                        error = error.message
                                    )
                                    onResult(false, error.message, hadOldFamily)
                                }
                            )
                        },
                        onFailure = { error ->
                            _authState.value = _authState.value.copy(
                                isLoading = false,
                                error = error.message
                            )
                            onResult(false, error.message, false)
                        }
                    )
                },
                onFailure = { error ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                    onResult(false, error.message, false)
                }
            )
        }
    }
}

package com.jmp.pocketmoneyapp.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.jmp.pocketmoneyapp.data.model.DeletionRequest
import com.jmp.pocketmoneyapp.data.model.Family
import com.jmp.pocketmoneyapp.data.model.FamilyMember
import com.jmp.pocketmoneyapp.data.model.MemberRole
import com.jmp.pocketmoneyapp.data.model.User
import com.jmp.pocketmoneyapp.data.model.UserRole
import com.jmp.pocketmoneyapp.utils.PinUtils
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Get current Firebase user
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    // Check if email exists (using deprecated API but still functional)
    suspend fun checkEmailExists(email: String): Result<Boolean> {
        return try {
            @Suppress("DEPRECATION")
            val methods = auth.fetchSignInMethodsForEmail(email).await()
            Result.success(!methods.signInMethods.isNullOrEmpty())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Sign up with email and password
    suspend fun signUp(email: String, password: String, name: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Failed to create user"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Sign in with email and password
    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Failed to sign in"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Sign out
    fun signOut() {
        auth.signOut()
    }

    // Send password reset email
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Delete current auth user
    suspend fun deleteCurrentAuthUser(): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            currentUser?.delete()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Create user document in Firestore
    suspend fun createUserDocument(userId: String, email: String, name: String, pin: String, familyId: String = "", userRole: UserRole = UserRole.PARENT): Result<Unit> {
        return try {
            val user = User(
                id = userId,
                email = email,
                name = name,
                role = userRole,
                familyId = familyId,
                pinHash = PinUtils.hashPin(pin)
            )
            firestore.collection("users").document(userId).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get user document from Firestore
    suspend fun getUserDocument(userId: String): Result<User?> {
        return try {
            // Retry up to 3 times with delays to handle potential race conditions
            var user: User? = null
            var attempts = 0
            val maxAttempts = 3
            
            while (user == null && attempts < maxAttempts) {
                val doc = firestore.collection("users").document(userId).get().await()
                
                // Check if document exists
                if (!doc.exists()) {
                    // Document doesn't exist - no need to retry
                    return Result.success(null)
                }
                
                user = doc.toObject(User::class.java)
                
                if (user == null && attempts < maxAttempts - 1) {
                    kotlinx.coroutines.delay(500) // Wait 500ms before retry
                }
                attempts++
            }
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Create a new family
    suspend fun createFamily(familyName: String, userId: String): Result<String> {
        return try {
            // Get current user to check if they're already in a family
            val userDoc = firestore.collection("users").document(userId).get().await()
            val currentUser = userDoc.toObject(User::class.java)
            val oldFamilyId = currentUser?.familyId
            
            // If user is already in a family, disconnect them from their old family member
            if (oldFamilyId != null) {
                val oldMembersSnapshot = firestore.collection("familyMembers")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("familyId", oldFamilyId)
                    .get()
                    .await()
                
                for (doc in oldMembersSnapshot.documents) {
                    // Unlink the old family member from this user
                    doc.reference.update("userId", null).await()
                }
            }
            
            val familyRef = firestore.collection("families").document()
            val family = Family(
                id = familyRef.id,
                name = familyName,
                createdBy = userId,
                members = mapOf(userId to UserRole.PARENT.name)
            )
            familyRef.set(family).await()
            
            // Update user's familyId
            firestore.collection("users").document(userId)
                .update("familyId", familyRef.id).await()
            
            // Get user's name to create family member
            val userName = currentUser?.name ?: "User"
            
            // Create a FamilyMember document for the user
            val familyMember = FamilyMember(
                id = "",
                familyId = familyRef.id,
                name = userName,
                role = MemberRole.PARENT,
                userId = userId,
                avatarEmoji = "👤",
                createdAt = Timestamp.now()
            )
            firestore.collection("family_members").add(familyMember).await()
            
            Result.success(familyRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get family document
    suspend fun getFamily(familyId: String): Result<Family?> {
        return try {
            val doc = firestore.collection("families").document(familyId).get().await()
            val family = doc.toObject(Family::class.java)
            Result.success(family)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Set up real-time listener for family changes
     * Returns a listener registration that should be removed when done
     */
    fun listenToFamily(familyId: String, onUpdate: (Family?) -> Unit): com.google.firebase.firestore.ListenerRegistration {
        return firestore.collection("families").document(familyId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onUpdate(null)
                    return@addSnapshotListener
                }
                
                val family = snapshot?.toObject(Family::class.java)
                onUpdate(family)
            }
    }
    
    // Verify user's PIN
    suspend fun verifyPin(userId: String, pin: String): Result<Boolean> {
        return try {
            val userDoc = firestore.collection("users").document(userId).get().await()
            val user = userDoc.toObject(User::class.java)
            val isValid = user?.pinHash?.let { PinUtils.verifyPin(pin, it) } ?: false
            Result.success(isValid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Update user's PIN
    suspend fun updatePin(userId: String, newPin: String): Result<Unit> {
        return try {
            val pinHash = PinUtils.hashPin(newPin)
            firestore.collection("users").document(userId)
                .update("pinHash", pinHash).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Switch user from one family to another (used when joining a new family with existing account)
     * Disconnects from old family member and connects to new one
     */
    suspend fun switchUserToNewFamily(
        userId: String,
        newFamilyId: String,
        newMemberId: String,
        oldFamilyId: String?
    ): Result<Unit> {
        return try {
            // If user was in an old family, disconnect from that family member
            if (oldFamilyId != null && oldFamilyId.isNotEmpty()) {
                val oldMembersSnapshot = firestore.collection("family_members")
                    .whereEqualTo("familyId", oldFamilyId)
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                
                // Unlink user from old family member(s)
                oldMembersSnapshot.documents.forEach { doc ->
                    doc.reference.update("userId", null).await()
                }
            }
            
            // Update user's familyId to new family
            firestore.collection("users")
                .document(userId)
                .update("familyId", newFamilyId)
                .await()
            
            // Link user to new family member
            firestore.collection("family_members")
                .document(newMemberId)
                .update("userId", userId)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all parent users in a family (users with UserRole.PARENT)
     */
    suspend fun getParentUsersInFamily(familyId: String): Result<List<User>> {
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("familyId", familyId)
                .whereEqualTo("role", "PARENT")
                .get()
                .await()
            
            val parentUsers = snapshot.documents.mapNotNull { 
                it.toObject(User::class.java) 
            }
            Result.success(parentUsers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Initiate a family deletion request
     */
    suspend fun requestFamilyDeletion(familyId: String, userId: String, userName: String): Result<Unit> {
        return try {
            // Get all parent users in the family
            val parentUsersResult = getParentUsersInFamily(familyId)
            if (parentUsersResult.isFailure) {
                return Result.failure(parentUsersResult.exceptionOrNull()!!)
            }
            
            val parentUsers = parentUsersResult.getOrNull() ?: emptyList()
            val requiredApprovals = parentUsers.map { it.id }
            
            // Create deletion request
            val deletionRequest = DeletionRequest(
                active = true,
                initiatedBy = userId,
                initiatedByName = userName,
                initiatedAt = Timestamp.now(),
                requiredApprovals = requiredApprovals,
                approvedBy = listOf(userId)  // Initiator automatically approves
            )
            
            // Check if all approvals are already satisfied (single parent case)
            val allApproved = requiredApprovals.all { deletionRequest.approvedBy.contains(it) }
            
            if (allApproved) {
                // Only one parent, delete immediately
                val deleteResult = deleteFamilyAndUsers(familyId)
                if (deleteResult.isFailure) {
                    return Result.failure(deleteResult.exceptionOrNull()!!)
                }
            } else {
                // Multiple parents, save the request for others to approve
                firestore.collection("families").document(familyId)
                    .update("deletionRequest", deletionRequest)
                    .await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Approve a family deletion request
     */
    suspend fun approveFamilyDeletion(familyId: String, userId: String): Result<Boolean> {
        return try {
            val familyResult = getFamily(familyId)
            if (familyResult.isFailure) {
                return Result.failure(familyResult.exceptionOrNull()!!)
            }
            
            val family = familyResult.getOrNull() ?: return Result.failure(Exception("Family not found"))
            val deletionRequest = family.deletionRequest ?: return Result.failure(Exception("No deletion request found"))
            
            // Add user to approved list if not already there
            val updatedApprovedBy = if (!deletionRequest.approvedBy.contains(userId)) {
                deletionRequest.approvedBy + userId
            } else {
                deletionRequest.approvedBy
            }
            
            val updatedDeletionRequest = deletionRequest.copy(approvedBy = updatedApprovedBy)
            
            firestore.collection("families").document(familyId)
                .update("deletionRequest", updatedDeletionRequest)
                .await()
            
            // Check if all required approvals are received
            val allApproved = updatedDeletionRequest.requiredApprovals.all { 
                updatedApprovedBy.contains(it) 
            }
            
            // If all approved, delete the family and all users
            if (allApproved) {
                val deleteResult = deleteFamilyAndUsers(familyId)
                if (deleteResult.isFailure) {
                    return Result.failure(deleteResult.exceptionOrNull()!!)
                }
            }
            
            Result.success(allApproved)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update the family's display name
     */
    suspend fun updateFamilyName(familyId: String, newName: String): Result<Unit> {
        return try {
            firestore.collection("families").document(familyId)
                .update("name", newName.trim())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cancel a family deletion request
     */
    suspend fun cancelFamilyDeletion(familyId: String): Result<Unit> {
        return try {
            firestore.collection("families").document(familyId)
                .update("deletionRequest", null)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete the family and all associated data
     */
    private suspend fun deleteFamilyAndUsers(familyId: String): Result<Unit> {
        return try {
            // Collect all Auth UIDs from family_members and stamp them onto the family document
            // so the Cloud Function that triggers on family deletion can clean up Auth accounts.
            val memberAuthUids = firestore.collection("family_members")
                .whereEqualTo("familyId", familyId)
                .get()
                .await()
                .documents
                .mapNotNull { it.getString("userId") }
            firestore.collection("families").document(familyId)
                .update("memberAuthUids", memberAuthUids)
                .await()

            // Delete all users in the family
            val usersSnapshot = firestore.collection("users")
                .whereEqualTo("familyId", familyId)
                .get()
                .await()
            
            usersSnapshot.documents.forEach { userDoc ->
                // Delete Firestore user document
                userDoc.reference.delete().await()
            }
            
            // Delete the current user's Firebase Auth account
            // (Other family members will need to delete their own accounts manually)
            val currentUser = auth.currentUser
            if (currentUser != null) {
                try {
                    currentUser.delete().await()
                } catch (e: Exception) {
                    // If deletion fails (e.g., requires re-auth), just sign out
                    auth.signOut()
                }
            }
            
            // Delete all chores
            val choresSnapshot = firestore.collection("chores")
                .whereEqualTo("familyId", familyId)
                .get()
                .await()
            choresSnapshot.documents.forEach { it.reference.delete().await() }
            
            // Delete all chore library templates (and legacy recurring_chore_templates)
            val templatesSnapshot = firestore.collection("chore_library_templates")
                .whereEqualTo("familyId", familyId)
                .get()
                .await()
            templatesSnapshot.documents.forEach { it.reference.delete().await() }
            val legacyTemplatesSnapshot = firestore.collection("recurring_chore_templates")
                .whereEqualTo("familyId", familyId)
                .get()
                .await()
            legacyTemplatesSnapshot.documents.forEach { it.reference.delete().await() }
            
            // Delete all transactions
            val transactionsSnapshot = firestore.collection("transactions")
                .whereEqualTo("familyId", familyId)
                .get()
                .await()
            transactionsSnapshot.documents.forEach { it.reference.delete().await() }
            
            // Delete all family members
            val membersSnapshot = firestore.collection("family_members")
                .whereEqualTo("familyId", familyId)
                .get()
                .await()
            membersSnapshot.documents.forEach { it.reference.delete().await() }
            
            // Finally, delete the family
            firestore.collection("families").document(familyId).delete().await()
            
            // Sign out the current user
            auth.signOut()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Save or update the FCM push token for a user. */
    suspend fun saveFcmToken(userId: String, token: String) {
        try {
            firestore.collection("users").document(userId)
                .update("fcmToken", token)
                .await()
        } catch (_: Exception) {
            // Field may not exist yet on older documents — use set with merge
            firestore.collection("users").document(userId)
                .set(mapOf("fcmToken" to token), com.google.firebase.firestore.SetOptions.merge())
                .await()
        }
    }
}

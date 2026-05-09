package com.jmp.pocketmoneyapp.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.jmp.pocketmoneyapp.data.model.FamilyInvitation
import com.jmp.pocketmoneyapp.data.model.FamilyMember
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FamilyMemberRepository {
    private val db = FirebaseFirestore.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val membersCollection = db.collection("family_members")
    
    // Create a new family member
    suspend fun createMember(member: FamilyMember): Result<String> {
        return try {
            val docRef = membersCollection.add(member).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get all family members for a family (real-time)
    fun getFamilyMembers(familyId: String): Flow<List<FamilyMember>> = callbackFlow {
        val subscription = membersCollection
            .whereEqualTo("familyId", familyId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val members = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FamilyMember::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(members)
            }
        
        awaitClose { subscription.remove() }
    }
    
    // Get a single family member
    suspend fun getMember(memberId: String): Result<FamilyMember?> {
        return try {
            val doc = membersCollection.document(memberId).get().await()
            val member = doc.toObject(FamilyMember::class.java)?.copy(id = doc.id)
            Result.success(member)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Update a family member
    suspend fun updateMember(member: FamilyMember): Result<Unit> {
        return try {
            membersCollection.document(member.id).set(member).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Delete a family member
    suspend fun deleteMember(memberId: String): Result<Unit> {
        return try {
            membersCollection.document(memberId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get children only (filter by role)
    fun getChildren(familyId: String): Flow<List<FamilyMember>> = callbackFlow {
        val subscription = membersCollection
            .whereEqualTo("familyId", familyId)
            .whereEqualTo("role", "CHILD")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val children = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FamilyMember::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(children)
            }
        
        awaitClose { subscription.remove() }
    }
    
    /**
     * Create an invitation for a family member
     */
    suspend fun createInvitation(member: FamilyMember, familyName: String): Result<FamilyInvitation> {
        return try {
            if (member.userId != null) {
                return Result.failure(Exception("Member already has a linked user account"))
            }
            
            // Generate unique invitation code
            val invitationCode = java.util.UUID.randomUUID().toString().substring(0, 8).uppercase()
            
            // Set expiration to 7 days from now
            val expiresAt = Timestamp(
                Timestamp.now().seconds + (7 * 24 * 60 * 60), // 7 days in seconds
                0
            )
            
            val invitation = FamilyInvitation(
                familyId = member.familyId,
                memberId = member.id,
                memberName = member.name,
                familyName = familyName,
                invitationCode = invitationCode,
                expiresAt = expiresAt
            )
            
            // Store invitation in Firestore
            firestore.collection("invitations").document(invitationCode)
                .set(invitation)
                .await()
            
            Result.success(invitation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get invitation by code
     */
    suspend fun getInvitation(invitationCode: String): Result<FamilyInvitation?> {
        return try {
            val doc = firestore.collection("invitations").document(invitationCode)
                .get()
                .await()
            
            val invitation = doc.toObject(FamilyInvitation::class.java)
            
            // Check if invitation exists and is not expired
            if (invitation != null) {
                if (Timestamp.now().seconds > invitation.expiresAt.seconds) {
                    // Delete expired invitation
                    doc.reference.delete().await()
                    return Result.failure(Exception("Invitation has expired"))
                }
            }
            
            Result.success(invitation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Accept invitation and link user to family member
     */
    suspend fun acceptInvitation(invitationCode: String, userId: String): Result<FamilyMember> {
        return try {
            // Get the invitation
            val invitationResult = getInvitation(invitationCode)
            if (invitationResult.isFailure) {
                return Result.failure(invitationResult.exceptionOrNull()!!)
            }
            
            val invitation = invitationResult.getOrNull() 
                ?: return Result.failure(Exception("Invitation not found"))
            
            // Get the family member
            val memberResult = getMember(invitation.memberId)
            if (memberResult.isFailure) {
                return Result.failure(memberResult.exceptionOrNull()!!)
            }
            
            val member = memberResult.getOrNull() 
                ?: return Result.failure(Exception("Family member not found"))
            
            // Check if member already has a user
            if (member.userId != null) {
                return Result.failure(Exception("Family member already linked to a user"))
            }
            
            // Link the user to the member
            val updatedMember = member.copy(userId = userId)
            membersCollection.document(invitation.memberId)
                .update("userId", userId)
                .await()
            
            // Delete the invitation
            firestore.collection("invitations").document(invitationCode)
                .delete()
                .await()
            
            Result.success(updatedMember)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

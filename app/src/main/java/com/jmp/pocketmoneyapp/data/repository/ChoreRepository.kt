package com.jmp.pocketmoneyapp.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.jmp.pocketmoneyapp.data.model.Chore
import com.jmp.pocketmoneyapp.data.model.ChoreStatus
import kotlinx.coroutines.tasks.await

class ChoreRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val choresCollection = firestore.collection("chores")
    
    /**
     * Create a new chore
     */
    suspend fun createChore(chore: Chore): Result<String> {
        return try {
            val choreRef = choresCollection.document()
            val choreWithId = chore.copy(id = choreRef.id)
            choreRef.set(choreWithId).await()
            Result.success(choreRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all chores for a family
     */
    suspend fun getFamilyChores(familyId: String): Result<List<Chore>> {
        return try {
            val snapshot = choresCollection
                .whereEqualTo("familyId", familyId)
                .get()
                .await()
            
            val chores = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Chore::class.java)
            }
            Result.success(chores)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get chores assigned to a specific user
     */
    suspend fun getUserChores(userId: String): Result<List<Chore>> {
        return try {
            val snapshot = choresCollection
                .whereEqualTo("assignedTo", userId)
                .get()
                .await()
            
            val chores = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Chore::class.java)
            }
            Result.success(chores)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update chore status
     */
    suspend fun updateChoreStatus(choreId: String, status: ChoreStatus): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "status" to status.name
            )
            
            // Add completion timestamp if completed
            if (status == ChoreStatus.COMPLETED) {
                updates["completedDate"] = Timestamp.now()
            }
            
            choresCollection.document(choreId)
                .update(updates)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update a chore
     */
    suspend fun updateChore(chore: Chore): Result<Unit> {
        return try {
            choresCollection.document(chore.id)
                .set(chore)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete a chore
     */
    suspend fun deleteChore(choreId: String): Result<Unit> {
        return try {
            choresCollection.document(choreId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

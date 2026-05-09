package com.jmp.pocketmoneyapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.jmp.pocketmoneyapp.data.model.Transaction
import com.jmp.pocketmoneyapp.data.model.TransactionType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TransactionRepository {
    private val db = FirebaseFirestore.getInstance()
    private val transactionsCollection = db.collection("transactions")
    
    // Create a new transaction
    suspend fun createTransaction(transaction: Transaction): Result<String> {
        return try {
            val docRef = transactionsCollection.add(transaction).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get all transactions for a family member (real-time)
    fun getMemberTransactions(memberId: String): Flow<List<Transaction>> = callbackFlow {
        val subscription = transactionsCollection
            .whereEqualTo("memberId", memberId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val transactions = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Transaction::class.java)?.copy(id = doc.id)
                }?.sortedByDescending { it.createdAt.toDate() } ?: emptyList()
                
                trySend(transactions)
            }
        
        awaitClose { subscription.remove() }
    }
    
    // Get all transactions for a family (real-time)
    fun getFamilyTransactions(familyId: String): Flow<List<Transaction>> = callbackFlow {
        val subscription = transactionsCollection
            .whereEqualTo("familyId", familyId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val transactions = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Transaction::class.java)?.copy(id = doc.id)
                }?.sortedByDescending { it.createdAt.toDate() } ?: emptyList()
                
                trySend(transactions)
            }
        
        awaitClose { subscription.remove() }
    }
    
    // Calculate balance for a family member
    suspend fun calculateBalance(memberId: String): Result<Double> {
        return try {
            val snapshot = transactionsCollection
                .whereEqualTo("memberId", memberId)
                .get()
                .await()
            
            val balance = snapshot.documents
                .mapNotNull { it.toObject(Transaction::class.java) }
                .sumOf { it.amount }
            
            Result.success(balance)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get earnings breakdown (allowance vs chores)
    suspend fun getEarningsBreakdown(memberId: String): Result<EarningsBreakdown> {
        return try {
            val snapshot = transactionsCollection
                .whereEqualTo("memberId", memberId)
                .get()
                .await()
            
            val transactions = snapshot.documents
                .mapNotNull { it.toObject(Transaction::class.java) }
            
            val choreEarnings = transactions
                .filter { it.type == TransactionType.CHORE_APPROVED || 
                         it.type == TransactionType.CHORE_PAID }
                .filter { it.amount > 0 }
                .sumOf { it.amount }
            
            val allowanceEarnings = transactions
                .filter { it.type == TransactionType.ALLOWANCE }
                .sumOf { it.amount }
            
            val bonusEarnings = transactions
                .filter { it.type == TransactionType.BONUS }
                .sumOf { it.amount }
            
            val spending = transactions
                .filter { it.type == TransactionType.SPENDING }
                .sumOf { it.amount }
            
            val adjustments = transactions
                .filter { it.type == TransactionType.ADJUSTMENT }
                .sumOf { it.amount }
            
            val totalBalance = choreEarnings + allowanceEarnings + bonusEarnings + spending + adjustments
            
            Result.success(
                EarningsBreakdown(
                    choreEarnings = choreEarnings,
                    allowanceEarnings = allowanceEarnings,
                    bonusEarnings = bonusEarnings,
                    spending = spending,
                    adjustments = adjustments,
                    totalBalance = totalBalance
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Delete a transaction
    suspend fun deleteTransaction(transactionId: String): Result<Unit> {
        return try {
            transactionsCollection.document(transactionId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Delete all transactions for a specific chore
    suspend fun deleteTransactionsByChoreId(choreId: String): Result<Unit> {
        return try {
            val snapshot = transactionsCollection
                .whereEqualTo("choreId", choreId)
                .get()
                .await()
            
            // Delete all matching transactions
            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class EarningsBreakdown(
    val choreEarnings: Double = 0.0,
    val allowanceEarnings: Double = 0.0,
    val bonusEarnings: Double = 0.0,
    val spending: Double = 0.0,
    val adjustments: Double = 0.0,
    val totalBalance: Double = 0.0
)

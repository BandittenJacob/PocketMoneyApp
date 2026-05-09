package com.jmp.pocketmoneyapp.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.jmp.pocketmoneyapp.data.model.ChoreProposal
import com.jmp.pocketmoneyapp.data.model.ProposalStatus
import kotlinx.coroutines.tasks.await

class ChoreProposalRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val proposals = firestore.collection("choreProposals")
    private val families = firestore.collection("families")

    suspend fun createProposal(proposal: ChoreProposal): Result<String> {
        return try {
            val ref = proposals.document()
            val withId = proposal.copy(id = ref.id)
            ref.set(withId).await()
            Result.success(ref.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProposalsForFamily(familyId: String): Result<List<ChoreProposal>> {
        return try {
            val snapshot = proposals
                .whereEqualTo("familyId", familyId)
                .get().await()
            val list = snapshot.documents.mapNotNull { it.toObject(ChoreProposal::class.java) }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProposalsForMember(familyId: String, memberId: String): Result<List<ChoreProposal>> {
        return try {
            val snapshot = proposals
                .whereEqualTo("familyId", familyId)
                .whereEqualTo("proposedByMemberId", memberId)
                .get().await()
            val list = snapshot.documents.mapNotNull { it.toObject(ChoreProposal::class.java) }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resolveProposal(
        proposalId: String,
        status: ProposalStatus,
        parentNote: String = ""
    ): Result<Unit> {
        return try {
            proposals.document(proposalId).update(
                mapOf(
                    "status" to status.name,
                    "parentNote" to parentNote,
                    "resolvedAt" to Timestamp.now()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProposal(proposalId: String): Result<Unit> {
        return try {
            proposals.document(proposalId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setProposalsEnabled(familyId: String, enabled: Boolean): Result<Unit> {
        return try {
            families.document(familyId)
                .update("choreProposalsEnabled", enabled).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setDueDateIndicatorsEnabled(familyId: String, enabled: Boolean): Result<Unit> {
        return try {
            families.document(familyId)
                .update("dueDateIndicatorsEnabled", enabled).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

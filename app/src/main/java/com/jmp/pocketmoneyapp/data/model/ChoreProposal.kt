package com.jmp.pocketmoneyapp.data.model

import com.google.firebase.Timestamp

enum class ProposalStatus {
    PENDING,
    ACCEPTED,
    ACCEPTED_WITH_EDITS,
    REJECTED
}

data class ChoreProposal(
    val id: String = "",
    val familyId: String = "",
    val proposedByMemberId: String = "",   // FamilyMember id
    val proposedByName: String = "",        // e.g. "🐭 Isabella"
    val name: String = "",
    val description: String = "",
    val suggestedReward: Double = 0.0,
    val status: ProposalStatus = ProposalStatus.PENDING,
    val parentNote: String = "",            // rejection reason or edit note
    val createdAt: Timestamp = Timestamp.now(),
    val resolvedAt: Timestamp? = null
)

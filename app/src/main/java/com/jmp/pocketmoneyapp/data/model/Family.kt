package com.jmp.pocketmoneyapp.data.model

import com.google.firebase.Timestamp

/**
 * Represents a pending deletion request for the family
 */
data class DeletionRequest(
    val active: Boolean = false,  // Changed from isActive to avoid Firestore serialization issues
    val initiatedBy: String = "",  // userId who initiated deletion
    val initiatedByName: String = "",  // Name for display
    val initiatedAt: Timestamp = Timestamp.now(),
    val requiredApprovals: List<String> = emptyList(),  // userIds that need to approve
    val approvedBy: List<String> = emptyList()  // userIds that have approved
)

data class Family(
    val id: String = "",
    val name: String = "",
    val createdBy: String = "", // userId
    val createdAt: Timestamp = Timestamp.now(),
    val members: Map<String, String> = emptyMap(), // userId -> role
    val deletionRequest: DeletionRequest? = null,  // null if no pending deletion
    val choreProposalsEnabled: Boolean = false,
    val dueDateIndicatorsEnabled: Boolean = false
)

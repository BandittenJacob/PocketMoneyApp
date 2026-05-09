package com.jmp.pocketmoneyapp.data.model

import com.google.firebase.Timestamp

/**
 * Represents a chore that can be assigned and completed.
 * For chore templates, use ChoreLibraryTemplate to generate instances.
 */
data class Chore(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val value: Double = 0.0,  // Monetary value for completing the chore
    val assignedTo: String = "",  // Display string of assigned member (e.g. "🐭 Isabella")
    val assignedToUserId: String = "",  // Firebase Auth UID of assigned member (used for notifications)
    val createdBy: String = "",  // User ID of creator (usually parent)
    val familyId: String = "",
    val status: ChoreStatus = ChoreStatus.PENDING,
    val dueDate: Timestamp? = null,
    val completedDate: Timestamp? = null,
    val templateId: String? = null,  // ID of ChoreLibraryTemplate if this is an instance
    val createdAt: Timestamp = Timestamp.now()
)

enum class ChoreStatus {
    PENDING,      // Not yet started
    IN_PROGRESS,  // Started but not completed
    COMPLETED,    // Completed, awaiting approval
    APPROVED,     // Approved by parent, payment pending
    PAID          // Payment has been made
}

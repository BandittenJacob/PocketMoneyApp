package com.jmp.pocketmoneyapp.data.model

import com.google.firebase.Timestamp

data class Transaction(
    val id: String = "",
    val familyId: String = "",
    val memberId: String = "",
    val memberName: String = "",
    val amount: Double = 0.0,
    val type: TransactionType = TransactionType.ADJUSTMENT,
    val description: String = "",
    val choreId: String? = null,
    val choreName: String? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val createdBy: String = ""
)

enum class TransactionType {
    CHORE_APPROVED,      // When parent approves a completed chore
    CHORE_PAID,          // When parent marks chore as paid
    ALLOWANCE,           // Weekly/monthly allowance credit (legacy)
    SPENDING,            // Money spent (legacy)
    ADJUSTMENT,          // Correction by parent (can be + or -)
    BONUS,               // One-time bonus from parent (always positive)
    BANK_TRANSFER        // Money physically given - balance goes down (always negative)
}

package com.jmp.pocketmoneyapp.data.model

import com.google.firebase.Timestamp

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val role: UserRole = UserRole.PARENT,
    val familyId: String = "",
    val pinHash: String = "", // SHA-256 hash of user's PIN
    val biometricEnabled: Boolean = false,
    val createdAt: Timestamp = Timestamp.now()
)

enum class UserRole {
    ADMIN,   // Legacy role, treated as PARENT (for backwards compatibility)
    PARENT,  // Parent with full permissions
    CHILD    // Child with limited permissions
}

package com.jmp.pocketmoneyapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class FamilyMember(
    @DocumentId
    val id: String = "",
    val familyId: String = "",
    val name: String = "",
    val role: MemberRole = MemberRole.CHILD,
    val userId: String? = null, // Optional: link to user account if member has own device
    val avatarEmoji: String = "👤", // Simple emoji avatar
    val createdAt: Timestamp = Timestamp.now()
)

enum class MemberRole {
    PARENT,
    CHILD
}

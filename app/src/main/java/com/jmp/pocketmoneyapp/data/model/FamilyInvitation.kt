package com.jmp.pocketmoneyapp.data.model

import com.google.firebase.Timestamp

/**
 * Represents an invitation to link a user account to a family member
 */
data class FamilyInvitation(
    val familyId: String = "",
    val memberId: String = "",
    val memberName: String = "",
    val familyName: String = "",
    val invitationCode: String = "",  // Unique code for this invitation
    val expiresAt: Timestamp = Timestamp.now(),
    val createdAt: Timestamp = Timestamp.now()
) {
    /**
     * Convert invitation to QR code data string
     */
    fun toQrData(): String {
        return "PMApp:$invitationCode:$familyId:$memberId"
    }
    
    companion object {
        /**
         * Parse QR code data string to invitation details
         */
        fun fromQrData(qrData: String): Pair<String, String>? {
            val parts = qrData.split(":")
            if (parts.size != 4 || parts[0] != "PMApp") {
                return null
            }
            return Pair(parts[1], parts[2]) // invitationCode and familyId
        }
    }
}

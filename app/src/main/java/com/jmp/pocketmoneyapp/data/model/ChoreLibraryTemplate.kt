package com.jmp.pocketmoneyapp.data.model

import com.google.firebase.Timestamp

/**
 * Template for chore library entries. These are not completed themselves,
 * but generate Chore instances that appear on the main chores list.
 */
data class ChoreLibraryTemplate(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val value: Double = 0.0,  // Monetary value for completing each instance
    val familyId: String = "",
    val createdBy: String = "",
    val defaultAssignedTo: String = "",  // Default assignment for new instances (can be empty)
    
    // Recurrence schedule
    val recurrenceType: RecurrenceType = RecurrenceType.DAILY,
    val timeOfDay: String? = null,  // HH:mm format for daily recurrence
    val dayOfWeek: Int? = null,  // 1=Monday, 7=Sunday for weekly recurrence
    val dayOfMonth: Int? = null,  // 1-31 for monthly recurrence
    
    // Template status
    val isActive: Boolean = true,  // Can be disabled without deleting
    val createdAt: Timestamp = Timestamp.now(),
    val lastInstanceCreated: Timestamp? = null  // Track when last instance was generated
)

/**
 * Frequency for recurring chore templates.
 */
enum class RecurrenceType {
    NONE,      // Not recurring
    DAILY,     // Every day at specified time
    WEEKLY,    // Once per week on specified day
    MONTHLY    // Once per month on specified day
}

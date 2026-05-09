package com.jmp.pocketmoneyapp.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.jmp.pocketmoneyapp.data.model.Chore
import com.jmp.pocketmoneyapp.data.model.ChoreStatus
import com.jmp.pocketmoneyapp.data.model.RecurrenceType
import com.jmp.pocketmoneyapp.data.model.ChoreLibraryTemplate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

class ChoreLibraryRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val templatesCollection = firestore.collection("chore_library_templates")
    private val legacyTemplatesCollection = firestore.collection("recurring_chore_templates")
    
    private val choresCollection = firestore.collection("chores")

    /**
     * Create a new chore library template
     */
    suspend fun createTemplate(template: ChoreLibraryTemplate): Result<String> {
        return try {
            val templateRef = templatesCollection.document()
            val templateWithId = template.copy(id = templateRef.id)
            templateRef.set(templateWithId).await()
            Result.success(templateRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all chore library templates for a family.
     * Transparently migrates any legacy documents from recurring_chore_templates.
     */
    suspend fun getFamilyTemplates(familyId: String): Result<List<ChoreLibraryTemplate>> {
        return try {
            val snapshot = templatesCollection
                .whereEqualTo("familyId", familyId)
                .get(Source.SERVER)
                .await()

            // Transparent migration: if nothing in the new collection, migrate from legacy
            if (snapshot.isEmpty) {
                val legacySnapshot = legacyTemplatesCollection
                    .whereEqualTo("familyId", familyId)
                    .get(Source.SERVER)
                    .await()
                if (!legacySnapshot.isEmpty) {
                    for (doc in legacySnapshot.documents) {
                        doc.data?.let { data -> templatesCollection.document(doc.id).set(data).await() }
                        legacyTemplatesCollection.document(doc.id).delete().await()
                    }
                    // Re-query new collection after migration
                    return getFamilyTemplates(familyId)
                }
            }

            val templates = snapshot.documents.mapNotNull { doc ->
                // Migration: Clean up duplicate 'active' field if it exists
                if (doc.contains("active")) {
                    templatesCollection.document(doc.id)
                        .update("active", com.google.firebase.firestore.FieldValue.delete())
                }
                val template = doc.toObject(ChoreLibraryTemplate::class.java)
                val actualIsActive = doc.getBoolean("isActive") ?: true
                template?.copy(isActive = actualIsActive)
            }
            Result.success(templates)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get a specific template by ID
     */
    suspend fun getTemplate(templateId: String): Result<ChoreLibraryTemplate?> {
        return try {
            val snapshot = templatesCollection.document(templateId).get().await()
            val template = snapshot.toObject(ChoreLibraryTemplate::class.java)
            Result.success(template)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update a recurring chore template
     */
    suspend fun updateTemplate(template: ChoreLibraryTemplate): Result<Unit> {
        return try {
            templatesCollection.document(template.id)
                .set(template)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete a template from both new and legacy collections
     */
    suspend fun deleteTemplate(templateId: String): Result<Unit> {
        return try {
            templatesCollection.document(templateId).delete().await()
            legacyTemplatesCollection.document(templateId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Toggle template active status
     */
    suspend fun toggleTemplateActive(templateId: String, isActive: Boolean): Result<Unit> {
        return try {
            // Update isActive and remove the conflicting 'active' field
            val updates = hashMapOf<String, Any>(
                "isActive" to isActive,
                "active" to com.google.firebase.firestore.FieldValue.delete()  // Remove the duplicate field
            )
            
            templatesCollection.document(templateId)
                .update(updates)
                .await()
                
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate a chore instance from a template
     * This creates a regular Chore that shows up on the main chores list
     */
    suspend fun generateInstance(template: ChoreLibraryTemplate): Result<String> {
        return try {
            val choreRef = choresCollection.document()
            val dueDate = calculateNextDueDate(template)
            
            val chore = Chore(
                id = choreRef.id,
                name = template.name,
                description = template.description,
                value = template.value,
                assignedTo = template.defaultAssignedTo,
                createdBy = template.createdBy,
                familyId = template.familyId,
                status = ChoreStatus.PENDING,
                dueDate = dueDate,
                templateId = template.id,  // Link back to template
                createdAt = Timestamp.now()
            )
            
            choreRef.set(chore).await()
            
            // Update template's lastInstanceCreated timestamp
            templatesCollection.document(template.id)
                .update("lastInstanceCreated", Timestamp.now())
                .await()
            
            Result.success(choreRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if a template already has an active (non-completed) instance
     * Prevents creating duplicate instances
     */
    suspend fun hasActiveInstance(templateId: String): Result<Boolean> {
        return try {
            val snapshot = choresCollection
                .whereEqualTo("templateId", templateId)
                .whereEqualTo("status", ChoreStatus.PENDING.name)
                .get()
                .await()
            
            Result.success(!snapshot.isEmpty)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all instances (chores) created from a specific template
     */
    suspend fun getTemplateInstances(templateId: String): Result<List<Chore>> {
        return try {
            val snapshot = choresCollection
                .whereEqualTo("templateId", templateId)
                .get()
                .await()
            
            val chores = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Chore::class.java)
            }
            Result.success(chores)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Calculate the next due date based on the template's recurrence settings
     */
    private fun calculateNextDueDate(template: ChoreLibraryTemplate): Timestamp {
        val calendar = Calendar.getInstance()
        
        when (template.recurrenceType) {
            RecurrenceType.DAILY -> {
                // Set to today at the specified time
                template.timeOfDay?.let { time ->
                    val parts = time.split(":")
                    if (parts.size == 2) {
                        val hour = parts[0].toIntOrNull() ?: 12
                        val minute = parts[1].toIntOrNull() ?: 0
                        
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        
                        // If the time has already passed today, schedule for tomorrow
                        if (calendar.timeInMillis <= System.currentTimeMillis()) {
                            calendar.add(Calendar.DAY_OF_MONTH, 1)
                        }
                    }
                }
            }
            
            RecurrenceType.WEEKLY -> {
                // Calculate next occurrence of the specified day of week
                template.dayOfWeek?.let { targetDay ->
                    val currentDay = calendar.get(Calendar.DAY_OF_WEEK)
                    // Convert from template format (1=Monday) to Calendar format (1=Sunday, 2=Monday)
                    val targetCalendarDay = if (targetDay == 7) Calendar.SUNDAY else targetDay + 1
                    
                    // Calculate days until target day
                    var daysUntil = targetCalendarDay - currentDay
                    if (daysUntil <= 0) {
                        daysUntil += 7  // Schedule for next week
                    }
                    
                    calendar.add(Calendar.DAY_OF_MONTH, daysUntil)
                    
                    // Set to start of day
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                }
            }
            
            RecurrenceType.MONTHLY -> {
                // Calculate next occurrence of the specified day of month
                template.dayOfMonth?.let { targetDay ->
                    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
                    
                    if (currentDay >= targetDay) {
                        // Already passed this month, schedule for next month
                        calendar.add(Calendar.MONTH, 1)
                    }
                    
                    // Set to target day, handling months with fewer days
                    val maxDayInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                    calendar.set(Calendar.DAY_OF_MONTH, minOf(targetDay, maxDayInMonth))
                    
                    // Set to start of day
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                }
            }
            
            RecurrenceType.NONE -> {
                // No recurrence, set to now
            }
        }
        
        return Timestamp(Date(calendar.timeInMillis))
    }
}

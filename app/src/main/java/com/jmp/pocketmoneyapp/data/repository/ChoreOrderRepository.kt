package com.jmp.pocketmoneyapp.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.choreOrderDataStore by preferencesDataStore(name = "chore_order")

class ChoreOrderRepository(private val context: Context) {

    private fun keyFor(userId: String) = stringPreferencesKey("order_$userId")

    suspend fun getOrderForUser(userId: String): List<String> =
        context.choreOrderDataStore.data.map { prefs ->
            prefs[keyFor(userId)]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
        }.first()

    suspend fun saveOrderForUser(userId: String, orderedIds: List<String>) {
        context.choreOrderDataStore.edit { prefs ->
            prefs[keyFor(userId)] = orderedIds.joinToString(",")
        }
    }

    /**
     * Reconcile a saved order against the current set of chore IDs.
     * - IDs in savedOrder that are no longer in currentIds are dropped.
     * - IDs in currentIds that are not in savedOrder are appended at the bottom.
     */
    fun reconcile(savedOrder: List<String>, currentIds: Set<String>): List<String> {
        val filtered = savedOrder.filter { it in currentIds }
        val newIds = (currentIds - filtered.toSet()).sorted()
        return filtered + newIds
    }
}

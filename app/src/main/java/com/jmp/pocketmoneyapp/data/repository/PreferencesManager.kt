package com.jmp.pocketmoneyapp.data.repository

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "pocket_money_prefs",
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_LAST_USER_EMAIL = "last_user_email"
        private const val KEY_AUTO_GENERATE_CHORES = "auto_generate_chores"
        private const val KEY_TOUR_CHORES = "tour_chores_seen"
        private const val KEY_TOUR_DASHBOARD = "tour_dashboard_seen"
        private const val KEY_TOUR_LIBRARY = "tour_library_seen"
        private const val KEY_TOUR_SETTINGS = "tour_settings_seen"
        private const val KEY_TOUR_FAMILY_MEMBERS = "tour_family_members_seen"
    }
    
    var isBiometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, value).apply()
    
    var lastUserEmail: String?
        get() = prefs.getString(KEY_LAST_USER_EMAIL, null)
        set(value) = prefs.edit().putString(KEY_LAST_USER_EMAIL, value).apply()
    
    var isAutoGenerateChoresEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_GENERATE_CHORES, true)  // Enabled by default
        set(value) = prefs.edit().putBoolean(KEY_AUTO_GENERATE_CHORES, value).apply()

    var hasSeenChoresTour: Boolean
        get() = prefs.getBoolean(KEY_TOUR_CHORES, false)
        set(value) = prefs.edit().putBoolean(KEY_TOUR_CHORES, value).apply()

    var hasSeenDashboardTour: Boolean
        get() = prefs.getBoolean(KEY_TOUR_DASHBOARD, false)
        set(value) = prefs.edit().putBoolean(KEY_TOUR_DASHBOARD, value).apply()

    var hasSeenLibraryTour: Boolean
        get() = prefs.getBoolean(KEY_TOUR_LIBRARY, false)
        set(value) = prefs.edit().putBoolean(KEY_TOUR_LIBRARY, value).apply()

    var hasSeenSettingsTour: Boolean
        get() = prefs.getBoolean(KEY_TOUR_SETTINGS, false)
        set(value) = prefs.edit().putBoolean(KEY_TOUR_SETTINGS, value).apply()

    var hasSeenFamilyMembersTour: Boolean
        get() = prefs.getBoolean(KEY_TOUR_FAMILY_MEMBERS, false)
        set(value) = prefs.edit().putBoolean(KEY_TOUR_FAMILY_MEMBERS, value).apply()

    fun resetAllTours() {
        prefs.edit()
            .putBoolean(KEY_TOUR_CHORES, false)
            .putBoolean(KEY_TOUR_DASHBOARD, false)
            .putBoolean(KEY_TOUR_LIBRARY, false)
            .putBoolean(KEY_TOUR_SETTINGS, false)
            .putBoolean(KEY_TOUR_FAMILY_MEMBERS, false)
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}

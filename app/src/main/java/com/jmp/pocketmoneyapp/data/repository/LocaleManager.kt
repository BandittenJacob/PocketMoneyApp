package com.jmp.pocketmoneyapp.data.repository

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.core.content.edit
import java.util.*

class LocaleManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("locale_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_LANGUAGE = "selected_language"
        const val LANGUAGE_ENGLISH = "en"
        const val LANGUAGE_DANISH = "da"
    }
    
    var selectedLanguage: String
        get() = prefs.getString(KEY_LANGUAGE, LANGUAGE_DANISH) ?: LANGUAGE_DANISH
        set(value) {
            prefs.edit { putString(KEY_LANGUAGE, value) }
        }
    
    fun setLocale(language: String): Context {
        selectedLanguage = language
        return updateResources(context, language)
    }
    
    fun getLocale(): String {
        return selectedLanguage
    }
    
    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
            context
        }
    }
}

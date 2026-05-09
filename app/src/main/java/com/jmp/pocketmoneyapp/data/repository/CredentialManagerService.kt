package com.jmp.pocketmoneyapp.data.repository

import android.content.Context
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service for managing credentials using Android's Credential Manager API.
 * This integrates with Google Password Manager to save and retrieve user credentials.
 */
class CredentialManagerService(private val context: Context) {
    
    private val credentialManager = CredentialManager.create(context)
    
    /**
     * Save credentials to Google Password Manager after successful signup/login.
     * 
     * @param email The user's email address (used as the credential ID)
     * @param password The user's password
     * @return true if credentials were saved, false if user canceled
     */
    suspend fun saveCredentials(email: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val createPasswordRequest = CreatePasswordRequest(
                    id = email,
                    password = password
                )
                
                credentialManager.createCredential(
                    request = createPasswordRequest,
                    context = context
                )
                
                true
            } catch (e: CreateCredentialCancellationException) {
                // User canceled the save dialog
                false
            } catch (e: Exception) {
                // Other errors (log if needed)
                false
            }
        }
    }
    
    /**
     * Retrieve saved credentials from Google Password Manager.
     * 
     * @return Pair of (email, password) if found, null if not found or canceled
     */
    suspend fun getSavedCredentials(): Pair<String, String>? {
        return withContext(Dispatchers.IO) {
            try {
                val getPasswordOption = GetPasswordOption()
                val getCredRequest = GetCredentialRequest(
                    listOf(getPasswordOption)
                )
                
                val result = credentialManager.getCredential(
                    request = getCredRequest,
                    context = context
                )
                
                // Extract the password credential
                when (val credential = result.credential) {
                    is PasswordCredential -> {
                        Pair(credential.id, credential.password)
                    }
                    else -> null
                }
            } catch (e: GetCredentialCancellationException) {
                // User canceled the selection
                null
            } catch (e: NoCredentialException) {
                // No credentials found
                null
            } catch (e: Exception) {
                // Other errors
                null
            }
        }
    }
}

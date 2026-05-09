package com.jmp.pocketmoneyapp.utils

import java.security.MessageDigest

object PinUtils {
    /**
     * Hash a PIN using SHA-256
     */
    fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(pin.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Verify a PIN against its hash
     */
    fun verifyPin(pin: String, hash: String): Boolean {
        return hashPin(pin) == hash
    }
    
    /**
     * Validate PIN format (4-6 digits)
     */
    fun isValidPin(pin: String): Boolean {
        return pin.length in 4..6 && pin.all { it.isDigit() }
    }
}

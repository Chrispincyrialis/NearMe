package com.nearme.security

import java.security.MessageDigest

object SecurityUtils {
    fun hashSha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun validatePin(enteredPin: String, expectedHash: String): Boolean {
        return hashSha256(enteredPin) == expectedHash
    }
}

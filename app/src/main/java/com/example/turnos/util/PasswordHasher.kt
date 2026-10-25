package com.example.turnos.util

import java.security.MessageDigest

/**
 * Nunca guardamos contraseñas en texto plano, ni siquiera en la base local.
 * SHA-256 es suficiente para la demo; con un backend real se usaría su
 * propio sistema de autenticación.
 */

object PasswordHasher {
    fun hash(password: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray())
            .joinToString("") { "%02x".format(it) }
}

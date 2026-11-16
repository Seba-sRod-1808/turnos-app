package com.example.turnos.util

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Nunca guardamos contraseñas ni respuestas de seguridad en texto plano, ni siquiera en la base local.
 * SHA-256 es suficiente para la demo; con un backend real se usaría su propio sistema de autenticación.
 */

object PasswordHasher {
    private const val TEMP_PASSWORD_LENGTH = 8
    private const val TEMP_ALPHABET = "abcdefghjkmnpqrstuvwxyz23456789"
    private val random = SecureRandom()

    fun hash(password: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray())
            .joinToString("") { "%02x".format(it) }

    fun hashAnswer(answer: String): String = hash(Validators.normalizeAnswer(answer))

    /** Sin caracteres ambiguos (0/o, 1/l/i) para que el dueño pueda dictarla al cliente. */
    fun generateTemporaryPassword(): String =
        (1..TEMP_PASSWORD_LENGTH).map { TEMP_ALPHABET[random.nextInt(TEMP_ALPHABET.length)] }.joinToString("")
}

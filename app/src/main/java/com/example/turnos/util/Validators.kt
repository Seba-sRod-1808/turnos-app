package com.example.turnos.util

/**
 * Validaciones de formularios (Kotlin puro, sin dependencias de Android).
 */

object Validators {
    private val usernameRegex = Regex("^[a-z0-9._]{3,20}$")
    private val phoneRegex = Regex("^[0-9 +-]{8,15}$")

    const val MIN_PASSWORD_LENGTH = 6
    const val MIN_ANSWER_LENGTH = 2

    fun normalizeUsername(username: String): String = username.trim().lowercase()

    fun isValidUsername(username: String): Boolean = usernameRegex.matches(normalizeUsername(username))

    fun isValidPassword(password: String): Boolean = password.length >= MIN_PASSWORD_LENGTH

    fun isValidPhone(phone: String): Boolean = phone.isBlank() || phoneRegex.matches(phone.trim())

    fun isValidSecurityAnswer(answer: String): Boolean = normalizeAnswer(answer).length >= MIN_ANSWER_LENGTH

    /** Las respuestas de seguridad se comparan sin importar mayúsculas, tildes ni espacios extra. */
    fun normalizeAnswer(answer: String): String =
        java.text.Normalizer.normalize(answer.trim().lowercase(), java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .replace(Regex("\\s+"), " ")
}

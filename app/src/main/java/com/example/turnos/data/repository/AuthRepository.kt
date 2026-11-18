package com.example.turnos.data.repository

import com.example.turnos.data.local.dao.UserDao
import com.example.turnos.data.model.User
import com.example.turnos.util.PasswordHasher
import com.example.turnos.util.Validators

/**
 * Autenticación contra la tabla local de usuarios (Room).
 * Los clientes no se registran solos: el dueño del local crea su cuenta con una contraseña
 * temporal y en el primer ingreso el cliente define su contraseña y su pregunta de seguridad,
 * que luego sirve para recuperar la contraseña sin depender del local.
 */

class InvalidCredentialsException : Exception("Usuario o contraseña incorrectos.")
class UserNotFoundException : Exception("No existe una cuenta con ese usuario.")
class NoRecoveryQuestionException :
    Exception("Esta cuenta aún no tiene pregunta de seguridad. Pide al local que restablezca tu contraseña.")
class WrongSecurityAnswerException : Exception("La respuesta no coincide.")

sealed interface LoginResult {
    data class LoggedIn(val user: User) : LoginResult
    data class NeedsSetup(val userId: Long) : LoginResult
}

class AuthRepository(
    private val userDao: UserDao,
    private val session: SessionRepository,
) {
    suspend fun login(username: String, password: String): Result<LoginResult> {
        val entity = userDao.findByUsername(Validators.normalizeUsername(username))
        if (entity == null || entity.passwordHash != PasswordHasher.hash(password)) {
            return Result.failure(InvalidCredentialsException())
        }
        val user = entity.toModel()
        if (user.mustChangePassword) return Result.success(LoginResult.NeedsSetup(user.id))
        session.start(user)
        return Result.success(LoginResult.LoggedIn(user))
    }

    suspend fun getUserName(userId: Long): String? = userDao.getById(userId)?.name

    suspend fun completeSetup(userId: Long, newPassword: String, question: String, answer: String): Result<User> {
        userDao.getById(userId) ?: return Result.failure(UserNotFoundException())
        userDao.updatePassword(userId, PasswordHasher.hash(newPassword), mustChange = false)
        userDao.updateSecurityQuestion(userId, question, PasswordHasher.hashAnswer(answer))
        val user = userDao.getById(userId)!!.toModel()
        session.start(user)
        return Result.success(user)
    }

    suspend fun getSecurityQuestion(username: String): Result<String> {
        val entity = userDao.findByUsername(Validators.normalizeUsername(username))
            ?: return Result.failure(UserNotFoundException())
        val question = entity.securityQuestion ?: return Result.failure(NoRecoveryQuestionException())
        return Result.success(question)
    }

    suspend fun verifySecurityAnswer(username: String, answer: String): Result<Unit> {
        val entity = userDao.findByUsername(Validators.normalizeUsername(username))
            ?: return Result.failure(UserNotFoundException())
        return if (entity.securityAnswerHash == PasswordHasher.hashAnswer(answer)) Result.success(Unit)
        else Result.failure(WrongSecurityAnswerException())
    }

    suspend fun resetPasswordWithAnswer(username: String, answer: String, newPassword: String): Result<Unit> {
        verifySecurityAnswer(username, answer).onFailure { return Result.failure(it) }
        val entity = userDao.findByUsername(Validators.normalizeUsername(username))!!
        userDao.updatePassword(entity.id, PasswordHasher.hash(newPassword), mustChange = false)
        return Result.success(Unit)
    }

    fun logout() = session.end()

    companion object {
        const val MAX_RECOVERY_ATTEMPTS = 3

        val SECURITY_QUESTIONS = listOf(
            "¿Cuál es el nombre de tu primera mascota?",
            "¿En qué ciudad naciste?",
            "¿Cuál es tu comida favorita?",
            "¿Cómo se llamaba tu escuela primaria?",
        )
    }
}

package com.example.turnos.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.example.turnos.data.local.dao.UserDao
import com.example.turnos.data.local.entity.UserEntity
import com.example.turnos.data.model.User
import com.example.turnos.data.model.UserRole
import com.example.turnos.util.PasswordHasher

/**
 * Autenticación contra la tabla local de usuarios (Room).
 */

class InvalidCredentialsException : Exception("Correo o contraseña incorrectos.")
class EmailAlreadyUsedException : Exception("Ya existe una cuenta con ese correo.")

class AuthRepository(
    private val userDao: UserDao,
    private val session: SessionRepository,
) {
    suspend fun login(email: String, password: String): Result<User> {
        val entity = userDao.findByEmail(email.trim())
        if (entity == null || entity.passwordHash != PasswordHasher.hash(password)) {
            return Result.failure(InvalidCredentialsException())
        }
        return Result.success(entity.toModel().also(session::start))
    }

    suspend fun register(name: String, email: String, password: String, role: UserRole): Result<User> =
        try {
            val entity = UserEntity(
                name = name.trim(),
                email = email.trim().lowercase(),
                passwordHash = PasswordHasher.hash(password),
                role = role.name,
            )
            val id = userDao.insert(entity)
            Result.success(entity.copy(id = id).toModel().also(session::start))
        } catch (e: SQLiteConstraintException) {
            Result.failure(EmailAlreadyUsedException())
        }

    fun logout() = session.end()
}

package com.example.turnos.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.example.turnos.data.local.dao.UserDao
import com.example.turnos.data.local.entity.UserEntity
import com.example.turnos.data.model.User
import com.example.turnos.data.model.UserRole
import com.example.turnos.util.PasswordHasher
import com.example.turnos.util.Validators
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repositorio para la administración de cuentas de clientes por parte del dueño del local.
 *
 * Permite dar de alta nuevos clientes con una contraseña temporal generada en memoria y
 * restablecer su acceso exigiendo cambio de contraseña en el siguiente inicio de sesión.
 */
class UsernameTakenException : Exception("Ese usuario ya existe. Elige otro.")

data class ClientCredentials(val name: String, val username: String, val temporaryPassword: String)

class ClientRepository(private val userDao: UserDao) {

    fun observeClients(): Flow<List<User>> = userDao.observeClients().map { list -> list.map { it.toModel() } }

    suspend fun createClient(name: String, username: String, phone: String): Result<ClientCredentials> {
        val temporaryPassword = PasswordHasher.generateTemporaryPassword()
        val normalized = Validators.normalizeUsername(username)
        return try {
            userDao.insert(
                UserEntity(
                    name = name.trim(),
                    username = normalized,
                    phone = phone.trim(),
                    passwordHash = PasswordHasher.hash(temporaryPassword),
                    role = UserRole.CLIENT.name,
                    mustChangePassword = true,
                )
            )
            Result.success(ClientCredentials(name.trim(), normalized, temporaryPassword))
        } catch (e: SQLiteConstraintException) {
            Result.failure(UsernameTakenException())
        }
    }

    suspend fun resetPassword(client: User): ClientCredentials {
        val temporaryPassword = PasswordHasher.generateTemporaryPassword()
        userDao.updatePassword(client.id, PasswordHasher.hash(temporaryPassword), mustChange = true)
        return ClientCredentials(client.name, client.username, temporaryPassword)
    }
}

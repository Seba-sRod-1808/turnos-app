package com.example.turnos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.turnos.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * El usuario es único (índice único): insertar uno repetido lanza SQLiteConstraintException.
 */

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username COLLATE NOCASE LIMIT 1")
    suspend fun findByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE role = 'CLIENT' ORDER BY name COLLATE NOCASE")
    fun observeClients(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Query("UPDATE users SET passwordHash = :passwordHash, mustChangePassword = :mustChange WHERE id = :id")
    suspend fun updatePassword(id: Long, passwordHash: String, mustChange: Boolean)

    @Query("UPDATE users SET securityQuestion = :question, securityAnswerHash = :answerHash WHERE id = :id")
    suspend fun updateSecurityQuestion(id: Long, question: String, answerHash: String)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int
}

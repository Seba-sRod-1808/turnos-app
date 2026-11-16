package com.example.turnos.domain

import com.example.turnos.util.PasswordHasher
import com.example.turnos.util.Validators
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsTest {

    @Test
    fun `el usuario se normaliza y valida`() {
        assertEquals("ana.lopez", Validators.normalizeUsername("  Ana.Lopez "))
        assertTrue(Validators.isValidUsername("ana_lopez"))
        assertFalse(Validators.isValidUsername("an"))
        assertFalse(Validators.isValidUsername("ana lopez"))
    }

    @Test
    fun `la respuesta de seguridad ignora mayusculas tildes y espacios`() {
        assertEquals(PasswordHasher.hashAnswer("  Quetzaltenango "), PasswordHasher.hashAnswer("quetzaltenango"))
        assertEquals(PasswordHasher.hashAnswer("Petén"), PasswordHasher.hashAnswer("peten"))
        assertNotEquals(PasswordHasher.hashAnswer("peten"), PasswordHasher.hashAnswer("xela"))
    }

    @Test
    fun `la contrasena temporal no usa caracteres ambiguos`() {
        repeat(50) {
            val password = PasswordHasher.generateTemporaryPassword()
            assertEquals(8, password.length)
            assertTrue(password.none { it in "01ilo" })
        }
    }
}

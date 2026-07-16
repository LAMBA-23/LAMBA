package com.lamba.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileFormValidatorTest {

    @Test
    fun vehicleValidationAcceptsValidEditableValues() {
        assertNull(
            ProfileFormValidator.vehicleError(
                brand = "Toyota",
                model = "Camry",
                productionYear = "2023",
                mileage = "15000",
            ),
        )
    }

    @Test
    fun vehicleValidationRejectsInvalidValues() {
        assertEquals(
            "Заполните марку автомобиля",
            ProfileFormValidator.vehicleError(" ", "Camry", "2023", "15000"),
        )
        assertEquals(
            "Укажите корректный год выпуска",
            ProfileFormValidator.vehicleError("Toyota", "Camry", "1800", "15000"),
        )
        assertEquals(
            "Укажите корректный пробег",
            ProfileFormValidator.vehicleError("Toyota", "Camry", "2023", "-1"),
        )
    }

    @Test
    fun passwordValidationChecksLengthAndConfirmation() {
        assertEquals(
            "Новый пароль должен содержать от 8 до 128 символов",
            ProfileFormValidator.passwordError("password123", "short", "short"),
        )
        assertEquals(
            "Новые пароли не совпадают",
            ProfileFormValidator.passwordError("password123", "new-password123", "different"),
        )
        assertNull(
            ProfileFormValidator.passwordError(
                "password123",
                "new-password123",
                "new-password123",
            ),
        )
    }
}

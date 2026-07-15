package com.lamba.app

object ProfileFormValidator {
    fun vehicleError(
        brand: String,
        model: String,
        productionYear: String,
        mileage: String,
    ): String? {
        if (brand.isBlank()) return "Заполните марку автомобиля"
        if (model.isBlank()) return "Заполните модель автомобиля"
        val year = productionYear.toIntOrNull()
        if (year == null || year !in 1886..2100) return "Укажите корректный год выпуска"
        if (mileage.toIntOrNull()?.takeIf { it >= 0 } == null) {
            return "Укажите корректный пробег"
        }
        return null
    }

    fun passwordError(
        currentPassword: String,
        newPassword: String,
        confirmation: String,
    ): String? {
        if (currentPassword.isEmpty()) return "Введите текущий пароль"
        if (newPassword.length !in 8..128) {
            return "Новый пароль должен содержать от 8 до 128 символов"
        }
        if (newPassword != confirmation) return "Новые пароли не совпадают"
        return null
    }
}

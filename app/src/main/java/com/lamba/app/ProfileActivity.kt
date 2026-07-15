package com.lamba.app

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import com.lamba.app.chat.LocalChatService
import com.lamba.app.network.ChangePasswordRequest
import com.lamba.app.network.RetrofitClient
import com.lamba.app.network.SessionManager
import com.lamba.app.network.Vehicle
import com.lamba.app.network.VehicleUpdateRequest
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {
    private val localChatRepository by lazy { LocalChatService.getRepository(this) }
    private var vehicle: Vehicle? = null
    private var isSaving = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val userId = SessionManager.getUserId(this)
        if (userId == null) {
            openLogin()
            return
        }

        val etUsername = findViewById<EditText>(R.id.etProfileUsername)
        val etBrand = findViewById<EditText>(R.id.etProfileBrand)
        val etModel = findViewById<EditText>(R.id.etProfileModel)
        val etYear = findViewById<EditText>(R.id.etProfileYear)
        val etMileage = findViewById<EditText>(R.id.etProfileMileage)
        val etCurrentPassword = findViewById<EditText>(R.id.etCurrentPassword)
        val etNewPassword = findViewById<EditText>(R.id.etNewPassword)
        val etNewPasswordConfirmation = findViewById<EditText>(R.id.etNewPasswordConfirmation)
        val tvVehicleError = findViewById<TextView>(R.id.tvVehicleError)
        val tvPasswordError = findViewById<TextView>(R.id.tvPasswordError)
        val tvMileageLocked = findViewById<TextView>(R.id.tvMileageLocked)
        val progressBar = findViewById<ProgressBar>(R.id.profileProgressBar)
        val btnSaveVehicle = findViewById<AppCompatButton>(R.id.btnSaveProfileVehicle)
        val btnChangePassword = findViewById<AppCompatButton>(R.id.btnChangePassword)

        etUsername.setText(SessionManager.getUserName(this) ?: "")
        findViewById<ImageView>(R.id.btnProfileBack).setOnClickListener { finish() }

        fun renderVehicle(current: Vehicle) {
            vehicle = current
            etBrand.setText(current.brand)
            etModel.setText(current.model)
            etYear.setText(current.productionYear.toString())
            etMileage.setText(current.currentMileage.toString())
            etMileage.isEnabled = current.canEditMileage
            tvMileageLocked.visibility = if (current.canEditMileage) View.GONE else View.VISIBLE
        }

        fun setBusy(busy: Boolean) {
            isSaving = busy
            progressBar.visibility = if (busy) View.VISIBLE else View.GONE
            btnSaveVehicle.isEnabled = !busy && vehicle != null
            btnChangePassword.isEnabled = !busy
        }

        fun loadVehicle() {
            setBusy(true)
            lifecycleScope.launch {
                val response = runCatching { RetrofitClient.apiService.getVehicle(userId) }
                setBusy(false)
                response.getOrNull()?.body()?.takeIf { response.getOrNull()?.isSuccessful == true }
                    ?.let(::renderVehicle)
                    ?: run {
                        tvVehicleError.text = "Не удалось загрузить данные автомобиля"
                        tvVehicleError.visibility = View.VISIBLE
                    }
            }
        }

        btnSaveVehicle.setOnClickListener {
            val current = vehicle ?: return@setOnClickListener
            val brand = etBrand.text.toString().trim()
            val model = etModel.text.toString().trim()
            val year = etYear.text.toString().trim()
            val mileage = etMileage.text.toString().trim()
            ProfileFormValidator.vehicleError(brand, model, year, mileage)?.let { error ->
                tvVehicleError.text = error
                tvVehicleError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            tvVehicleError.visibility = View.GONE
            setBusy(true)
            lifecycleScope.launch {
                val response = runCatching {
                    RetrofitClient.apiService.updateVehicle(
                        VehicleUpdateRequest(brand, model, year.toInt(), mileage.toInt()),
                        userId,
                    )
                }.getOrNull()
                setBusy(false)
                if (response?.isSuccessful == true && response.body() != null) {
                    renderVehicle(response.body()!!)
                    tvVehicleError.visibility = View.GONE
                } else {
                    etMileage.setText(current.currentMileage.toString())
                    tvVehicleError.text = "Не удалось сохранить данные автомобиля"
                    tvVehicleError.visibility = View.VISIBLE
                }
            }
        }

        btnChangePassword.setOnClickListener {
            val currentPassword = etCurrentPassword.text.toString()
            val newPassword = etNewPassword.text.toString()
            val confirmation = etNewPasswordConfirmation.text.toString()
            ProfileFormValidator.passwordError(currentPassword, newPassword, confirmation)?.let { error ->
                tvPasswordError.text = error
                tvPasswordError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            tvPasswordError.visibility = View.GONE
            setBusy(true)
            lifecycleScope.launch {
                val response = runCatching {
                    RetrofitClient.apiService.changePassword(
                        ChangePasswordRequest(currentPassword, newPassword, confirmation),
                        userId,
                    )
                }.getOrNull()
                setBusy(false)
                if (response?.isSuccessful == true) {
                    clearSessionAndOpen(LoginActivity::class.java)
                } else {
                    tvPasswordError.text = "Не удалось изменить пароль. Проверьте данные и попробуйте снова."
                    tvPasswordError.visibility = View.VISIBLE
                }
            }
        }

        findViewById<AppCompatButton>(R.id.btnProfileLogout).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Выйти из аккаунта?")
                .setMessage("Мы удалим локальные данные этого аккаунта с устройства.")
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Выйти") { _, _ -> clearSessionAndOpen(WelcomeActivity::class.java) }
                .show()
        }

        loadVehicle()
    }

    private fun clearSessionAndOpen(target: Class<out AppCompatActivity>) {
        lifecycleScope.launch {
            SessionManager.getUserId(this@ProfileActivity)?.let { localChatRepository.clearUserChats(it) }
            SessionManager.clearSession(this@ProfileActivity)
            startActivity(Intent(this@ProfileActivity, target).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }

    private fun openLogin() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}

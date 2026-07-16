package com.lamba.app

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import java.io.InputStream
import java.io.OutputStream

class ProfileActivity : AppCompatActivity() {
    private val localChatRepository by lazy { LocalChatService.getRepository(this) }
    private var vehicle: Vehicle? = null
    private var isSaving = false
    private var isPasswordFormVisible = false

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
        val passwordFormContainer = findViewById<LinearLayout>(R.id.passwordFormContainer)
        val progressBar = findViewById<ProgressBar>(R.id.profileProgressBar)
        val btnSaveVehicle = findViewById<AppCompatButton>(R.id.btnSaveProfileVehicle)
        val btnExport = findViewById<AppCompatButton>(R.id.btnExportProfileData)
        val btnChangePassword = findViewById<AppCompatButton>(R.id.btnChangePassword)
        val btnThemeToggle = findViewById<ImageButton>(R.id.btnProfileThemeToggle)

        etUsername.setText(SessionManager.getUserName(this) ?: "")
        findViewById<ImageView>(R.id.btnProfileBack).setOnClickListener { finish() }

        if (ThemeManager.current(this).isEnabled) {
            btnThemeToggle.setImageResource(R.drawable.ic_lamba_moon)
        } else {
            btnThemeToggle.setImageResource(R.drawable.ic_lamba_sun)
        }

        btnThemeToggle.setOnClickListener {
            val currentlyEnabled = ThemeManager.current(this).isEnabled
            ThemeManager.save(this, !currentlyEnabled)
            startActivity(Intent(this, ProfileActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }

        fun renderVehicle(current: Vehicle) {
            vehicle = current
            etBrand.setText(current.brand)
            etModel.setText(current.model)
            etYear.setText(current.productionYear.toString())
            etMileage.setText(current.currentMileage.toString())
            etMileage.isEnabled = current.canEditMileage
            etMileage.setTextColor(
                Color.parseColor(if (current.canEditMileage) "#101114" else "#77777E"),
            )
            btnSaveVehicle.isEnabled = !isSaving
        }

        fun setBusy(busy: Boolean) {
            isSaving = busy
            progressBar.visibility = if (busy) View.VISIBLE else View.GONE
            btnSaveVehicle.isEnabled = !busy && vehicle != null
            btnChangePassword.isEnabled = !busy
            btnExport.isEnabled = !busy
        }

        val createExportDocument = registerForActivityResult(
            ActivityResultContracts.CreateDocument(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            ),
        ) { uri ->
            if (uri == null) {
                return@registerForActivityResult
            }

            tvVehicleError.visibility = View.GONE
            setBusy(true)
            lifecycleScope.launch {
                val saved = runCatching {
                    val response = RetrofitClient.apiService.exportVehicleData(userId)
                    if (!response.isSuccessful) {
                        return@runCatching false
                    }
                    response.body()?.use { body ->
                        contentResolver.openOutputStream(uri)?.use { output ->
                            ProfileExportWriter.copy(body.byteStream(), output)
                            true
                        }
                    } ?: false
                }.getOrDefault(false)
                setBusy(false)
                if (saved) {
                    Toast.makeText(
                        this@ProfileActivity,
                        "Данные экспортированы",
                        Toast.LENGTH_SHORT,
                    ).show()
                } else {
                    tvVehicleError.text = "Не удалось экспортировать данные. Попробуйте снова."
                    tvVehicleError.visibility = View.VISIBLE
                }
            }
        }

        btnExport.setOnClickListener {
            createExportDocument.launch("LAMBA_export.xlsx")
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
            if (!isPasswordFormVisible) {
                isPasswordFormVisible = true
                passwordFormContainer.visibility = View.VISIBLE
                btnChangePassword.text = "Сохранить пароль"
                return@setOnClickListener
            }

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

internal object ProfileExportWriter {
    fun copy(input: InputStream, output: OutputStream) {
        input.copyTo(output)
    }
}

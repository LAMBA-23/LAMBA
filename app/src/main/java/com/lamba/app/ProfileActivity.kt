package com.lamba.app

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
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

class ProfileActivity : AppCompatActivity() {
    private val localChatRepository by lazy { LocalChatService.getRepository(this) }
    private var vehicle: Vehicle? = null
    private var isSaving = false
    private var isPasswordFormVisible = false

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            SessionManager.saveUserAvatarUri(this, uri.toString())
            findViewById<ImageView>(R.id.ivProfileAvatar).setImageURI(uri)
        }
    }

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
        val btnChangePassword = findViewById<AppCompatButton>(R.id.btnChangePassword)
        val btnThemeToggle = findViewById<ImageButton>(R.id.btnProfileThemeToggle)
        val ivAvatar = findViewById<ImageView>(R.id.ivProfileAvatar)
        val btnEditAvatar = findViewById<ImageButton>(R.id.btnEditAvatar)

        etUsername.setText(SessionManager.getUserName(this) ?: "")
        findViewById<ImageView>(R.id.btnProfileBack).setOnClickListener { finish() }

        SessionManager.getUserAvatarUri(this)?.let { uriString ->
            runCatching {
                val uri = Uri.parse(uriString)
                ivAvatar.setImageURI(uri)
            }
        }

        btnEditAvatar.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

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

        val btnChatStyle = findViewById<AppCompatButton>(R.id.btnChatStyle)
        val styleLabels = mapOf(
            SessionManager.STYLE_FRIENDLY to "Дружелюбный",
            SessionManager.STYLE_SELFISH to "Эгоистичный",
            SessionManager.STYLE_PRAGMATIC to "Прагматичный",
        )
        val styleKeys = listOf(
            SessionManager.STYLE_FRIENDLY,
            SessionManager.STYLE_SELFISH,
            SessionManager.STYLE_PRAGMATIC,
        )

        fun updateStyleButton() {
            val currentStyle = SessionManager.getChatStyle(this)
            btnChatStyle.text = styleLabels[currentStyle] ?: styleLabels[SessionManager.DEFAULT_STYLE]
        }

        updateStyleButton()

        btnChatStyle.setOnClickListener {
            val currentStyle = SessionManager.getChatStyle(this)
            val checkedIndex = styleKeys.indexOf(currentStyle).coerceAtLeast(0)
            val displayItems = styleKeys.map { styleLabels[it] ?: it }.toTypedArray()

            AlertDialog.Builder(this)
                .setTitle("Выберите стиль общения")
                .setSingleChoiceItems(displayItems, checkedIndex) { dialog, which ->
                    SessionManager.saveChatStyle(this, styleKeys[which])
                    updateStyleButton()
                    dialog.dismiss()
                }
                .setNegativeButton("Отмена", null)
                .show()
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

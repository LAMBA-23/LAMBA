package com.lamba.app

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.app.Dialog
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.Button
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.lamba.app.chat.LocalChatService
import com.lamba.app.network.ChangePasswordRequest
import com.lamba.app.network.RetrofitClient
import com.lamba.app.network.SessionManager
import com.lamba.app.network.Vehicle
import com.lamba.app.network.VehicleUpdateRequest
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.io.InputStream
import java.io.OutputStream

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

        val styleOptions = mapOf(
            SessionManager.STYLE_FRIENDLY to findViewById<View>(R.id.styleOptionFriendly),
            SessionManager.STYLE_SELFISH to findViewById<View>(R.id.styleOptionSelfish),
            SessionManager.STYLE_PRAGMATIC to findViewById<View>(R.id.styleOptionPragmatic),
        )

        fun updateStyleSelection() {
            val currentStyle = SessionManager.getChatStyle(this)
            styleOptions.forEach { (key, view) ->
                view.setBackgroundResource(
                    if (key == currentStyle) R.drawable.bg_lamba_style_selected
                    else R.drawable.bg_lamba_style_unselected
                )
            }
        }

        updateStyleSelection()

        styleOptions.forEach { (key, view) ->
            view.setOnClickListener {
                SessionManager.saveChatStyle(this, key)
                updateStyleSelection()
            }
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
            showLogoutConfirmationDialog()
        }

        findViewById<AppCompatButton>(R.id.btnExportProfileData).setOnClickListener {
            lifecycleScope.launch {
                setBusy(true)
                try {
                    val response = RetrofitClient.apiService.exportVehicleData(userId)
                    if (response.isSuccessful && response.body() != null) {
                        val fileName = "lamba_profile_${System.currentTimeMillis()}.xlsx"
                        val outputStream = openFileOutput(fileName, MODE_PRIVATE)
                        ProfileExportWriter.copy(response.body()!!.byteStream(), outputStream)
                        outputStream.close()
                        
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                this@ProfileActivity,
                                "${packageName}.provider",
                                java.io.File(filesDir, fileName)
                            )
                            setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        startActivity(Intent.createChooser(intent, "Открыть отчет"))
                    } else {
                        android.widget.Toast.makeText(this@ProfileActivity, "Ошибка экспорта", android.widget.Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    android.widget.Toast.makeText(this@ProfileActivity, "Не удалось экспортировать данные", android.widget.Toast.LENGTH_SHORT).show()
                } finally {
                    setBusy(false)
                }
            }
        }

        loadVehicle()
    }

    private fun showLogoutConfirmationDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(24.dp, 24.dp, 24.dp, 24.dp)
            background = roundedBackground("#FFFFFF", 24.dp.toFloat())
        }

        container.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(56.dp, 56.dp)
            background = roundedBackground("#FFEBEE", 18.dp.toFloat())
            setImageResource(R.drawable.ic_lamba_close)
            setColorFilter(android.graphics.Color.parseColor("#960018"))
            setPadding(14.dp, 14.dp, 14.dp, 14.dp)
        })

        container.addView(TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 16.dp }
            gravity = Gravity.CENTER
            text = "Выйти из аккаунта?"
            setTextColor(android.graphics.Color.parseColor("#101114"))
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
        })

        container.addView(TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 8.dp }
            gravity = Gravity.CENTER
            text = "Локальные данные чатов этого аккаунта будут удалены с устройства."
            setTextColor(android.graphics.Color.parseColor("#77777E"))
            textSize = 15f
        })

        val btnLogout = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                54.dp
            ).apply { topMargin = 24.dp }
            background = ContextCompat.getDrawable(this@ProfileActivity, R.drawable.bg_lamba_send_button)
            text = "Выйти"
            setTextColor(android.graphics.Color.WHITE)
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            isAllCaps = false
            setOnClickListener {
                dialog.dismiss()
                clearSessionAndOpen(WelcomeActivity::class.java)
            }
        }

        val btnCancel = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                54.dp
            ).apply { topMargin = 8.dp }
            background = ContextCompat.getDrawable(this@ProfileActivity, R.drawable.bg_lamba_style_unselected)
            text = "Отмена"
            setTextColor(android.graphics.Color.parseColor("#101114"))
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            isAllCaps = false
            setOnClickListener { dialog.dismiss() }
        }

        container.addView(btnLogout)
        container.addView(btnCancel)

        dialog.setContentView(container)
        dialog.show()
        dialog.window?.let { window ->
            window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
            window.setLayout(
                (resources.displayMetrics.widthPixels * 0.85f).toInt(),
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun roundedBackground(color: String, radius: Float): GradientDrawable {
        return GradientDrawable().apply {
            setColor(android.graphics.Color.parseColor(color))
            cornerRadius = radius
        }
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()

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

object ProfileExportWriter {
    fun copy(inputStream: InputStream, outputStream: OutputStream) {
        val buffer = ByteArray(4096)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }
    }
}

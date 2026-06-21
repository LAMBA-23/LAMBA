package com.lamba.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.lamba.app.network.RegisterRequest
import com.lamba.app.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val btnBack = findViewById<ImageView>(R.id.btnRegisterBack)
        val etName = findViewById<EditText>(R.id.etRegisterName)
        val etEmail = findViewById<EditText>(R.id.etRegisterEmail)
        val etPassword = findViewById<EditText>(R.id.etRegisterPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etRegisterConfirmPassword)
        val btnSubmit = findViewById<AppCompatButton>(R.id.btnSubmitRegister)
        val tvToLogin = findViewById<TextView>(R.id.tvToLogin)
        val tvError = findViewById<TextView>(R.id.tvRegisterError)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        btnBack.setOnClickListener {
            finish()
        }

        tvToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnSubmit.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            tvError.visibility = View.GONE

            when {
                name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                    showError(tvError, "Пожалуйста, заполните все поля")
                    return@setOnClickListener
                }
                password.length < 8 -> {
                    showError(tvError, "Пароль должен содержать минимум 8 символов")
                    return@setOnClickListener
                }
                password != confirmPassword -> {
                    showError(tvError, "Пароли не совпадают")
                    return@setOnClickListener
                }
            }

            btnSubmit.isEnabled = false
            progressBar.visibility = View.VISIBLE

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitClient.apiService.register(
                        RegisterRequest(username = email, password = password)
                    )
                    val body = response.body()

                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE

                        if (response.isSuccessful && body?.success == true && body.userId != null) {
                            SessionStore.saveUserId(this@RegisterActivity, body.userId)
                            openVehicleSetup(body.userId)
                        } else {
                            btnSubmit.isEnabled = true
                            val message = when (response.code()) {
                                400 -> "Такой аккаунт уже существует"
                                422 -> "Проверьте правильность введённых данных"
                                else -> "Не удалось создать аккаунт"
                            }
                            showError(tvError, message)
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        btnSubmit.isEnabled = true
                        showError(tvError, "Не удалось подключиться к бэкенду")
                    }
                }
            }
        }
    }

    private fun showError(tvError: TextView, message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    private fun openVehicleSetup(userId: Int) {
        val intent = Intent(this, AddVehicleActivity::class.java)
        intent.putExtra("USER_ID", userId)
        startActivity(intent)
        finish()
    }
}

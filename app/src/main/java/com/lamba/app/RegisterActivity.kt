package com.lamba.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.lamba.app.network.RegisterRequest
import com.lamba.app.network.RetrofitClient
import com.lamba.app.network.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etName = findViewById<EditText>(R.id.etRegisterName)
        val etEmail = findViewById<EditText>(R.id.etRegisterEmail)
        val etPassword = findViewById<EditText>(R.id.etRegisterPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etRegisterConfirmPassword)
        val btnSubmit = findViewById<View>(R.id.btnSubmitRegister)
        val btnBack = findViewById<View>(R.id.btnRegisterBack)
        val tvToLogin = findViewById<TextView>(R.id.tvToLogin)

        btnBack.setOnClickListener {
            finish()
        }

        btnSubmit.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (password.length < 8) {
                Toast.makeText(this, "Пароль должен содержать минимум 8 символов", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            btnSubmit.isEnabled = false
            Toast.makeText(this, "Регистрация...", Toast.LENGTH_SHORT).show()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitClient.apiService.register(
                        RegisterRequest(username = email, password = password)
                    )
                    val registeredUserId = response.body()?.userId
                    val registrationSuccessful = response.isSuccessful &&
                        response.body()?.success == true &&
                        registeredUserId != null

                    withContext(Dispatchers.Main) {
                        btnSubmit.isEnabled = true

                        if (registrationSuccessful) {
                            SessionManager.saveUserId(this@RegisterActivity, registeredUserId!!)
                            Toast.makeText(this@RegisterActivity, "Регистрация успешна!", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        } else {
                            val message = when (response.code()) {
                                400 -> "Пользователь с таким email уже существует"
                                422 -> "Проверьте правильность данных"
                                else -> "Не удалось зарегистрироваться"
                            }
                            Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        btnSubmit.isEnabled = true
                        Toast.makeText(this@RegisterActivity, "Не удалось подключиться к бэкенду", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        tvToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}

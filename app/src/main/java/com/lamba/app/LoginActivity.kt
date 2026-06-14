package com.lamba.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.lamba.app.network.LoginRequest
import com.lamba.app.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegisterLink = findViewById<TextView>(R.id.tvRegisterLink)

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnLogin.setOnClickListener {
            val username = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            } else {
                btnLogin.isEnabled = false
                Toast.makeText(this, "Выполняется вход...", Toast.LENGTH_SHORT).show()

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = RetrofitClient.apiService.login(
                            LoginRequest(username = username, password = password)
                        )
                        val loginSuccessful = response.isSuccessful && response.body()?.success == true

                        withContext(Dispatchers.Main) {
                            btnLogin.isEnabled = true

                            if (loginSuccessful) {
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this@LoginActivity, "Неверный логин или пароль", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            btnLogin.isEnabled = true
                            Toast.makeText(this@LoginActivity, "Не удалось подключиться к бэкенду", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

        tvRegisterLink.setOnClickListener {
            Toast.makeText(this, "Переход на экран регистрации", Toast.LENGTH_SHORT).show()
        }
    }
}

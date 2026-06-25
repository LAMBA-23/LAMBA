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
import com.lamba.app.network.SessionManager
import com.lamba.app.network.Vehicle
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
                return@setOnClickListener
            }

            btnLogin.isEnabled = false
            Toast.makeText(this, "Выполняется вход...", Toast.LENGTH_SHORT).show()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitClient.apiService.login(
                        LoginRequest(username = username, password = password)
                    )
                    val body = response.body()

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful && body?.success == true && body.userId != null) {
                            SessionManager.saveUserId(this@LoginActivity, body.userId)
                            SessionManager.saveUserName(this@LoginActivity, username.substringBefore('@').ifBlank { username })
                            routeAfterLogin(body.userId, btnLogin)
                        } else {
                            btnLogin.isEnabled = true
                            Toast.makeText(
                                this@LoginActivity,
                                "Неверный логин или пароль",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        btnLogin.isEnabled = true
                        Toast.makeText(
                            this@LoginActivity,
                            "Не удалось подключиться к бэкенду",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        tvRegisterLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun routeAfterLogin(userId: Int, btnLogin: Button) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val vehicleResponse = RetrofitClient.apiService.getVehicle(userId)

                withContext(Dispatchers.Main) {
                    btnLogin.isEnabled = true

                    val vehicle = vehicleResponse.body()
                    if (vehicleResponse.isSuccessful && vehicle != null) {
                        if (isPlaceholderVehicle(vehicle)) {
                            openVehicleSetup(userId)
                        } else {
                            openMainFlow(userId)
                        }
                    } else if (vehicleResponse.code() == 404) {
                        openVehicleSetup(userId)
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "Не удалось проверить данные автомобиля",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    btnLogin.isEnabled = true
                    Toast.makeText(
                        this@LoginActivity,
                        "Не удалось подключиться к бэкенду",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun isPlaceholderVehicle(vehicle: Vehicle): Boolean {
        return vehicle.brand == "Not set" &&
                vehicle.model == "Not set" &&
                vehicle.productionYear == 0 &&
                vehicle.currentMileage == 0
    }

    private fun openVehicleSetup(userId: Int) {
        val intent = Intent(this, AddVehicleActivity::class.java)
        intent.putExtra("USER_ID", userId)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    private fun openMainFlow(userId: Int) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("USER_ID", userId)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}

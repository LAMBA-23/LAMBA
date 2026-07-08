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

        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        btnLogin.setOnClickListener {
            val username = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    this,
                    "\u0417\u0430\u043f\u043e\u043b\u043d\u0438\u0442\u0435 email \u0438 \u043f\u0430\u0440\u043e\u043b\u044c",
                    Toast.LENGTH_SHORT,
                ).show()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitClient.apiService.login(
                        LoginRequest(username = username, password = password),
                    )
                    val body = response.body()

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful && body?.success == true && body.userId != null) {
                            SessionManager.saveUserId(this@LoginActivity, body.userId)
                            SessionManager.saveUserName(
                                this@LoginActivity,
                                body.name ?: body.username ?: username,
                            )
                            SessionRestoreNavigator.restore(
                                activity = this@LoginActivity,
                                userId = body.userId,
                                onStart = { btnLogin.isEnabled = false },
                                onComplete = { btnLogin.isEnabled = true },
                            )
                        } else {
                            btnLogin.isEnabled = true
                            Toast.makeText(
                                this@LoginActivity,
                                "\u041d\u0435\u0432\u0435\u0440\u043d\u044b\u0439 \u043b\u043e\u0433\u0438\u043d \u0438\u043b\u0438 \u043f\u0430\u0440\u043e\u043b\u044c",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        btnLogin.isEnabled = true
                        Toast.makeText(
                            this@LoginActivity,
                            "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u043e\u0434\u043a\u043b\u044e\u0447\u0438\u0442\u044c\u0441\u044f \u043a \u0431\u044d\u043a\u0435\u043d\u0434\u0443",
                            Toast.LENGTH_LONG,
                        ).show()
                    }
                }
            }
        }

        tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}

package com.lamba.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // UI elements
        val btnBack = findViewById<ImageView>(R.id.btnBack) // button back
        val etEmail = findViewById<EditText>(R.id.etEmail) // email field
        val etPassword = findViewById<EditText>(R.id.etPassword)  // password field
        val btnLogin = findViewById<Button>(R.id.btnLogin)  // log in button
        val tvRegisterLink = findViewById<TextView>(R.id.tvRegisterLink)  // link to registration screen

        // back button pressing
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // processing click on log in button
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Выполняется вход...", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java) 
                startActivity(intent)
                finish() // close the current screen so that user can not return here using back button
            }
        }

        // Registration link
        tvRegisterLink.setOnClickListener {
            Toast.makeText(this, "Переход на экран регистрации", Toast.LENGTH_SHORT).show()
            
        }
    }
}
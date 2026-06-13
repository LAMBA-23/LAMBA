package com.lamba.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val btnCreateAccount = findViewById<View>(R.id.btnCreateAccount)
        val btnWelcomeLogin = findViewById<View>(R.id.btnWelcomeLogin)

        // Create an account button
        btnCreateAccount?.setOnClickListener {
            Toast.makeText(this, "Регистрация будет доступна в MVP-v1", Toast.LENGTH_SHORT).show()
        }

        //  go to log in screen after pressing log in button
        btnWelcomeLogin?.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
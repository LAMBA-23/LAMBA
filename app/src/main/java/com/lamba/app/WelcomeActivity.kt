package com.lamba.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.lamba.app.network.SessionManager

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SessionManager.getUserId(this)?.let { userId ->
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_welcome)

        val btnCreateAccount = findViewById<View>(R.id.btnCreateAccount)
        val btnWelcomeLogin = findViewById<View>(R.id.btnWelcomeLogin)

        btnCreateAccount?.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        btnWelcomeLogin?.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}

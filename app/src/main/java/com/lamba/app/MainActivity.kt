package com.lamba.app

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnTalkToCar = findViewById<ConstraintLayout>(R.id.btnTalkToCar)
        val btnHistory = findViewById<ConstraintLayout>(R.id.btnHistory)
        val navChat = findViewById<LinearLayout>(R.id.navChat)

        btnTalkToCar.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }

        navChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }

        btnHistory.setOnClickListener {
            val intent = Intent(this, com.lamba.app.network.HistoryActivity::class.java)
            startActivity(intent)
        }
    }
}

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
        val navChat = findViewById<LinearLayout>(R.id.navChat)

        // "Talk to car" goes to chat screen
        btnTalkToCar.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }

        // "Chat" goes to chat screen
        navChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }
        // Переход на экран истории и статистики
        val navHistory = findViewById<android.widget.LinearLayout>(R.id.btnTalkToCar)
        navHistory.setOnClickListener {
            val intent = Intent(this, com.lamba.app.network.HistoryActivity::class.java)
            startActivity(intent)
        }
    }
}
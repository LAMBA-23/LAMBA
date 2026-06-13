package com.lamba.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatActivity : AppCompatActivity() {

    private val messageList = mutableListOf<Message>()
    private lateinit var adapter: ChatAdapter
    private lateinit var layoutSuggestions: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        layoutSuggestions = findViewById(R.id.layoutSuggestions)
        val rvChatMessages = findViewById<RecyclerView>(R.id.rvChatMessages)
        val etChatBackMessage = findViewById<EditText>(R.id.etChatBackMessage)
        val btnChatSend = findViewById<ImageButton>(R.id.btnChatSend)
        val navBackToCar = findViewById<LinearLayout>(R.id.navBackToCar)

        // list of messages
        adapter = ChatAdapter(messageList)
        rvChatMessages.layoutManager = LinearLayoutManager(this)
        rvChatMessages.adapter = adapter

        // hints text
        setupSuggestion(R.id.suggestStatus, "Проверить состояние")
        setupSuggestion(R.id.suggestExpenses, "Последние расходы")
        setupSuggestion(R.id.suggestService, "Когда было ТО?")
        setupSuggestion(R.id.suggestAddRecord, "Добавить запись")

        // processing of send button
        btnChatSend.setOnClickListener {
            val text = etChatBackMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
                etChatBackMessage.text.clear()
            }
        }

        // bakc to main screen when button "back to car" was pressed
        navBackToCar.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun setupSuggestion(viewId: Int, title: String) {
        val layout = findViewById<View>(viewId)
        layout.findViewById<TextView>(R.id.tvSuggestionText).text = title
        layout.setOnClickListener {
            sendMessage(title)
        }
    }

    private fun sendMessage(text: String) {
        // hide hints when the conversation started
        layoutSuggestions.visibility = View.GONE
        
        // add user's message
        messageList.add(Message(text, true))
        adapter.notifyItemInserted(messageList.size - 1)

        // IMITATION for the car answer (FOR MVP-V0)
        layoutSuggestions.postDelayed({
            messageList.add(Message("Запись: '$text' сохранена в историю автомобиля", false))
            adapter.notifyItemInserted(messageList.size - 1)
        }, 1000)
    }
}
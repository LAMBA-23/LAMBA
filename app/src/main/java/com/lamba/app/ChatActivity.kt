package com.lamba.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatActivity : AppCompatActivity() {

    private val messageList = mutableListOf<Message>()
    private lateinit var adapter: ChatAdapter
    private lateinit var layoutSuggestions: LinearLayout
    private lateinit var rvChatMessages: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        layoutSuggestions = findViewById(R.id.layoutSuggestions)
        rvChatMessages = findViewById(R.id.rvChatMessages)
        val etChatBackMessage = findViewById<EditText>(R.id.etChatBackMessage)
        val btnChatSend = findViewById<ImageButton>(R.id.btnChatSend)
        val navBackToCar = findViewById<LinearLayout>(R.id.navBackToCar)

        adapter = ChatAdapter(messageList)
        rvChatMessages.layoutManager = LinearLayoutManager(this)
        rvChatMessages.adapter = adapter

        setupSuggestion(R.id.suggestStatus, "Проверить состояние")
        setupSuggestion(R.id.suggestExpenses, "Последние расходы")
        setupSuggestion(R.id.suggestService, "Когда было ТО?")
        setupSuggestion(R.id.suggestAddRecord, "Добавить запись")

        btnChatSend.setOnClickListener {
            val text = etChatBackMessage.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(this, "Введите сообщение", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendMessage(text, btnChatSend)
            etChatBackMessage.text.clear()
        }

        navBackToCar.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun setupSuggestion(viewId: Int, title: String) {
        val layout = findViewById<View>(viewId)
        layout.findViewById<TextView>(R.id.tvSuggestionText).text = title
        layout.setOnClickListener {
            sendMessage(title, null)
        }
    }

    private fun sendMessage(text: String, btnChatSend: ImageButton?) {
        layoutSuggestions.visibility = View.GONE
        btnChatSend?.isEnabled = false

        appendMessage(Message(text, true))

        layoutSuggestions.postDelayed({
            appendMessage(Message("Ответ ассистента: сообщение \"$text\" получено.", false))
            btnChatSend?.isEnabled = true
        }, 1000)
    }

    private fun appendMessage(message: Message) {
        messageList.add(message)
        adapter.notifyItemInserted(messageList.size - 1)
        rvChatMessages.scrollToPosition(messageList.size - 1)
    }
}

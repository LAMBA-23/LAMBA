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
import com.lamba.app.network.ChatParseRequest
import com.lamba.app.network.ParsedEventPayload
import com.lamba.app.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatActivity : AppCompatActivity() {

    private val messageList = mutableListOf<Message>()
    private lateinit var adapter: ChatAdapter
    private lateinit var layoutSuggestions: LinearLayout
    private lateinit var rvChatMessages: RecyclerView
    private lateinit var etChatBackMessage: EditText
    private lateinit var btnChatSend: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        layoutSuggestions = findViewById(R.id.layoutSuggestions)
        rvChatMessages = findViewById(R.id.rvChatMessages)
        etChatBackMessage = findViewById(R.id.etChatBackMessage)
        btnChatSend = findViewById(R.id.btnChatSend)
        val navBackToCar = findViewById<LinearLayout>(R.id.navBackToCar)

        adapter = ChatAdapter(messageList)
        rvChatMessages.layoutManager = LinearLayoutManager(this)
        rvChatMessages.adapter = adapter

        setupSuggestion(
            R.id.suggestStatus,
            "Проверить состояние",
        )
        setupSuggestion(
            R.id.suggestExpenses,
            "Последние расходы",
        )
        setupSuggestion(
            R.id.suggestService,
            "Когда было ТО?",
        )
        setupSuggestion(
            R.id.suggestAddRecord,
            "Добавить запись",
        )

        btnChatSend.setOnClickListener {
            val text = etChatBackMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
                etChatBackMessage.text.clear()
            }
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
            sendMessage(title)
        }
    }

    private fun sendMessage(text: String) {
        layoutSuggestions.visibility = View.GONE
        addMessage(text, isFromUser = true)
        setSendingState(isSending = true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.parseChatEvent(
                    ChatParseRequest(message = text)
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        if (body.status == "parsed" && body.parsedEvent != null) {
                            addMessage(formatParsedEvent(body.parsedEvent), isFromUser = false)
                        } else {
                            addMessage(
                                body.clarificationQuestion
                                    ?: "Уточните, пожалуйста, детали записи.",
                                isFromUser = false,
                            )
                        }
                    } else {
                        addMessage(
                            "Бэкенд не смог распознать запись.",
                            isFromUser = false,
                        )
                    }
                    setSendingState(isSending = false)
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    addMessage(
                        "Не удалось связаться с сервисом распознавания.",
                        isFromUser = false,
                    )
                    setSendingState(isSending = false)
                }
            }
        }
    }

    private fun addMessage(text: String, isFromUser: Boolean) {
        messageList.add(Message(text, isFromUser))
        adapter.notifyItemInserted(messageList.size - 1)
        rvChatMessages.scrollToPosition(messageList.size - 1)
    }

    private fun setSendingState(isSending: Boolean) {
        btnChatSend.isEnabled = !isSending
        etChatBackMessage.isEnabled = !isSending
    }

    private fun formatParsedEvent(event: ParsedEventPayload): String {
        val lines = mutableListOf<String>()
        lines.add("Распознал запись:")
        lines.add("Тип: ${event.type}")
        lines.add("Описание: ${event.description}")
        event.amount?.let {
            lines.add("Сумма: $it")
        }
        event.mileage?.let {
            lines.add("Пробег: $it")
        }
        return lines.joinToString(separator = "\n")
    }
}

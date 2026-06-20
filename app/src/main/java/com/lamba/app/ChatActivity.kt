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
            "\u041f\u0440\u043e\u0432\u0435\u0440\u0438\u0442\u044c "
                + "\u0441\u043e\u0441\u0442\u043e\u044f\u043d\u0438\u0435",
        )
        setupSuggestion(
            R.id.suggestExpenses,
            "\u041f\u043e\u0441\u043b\u0435\u0434\u043d\u0438\u0435 "
                + "\u0440\u0430\u0441\u0445\u043e\u0434\u044b",
        )
        setupSuggestion(
            R.id.suggestService,
            "\u041a\u043e\u0433\u0434\u0430 "
                + "\u0431\u044b\u043b\u043e \u0422\u041e?",
        )
        setupSuggestion(
            R.id.suggestAddRecord,
            "\u0414\u043e\u0431\u0430\u0432\u0438\u0442\u044c "
                + "\u0437\u0430\u043f\u0438\u0441\u044c",
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
                                    ?: "\u0423\u0442\u043e\u0447\u043d\u0438\u0442\u0435, "
                                    + "\u043f\u043e\u0436\u0430\u043b\u0443\u0439\u0441\u0442\u0430, "
                                    + "\u0434\u0435\u0442\u0430\u043b\u0438 "
                                    + "\u0437\u0430\u043f\u0438\u0441\u0438.",
                                isFromUser = false,
                            )
                        }
                    } else {
                        addMessage(
                            "\u0411\u044d\u043a\u0435\u043d\u0434 "
                                + "\u043d\u0435 \u0441\u043c\u043e\u0433 "
                                + "\u0440\u0430\u0441\u043f\u043e\u0437\u043d\u0430\u0442\u044c "
                                + "\u0437\u0430\u043f\u0438\u0441\u044c.",
                            isFromUser = false,
                        )
                    }
                    setSendingState(isSending = false)
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    addMessage(
                        "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c "
                            + "\u0441\u0432\u044f\u0437\u0430\u0442\u044c\u0441\u044f "
                            + "\u0441 \u0441\u0435\u0440\u0432\u0438\u0441\u043e\u043c "
                            + "\u0440\u0430\u0441\u043f\u043e\u0437\u043d\u0430\u0432\u0430\u043d\u0438\u044f.",
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
        lines.add(
            "\u0420\u0430\u0441\u043f\u043e\u0437\u043d\u0430\u043b "
                + "\u0437\u0430\u043f\u0438\u0441\u044c:"
        )
        lines.add(
            "\u0422\u0438\u043f: ${event.type}"
        )
        lines.add(
            "\u041e\u043f\u0438\u0441\u0430\u043d\u0438\u0435: ${event.description}"
        )
        event.amount?.let {
            lines.add("\u0421\u0443\u043c\u043c\u0430: $it")
        }
        event.mileage?.let {
            lines.add("\u041f\u0440\u043e\u0431\u0435\u0433: $it")
        }
        return lines.joinToString(separator = "\n")
    }
}

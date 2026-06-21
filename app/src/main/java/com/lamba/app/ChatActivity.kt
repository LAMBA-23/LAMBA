package com.lamba.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lamba.app.network.ChatFailureStage
import com.lamba.app.network.ChatRepository
import com.lamba.app.network.ChatSendResult
import com.lamba.app.network.Event
import com.lamba.app.network.RetrofitChatBackend
import com.lamba.app.network.RetrofitClient
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private val messageList = mutableListOf<Message>()
    private lateinit var adapter: ChatAdapter
    private lateinit var layoutSuggestions: LinearLayout
    private lateinit var rvChatMessages: RecyclerView
    private lateinit var etChatBackMessage: EditText
    private lateinit var btnChatSend: ImageButton
    private lateinit var progressChatSend: ProgressBar
    private var isSending = false

    private val chatRepository by lazy {
        ChatRepository(RetrofitChatBackend(RetrofitClient.apiService))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        layoutSuggestions = findViewById(R.id.layoutSuggestions)
        rvChatMessages = findViewById(R.id.rvChatMessages)
        etChatBackMessage = findViewById(R.id.etChatBackMessage)
        btnChatSend = findViewById(R.id.btnChatSend)
        progressChatSend = findViewById(R.id.progressChatSend)
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
            if (text.isEmpty()) {
                Toast.makeText(this, "Введите сообщение", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!isSending) {
                etChatBackMessage.text.clear()
                sendMessage(text)
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
        val message = text.trim()
        if (message.isEmpty() || isSending) return

        layoutSuggestions.visibility = View.GONE
        addMessage(message, isFromUser = true)
        setSendingState(isSending = true)

        lifecycleScope.launch {
            try {
                when (val result = chatRepository.sendMessage(message)) {
                    is ChatSendResult.Saved -> {
                        addMessage(formatSavedEvent(result.event), isFromUser = false)
                    }

                    is ChatSendResult.Clarification -> {
                        addMessage(result.question, isFromUser = false)
                    }

                    is ChatSendResult.Failure -> {
                        addMessage(failureMessage(result.stage), isFromUser = false)
                    }
                }
            } finally {
                setSendingState(isSending = false)
            }
        }
    }

    private fun addMessage(text: String, isFromUser: Boolean) {
        messageList.add(Message(text, isFromUser))
        adapter.notifyItemInserted(messageList.size - 1)
        rvChatMessages.scrollToPosition(messageList.size - 1)
    }

    private fun setSendingState(isSending: Boolean) {
        this.isSending = isSending
        btnChatSend.isEnabled = !isSending
        etChatBackMessage.isEnabled = !isSending
        btnChatSend.visibility = if (isSending) View.INVISIBLE else View.VISIBLE
        progressChatSend.visibility = if (isSending) View.VISIBLE else View.GONE
    }

    private fun formatSavedEvent(event: Event): String {
        val eventType = when (event.type) {
            "fuel" -> "\u0417\u0430\u043f\u0440\u0430\u0432\u043a\u0430"
            "repair" -> "\u0420\u0435\u043c\u043e\u043d\u0442"
            "trip" -> "\u041f\u043e\u0435\u0437\u0434\u043a\u0430"
            "issue" -> "\u041f\u0440\u043e\u0431\u043b\u0435\u043c\u0430"
            else -> event.type
        }
        val lines = mutableListOf(
            "\u042f \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u043b\u0430 \u0437\u0430\u043f\u0438\u0441\u044c \u0432 \u0438\u0441\u0442\u043e\u0440\u0438\u0438 BMW M4.",
            "$eventType: ${event.description}",
        )
        if (event.amount > 0) {
            lines.add("\u0421\u0443\u043c\u043c\u0430: ${event.amount} \u20bd")
        }
        lines.add("\u041f\u0440\u043e\u0431\u0435\u0433: ${event.mileage} \u043a\u043c")
        return lines.joinToString(separator = "\n")
    }

    private fun failureMessage(stage: ChatFailureStage): String {
        return when (stage) {
            ChatFailureStage.PARSING ->
                "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043e\u0431\u0440\u0430\u0431\u043e\u0442\u0430\u0442\u044c \u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0435. "
                    + "\u041f\u0440\u043e\u0432\u0435\u0440\u044c\u0442\u0435 \u043f\u043e\u0434\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u0435 \u0438 \u043f\u043e\u043f\u0440\u043e\u0431\u0443\u0439\u0442\u0435 \u0435\u0449\u0451 \u0440\u0430\u0437."

            ChatFailureStage.SAVING ->
                "\u042f \u043f\u043e\u043d\u044f\u043b\u0430 \u0437\u0430\u043f\u0438\u0441\u044c, \u043d\u043e \u043d\u0435 \u0441\u043c\u043e\u0433\u043b\u0430 \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u0435\u0451 \u0432 \u0438\u0441\u0442\u043e\u0440\u0438\u044e. "
                    + "\u041f\u043e\u043f\u0440\u043e\u0431\u0443\u0439\u0442\u0435 \u0435\u0449\u0451 \u0440\u0430\u0437."
        }
    }
}

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
import com.lamba.app.network.SessionManager
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_INITIAL_MESSAGE = "com.lamba.app.extra.INITIAL_MESSAGE"
        const val EXTRA_VEHICLE_NAME = "com.lamba.app.extra.VEHICLE_NAME"
    }

    private val messageList = mutableListOf<Message>()
    private lateinit var adapter: ChatAdapter
    private lateinit var layoutSuggestions: LinearLayout
    private lateinit var rvChatMessages: RecyclerView
    private lateinit var etChatBackMessage: EditText
    private lateinit var btnChatSend: ImageButton
    private lateinit var progressChatSend: ProgressBar
    private lateinit var tvChatStatus: TextView
    private var isSending = false
    private var vehicleName = "машина"

    private val chatRepository by lazy {
        SessionManager.getUserId(this)?.let { userId ->
            ChatRepository(RetrofitChatBackend(RetrofitClient.apiService, userId))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        vehicleName = intent.getStringExtra(EXTRA_VEHICLE_NAME)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: "машина"

        layoutSuggestions = findViewById(R.id.layoutSuggestions)
        rvChatMessages = findViewById(R.id.rvChatMessages)
        etChatBackMessage = findViewById(R.id.etChatBackMessage)
        btnChatSend = findViewById(R.id.btnChatSend)
        progressChatSend = findViewById(R.id.progressChatSend)
        tvChatStatus = findViewById(R.id.tvChatStatus)
        val navBackToCar = findViewById<LinearLayout>(R.id.navBackToCar)

        tvChatStatus.text = vehicleName

        adapter = ChatAdapter(messageList)
        rvChatMessages.layoutManager = LinearLayoutManager(this)
        rvChatMessages.adapter = adapter
        addMessage(initialGreeting(), isFromUser = false)
        loadVehicleNameIfNeeded()

        setupSuggestion(
            R.id.suggestStatus,
            "Проверь состояние автомобиля",
        )
        setupSuggestion(
            R.id.suggestExpenses,
            "Покажи последние расходы",
        )
        setupSuggestion(
            R.id.suggestService,
            "Когда было последнее ТО?",
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

        if (savedInstanceState == null) {
            intent.getStringExtra(EXTRA_INITIAL_MESSAGE)
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?.let { sendMessage(it) }
        }
    }

    private fun setupSuggestion(viewId: Int, title: String) {
        val layout = findViewById<View>(viewId)
        layout.findViewById<TextView>(R.id.tvSuggestionText).text = title
        layout.setOnClickListener {
            sendMessage(title)
        }
    }

    private fun loadVehicleNameIfNeeded() {
        if (vehicleName != "машина") return
        val userId = SessionManager.getUserId(this) ?: return

        lifecycleScope.launch {
            runCatching { RetrofitClient.apiService.getVehicle(userId) }
                .onSuccess { response ->
                    val vehicle = response.body()
                    if (response.isSuccessful && vehicle != null) {
                        vehicleName = "${vehicle.brand} ${vehicle.model}".trim()
                        tvChatStatus.text = vehicleName
                        if (messageList.isNotEmpty() && !messageList[0].isFromUser) {
                            messageList[0] = Message(initialGreeting(), isFromUser = false)
                            adapter.notifyItemChanged(0)
                        }
                    }
                }
        }
    }

    private fun initialGreeting(): String {
        return if (vehicleName == "машина") {
            "Привет. Я твоя машина. До следующей замены масла осталось примерно 1 250 км. Что хочешь узнать?"
        } else {
            "Привет. Я твоя $vehicleName. До следующей замены масла осталось примерно 1 250 км. Что хочешь узнать?"
        }
    }

    private fun sendMessage(text: String) {
        val message = text.trim()
        if (message.isEmpty() || isSending) return

        layoutSuggestions.visibility = View.GONE
        SessionManager.addChatRequest(this, message)
        addMessage(message, isFromUser = true)
        setSendingState(isSending = true)

        val repository = chatRepository
        if (repository == null) {
            addMessage(
                "Войдите в аккаунт, чтобы сохранять записи автомобиля.",
                isFromUser = false,
            )
            setSendingState(isSending = false)
            return
        }

        lifecycleScope.launch {
            try {
                when (val result = repository.sendMessage(message)) {
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
            "fuel" -> "Заправка"
            "repair" -> "Ремонт"
            "trip" -> "Поездка"
            "issue" -> "Проблема"
            "condition" -> "Техническое состояние"
            else -> event.type
        }
        val lines = mutableListOf(
            "Я сохранила запись в истории $vehicleName.",
            "$eventType: ${event.description}",
        )
        if (event.amount > 0) {
            lines.add("Сумма: ${event.amount} ₽")
        }
        lines.add("Пробег: ${event.mileage} км")
        return lines.joinToString(separator = "\n")
    }

    private fun failureMessage(stage: ChatFailureStage): String {
        return when (stage) {
            ChatFailureStage.PARSING ->
                "Не удалось обработать сообщение. " +
                    "Проверьте подключение и попробуйте ещё раз."

            ChatFailureStage.SAVING ->
                "Я поняла запись, но не смогла сохранить её в историю. " +
                    "Попробуйте ещё раз."
        }
    }
}

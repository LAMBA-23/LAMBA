package com.lamba.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lamba.app.chat.ChatMessageType
import com.lamba.app.chat.ChatSender
import com.lamba.app.chat.LocalChatService
import com.lamba.app.chat.LocalChatWithMessages
import com.lamba.app.network.ChatFailureStage
import com.lamba.app.network.ChatContextMessage
import com.lamba.app.network.ChatRepository
import com.lamba.app.network.ChatSendResult
import com.lamba.app.network.Event
import com.lamba.app.network.ChatTitleRequest
import com.lamba.app.network.RetrofitChatBackend
import com.lamba.app.network.RetrofitClient
import com.lamba.app.network.SessionManager
import java.time.Instant
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_INITIAL_MESSAGE = "com.lamba.app.extra.INITIAL_MESSAGE"
        const val EXTRA_VEHICLE_NAME = "com.lamba.app.extra.VEHICLE_NAME"
        const val EXTRA_CHAT_ID = "com.lamba.app.extra.CHAT_ID"
    }

    private val messageList = mutableListOf<Message>()
    private lateinit var adapter: ChatAdapter
    private lateinit var layoutSuggestions: LinearLayout
    private lateinit var rvChatMessages: RecyclerView
    private lateinit var etChatBackMessage: EditText
    private lateinit var btnChatSend: ImageButton
    private lateinit var progressChatSend: ProgressBar
    private lateinit var tvChatStatus: TextView
    private lateinit var tvChatTitle: TextView
    private var isSending = false
    private var vehicleName = "РјР°С€РёРЅР°"
    private var currentChatId: Long? = null
    private var hasPersistedMessages = false
    private var hasGeneratedTitle = false
    private val localChatRepository by lazy { LocalChatService.getRepository(this) }

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
            ?: "РјР°С€РёРЅР°"

        layoutSuggestions = findViewById(R.id.layoutSuggestions)
        rvChatMessages = findViewById(R.id.rvChatMessages)
        etChatBackMessage = findViewById(R.id.etChatBackMessage)
        btnChatSend = findViewById(R.id.btnChatSend)
        progressChatSend = findViewById(R.id.progressChatSend)
        tvChatStatus = findViewById(R.id.tvChatStatus)
        tvChatTitle = findViewById(R.id.tvChatTitle)
        val navBackToCar = findViewById<LinearLayout>(R.id.navBackToCar)

        tvChatStatus.text = vehicleName

        adapter = ChatAdapter(messageList)
        rvChatMessages.layoutManager = LinearLayoutManager(this)
        rvChatMessages.adapter = adapter
        loadVehicleNameIfNeeded()

        setupSuggestion(R.id.suggestStatus, "РџСЂРѕРІРµСЂСЊ СЃРѕСЃС‚РѕСЏРЅРёРµ Р°РІС‚РѕРјРѕР±РёР»СЏ", R.drawable.ic_lamba_timeline)
        setupSuggestion(R.id.suggestExpenses, "РџРѕРєР°Р¶Рё РїРѕСЃР»РµРґРЅРёРµ СЂР°СЃС…РѕРґС‹", R.drawable.ic_lamba_wallet)
        setupSuggestion(R.id.suggestService, "РљРѕРіРґР° Р±С‹Р»Рѕ РїРѕСЃР»РµРґРЅРµРµ РўРћ?", R.drawable.ic_lamba_build)
        setupSuggestion(R.id.suggestAddRecord, "Р”РѕР±Р°РІРёС‚СЊ Р·Р°РїРёСЃСЊ", R.drawable.ic_lamba_add_box)

        btnChatSend.setOnClickListener {
            val text = etChatBackMessage.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(this, "Р’РІРµРґРёС‚Рµ СЃРѕРѕР±С‰РµРЅРёРµ", Toast.LENGTH_SHORT).show()
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
            currentChatId = intent.getLongExtra(EXTRA_CHAT_ID, -1L)
                .takeIf { it > 0 }
                ?: SessionManager.getCurrentChatId(this)
            loadChatState(
                initialMessage = intent.getStringExtra(EXTRA_INITIAL_MESSAGE)
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
            )
        } else if (messageList.isEmpty()) {
            showNewChatGreeting()
        }
    }

    private fun loadChatState(initialMessage: String?) {
        lifecycleScope.launch {
            val persistedChatId = currentChatId
            if (persistedChatId != null) {
                val chat = localChatRepository.getChat(persistedChatId)
                if (chat != null) {
                    bindPersistedChat(chat)
                } else {
                    currentChatId = null
                    SessionManager.clearCurrentChatId(this@ChatActivity)
                    showNewChatGreeting()
                }
            } else {
                showNewChatGreeting()
            }

            initialMessage?.let { sendMessage(it) }
        }
    }

    private fun bindPersistedChat(chat: LocalChatWithMessages) {
        hasPersistedMessages = chat.messages.isNotEmpty()
        tvChatTitle.text = chat.chat.title
        hasGeneratedTitle = chat.chat.title != "LAMBA"
        messageList.clear()
        chat.messages.forEach { storedMessage ->
            messageList.add(
                Message(
                    text = storedMessage.text,
                    isFromUser = storedMessage.sender == ChatSender.USER,
                ),
            )
        }
        adapter.notifyDataSetChanged()
        if (messageList.isNotEmpty()) {
            rvChatMessages.scrollToPosition(messageList.size - 1)
        }
        layoutSuggestions.visibility = if (messageList.size <= 1) View.VISIBLE else View.GONE
    }

    private fun showNewChatGreeting() {
        hasPersistedMessages = false
        hasGeneratedTitle = false
        tvChatTitle.text = "LAMBA"
        messageList.clear()
        adapter.notifyDataSetChanged()
        addMessage(initialGreeting(), isFromUser = false)
        layoutSuggestions.visibility = View.VISIBLE
    }

    private fun setupSuggestion(viewId: Int, title: String, iconRes: Int) {
        val layout = findViewById<View>(viewId)
        layout.findViewById<TextView>(R.id.tvSuggestionText).text = title
        layout.findViewById<ImageView>(R.id.suggestionIcon).setImageResource(iconRes)
        layout.setOnClickListener { sendMessage(title) }
    }

    private fun loadVehicleNameIfNeeded() {
        if (vehicleName != "РјР°С€РёРЅР°") return
        val userId = SessionManager.getUserId(this) ?: return

        lifecycleScope.launch {
            runCatching { RetrofitClient.apiService.getVehicle(userId) }
                .onSuccess { response ->
                    val vehicle = response.body()
                    if (response.isSuccessful && vehicle != null) {
                        vehicleName = "${vehicle.brand} ${vehicle.model}".trim()
                        tvChatStatus.text = vehicleName
                        if (!hasPersistedMessages && messageList.isNotEmpty() && !messageList[0].isFromUser) {
                            messageList[0] = Message(initialGreeting(), isFromUser = false)
                            adapter.notifyItemChanged(0)
                        }
                    }
                }
        }
    }

    private fun initialGreeting(): String {
        return if (vehicleName == "РјР°С€РёРЅР°") {
            "РџСЂРёРІРµС‚. РЇ С‚РІРѕСЏ РјР°С€РёРЅР°. Р”Рѕ СЃР»РµРґСѓСЋС‰РµР№ Р·Р°РјРµРЅС‹ РјР°СЃР»Р° РѕСЃС‚Р°Р»РѕСЃСЊ РїСЂРёРјРµСЂРЅРѕ 1 250 РєРј. Р§С‚Рѕ С…РѕС‡РµС€СЊ СѓР·РЅР°С‚СЊ?"
        } else {
            "РџСЂРёРІРµС‚. РЇ С‚РІРѕСЏ $vehicleName. Р”Рѕ СЃР»РµРґСѓСЋС‰РµР№ Р·Р°РјРµРЅС‹ РјР°СЃР»Р° РѕСЃС‚Р°Р»РѕСЃСЊ РїСЂРёРјРµСЂРЅРѕ 1 250 РєРј. Р§С‚Рѕ С…РѕС‡РµС€СЊ СѓР·РЅР°С‚СЊ?"
        }
    }

    private fun sendMessage(text: String) {
        val message = text.trim()
        if (message.isEmpty() || isSending) return

        layoutSuggestions.visibility = View.GONE
        val previousChatContext = buildCurrentChatContext()
        addMessage(message, isFromUser = true)
        setSendingState(isSending = true)

        val repository = chatRepository
        if (repository == null) {
            val errorText =
                "Р’РѕР№РґРёС‚Рµ РІ Р°РєРєР°СѓРЅС‚, С‡С‚РѕР±С‹ СЃРѕС…СЂР°РЅСЏС‚СЊ Р·Р°РїРёСЃРё Р°РІС‚РѕРјРѕР±РёР»СЏ."
            addMessage(errorText, isFromUser = false)
            lifecycleScope.launch {
                persistAssistantReplyIfNeeded(message, errorText, ChatMessageType.ERROR)
                setSendingState(isSending = false)
            }
            return
        }

        lifecycleScope.launch {
            try {
                when (val result = repository.sendMessage(message, previousChatContext)) {
                    is ChatSendResult.Saved -> {
                        val responseText = formatSavedEvent(result.event)
                        addMessage(responseText, isFromUser = false)
                        persistAssistantReplyIfNeeded(
                            userMessage = message,
                            assistantReply = responseText,
                            messageType = ChatMessageType.EVENT_CONFIRMATION,
                        )
                    }

                    is ChatSendResult.Clarification -> {
                        addMessage(result.question, isFromUser = false)
                        persistAssistantReplyIfNeeded(
                            userMessage = message,
                            assistantReply = result.question,
                            messageType = ChatMessageType.CLARIFICATION,
                        )
                    }

                    is ChatSendResult.Answer -> {
                        addMessage(result.text, isFromUser = false)
                        persistAssistantReplyIfNeeded(
                            userMessage = message,
                            assistantReply = result.text,
                            messageType = ChatMessageType.ASSISTANT,
                        )
                    }

                    is ChatSendResult.Failure -> {
                        val errorText = failureMessage(result.stage)
                        addMessage(errorText, isFromUser = false)
                        persistAssistantReplyIfNeeded(
                            userMessage = message,
                            assistantReply = errorText,
                            messageType = ChatMessageType.ERROR,
                        )
                    }
                }
            } finally {
                setSendingState(isSending = false)
            }
        }
    }

    private suspend fun persistAssistantReplyIfNeeded(
        userMessage: String,
        assistantReply: String,
        messageType: ChatMessageType,
    ) {
        val userId = SessionManager.getUserId(this) ?: return
        val now = Instant.now()
        val existingChatId = currentChatId

        if (existingChatId == null || !hasPersistedMessages) {
            val createdChatId = localChatRepository.createChatWithFirstExchange(
                userId = userId,
                greeting = initialGreeting(),
                userMessage = userMessage,
                assistantMessage = assistantReply,
                assistantMessageType = messageType,
                createdAt = now,
            )
            currentChatId = createdChatId
            hasPersistedMessages = true
            SessionManager.setCurrentChatId(this, createdChatId)
            localChatRepository.getChat(createdChatId)?.let { createdChat ->
                tvChatTitle.text = createdChat.chat.title
            }
            if (messageType != ChatMessageType.ERROR) {
                maybeGenerateChatTitle(createdChatId, userMessage, assistantReply)
            }
            return
        }

        localChatRepository.appendMessage(
            chatId = existingChatId,
            sender = ChatSender.USER,
            text = userMessage,
            type = ChatMessageType.USER,
            createdAt = now,
        )
        localChatRepository.appendMessage(
            chatId = existingChatId,
            sender = ChatSender.ASSISTANT,
            text = assistantReply,
            type = messageType,
            createdAt = now,
        )
    }

    private fun buildCurrentChatContext(): List<ChatContextMessage> {
        if (currentChatId == null || !hasPersistedMessages) {
            return emptyList()
        }
        return messageList.mapNotNull { message ->
            val sender = if (message.isFromUser) "user" else "assistant"
            val text = message.text.trim()
            if (text.isEmpty()) {
                null
            } else {
                ChatContextMessage(sender = sender, text = text)
            }
        }
    }

    private suspend fun maybeGenerateChatTitle(
        chatId: Long,
        firstUserMessage: String,
        firstAssistantReply: String,
    ) {
        if (hasGeneratedTitle) return
        val userId = SessionManager.getUserId(this) ?: return

        val generatedTitle = runCatching {
            val response = RetrofitClient.apiService.chatTitle(
                ChatTitleRequest(
                    firstUserMessage = firstUserMessage,
                    firstAssistantReply = firstAssistantReply,
                ),
                userId,
            )
            if (!response.isSuccessful) {
                null
            } else {
                response.body()?.title
            }
        }.getOrNull()

        val finalTitle = generatedTitle
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: firstUserMessage.trim().replace(Regex("\\s+"), " ").take(50)

        if (finalTitle.isBlank()) return

        localChatRepository.updateChatTitle(chatId, finalTitle)
        tvChatTitle.text = finalTitle
        hasGeneratedTitle = true
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
            "fuel" -> "Р—Р°РїСЂР°РІРєР°"
            "repair" -> "Р РµРјРѕРЅС‚"
            "trip" -> "РџРѕРµР·РґРєР°"
            "issue" -> "РџСЂРѕР±Р»РµРјР°"
            else -> event.type
        }
        val lines = mutableListOf(
            "РЇ СЃРѕС…СЂР°РЅРёР»Р° Р·Р°РїРёСЃСЊ РІ РёСЃС‚РѕСЂРёРё $vehicleName.",
            "$eventType: ${event.description}",
        )
        if (event.amount > 0) {
            lines.add("РЎСѓРјРјР°: ${event.amount} в‚Ѕ")
        }
        if (event.type == "trip" || event.mileage > 0) {
            lines.add("РџСЂРѕР±РµРі: ${event.mileage} РєРј")
        }
        return lines.joinToString(separator = "\n")
    }

    private fun failureMessage(stage: ChatFailureStage): String {
        return when (stage) {
            ChatFailureStage.PARSING ->
                "РќРµ СѓРґР°Р»РѕСЃСЊ РѕР±СЂР°Р±РѕС‚Р°С‚СЊ СЃРѕРѕР±С‰РµРЅРёРµ. " +
                    "РџСЂРѕРІРµСЂСЊС‚Рµ РїРѕРґРєР»СЋС‡РµРЅРёРµ Рё РїРѕРїСЂРѕР±СѓР№С‚Рµ РµС‰С‘ СЂР°Р·."

            ChatFailureStage.SAVING ->
                "РЇ РїРѕРЅСЏР»Р° Р·Р°РїРёСЃСЊ, РЅРѕ РЅРµ СЃРјРѕРіР»Р° СЃРѕС…СЂР°РЅРёС‚СЊ РµС‘ РІ РёСЃС‚РѕСЂРёСЋ. " +
                    "РџРѕРїСЂРѕР±СѓР№С‚Рµ РµС‰С‘ СЂР°Р·."
        }
    }
}

package com.lamba.app

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.lamba.app.chat.LocalChatService
import com.lamba.app.network.RetrofitClient
import com.lamba.app.network.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private var userId: Int = -1
    private lateinit var drawerOverlay: View
    private lateinit var menuRequests: LinearLayout
    private lateinit var menuProfile: LinearLayout
    private lateinit var notificationDot: View
    private var vehicleName: String = "машина"
    private val localChatRepository by lazy { LocalChatService.getRepository(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userId = intent.getIntExtra("USER_ID", -1)
        if (userId == -1) {
            userId = SessionManager.getUserId(this) ?: -1
        }

        val tvProfileName = findViewById<TextView>(R.id.tvProfileName)
        val btnTalkToCar = findViewById<View>(R.id.btnTalkToCar)
        val btnExpenses = findViewById<View>(R.id.btnExpenses)
        val btnService = findViewById<View>(R.id.btnService)
        val btnAddRecord = findViewById<View>(R.id.btnAddRecord)
        val etHomeMessage = findViewById<EditText>(R.id.etHomeMessage)
        val btnHomeSend = findViewById<ImageButton>(R.id.btnHomeSend)
        val btnMenu = findViewById<ImageButton>(R.id.btnMenu)
        val ivNotification = findViewById<ImageView>(R.id.ivNotification)
        notificationDot = findViewById(R.id.notificationDot)
        drawerOverlay = findViewById(R.id.drawerOverlay)
        val drawerScrim = findViewById<View>(R.id.drawerScrim)
        val btnDrawerClose = findViewById<ImageButton>(R.id.btnDrawerClose)
        val menuHistory = findViewById<LinearLayout>(R.id.menuHistory)
        val menuStats = findViewById<LinearLayout>(R.id.menuStats)
        menuRequests = findViewById(R.id.menuRequests)
        menuProfile = findViewById(R.id.menuProfile)

        tvProfileName.text = SessionManager.getUserName(this) ?: "Пользователь"
        renderChatHistory(menuRequests, drawerOverlay)

        btnTalkToCar.setOnClickListener {
            openChatWithMessage("Проверить состояние автомобиля")
        }

        btnExpenses.setOnClickListener {
            openChatWithMessage("Покажи последние расходы")
        }

        btnService.setOnClickListener {
            openChatWithMessage("Когда было последнее ТО?")
        }

        btnAddRecord.setOnClickListener {
            openChatWithMessage("Добавить запись")
        }

        btnHomeSend.setOnClickListener {
            val text = etHomeMessage.text.toString().trim().ifBlank { "Привет" }
            etHomeMessage.text.clear()
            openChatWithMessage(text)
        }

        etHomeMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                btnHomeSend.performClick()
                true
            } else {
                false
            }
        }

        btnMenu.setOnClickListener {
            drawerOverlay.visibility = View.VISIBLE
        }

        ivNotification.setOnClickListener {
            openNotifications()
        }

        drawerScrim.setOnClickListener {
            drawerOverlay.visibility = View.GONE
        }

        btnDrawerClose.setOnClickListener {
            drawerOverlay.visibility = View.GONE
        }

        menuHistory.setOnClickListener {
            drawerOverlay.visibility = View.GONE
            startActivity(Intent(this, com.lamba.app.network.HistoryActivity::class.java))
        }

        menuStats.setOnClickListener {
            drawerOverlay.visibility = View.GONE
            startActivity(Intent(this, com.lamba.app.network.StatisticsActivity::class.java))
        }

        menuProfile.setOnClickListener {
            drawerOverlay.visibility = View.GONE
            startActivity(Intent(this, ProfileActivity::class.java))
        }

    }

    override fun onResume() {
        super.onResume()
        if (::menuRequests.isInitialized && ::drawerOverlay.isInitialized) {
            renderChatHistory(menuRequests, drawerOverlay)
        }
        refreshNotificationBadge()
        loadVehicleData(
            findViewById(R.id.tvHeader),
            findViewById(R.id.tvCarName),
            findViewById(R.id.tvCarInfo),
        )
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    private fun openChatWithMessage(initialMessage: String) {
        SessionManager.clearCurrentChatId(this)
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra(ChatActivity.EXTRA_INITIAL_MESSAGE, initialMessage)
        intent.putExtra(ChatActivity.EXTRA_VEHICLE_NAME, vehicleName)
        startActivity(intent)
    }

    private fun openChat(chatId: Long? = null) {
        val intent = Intent(this, ChatActivity::class.java)
        if (chatId != null) {
            intent.putExtra(ChatActivity.EXTRA_CHAT_ID, chatId)
        }
        intent.putExtra(ChatActivity.EXTRA_VEHICLE_NAME, vehicleName)
        startActivity(intent)
    }

    private fun renderChatHistory(container: LinearLayout, drawerOverlay: View) {
        lifecycleScope.launch {
            val chats = SessionManager.getUserId(this@MainActivity)
                ?.let { localChatRepository.getChatsForUser(it) }
                .orEmpty()

            container.removeAllViews()
            chats.forEach { chat ->
                val item = TextView(this@MainActivity).apply {
                    text = chat.chat.title
                    setTextColor(android.graphics.Color.parseColor("#101114"))
                    textSize = 18f
                    maxLines = 2
                    ellipsize = android.text.TextUtils.TruncateAt.END
                    setPadding(0, 12.dp, 0, 12.dp)
                    setOnClickListener {
                        drawerOverlay.visibility = View.GONE
                        SessionManager.setCurrentChatId(this@MainActivity, chat.chat.id)
                        openChat(chat.chat.id)
                    }
                }
                container.addView(item)
                container.addView(View(this@MainActivity).apply {
                    setBackgroundColor(android.graphics.Color.parseColor("#E7E7EA"))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1,
                    )
                })
            }
        }
    }

    private fun loadVehicleData(tvHeader: TextView, tvCarName: TextView, tvCarInfo: TextView) {
        if (userId == -1) {
            vehicleName = "машина"
            tvHeader.text = "Привет! Я твоя машина."
            tvCarName.text = "Автомобиль не добавлен"
            tvCarInfo.text = "Добавьте автомобиль, чтобы начать"
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.getVehicle(userId)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val vehicle = response.body()!!
                        vehicleName = "${vehicle.brand} ${vehicle.model}".trim()
                        tvHeader.text = "Привет! Я твоя $vehicleName."
                        tvCarName.text = vehicleName
                        tvCarInfo.text = "${vehicle.currentMileage} км • ${vehicle.productionYear}"
                    } else if (response.code() == 404) {
                        val intent = Intent(this@MainActivity, AddVehicleActivity::class.java)
                        intent.putExtra("USER_ID", userId)
                        startActivity(intent)
                        finish()
                    } else {
                        vehicleName = "машина"
                        tvHeader.text = "Привет! Я твоя машина."
                        tvCarName.text = "Ошибка загрузки"
                        tvCarInfo.text = "Попробуйте позже"
                    }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    vehicleName = "машина"
                    tvHeader.text = "Привет! Я твоя машина."
                    tvCarName.text = "Автомобиль не добавлен"
                    tvCarInfo.text = "Добавьте автомобиль, чтобы начать"
                }
            }
        }
    }

    private fun openNotifications() {
        if (userId == -1) {
            AlertDialog.Builder(this)
                .setTitle("Уведомления")
                .setMessage("Не удалось определить пользователя.")
                .setPositiveButton("ОК", null)
                .show()
            return
        }

        startActivity(Intent(this, NotificationsActivity::class.java))
    }

    private fun refreshNotificationBadge() {
        if (userId == -1 || !::notificationDot.isInitialized) {
            return
        }

        lifecycleScope.launch {
            runCatching { RetrofitClient.apiService.getRecommendations(userId) }
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        notificationDot.visibility =
                            if (SessionManager.hasUnreadRecommendations(
                                    this@MainActivity,
                                    userId,
                                    response.body()?.recommendations.orEmpty(),
                                )
                            ) {
                                View.VISIBLE
                            } else {
                                View.GONE
                            }
                    }
                }
        }
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}

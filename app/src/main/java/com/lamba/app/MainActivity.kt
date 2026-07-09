package com.lamba.app

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
    private var vehicleName: String = "машина"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userId = intent.getIntExtra("USER_ID", -1)
        if (userId == -1) {
            userId = SessionManager.getUserId(this) ?: -1
        }

        val tvHeader = findViewById<TextView>(R.id.tvHeader)
        val tvCarName = findViewById<TextView>(R.id.tvCarName)
        val tvCarInfo = findViewById<TextView>(R.id.tvCarInfo)
        val tvProfileName = findViewById<TextView>(R.id.tvProfileName)
        val cardCar = findViewById<View>(R.id.cardCar)
        val btnTalkToCar = findViewById<View>(R.id.btnTalkToCar)
        val btnExpenses = findViewById<View>(R.id.btnExpenses)
        val btnService = findViewById<View>(R.id.btnService)
        val btnAddRecord = findViewById<View>(R.id.btnAddRecord)
        val etHomeMessage = findViewById<EditText>(R.id.etHomeMessage)
        val btnHomeSend = findViewById<ImageButton>(R.id.btnHomeSend)
        val btnMenu = findViewById<ImageButton>(R.id.btnMenu)
        drawerOverlay = findViewById(R.id.drawerOverlay)
        val drawerScrim = findViewById<View>(R.id.drawerScrim)
        val btnDrawerClose = findViewById<ImageButton>(R.id.btnDrawerClose)
        val menuHistory = findViewById<LinearLayout>(R.id.menuHistory)
        val menuStats = findViewById<LinearLayout>(R.id.menuStats)
        val menuLogout = findViewById<LinearLayout>(R.id.menuLogout)
        menuRequests = findViewById(R.id.menuRequests)
        val menuProfile = findViewById<LinearLayout>(R.id.menuProfile)

        tvProfileName.text = SessionManager.getUserName(this) ?: "Пользователь"
        renderRequestHistory(menuRequests, drawerOverlay)

        btnTalkToCar.setOnClickListener {
            openChat("Проверь состояние автомобиля")
        }

        btnExpenses.setOnClickListener {
            openChat("Покажи последние расходы")
        }

        btnService.setOnClickListener {
            openChat("Когда было последнее ТО?")
        }

        btnAddRecord.setOnClickListener {
            openChat("Добавить запись")
        }

        btnHomeSend.setOnClickListener {
            val text = etHomeMessage.text.toString().trim().ifBlank { "Привет" }
            etHomeMessage.text.clear()
            openChat(text)
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

        menuLogout.setOnClickListener {
            drawerOverlay.visibility = View.GONE
            showLogoutDialog()
        }

        loadVehicleData(tvHeader, tvCarName, tvCarInfo)
    }

    override fun onResume() {
        super.onResume()
        if (::menuRequests.isInitialized && ::drawerOverlay.isInitialized) {
            renderRequestHistory(menuRequests, drawerOverlay)
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    private fun openChat(initialMessage: String) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra(ChatActivity.EXTRA_INITIAL_MESSAGE, initialMessage)
        intent.putExtra(ChatActivity.EXTRA_VEHICLE_NAME, vehicleName)
        startActivity(intent)
    }

    private fun renderRequestHistory(container: LinearLayout, drawerOverlay: View) {
        container.removeAllViews()
        SessionManager.getChatRequests(this).forEach { request ->
            val item = TextView(this).apply {
                text = request
                setTextColor(android.graphics.Color.parseColor("#101114"))
                textSize = 18f
                maxLines = 2
                ellipsize = android.text.TextUtils.TruncateAt.END
                setPadding(0, 12.dp, 0, 12.dp)
                setOnClickListener {
                    drawerOverlay.visibility = View.GONE
                    openChat(request)
                }
            }
            container.addView(item)
            container.addView(View(this).apply {
                setBackgroundColor(android.graphics.Color.parseColor("#E7E7EA"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1,
                )
            })
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
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    vehicleName = "машина"
                    tvHeader.text = "Привет! Я твоя машина."
                    tvCarName.text = "Автомобиль не добавлен"
                    tvCarInfo.text = "Добавьте автомобиль, чтобы начать"
                }
            }
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Выйти из аккаунта?")
            .setMessage("Мы удалим локальные данные этого аккаунта с устройства.")
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Выйти") { _, _ ->
                SessionManager.clearSession(this)
                val intent = Intent(this, WelcomeActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
            .show()
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}

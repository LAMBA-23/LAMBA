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
    private lateinit var logoutPopup: TextView
    private lateinit var menuProfile: LinearLayout
    private var isLogoutPopupVisible = false
    private var vehicleName: String = "РјР°С€РёРЅР°"

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
        val btnTalkToCar = findViewById<View>(R.id.btnTalkToCar)
        val btnExpenses = findViewById<View>(R.id.btnExpenses)
        val btnService = findViewById<View>(R.id.btnService)
        val btnAddRecord = findViewById<View>(R.id.btnAddRecord)
        val etHomeMessage = findViewById<EditText>(R.id.etHomeMessage)
        val btnHomeSend = findViewById<ImageButton>(R.id.btnHomeSend)
        val btnMenu = findViewById<ImageButton>(R.id.btnMenu)
        drawerOverlay = findViewById(R.id.drawerOverlay)
        val drawerPanel = findViewById<LinearLayout>(R.id.drawerPanel)
        val drawerScrim = findViewById<View>(R.id.drawerScrim)
        val btnDrawerClose = findViewById<ImageButton>(R.id.btnDrawerClose)
        val menuHistory = findViewById<LinearLayout>(R.id.menuHistory)
        val menuStats = findViewById<LinearLayout>(R.id.menuStats)
        menuRequests = findViewById(R.id.menuRequests)
        logoutPopup = findViewById(R.id.logoutPopup)
        menuProfile = findViewById(R.id.menuProfile)

        tvProfileName.text = SessionManager.getUserName(this) ?: "РџРѕР»СЊР·РѕРІР°С‚РµР»СЊ"
        renderRequestHistory(menuRequests, drawerOverlay)

        btnTalkToCar.setOnClickListener {
            openChat("РџСЂРѕРІРµСЂСЊ СЃРѕСЃС‚РѕСЏРЅРёРµ Р°РІС‚РѕРјРѕР±РёР»СЏ")
        }

        btnExpenses.setOnClickListener {
            openChat("РџРѕРєР°Р¶Рё РїРѕСЃР»РµРґРЅРёРµ СЂР°СЃС…РѕРґС‹")
        }

        btnService.setOnClickListener {
            openChat("РљРѕРіРґР° Р±С‹Р»Рѕ РїРѕСЃР»РµРґРЅРµРµ РўРћ?")
        }

        btnAddRecord.setOnClickListener {
            openChat("Р”РѕР±Р°РІРёС‚СЊ Р·Р°РїРёСЃСЊ")
        }

        btnHomeSend.setOnClickListener {
            val text = etHomeMessage.text.toString().trim().ifBlank { "РџСЂРёРІРµС‚" }
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
            dismissLogoutPopup(immediate = true)
            drawerOverlay.visibility = View.VISIBLE
        }

        drawerScrim.setOnClickListener {
            dismissLogoutPopup(immediate = true)
            drawerOverlay.visibility = View.GONE
        }

        drawerPanel.setOnClickListener {
            dismissLogoutPopup()
        }

        btnDrawerClose.setOnClickListener {
            dismissLogoutPopup(immediate = true)
            drawerOverlay.visibility = View.GONE
        }

        menuHistory.setOnClickListener {
            dismissLogoutPopup(immediate = true)
            drawerOverlay.visibility = View.GONE
            startActivity(Intent(this, com.lamba.app.network.HistoryActivity::class.java))
        }

        menuStats.setOnClickListener {
            dismissLogoutPopup(immediate = true)
            drawerOverlay.visibility = View.GONE
            startActivity(Intent(this, com.lamba.app.network.StatisticsActivity::class.java))
        }

        menuProfile.setOnClickListener {
            toggleLogoutPopup()
        }

        logoutPopup.setOnClickListener {
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
        if (isLogoutPopupVisible) {
            dismissLogoutPopup()
            return
        }
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
                    dismissLogoutPopup(immediate = true)
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
            vehicleName = "РјР°С€РёРЅР°"
            tvHeader.text = "РџСЂРёРІРµС‚! РЇ С‚РІРѕСЏ РјР°С€РёРЅР°."
            tvCarName.text = "РђРІС‚РѕРјРѕР±РёР»СЊ РЅРµ РґРѕР±Р°РІР»РµРЅ"
            tvCarInfo.text = "Р”РѕР±Р°РІСЊС‚Рµ Р°РІС‚РѕРјРѕР±РёР»СЊ, С‡С‚РѕР±С‹ РЅР°С‡Р°С‚СЊ"
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.getVehicle(userId)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val vehicle = response.body()!!
                        vehicleName = "${vehicle.brand} ${vehicle.model}".trim()
                        tvHeader.text = "РџСЂРёРІРµС‚! РЇ С‚РІРѕСЏ $vehicleName."
                        tvCarName.text = vehicleName
                        tvCarInfo.text = "${vehicle.currentMileage} РєРј вЂў ${vehicle.productionYear}"
                    } else if (response.code() == 404) {
                        val intent = Intent(this@MainActivity, AddVehicleActivity::class.java)
                        intent.putExtra("USER_ID", userId)
                        startActivity(intent)
                        finish()
                    } else {
                        vehicleName = "РјР°С€РёРЅР°"
                        tvHeader.text = "РџСЂРёРІРµС‚! РЇ С‚РІРѕСЏ РјР°С€РёРЅР°."
                        tvCarName.text = "РћС€РёР±РєР° Р·Р°РіСЂСѓР·РєРё"
                        tvCarInfo.text = "РџРѕРїСЂРѕР±СѓР№С‚Рµ РїРѕР·Р¶Рµ"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    vehicleName = "РјР°С€РёРЅР°"
                    tvHeader.text = "РџСЂРёРІРµС‚! РЇ С‚РІРѕСЏ РјР°С€РёРЅР°."
                    tvCarName.text = "РђРІС‚РѕРјРѕР±РёР»СЊ РЅРµ РґРѕР±Р°РІР»РµРЅ"
                    tvCarInfo.text = "Р”РѕР±Р°РІСЊС‚Рµ Р°РІС‚РѕРјРѕР±РёР»СЊ, С‡С‚РѕР±С‹ РЅР°С‡Р°С‚СЊ"
                }
            }
        }
    }

    private fun showLogoutDialog() {
        dismissLogoutPopup(immediate = true)
        AlertDialog.Builder(this)
            .setTitle("Р’С‹Р№С‚Рё РёР· Р°РєРєР°СѓРЅС‚Р°?")
            .setMessage("РњС‹ СѓРґР°Р»РёРј Р»РѕРєР°Р»СЊРЅС‹Рµ РґР°РЅРЅС‹Рµ СЌС‚РѕРіРѕ Р°РєРєР°СѓРЅС‚Р° СЃ СѓСЃС‚СЂРѕР№СЃС‚РІР°.")
            .setNegativeButton("РћС‚РјРµРЅР°", null)
            .setPositiveButton("Р’С‹Р№С‚Рё") { _, _ ->
                SessionManager.clearSession(this)
                val intent = Intent(this, WelcomeActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
            .show()
    }

    private fun toggleLogoutPopup() {
        if (isLogoutPopupVisible) {
            dismissLogoutPopup()
        } else {
            showLogoutPopup()
        }
    }

    private fun showLogoutPopup() {
        logoutPopup.animate().cancel()
        isLogoutPopupVisible = true
        logoutPopup.alpha = 0f
        logoutPopup.translationY = 12.dp.toFloat()
        logoutPopup.visibility = View.VISIBLE
        logoutPopup.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(180L)
            .start()
    }

    private fun dismissLogoutPopup(immediate: Boolean = false) {
        if (!isLogoutPopupVisible && logoutPopup.visibility != View.VISIBLE) {
            return
        }

        logoutPopup.animate().cancel()
        isLogoutPopupVisible = false

        if (immediate) {
            logoutPopup.visibility = View.GONE
            logoutPopup.alpha = 1f
            logoutPopup.translationY = 0f
            return
        }

        logoutPopup.animate()
            .alpha(0f)
            .translationY(12.dp.toFloat())
            .setDuration(160L)
            .withEndAction {
                logoutPopup.visibility = View.GONE
                logoutPopup.alpha = 1f
                logoutPopup.translationY = 0f
            }
            .start()
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}

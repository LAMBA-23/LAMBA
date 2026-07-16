package com.lamba.app

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.lamba.app.chat.LocalChatService
import com.lamba.app.network.ActiveTrip
import com.lamba.app.network.OdometerInputResult
import com.lamba.app.network.RetrofitClient
import com.lamba.app.network.SessionManager
import com.lamba.app.network.TripFlowLogic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private var userId: Int = -1
    private lateinit var drawerOverlay: View
    private lateinit var menuRequests: LinearLayout
    private lateinit var menuProfile: LinearLayout
    private lateinit var notificationDot: View
    private lateinit var tvTripAction: TextView
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
        val btnTripAction = findViewById<View>(R.id.btnTripAction)
        tvTripAction = findViewById(R.id.tvTripAction)
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

        btnTripAction.setOnClickListener {
            handleTripAction()
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

        refreshTripAction()
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
        if (::tvTripAction.isInitialized) {
            refreshTripAction()
        }
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

    private fun handleTripAction() {
        val activeTrip = SessionManager.getActiveTrip(this)
        if (activeTrip == null) {
            showStartTripDialog()
        } else {
            showFinishTripDialog(activeTrip)
        }
    }

    private fun showStartTripDialog() {
        showTripOdometerDialog(
            title = TripFlowLogic.START_ACTION_LABEL,
            buttonText = TripFlowLogic.START_ACTION_LABEL,
            validate = TripFlowLogic::validateStartOdometer,
            onValid = { odometerStart, dialog, _, _ ->
                SessionManager.saveActiveTrip(this, odometerStart)
                refreshTripAction()
                dialog.dismiss()
            },
        )
    }

    private fun showFinishTripDialog(activeTrip: ActiveTrip) {
        showTripOdometerDialog(
            title = TripFlowLogic.FINISH_ACTION_LABEL,
            buttonText = TripFlowLogic.FINISH_ACTION_LABEL,
            validate = { value -> TripFlowLogic.validateFinishOdometer(value, activeTrip.odometerStart) },
            onValid = { odometerEnd, dialog, errorView, actionButton ->
                saveCompletedTrip(activeTrip, odometerEnd, dialog, errorView, actionButton)
            },
        )
    }

    private fun showTripOdometerDialog(
        title: String,
        buttonText: String,
        validate: (String) -> OdometerInputResult,
        onValid: (Int, Dialog, TextView, Button) -> Unit,
    ) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24.dp, 20.dp, 24.dp, 24.dp)
            background = roundedBackground("#FFFFFF", 24.dp.toFloat())
        }
        val errorView = TextView(this).apply {
            setTextColor(Color.parseColor("#D32F2F"))
            textSize = 14f
            visibility = View.GONE
            setPadding(4.dp, 8.dp, 4.dp, 0)
        }
        val input = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                58.dp,
            ).apply {
                topMargin = 18.dp
            }
            background = ContextCompat.getDrawable(this@MainActivity, R.drawable.bg_input_field)
            gravity = Gravity.CENTER_VERTICAL
            hint = "Текущий пробег, км"
            inputType = InputType.TYPE_CLASS_NUMBER
            keyListener = DigitsKeyListener.getInstance("0123456789")
            setSingleLine(true)
            setTextColor(Color.parseColor("#101114"))
            setHintTextColor(Color.parseColor("#9B9BA3"))
            textSize = 16f
            setPadding(18.dp, 0, 18.dp, 0)
        }
        val actionButton = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                56.dp,
            ).apply {
                topMargin = 18.dp
            }
            background = ContextCompat.getDrawable(this@MainActivity, R.drawable.bg_button_red)
            text = buttonText
            setTextColor(Color.WHITE)
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            isAllCaps = false
        }

        container.addView(createTripDialogHeader(title, dialog))
        container.addView(input)
        container.addView(errorView)
        container.addView(actionButton)

        actionButton.setOnClickListener {
            errorView.visibility = View.GONE
            when (val result = validate(input.text.toString())) {
                is OdometerInputResult.Valid -> onValid(result.value, dialog, errorView, actionButton)
                is OdometerInputResult.Invalid -> {
                    errorView.text = result.message
                    errorView.visibility = View.VISIBLE
                }
            }
        }

        dialog.setContentView(container)
        dialog.show()
        dialog.window?.let { window ->
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window.setLayout(
                (resources.displayMetrics.widthPixels * 0.88f).toInt(),
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
        }
    }

    private fun createTripDialogHeader(title: String, dialog: Dialog): LinearLayout {
        return LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL

            addView(TextView(this@MainActivity).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = title
                setTextColor(Color.parseColor("#101114"))
                textSize = 24f
                typeface = Typeface.DEFAULT_BOLD
                includeFontPadding = false
            })

            addView(ImageButton(this@MainActivity).apply {
                layoutParams = LinearLayout.LayoutParams(48.dp, 48.dp)
                background = ContextCompat.getDrawable(this@MainActivity, R.drawable.bg_history_back)
                contentDescription = "Закрыть"
                setImageResource(R.drawable.ic_lamba_close)
                setColorFilter(Color.parseColor("#960018"))
                setPadding(12.dp, 12.dp, 12.dp, 12.dp)
                setOnClickListener { dialog.dismiss() }
            })
        }
    }

    private fun saveCompletedTrip(
        activeTrip: ActiveTrip,
        odometerEnd: Int,
        dialog: Dialog,
        errorView: TextView,
        actionButton: Button,
    ) {
        val userId = SessionManager.getUserId(this)
        if (userId == null) {
            errorView.text = "Не удалось определить пользователя"
            errorView.visibility = View.VISIBLE
            return
        }

        val request = runCatching {
            TripFlowLogic.createTripRequest(
                odometerStart = activeTrip.odometerStart,
                odometerEnd = odometerEnd,
                date = todayForInput(),
            )
        }.getOrElse {
            errorView.text = "Проверьте пробег и попробуйте еще раз"
            errorView.visibility = View.VISIBLE
            return
        }

        actionButton.isEnabled = false
        errorView.visibility = View.GONE

        lifecycleScope.launch {
            runCatching { RetrofitClient.apiService.createEvent(request, userId) }
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        SessionManager.clearActiveTrip(this@MainActivity)
                        refreshTripAction()
                        dialog.dismiss()
                        showTripSavedDialog()
                    } else {
                        showTripSaveError(errorView, actionButton)
                    }
                }
                .onFailure {
                    showTripSaveError(errorView, actionButton)
                }
        }
    }

    private fun showTripSaveError(errorView: TextView, actionButton: Button) {
        actionButton.isEnabled = true
        errorView.text = "Не удалось сохранить поездку. Проверьте подключение и попробуйте еще раз"
        errorView.visibility = View.VISIBLE
    }

    private fun showTripSavedDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val nightMode = isNightMode()
        val surfaceColor = if (nightMode) "#1F1F23" else "#FFFFFF"
        val primaryTextColor = if (nightMode) "#F7F7F8" else "#101114"
        val secondaryTextColor = if (nightMode) "#C9C9CE" else "#77777E"
        val iconBackgroundColor = if (nightMode) "#2B151B" else "#FFEBEE"
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(24.dp, 24.dp, 24.dp, 22.dp)
            background = roundedBackground(surfaceColor, 24.dp.toFloat())
        }

        container.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(56.dp, 56.dp)
            background = roundedBackground(iconBackgroundColor, 18.dp.toFloat())
            setImageResource(R.drawable.ic_lamba_check)
            setPadding(14.dp, 14.dp, 14.dp, 14.dp)
        })

        container.addView(TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = 16.dp
            }
            gravity = Gravity.CENTER
            text = "Поездка сохранена"
            setTextColor(Color.parseColor(primaryTextColor))
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            includeFontPadding = false
        })

        container.addView(TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = 8.dp
            }
            gravity = Gravity.CENTER
            text = "Данные поездки добавлены в историю"
            setTextColor(Color.parseColor(secondaryTextColor))
            textSize = 16f
        })

        container.addView(Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                54.dp,
            ).apply {
                topMargin = 22.dp
            }
            background = ContextCompat.getDrawable(this@MainActivity, R.drawable.bg_button_red)
            text = "ОК"
            setTextColor(Color.WHITE)
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            isAllCaps = false
            setOnClickListener { dialog.dismiss() }
        })

        dialog.setContentView(container)
        dialog.show()
        dialog.window?.let { window ->
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window.setLayout(
                (resources.displayMetrics.widthPixels * 0.86f).toInt(),
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
        }
    }

    private fun isNightMode(): Boolean {
        return (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
    }

    private fun refreshTripAction() {
        tvTripAction.text = TripFlowLogic.actionLabel(SessionManager.getActiveTrip(this))
    }

    private fun todayForInput(): String {
        return SimpleDateFormat("dd.MM.yyyy", Locale("ru")).format(Date())
    }

    private fun roundedBackground(color: String, radius: Float): GradientDrawable {
        return GradientDrawable().apply {
            setColor(Color.parseColor(color))
            cornerRadius = radius
        }
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}

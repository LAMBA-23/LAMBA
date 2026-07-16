package com.lamba.app.network

import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.lamba.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryActivity : AppCompatActivity() {

    private lateinit var layoutTimeline: LinearLayout
    private lateinit var progressHistory: ProgressBar
    private lateinit var tvHistoryState: TextView

    private val backendEvents = mutableListOf<Event>()
    private val historyRecordsByEventId = mutableMapOf<Int?, HistoryRecordUiModel>()
    private var currentVehicleMileage = 0.0
    private var activeIssuePhotoUri: String? = null
    private var activeIssuePhotoPreview: ImageView? = null
    private var activeIssuePhotoButton: Button? = null
    private var activeIssuePhotoRemoveButton: Button? = null
    private var activeIssuePhotoRemoved: Boolean = false

    private val issuePhotoPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            activeIssuePhotoUri = uri.toString()
            activeIssuePhotoRemoved = false
            activeIssuePhotoPreview?.let { showPhotoPreview(it, uri) }
            activeIssuePhotoButton?.text = "Заменить фото"
            activeIssuePhotoRemoveButton?.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }
        findViewById<ImageButton>(R.id.btnAddHistoryRecord).setOnClickListener {
            showRecordTypeSheet()
        }

        layoutTimeline = findViewById(R.id.layoutTimeline)
        progressHistory = findViewById(R.id.progressHistory)
        tvHistoryState = findViewById(R.id.tvHistoryState)

        loadEvents()
    }

    override fun onResume() {
        super.onResume()
        loadEvents()
    }
    
    private fun loadEvents() {
        val userId = SessionManager.getUserId(this)
        if (userId == null) {
            showState("Не удалось загрузить историю")
            return
        }

        showLoading()

        lifecycleScope.launch {
            runCatching {
                val vehicleResponse = RetrofitClient.apiService.getVehicle(userId)
                val eventsResponse = RetrofitClient.apiService.getEvents(userId)
                vehicleResponse to eventsResponse
            }
                .onSuccess { (vehicleResponse, eventsResponse) ->
                    currentVehicleMileage = vehicleResponse.body()?.currentMileage?.toDouble() ?: 0.0
                    if (eventsResponse.isSuccessful) {
                        backendEvents.clear()
                        backendEvents.addAll(eventsResponse.body().orEmpty())
                        renderTimeline()
                    } else {
                        showState("Не удалось загрузить историю")
                    }
                }
                .onFailure {
                    showState("Не удалось загрузить историю")
                }
        }
    }

    private fun showLoading() {
        layoutTimeline.removeAllViews()
        layoutTimeline.visibility = View.GONE
        tvHistoryState.visibility = View.GONE
        progressHistory.visibility = View.VISIBLE
    }

    private fun showState(message: String) {
        layoutTimeline.removeAllViews()
        layoutTimeline.visibility = View.GONE
        progressHistory.visibility = View.GONE
        tvHistoryState.text = message
        tvHistoryState.visibility = View.VISIBLE
    }

    private fun renderTimeline() {
        layoutTimeline.removeAllViews()
        progressHistory.visibility = View.GONE
        val supportedBackendEvents = backendEvents.filter { isSupportedBackendEvent(it.type) }
        val historyRecords = buildHistoryRecords(supportedBackendEvents)
        historyRecordsByEventId.clear()
        historyRecordsByEventId.putAll(historyRecords)

        if (supportedBackendEvents.isEmpty()) {
            showState("История пока пустая")
            return
        }

        tvHistoryState.visibility = View.GONE
        layoutTimeline.visibility = View.VISIBLE

        supportedBackendEvents.forEach { event ->
            val record = historyRecords[event.id] ?: event.toUiModel()
            addTimelineItem(
                title = record.title,
                iconRes = record.iconRes,
                details = formatBackendDate(event.createdAt),
                keyValue = record.keyValue,
                onClick = { showRecordDetailsSheet(event) },
            )
        }
    }

    private fun addTimelineItem(
        title: String,
        iconRes: Int,
        details: String,
        keyValue: String,
        onClick: (() -> Unit)?,
    ) {
        val itemView = LayoutInflater.from(this).inflate(R.layout.item_history_event, layoutTimeline, false)
        itemView.findViewById<ImageView>(R.id.ivEventIcon).setImageResource(iconRes)
        itemView.findViewById<TextView>(R.id.tvEventTitle).text = title
        itemView.findViewById<TextView>(R.id.tvEventDetails).text = details

        val amountView = itemView.findViewById<TextView>(R.id.tvEventAmount)
        amountView.text = keyValue
        amountView.visibility = if (keyValue.isBlank()) View.GONE else View.VISIBLE

        val dateView = itemView.findViewById<TextView>(R.id.tvEventDate)
        dateView.visibility = View.GONE

        if (onClick != null) {
            itemView.findViewById<View>(R.id.cardHistoryEvent).setOnClickListener { onClick() }
        }
        layoutTimeline.addView(itemView)
    }

    private fun showRecordTypeSheet() {
        val dialog = BottomSheetDialog(this)
        val isDark = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        val surfaceColor = if (isDark) "#1C1C1E" else "#FFFFFF"
        val textColor = if (isDark) "#F7F7F8" else "#101114"
        val optionBg = if (isDark) "#2C2C2E" else "#FFF7F8"

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24.dp, 8.dp, 24.dp, 28.dp)
            background = roundedBackground(surfaceColor, 28.dp.toFloat())
        }

        container.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(36.dp, 4.dp).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                topMargin = 10.dp
                bottomMargin = 18.dp
            }
            background = ContextCompat.getDrawable(this@HistoryActivity, R.drawable.bg_bottom_sheet_handle)
        })

        container.addView(TextView(this).apply {
            text = "Добавить запись"
            setTextColor(Color.parseColor(textColor))
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            includeFontPadding = false
            setPadding(0, 0, 0, 12.dp)
        })

        HistoryRecordType.values()
            .filterNot { it == HistoryRecordType.TRIP }
            .forEach { type ->
                container.addView(createRecordTypeOption(type, optionBg, textColor) {
                    dialog.dismiss()
                    showRecordFormSheet(type)
                })
            }

        dialog.setContentView(container)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    private fun createRecordTypeOption(type: HistoryRecordType, bg: String, textC: String, onClick: () -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                64.dp,
            ).apply {
                topMargin = 10.dp
            }
            background = roundedBackground(bg, 18.dp.toFloat())
            clickableForeground()
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
            setPadding(16.dp, 0, 16.dp, 0)
            setOnClickListener { onClick() }

            addView(ImageView(this@HistoryActivity).apply {
                layoutParams = LinearLayout.LayoutParams(46.dp, 46.dp)
                background = ContextCompat.getDrawable(this@HistoryActivity, R.drawable.bg_icon_red_light)
                setImageResource(type.iconRes)
                setColorFilter(Color.parseColor("#960018"))
                setPadding(11.dp, 11.dp, 11.dp, 11.dp)
            })

            addView(TextView(this@HistoryActivity).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = 16.dp
                }
                text = type.title
                setTextColor(Color.parseColor(textC))
                textSize = 18f
                typeface = Typeface.DEFAULT_BOLD
            })
        }
    }

    private fun showRecordFormSheet(type: HistoryRecordType, event: Event? = null) {
        val dialog = BottomSheetDialog(this)
        val isDark = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        val surfaceColor = if (isDark) "#1C1C1E" else "#FFFFFF"
        val textColor = if (isDark) "#F7F7F8" else "#101114"

        val editingRecord = event?.let { historyRecordsByEventId[it.id] ?: it.toUiModel() }
        val initialIssuePhotoUri = if (type == HistoryRecordType.BREAKDOWN) {
            editingRecord?.values?.get("photoUrl").orEmpty()
                .ifBlank { editingRecord?.values?.get("photoUri").orEmpty() }
        } else {
            ""
        }
        activeIssuePhotoUri = null
        activeIssuePhotoRemoved = false
        val formContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24.dp, 8.dp, 24.dp, 24.dp)
            background = roundedBackground(surfaceColor, 28.dp.toFloat())
        }

        formContent.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(36.dp, 4.dp).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                topMargin = 10.dp
                bottomMargin = 10.dp
            }
            background = ContextCompat.getDrawable(this@HistoryActivity, R.drawable.bg_bottom_sheet_handle)
        })

        val fields = buildFields(type)
        val errorView = TextView(this).apply {
            setTextColor(Color.parseColor("#D32F2F"))
            textSize = 14f
            visibility = View.GONE
            setPadding(4.dp, 12.dp, 4.dp, 0)
        }

        formContent.addView(createFormHeader(type.formTitle, textColor, dialog))
        fields.forEach { field ->
            formContent.addView(createFieldView(field, editingRecord?.values?.get(field.key)))
        }
        if (type == HistoryRecordType.BREAKDOWN) {
            formContent.addView(createIssuePhotoPickerView(initialIssuePhotoUri, textColor))
            dialog.setOnDismissListener {
                activeIssuePhotoUri = null
                activeIssuePhotoPreview = null
                activeIssuePhotoButton = null
                activeIssuePhotoRemoveButton = null
                activeIssuePhotoRemoved = false
            }
        }
        formContent.addView(errorView)
        formContent.addView(Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                56.dp,
            ).apply {
                topMargin = 18.dp
            }
            background = ContextCompat.getDrawable(this@HistoryActivity, R.drawable.bg_button_red)
            text = "Сохранить"
            setTextColor(Color.WHITE)
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            isAllCaps = false
            setOnClickListener {
                if (validateFields(fields, errorView)) {
                    hideKeyboard()
                    val values = fields.associate { field ->
                        field.key to field.input.text.toString().trim()
                    }.toMutableMap()
                    saveRecord(type, values, event?.id, errorView, dialog)
                }
            }
        })

        val scrollView = ScrollView(this).apply {
            isFillViewport = false
            addView(formContent)
        }

        dialog.setContentView(scrollView)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    private fun createFormHeader(title: String, textColor: String, dialog: BottomSheetDialog): LinearLayout {
        return LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                bottomMargin = 10.dp
            }
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL

            addView(ImageButton(this@HistoryActivity).apply {
                layoutParams = LinearLayout.LayoutParams(48.dp, 48.dp)
                background = ContextCompat.getDrawable(this@HistoryActivity, R.drawable.bg_history_back)
                contentDescription = "Назад"
                setImageResource(R.drawable.ic_lamba_arrow_back)
                setColorFilter(Color.parseColor("#960018"))
                setPadding(12.dp, 12.dp, 12.dp, 12.dp)
                setOnClickListener { dialog.dismiss() }
            })

            addView(TextView(this@HistoryActivity).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = 14.dp
                }
                text = title
                setTextColor(Color.parseColor(textColor))
                textSize = 26f
                typeface = Typeface.DEFAULT_BOLD
                includeFontPadding = false
            })
        }
    }

    private fun createFieldView(field: FormField, value: String?): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 12.dp, 0, 0)

            addView(EditText(this@HistoryActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    if (field.singleLine) 58.dp else 92.dp,
                )
                background = ContextCompat.getDrawable(this@HistoryActivity, R.drawable.bg_input_field)
                gravity = if (field.singleLine) Gravity.CENTER_VERTICAL else Gravity.TOP
                hint = field.hint
                setSingleLine(field.singleLine)
                inputType = field.inputType
                setTextColor(Color.parseColor("#101114"))
                setHintTextColor(Color.parseColor("#9B9BA3"))
                textSize = 16f
                setPadding(18.dp, if (field.singleLine) 0 else 14.dp, 18.dp, 0)
                if (field.numeric) {
                    keyListener = DigitsKeyListener.getInstance("0123456789.,")
                }
                setText(value ?: if (field.key == "date") todayForInput() else "")
                field.input = this
            })

            addView(TextView(this@HistoryActivity).apply {
                setTextColor(Color.parseColor("#D32F2F"))
                textSize = 13f
                visibility = View.GONE
                setPadding(18.dp, 6.dp, 18.dp, 0)
                field.error = this
            })
        }
    }

    private fun buildFields(type: HistoryRecordType): List<FormField> {
        return when (type) {
            HistoryRecordType.FUEL -> listOf(
                FormField("date", "Дата"),
                FormField("fuelType", "Тип топлива"),
                FormField("litres", "Количество, л", numeric = true),
                FormField("cost", "Стоимость, ₽", numeric = true),
            )
            HistoryRecordType.MAINTENANCE -> listOf(
                FormField("name", "Название ТО"),
                FormField("date", "Дата"),
                FormField("cost", "Стоимость, ₽", numeric = true),
                FormField("description", "Описание (необязательно)", required = false, singleLine = false),
            )
            HistoryRecordType.REPAIR -> listOf(
                FormField("name", "Название ремонта"),
                FormField("date", "Дата"),
                FormField("cost", "Стоимость, ₽", numeric = true),
                FormField("description", "Описание (необязательно)", required = false, singleLine = false),
            )
            HistoryRecordType.BREAKDOWN -> listOf(
                FormField("name", "Название поломки"),
                FormField("description", "Описание", singleLine = false),
                FormField("date", "Дата"),
            )
            HistoryRecordType.TRIP -> listOf(
                FormField("date", "Дата"),
                FormField("odometerStart", "Пробег в начале", numeric = true),
                FormField("odometerEnd", "Пробег в конце", numeric = true),
                FormField("description", "Маршрут / описание (необязательно)", required = false, singleLine = false),
            )
        }
    }

    private fun validateFields(fields: List<FormField>, formError: TextView): Boolean {
        var isValid = true
        formError.visibility = View.GONE

        fields.forEach { field ->
            val value = field.input.text.toString().trim()
            field.error.visibility = View.GONE

            when {
                field.required && value.isBlank() -> {
                    field.error.text = "Заполните поле"
                    field.error.visibility = View.VISIBLE
                    isValid = false
                }
                field.numeric && parsePositiveNumber(value) == null -> {
                    field.error.text = "Введите положительное число"
                    field.error.visibility = View.VISIBLE
                    isValid = false
                }
                field.key in setOf("odometerStart", "odometerEnd") && !Regex("""\d+""").matches(value) -> {
                    field.error.text = "Введите пробег целым числом"
                    field.error.visibility = View.VISIBLE
                    isValid = false
                }
            }
        }

        val odometerStart = fields.find { it.key == "odometerStart" }
        val odometerEnd = fields.find { it.key == "odometerEnd" }
        val startValue = odometerStart?.input?.text?.toString()?.trim()?.let(::parsePositiveNumber)
        val endValue = odometerEnd?.input?.text?.toString()?.trim()?.let(::parsePositiveNumber)
        if (startValue != null && endValue != null && endValue < startValue) {
            odometerEnd.error.text = "Пробег в конце не может быть меньше начального"
            odometerEnd.error.visibility = View.VISIBLE
            isValid = false
        }

        if (!isValid) {
            formError.text = "Проверьте поля и попробуйте еще раз"
            formError.visibility = View.VISIBLE
        }

        return isValid
    }

    private fun showRecordDetailsSheet(event: Event) {
        val record = historyRecordsByEventId[event.id] ?: event.toUiModel()
        val dialog = BottomSheetDialog(this)
        val isDark = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        val surfaceColor = if (isDark) "#1C1C1E" else "#FFFFFF"
        val textColor = if (isDark) "#F7F7F8" else "#101114"
        val secondaryTextColor = if (isDark) "#C9C9CE" else "#77777E"

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24.dp, 8.dp, 24.dp, 28.dp)
            background = roundedBackground(surfaceColor, 28.dp.toFloat())
        }

        container.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(36.dp, 4.dp).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                topMargin = 10.dp
                bottomMargin = 18.dp
            }
            background = ContextCompat.getDrawable(this@HistoryActivity, R.drawable.bg_bottom_sheet_handle)
        })

        container.addView(TextView(this).apply {
            text = record.type.title
            setTextColor(Color.parseColor(textColor))
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            includeFontPadding = false
        })

        record.detailRows.forEach { row ->
            container.addView(createDetailRow(row.first, row.second, textColor, secondaryTextColor))
        }
        if (record.type == HistoryRecordType.BREAKDOWN) {
            record.values["photoUrl"].orEmpty()
                .ifBlank { record.values["photoUri"].orEmpty() }
                .takeIf { it.isNotBlank() }?.let { photoReference ->
                    container.addView(createStoredPhotoView(photoReference))
            }
        }

        container.addView(createActionButton("Редактировать", filled = false) {
            dialog.dismiss()
            showRecordFormSheet(record.type, event)
        })
        container.addView(createActionButton("Удалить", filled = true) {
            dialog.dismiss()
            confirmDeleteRecord(event)
        })

        val scrollView = ScrollView(this).apply {
            isFillViewport = false
            addView(container)
        }

        dialog.setContentView(scrollView)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    private fun buildHistoryRecords(events: List<Event>): Map<Int?, HistoryRecordUiModel> {
        var knownMileage = currentVehicleMileage
        return events.associate { event ->
            val record = if (event.type.lowercase() == "trip") {
                val modernTripDistance = event.tripDistance?.toDouble() ?: event.calculatedTripDistance()
                val tripMileageOverride = if (modernTripDistance != null) {
                    event.odometerEnd?.let { knownMileage = maxOf(knownMileage, it.toDouble()) }
                    modernTripDistance
                } else {
                    val effectiveMileage = if (event.mileage <= knownMileage) {
                        knownMileage + event.mileage
                    } else {
                        event.mileage
                    }
                    val legacyTripMileage = maxOf(0.0, effectiveMileage - knownMileage)
                    knownMileage = maxOf(knownMileage, effectiveMileage)
                    legacyTripMileage
                }
                HistoryRecordUiModel.from(
                    HistoryRecordEventMapper.fromEvent(
                        event,
                        tripMileageOverride = tripMileageOverride,
                    ),
                )
            } else {
                event.toUiModel()
            }
            event.id to record
        }
    }

    private fun createDetailRow(label: String, value: String, textColor: String, secondaryTextColor: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 18.dp, 0, 0)

            addView(TextView(this@HistoryActivity).apply {
                text = label
                setTextColor(Color.parseColor(secondaryTextColor))
                textSize = 14f
            })
            addView(TextView(this@HistoryActivity).apply {
                text = value
                setTextColor(Color.parseColor(textColor))
                textSize = 18f
                typeface = Typeface.DEFAULT_BOLD
                setPadding(0, 4.dp, 0, 0)
            })
        }
    }

    private fun createIssuePhotoPickerView(initialPhotoUri: String, textColor: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 16.dp, 0, 0)

            addView(TextView(this@HistoryActivity).apply {
                text = "Фото поломки"
                setTextColor(Color.parseColor(textColor))
                textSize = 17f
                typeface = Typeface.DEFAULT_BOLD
                includeFontPadding = false
            })

            addView(TextView(this@HistoryActivity).apply {
                text = "Необязательно. Можно добавить одно фото."
                setTextColor(Color.parseColor("#77777E"))
                textSize = 14f
                setPadding(0, 6.dp, 0, 0)
            })

            addView(Button(this@HistoryActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    54.dp,
                ).apply {
                    topMargin = 12.dp
                }
                background = ContextCompat.getDrawable(this@HistoryActivity, R.drawable.bg_button_white_outline)
                text = if (initialPhotoUri.isBlank()) "Выбрать фото" else "Заменить фото"
                setTextColor(Color.parseColor("#960018"))
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
                isAllCaps = false
                activeIssuePhotoButton = this
                setOnClickListener {
                    issuePhotoPicker.launch(arrayOf("image/*"))
                }
            })

            addView(ImageView(this@HistoryActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    180.dp,
                ).apply {
                    topMargin = 12.dp
                }
                background = roundedBackground("#FFF7F8", 18.dp.toFloat())
                scaleType = ImageView.ScaleType.CENTER_CROP
                visibility = View.GONE
                activeIssuePhotoPreview = this
                if (initialPhotoUri.isNotBlank()) {
                    showPhotoPreview(this, initialPhotoUri)
                }
            })

            addView(Button(this@HistoryActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    48.dp,
                ).apply {
                    topMargin = 10.dp
                }
                background = ContextCompat.getDrawable(this@HistoryActivity, R.drawable.bg_button_white_outline)
                text = "Удалить фото"
                setTextColor(Color.parseColor("#960018"))
                textSize = 15f
                typeface = Typeface.DEFAULT_BOLD
                isAllCaps = false
                visibility = if (initialPhotoUri.isBlank()) View.GONE else View.VISIBLE
                activeIssuePhotoRemoveButton = this
                setOnClickListener {
                    activeIssuePhotoUri = null
                    activeIssuePhotoRemoved = true
                    activeIssuePhotoPreview?.visibility = View.GONE
                    activeIssuePhotoButton?.text = "Выбрать фото"
                    visibility = View.GONE
                }
            })
        }
    }

    private fun createStoredPhotoView(photoReference: String): ImageView {
        return ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = 18.dp
            }
            background = roundedBackground("#FFF7F8", 18.dp.toFloat())
            clipToOutline = true
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            showPhotoPreview(this, photoReference)
        }
    }

    private fun createActionButton(textValue: String, filled: Boolean, onClick: () -> Unit): Button {
        return Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                54.dp,
            ).apply {
                topMargin = 16.dp
            }
            background = ContextCompat.getDrawable(
                this@HistoryActivity,
                if (filled) R.drawable.bg_button_red else R.drawable.bg_button_white_outline,
            )
            text = textValue
            setTextColor(Color.parseColor(if (filled) "#FFFFFF" else "#960018"))
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            isAllCaps = false
            setOnClickListener { onClick() }
        }
    }

    private fun confirmDeleteRecord(event: Event) {
        AlertDialog.Builder(this)
            .setTitle("Удалить запись?")
            .setMessage("Запись будет удалена из истории и статистики.")
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Удалить") { _, _ ->
                deleteRecord(event)
            }
            .show()
    }

    private fun isSupportedBackendEvent(type: String): Boolean {
        return type.lowercase() in setOf("fuel", "repair", "trip", "issue")
    }

    private fun formatBackendDate(createdAt: String?): String {
        if (createdAt.isNullOrBlank()) {
            return ""
        }

        return try {
            val datePart = createdAt.substringBefore("T").takeIf { it.length == 10 } ?: createdAt
            val parsedDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(datePart)
            if (parsedDate == null) {
                datePart
            } else {
                SimpleDateFormat("dd.MM.yyyy", Locale("ru")).format(parsedDate)
            }
        } catch (_: Exception) {
            createdAt.substringBefore("T")
        }
    }

    private fun todayForInput(): String {
        return SimpleDateFormat("dd.MM.yyyy", Locale("ru")).format(Date())
    }

    private fun formatMoney(value: String): String {
        val number = parsePositiveNumber(value) ?: return value
        return DecimalNumberUtils.formatMoney(number)
    }

    private fun formatPlainNumber(value: String): String {
        val number = parsePositiveNumber(value) ?: return value
        return DecimalNumberUtils.formatDecimal(number)
    }

    private fun Event.calculatedTripDistance(): Double? {
        val start = odometerStart ?: return null
        val end = odometerEnd ?: return null
        return (end - start).takeIf { it >= 0 }?.toDouble()
    }

    private fun parsePositiveNumber(value: String): Double? {
        return DecimalNumberUtils.parsePositiveDecimal(value)
    }

    private fun roundedBackground(color: String, radius: Float): GradientDrawable {
        return GradientDrawable().apply {
            setColor(Color.parseColor(color))
            cornerRadius = radius
        }
    }

    private fun View.clickableForeground() {
        isClickable = true
        isFocusable = true
        foreground = ContextCompat.getDrawable(this@HistoryActivity, android.R.drawable.list_selector_background)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()

    private data class HistoryRecordUiModel(
        val type: HistoryRecordType,
        val values: Map<String, String>,
    ) {
        val title: String
            get() = if (type == HistoryRecordType.BREAKDOWN) {
                values["name"].orEmpty().ifBlank { type.title }
            } else {
                type.title
            }

        val iconRes: Int
            get() = type.iconRes

        val fullDate: String
            get() = values["date"].orEmpty()

        val keyValue: String
            get() = when (type) {
                HistoryRecordType.FUEL -> "${formatPlainNumberValue(values["litres"].orEmpty())} л"
                HistoryRecordType.MAINTENANCE,
                HistoryRecordType.REPAIR -> formatMoneyValue(values["cost"].orEmpty())
                HistoryRecordType.BREAKDOWN -> ""
                HistoryRecordType.TRIP -> "${formatPlainNumberValue(values["mileage"].orEmpty())} км"
            }

        val detailRows: List<Pair<String, String>>
            get() = when (type) {
                HistoryRecordType.FUEL -> listOf(
                    "Дата" to values["date"].orEmpty(),
                    "Тип топлива" to values["fuelType"].orEmpty(),
                    "Количество литров" to "${formatPlainNumberValue(values["litres"].orEmpty())} л",
                    "Стоимость" to formatMoneyValue(values["cost"].orEmpty()),
                )
                HistoryRecordType.MAINTENANCE -> listOf(
                    "Название ТО" to values["name"].orEmpty(),
                    "Дата" to values["date"].orEmpty(),
                    "Стоимость" to formatMoneyValue(values["cost"].orEmpty()),
                ).withOptionalDescription()
                HistoryRecordType.REPAIR -> listOf(
                    "Название ремонта" to values["name"].orEmpty(),
                    "Дата" to values["date"].orEmpty(),
                    "Стоимость" to formatMoneyValue(values["cost"].orEmpty()),
                ).withOptionalDescription()
                HistoryRecordType.BREAKDOWN -> listOf(
                    "Название поломки" to values["name"].orEmpty(),
                    "Дата" to values["date"].orEmpty(),
                    "Описание" to values["description"].orEmpty(),
                )
                HistoryRecordType.TRIP -> listOf(
                    "Дата" to values["date"].orEmpty(),
                )
                    .withOptionalValue("Пробег в начале", values["odometerStart"], suffix = " км")
                    .withOptionalValue("Пробег в конце", values["odometerEnd"], suffix = " км")
                    .plus("Дистанция" to "${formatPlainNumberValue(values["mileage"].orEmpty())} км")
                    .withOptionalDescription(label = "Маршрут / описание")
            }

        private fun List<Pair<String, String>>.withOptionalValue(
            label: String,
            value: String?,
            suffix: String = "",
        ): List<Pair<String, String>> {
            val formattedValue = value.orEmpty()
            return if (formattedValue.isBlank()) {
                this
            } else {
                this + (label to "${formatPlainNumberValue(formattedValue)}$suffix")
            }
        }

        private fun List<Pair<String, String>>.withOptionalDescription(
            label: String = "Описание",
        ): List<Pair<String, String>> {
            val description = values["description"].orEmpty()
            return if (description.isBlank()) this else this + (label to description)
        }

        private fun formatMoneyValue(value: String): String {
            val number = parsePositiveNumberValue(value) ?: return value
            return DecimalNumberUtils.formatMoney(number)
        }

        private fun formatPlainNumberValue(value: String): String {
            val number = parsePositiveNumberValue(value) ?: return value
            return DecimalNumberUtils.formatDecimal(number)
        }

        private fun parsePositiveNumberValue(value: String): Double? {
            return DecimalNumberUtils.parsePositiveDecimal(value)
        }

        companion object {
            fun from(data: HistoryRecordFormData): HistoryRecordUiModel {
                return HistoryRecordUiModel(data.type, data.values)
            }
        }
    }

    private class FormField(
        val key: String,
        val hint: String,
        val required: Boolean = true,
        val numeric: Boolean = false,
        val singleLine: Boolean = true,
    ) {
        lateinit var input: EditText
        lateinit var error: TextView

        val inputType: Int
            get() = if (numeric) {
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            } else if (singleLine) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            }
    }

    private fun Event.toUiModel(): HistoryRecordUiModel {
        return when (type.lowercase()) {
            "fuel", "repair", "trip", "issue" -> HistoryRecordUiModel.from(HistoryRecordEventMapper.fromEvent(this))
            else -> HistoryRecordUiModel(
                type = HistoryRecordType.REPAIR,
                values = mapOf(
                    "name" to description,
                    "date" to formatBackendDate(createdAt),
                    "cost" to DecimalNumberUtils.formatDecimal(amount),
                    "description" to "",
                ),
            )
        }
    }

    private fun saveRecord(
        type: HistoryRecordType,
        values: Map<String, String>,
        eventId: Int?,
        errorView: TextView,
        dialog: BottomSheetDialog,
    ) {
        val userId = SessionManager.getUserId(this)
        if (userId == null) {
            errorView.text = "Не удалось определить пользователя"
            errorView.visibility = View.VISIBLE
            return
        }

        val request = runCatching {
            HistoryRecordEventMapper.toEventRequest(type, values)
        }.getOrElse {
            errorView.text = "Проверьте поля и попробуйте еще раз"
            errorView.visibility = View.VISIBLE
            return
        }
        errorView.visibility = View.GONE

        lifecycleScope.launch {
            val response = runCatching {
                if (eventId == null) {
                    RetrofitClient.apiService.createEvent(request, userId)
                } else {
                    RetrofitClient.apiService.updateEvent(eventId, request, userId)
                }
            }

            response.onSuccess { eventResponse ->
                if (eventResponse.isSuccessful) {
                    val savedEventId = eventResponse.body()?.id
                    var uploadFailed = false
                    if (type == HistoryRecordType.BREAKDOWN && savedEventId != null) {
                        if (activeIssuePhotoRemoved && eventId != null) {
                            runCatching {
                                RetrofitClient.apiService.deleteEventPhoto(savedEventId, userId)
                            }
                        }
                        val selectedPhotoUri = activeIssuePhotoUri
                        if (!selectedPhotoUri.isNullOrBlank()) {
                            uploadFailed = runCatching {
                                val part = withContext(Dispatchers.IO) {
                                    UriMultipartHelper.createImagePart(
                                        contentResolver,
                                        Uri.parse(selectedPhotoUri),
                                    )
                                }
                                RetrofitClient.apiService.uploadEventPhoto(savedEventId, part, userId)
                            }.getOrNull()?.isSuccessful != true
                        }
                    }
                    activeIssuePhotoUri = null
                    activeIssuePhotoRemoved = false
                    dialog.dismiss()
                    loadEvents()
                    if (uploadFailed) {
                        Toast.makeText(
                            this@HistoryActivity,
                            "Запись сохранена, но фотографию загрузить не удалось.",
                            Toast.LENGTH_LONG,
                        ).show()
                    }
                } else {
                    errorView.text = "Не удалось сохранить запись"
                    errorView.visibility = View.VISIBLE
                }
            }.onFailure {
                errorView.text = "Не удалось сохранить запись"
                errorView.visibility = View.VISIBLE
            }
        }
    }

    private fun showPhotoPreview(imageView: ImageView, uri: Uri): Boolean {
        return runCatching {
            imageView.setImageURI(uri)
            imageView.visibility = View.VISIBLE
            true
        }.getOrDefault(false)
    }

    private fun showPhotoPreview(imageView: ImageView, photoReference: String): Boolean {
        if (RetrofitClient.isBackendUrlReference(photoReference)) {
            lifecycleScope.launch {
                val bitmap = withContext(Dispatchers.IO) {
                    runCatching {
                        URL(RetrofitClient.resolveBackendUrl(photoReference)).openStream().use {
                            BitmapFactory.decodeStream(it)
                        }
                    }.getOrNull()
                }
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap)
                    imageView.visibility = View.VISIBLE
                }
            }
            return true
        }
        return showPhotoPreview(imageView, Uri.parse(photoReference))
    }

    private fun deleteRecord(event: Event) {
        val userId = SessionManager.getUserId(this) ?: return
        lifecycleScope.launch {
            runCatching { RetrofitClient.apiService.deleteEvent(event.id ?: return@launch, userId) }
                .onSuccess {
                    if (it.isSuccessful) {
                        loadEvents()
                    }
                }
        }
    }
}

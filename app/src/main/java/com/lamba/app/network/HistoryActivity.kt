package com.lamba.app.network

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.lamba.app.R
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryActivity : AppCompatActivity() {

    private lateinit var layoutTimeline: LinearLayout
    private lateinit var progressHistory: ProgressBar
    private lateinit var tvHistoryState: TextView

    private val backendEvents = mutableListOf<Event>()
    private val manualRecords = mutableListOf<ManualHistoryRecord>()

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

    private fun loadEvents() {
        val userId = SessionManager.getUserId(this)
        if (userId == null) {
            showState("Не удалось загрузить историю")
            return
        }

        showLoading()

        lifecycleScope.launch {
            runCatching { RetrofitClient.apiService.getEvents(userId) }
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        backendEvents.clear()
                        backendEvents.addAll(response.body().orEmpty())
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

        if (manualRecords.isEmpty() && supportedBackendEvents.isEmpty()) {
            showState("История пока пустая")
            return
        }

        tvHistoryState.visibility = View.GONE
        layoutTimeline.visibility = View.VISIBLE

        manualRecords.forEachIndexed { index, record ->
            addTimelineItem(
                title = record.type.title,
                iconRes = record.type.iconRes,
                details = record.fullDate,
                keyValue = record.keyValue,
                onClick = { showRecordDetailsSheet(index) },
            )
        }
        supportedBackendEvents.forEach { event ->
            val mapping = mapEventType(event.type)
            addTimelineItem(
                title = mapping.title,
                iconRes = mapping.iconRes,
                details = formatBackendDate(event.createdAt),
                keyValue = mapping.backendKeyValue(event),
                onClick = null,
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
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24.dp, 18.dp, 24.dp, 28.dp)
            background = roundedBackground("#FFFFFF", 28.dp.toFloat())
        }

        container.addView(TextView(this).apply {
            text = "Добавить запись"
            setTextColor(Color.parseColor("#101114"))
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            includeFontPadding = false
            setPadding(0, 8.dp, 0, 12.dp)
        })

        RecordType.values().forEach { type ->
            container.addView(createRecordTypeOption(type) {
                dialog.dismiss()
                showRecordFormSheet(type)
            })
        }

        dialog.setContentView(container)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    private fun createRecordTypeOption(type: RecordType, onClick: () -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                64.dp,
            ).apply {
                topMargin = 10.dp
            }
            background = roundedBackground("#FFF7F8", 18.dp.toFloat())
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
                setTextColor(Color.parseColor("#101114"))
                textSize = 18f
                typeface = Typeface.DEFAULT_BOLD
            })
        }
    }

    private fun showRecordFormSheet(type: RecordType, editIndex: Int? = null) {
        val dialog = BottomSheetDialog(this)
        val editingRecord = editIndex?.let { manualRecords.getOrNull(it) }
        val formContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24.dp, 18.dp, 24.dp, 24.dp)
            background = roundedBackground("#FFFFFF", 28.dp.toFloat())
        }
        val fields = buildFields(type)
        val errorView = TextView(this).apply {
            setTextColor(Color.parseColor("#D32F2F"))
            textSize = 14f
            visibility = View.GONE
            setPadding(4.dp, 12.dp, 4.dp, 0)
        }

        formContent.addView(createFormHeader(type.formTitle, dialog))
        fields.forEach { field ->
            formContent.addView(createFieldView(field, editingRecord?.values?.get(field.key)))
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
                    }
                    if (editIndex == null) {
                        manualRecords.add(0, ManualHistoryRecord(type, values))
                    } else {
                        manualRecords[editIndex] = ManualHistoryRecord(type, values)
                    }
                    dialog.dismiss()
                    renderTimeline()
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

    private fun createFormHeader(title: String, dialog: BottomSheetDialog): LinearLayout {
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
                setTextColor(Color.parseColor("#101114"))
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

    private fun buildFields(type: RecordType): List<FormField> {
        return when (type) {
            RecordType.FUEL -> listOf(
                FormField("date", "Дата"),
                FormField("fuelType", "Тип топлива"),
                FormField("litres", "Количество, л", numeric = true),
                FormField("cost", "Стоимость, ₽", numeric = true),
            )
            RecordType.MAINTENANCE -> listOf(
                FormField("name", "Название ТО"),
                FormField("date", "Дата"),
                FormField("cost", "Стоимость, ₽", numeric = true),
                FormField("description", "Описание (необязательно)", required = false, singleLine = false),
            )
            RecordType.REPAIR -> listOf(
                FormField("name", "Название ремонта"),
                FormField("date", "Дата"),
                FormField("cost", "Стоимость, ₽", numeric = true),
                FormField("description", "Описание (необязательно)", required = false, singleLine = false),
            )
            RecordType.TRIP -> listOf(
                FormField("date", "Дата"),
                FormField("mileage", "Километраж, км", numeric = true),
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
            }
        }

        if (!isValid) {
            formError.text = "Проверьте поля и попробуйте еще раз"
            formError.visibility = View.VISIBLE
        }

        return isValid
    }

    private fun showRecordDetailsSheet(index: Int) {
        val record = manualRecords.getOrNull(index) ?: return
        val dialog = BottomSheetDialog(this)
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24.dp, 22.dp, 24.dp, 28.dp)
            background = roundedBackground("#FFFFFF", 28.dp.toFloat())
        }

        container.addView(TextView(this).apply {
            text = record.type.title
            setTextColor(Color.parseColor("#101114"))
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            includeFontPadding = false
        })

        record.detailRows.forEach { row ->
            container.addView(createDetailRow(row.first, row.second))
        }

        container.addView(createActionButton("Редактировать", filled = false) {
            dialog.dismiss()
            showRecordFormSheet(record.type, index)
        })
        container.addView(createActionButton("Удалить", filled = true) {
            dialog.dismiss()
            confirmDeleteRecord(index)
        })

        dialog.setContentView(container)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    private fun createDetailRow(label: String, value: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 18.dp, 0, 0)

            addView(TextView(this@HistoryActivity).apply {
                text = label
                setTextColor(Color.parseColor("#77777E"))
                textSize = 14f
            })
            addView(TextView(this@HistoryActivity).apply {
                text = value
                setTextColor(Color.parseColor("#101114"))
                textSize = 18f
                typeface = Typeface.DEFAULT_BOLD
                setPadding(0, 4.dp, 0, 0)
            })
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

    private fun confirmDeleteRecord(index: Int) {
        AlertDialog.Builder(this)
            .setTitle("Удалить запись?")
            .setMessage("Запись исчезнет из истории в этой сессии.")
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Удалить") { _, _ ->
                if (index in manualRecords.indices) {
                    manualRecords.removeAt(index)
                    renderTimeline()
                }
            }
            .show()
    }

    private fun mapEventType(type: String): EventDisplay {
        return when (type.lowercase()) {
            "fuel" -> EventDisplay("Заправка", R.drawable.ic_lamba_fuel)
            "maintenance" -> EventDisplay("ТО", R.drawable.ic_lamba_build)
            "repair" -> EventDisplay("Ремонт", R.drawable.ic_lamba_repair)
            "trip" -> EventDisplay("Поездка", R.drawable.ic_lamba_route)
            "issue" -> EventDisplay("Повреждение", R.drawable.ic_lamba_repair)
            else -> EventDisplay("Событие", R.drawable.ic_lamba_history)
        }
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
        return "%,d".format(Locale.US, number.toLong()).replace(',', ' ') + " ₽"
    }

    private fun formatPlainNumber(value: String): String {
        val number = parsePositiveNumber(value) ?: return value
        return if (number % 1.0 == 0.0) {
            number.toLong().toString()
        } else {
            value.trim().replace('.', ',')
        }
    }

    private fun parsePositiveNumber(value: String): Double? {
        return value.replace(" ", "").replace(',', '.').toDoubleOrNull()?.takeIf { it > 0.0 }
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

    private enum class RecordType(
        val title: String,
        val formTitle: String,
        val iconRes: Int,
    ) {
        FUEL("Заправка", "Новая заправка", R.drawable.ic_lamba_fuel),
        MAINTENANCE("ТО", "Новое ТО", R.drawable.ic_lamba_build),
        REPAIR("Ремонт", "Новый ремонт", R.drawable.ic_lamba_repair),
        TRIP("Поездка", "Новая поездка", R.drawable.ic_lamba_route),
    }

    private data class ManualHistoryRecord(
        val type: RecordType,
        val values: Map<String, String>,
    ) {
        val fullDate: String
            get() = values["date"].orEmpty()

        val keyValue: String
            get() = when (type) {
                RecordType.FUEL -> "${formatPlainNumberValue(values["litres"].orEmpty())} л"
                RecordType.MAINTENANCE,
                RecordType.REPAIR -> formatMoneyValue(values["cost"].orEmpty())
                RecordType.TRIP -> "${formatPlainNumberValue(values["mileage"].orEmpty())} км"
            }

        val detailRows: List<Pair<String, String>>
            get() = when (type) {
                RecordType.FUEL -> listOf(
                    "Дата" to values["date"].orEmpty(),
                    "Тип топлива" to values["fuelType"].orEmpty(),
                    "Количество литров" to "${formatPlainNumberValue(values["litres"].orEmpty())} л",
                    "Стоимость" to formatMoneyValue(values["cost"].orEmpty()),
                )
                RecordType.MAINTENANCE -> listOf(
                    "Название ТО" to values["name"].orEmpty(),
                    "Дата" to values["date"].orEmpty(),
                    "Стоимость" to formatMoneyValue(values["cost"].orEmpty()),
                ).withOptionalDescription()
                RecordType.REPAIR -> listOf(
                    "Название ремонта" to values["name"].orEmpty(),
                    "Дата" to values["date"].orEmpty(),
                    "Стоимость" to formatMoneyValue(values["cost"].orEmpty()),
                ).withOptionalDescription()
                RecordType.TRIP -> listOf(
                    "Дата" to values["date"].orEmpty(),
                    "Километраж, км" to "${formatPlainNumberValue(values["mileage"].orEmpty())} км",
                )
            }

        private fun List<Pair<String, String>>.withOptionalDescription(): List<Pair<String, String>> {
            val description = values["description"].orEmpty()
            return if (description.isBlank()) this else this + ("Описание" to description)
        }

        private fun formatMoneyValue(value: String): String {
            val number = parsePositiveNumberValue(value) ?: return value
            return "%,d".format(Locale.US, number.toLong()).replace(',', ' ') + " ₽"
        }

        private fun formatPlainNumberValue(value: String): String {
            val number = parsePositiveNumberValue(value) ?: return value
            return if (number % 1.0 == 0.0) {
                number.toLong().toString()
            } else {
                value.trim().replace('.', ',')
            }
        }

        private fun parsePositiveNumberValue(value: String): Double? {
            return value.replace(" ", "").replace(',', '.').toDoubleOrNull()?.takeIf { it > 0.0 }
        }
    }

    private data class EventDisplay(
        val title: String,
        val iconRes: Int,
    ) {
        fun backendKeyValue(event: Event): String {
            return when (title) {
                "Заправка" -> if (event.amount > 0) "${event.amount} л" else ""
                "Ремонт", "ТО" -> if (event.amount > 0) "%,d ₽".format(Locale.US, event.amount).replace(',', ' ') else ""
                "Поездка" -> if (event.mileage > 0) "${event.mileage} км" else ""
                else -> ""
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
}

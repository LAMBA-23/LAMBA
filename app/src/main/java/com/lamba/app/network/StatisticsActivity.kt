package com.lamba.app.network

import android.os.Bundle
import android.os.Build
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.lamba.app.R
import kotlinx.coroutines.launch

class StatisticsActivity : AppCompatActivity() {

    private lateinit var contentContainer: View
    private lateinit var progressStats: ProgressBar
    private lateinit var tvMileageValue: TextView
    private lateinit var tvTotalExpensesValue: TextView
    private lateinit var tvFuelValue: TextView
    private lateinit var tvRepairValue: TextView
    private lateinit var tvRecordsValue: TextView
    private lateinit var tvFuelBreakdownValue: TextView
    private lateinit var tvRepairBreakdownValue: TextView
    private lateinit var fuelProgressTrack: FrameLayout
    private lateinit var fuelProgressFill: View
    private lateinit var repairProgressTrack: FrameLayout
    private lateinit var repairProgressFill: View
    private lateinit var tvPeriodChip: TextView
    private lateinit var tvPeriodCheck: TextView
    private var selectedPeriod: String = PERIOD_ALL_TIME
    private var latestStats: Stats = Stats()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        findViewById<ImageButton>(R.id.btnStatsBack).setOnClickListener {
            finish()
        }

        contentContainer = findViewById(R.id.statsContent)
        progressStats = findViewById(R.id.progressStats)
        tvMileageValue = findViewById(R.id.tvMileageValue)
        tvTotalExpensesValue = findViewById(R.id.tvTotalExpensesValue)
        tvFuelValue = findViewById(R.id.tvFuelValue)
        tvRepairValue = findViewById(R.id.tvRepairValue)
        tvRecordsValue = findViewById(R.id.tvRecordsValue)
        tvFuelBreakdownValue = findViewById(R.id.tvFuelBreakdownValue)
        tvRepairBreakdownValue = findViewById(R.id.tvRepairBreakdownValue)
        fuelProgressTrack = findViewById(R.id.fuelProgressTrack)
        fuelProgressFill = findViewById(R.id.fuelProgressFill)
        repairProgressTrack = findViewById(R.id.repairProgressTrack)
        repairProgressFill = findViewById(R.id.repairProgressFill)
        tvPeriodCheck = findViewById(R.id.tvPeriodCheck)

        setupPeriodSelector()
        keepBrandColorInDarkTheme(tvPeriodChip)
        keepBrandColorInDarkTheme(tvPeriodCheck)
        renderStats(Stats())
    }

    override fun onResume() {
        super.onResume()
        loadStats()
    }

    private fun setupPeriodSelector() {
        val periodChip = findViewById<LinearLayout>(R.id.periodChip)
        tvPeriodChip = findViewById(R.id.tvPeriodChip)
        tvPeriodCheck = findViewById(R.id.tvPeriodCheck)
        periodChip.setOnClickListener {
            showPeriodDropdown(periodChip)
        }
    }

    private fun showPeriodDropdown(anchor: View) {
        val isDark = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        val bgColor = if (isDark) "#1C1C1E" else "#FFFFFF"
        val textColor = if (isDark) "#F7F7F8" else "#101114"

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(8.dp, 8.dp, 8.dp, 8.dp)
            background = roundedBackground(bgColor, 18.dp.toFloat())
            elevation = 12.dp.toFloat()
        }
        val popup = PopupWindow(
            container,
            anchor.width.coerceAtLeast(150.dp),
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true,
        ).apply {
            isOutsideTouchable = true
            elevation = 12.dp.toFloat()
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        listOf(PERIOD_WEEK, PERIOD_MONTH, PERIOD_ALL_TIME).forEachIndexed { index, period ->
            container.addView(createPeriodDropdownItem(period, textColor, popup))
            if (index < 2) {
                container.addView(View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        4.dp,
                    )
                })
            }
        }

        container.alpha = 0f
        container.scaleX = 0.96f
        container.scaleY = 0.96f
        popup.showAsDropDown(anchor, 0, 8.dp)
        container.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(170L)
            .start()
    }

    private fun createPeriodDropdownItem(period: String, textColor: String, popup: PopupWindow): LinearLayout {
        val isSelected = period == selectedPeriod
        val isDark = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES

        return LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                46.dp,
            )
            background = periodItemBackground(isSelected, isDark)
            isClickable = true
            isFocusable = true
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
            setPadding(14.dp, 0, 12.dp, 0)

            addView(TextView(this@StatisticsActivity).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = period
                setTextColor(Color.parseColor(if (isSelected) "#960018" else textColor))
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD.takeIf { isSelected } ?: Typeface.DEFAULT
                includeFontPadding = false
                keepBrandColorInDarkTheme(this)
            })

            addView(TextView(this@StatisticsActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                )
                text = if (isSelected) "✓" else ""
                setTextColor(Color.parseColor("#960018"))
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
                includeFontPadding = false
                keepBrandColorInDarkTheme(this)
            })

            setOnClickListener {
                selectedPeriod = period
                tvPeriodChip.text = period
                renderStats(latestStats)
                animateDropdownDismiss(popup)
            }
        }
    }

    private fun animateDropdownDismiss(popup: PopupWindow) {
        val content = popup.contentView
        content.animate()
            .alpha(0f)
            .scaleX(0.97f)
            .scaleY(0.97f)
            .setDuration(140L)
            .withEndAction { popup.dismiss() }
            .start()
    }

    private fun loadStats() {
        val userId = SessionManager.getUserId(this)
        if (userId == null) {
            showLoading(false)
            Toast.makeText(this, "Не удалось загрузить статистику", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        lifecycleScope.launch {
            runCatching { RetrofitClient.apiService.getStats(userId) }
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        renderStats(response.body() ?: Stats())
                    } else {
                        renderStats(Stats())
                        Toast.makeText(
                            this@StatisticsActivity,
                            "Не удалось загрузить статистику",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
                .onFailure {
                    renderStats(Stats())
                    Toast.makeText(
                        this@StatisticsActivity,
                        "Не удалось загрузить статистику",
                        Toast.LENGTH_SHORT,
                    ).show()
                }

            showLoading(false)
        }
    }

    private fun renderStats(stats: Stats) {
        latestStats = stats
        val period = stats.periodFor(selectedPeriodKey())
        val fuelExpenses = period.fuelExpenses
        val repairExpenses = period.repairExpenses
        val totalExpenses = fuelExpenses + repairExpenses

        tvMileageValue.text = formatMileage(period.mileage)
        tvTotalExpensesValue.text = formatLitres(period.fuelLiters)
        tvFuelValue.text = formatMoney(fuelExpenses)
        tvRepairValue.text = formatMoney(repairExpenses)
        tvRecordsValue.text = "${period.recordsCount} события"
        tvFuelBreakdownValue.text = formatMoney(fuelExpenses)
        tvRepairBreakdownValue.text = formatMoney(repairExpenses)

        updateProgress(fuelProgressTrack, fuelProgressFill, fuelExpenses, totalExpenses)
        updateProgress(repairProgressTrack, repairProgressFill, repairExpenses, totalExpenses)
    }

    private fun selectedPeriodKey(): StatsPeriodKey {
        return when (selectedPeriod) {
            PERIOD_WEEK -> StatsPeriodKey.WEEK
            PERIOD_MONTH -> StatsPeriodKey.MONTH
            else -> StatsPeriodKey.ALL_TIME
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressStats.visibility = if (isLoading) View.VISIBLE else View.GONE
        contentContainer.alpha = if (isLoading) 0.45f else 1f
    }

    private fun updateProgress(track: FrameLayout, fill: View, value: Double, total: Double) {
        track.post {
            val targetWidth = if (total > 0.0) {
                (track.width * (value.toFloat() / total.toFloat())).toInt()
            } else {
                0
            }

            fill.layoutParams = fill.layoutParams.apply {
                width = targetWidth.coerceIn(0, track.width)
            }
        }
    }

    private fun formatMoney(value: Double): String = DecimalNumberUtils.formatMoney(value)

    private fun formatLitres(value: Double): String = "${formatPlainNumber(value)} л"

    private fun formatMileage(value: Double): String = DecimalNumberUtils.formatKilometers(value)

    private fun formatPlainNumber(value: Double): String = DecimalNumberUtils.formatDecimal(value)

    private fun roundedBackground(color: String, radius: Float): GradientDrawable {
        return GradientDrawable().apply {
            setColor(Color.parseColor(color))
            cornerRadius = radius
        }
    }

    private fun periodItemBackground(isSelected: Boolean, isDark: Boolean): StateListDrawable {
        val normalColor = if (isSelected) {
            if (isDark) "#3A1C22" else "#FFFFF6F8"
        } else {
            "#00FFFFFF"
        }
        val pressedColor = if (isDark) "#4A242A" else "#FFFFEBEF"
        return StateListDrawable().apply {
            addState(
                intArrayOf(android.R.attr.state_pressed),
                roundedBackground(pressedColor, 14.dp.toFloat()),
            )
            addState(
                intArrayOf(),
                roundedBackground(normalColor, 14.dp.toFloat()),
            )
        }
    }

    private fun keepBrandColorInDarkTheme(view: TextView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            view.setForceDarkAllowed(false)
        }
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()

    private companion object {
        const val PERIOD_WEEK = "За неделю"
        const val PERIOD_MONTH = "За месяц"
        const val PERIOD_ALL_TIME = "За всё время"
    }
}

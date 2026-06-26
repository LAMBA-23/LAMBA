package com.lamba.app.network

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.lamba.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryActivity : AppCompatActivity() {

    private lateinit var layoutTimeline: LinearLayout
    private lateinit var progressHistory: ProgressBar
    private lateinit var tvHistoryState: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
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

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.getEvents(userId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val events = response.body().orEmpty()
                        if (events.isEmpty()) {
                            showState("История пока пуста")
                        } else {
                            renderEvents(events)
                        }
                    } else {
                        showState("Не удалось загрузить историю")
                    }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    showState("Не удалось загрузить историю")
                }
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

    private fun renderEvents(events: List<Event>) {
        layoutTimeline.removeAllViews()
        progressHistory.visibility = View.GONE
        tvHistoryState.visibility = View.GONE
        layoutTimeline.visibility = View.VISIBLE

        events.forEach { event ->
            val itemView = layoutInflater.inflate(R.layout.item_history_event, layoutTimeline, false)
            val mapping = mapEventType(event.type)

            itemView.findViewById<TextView>(R.id.tvEventBadge).text = mapping.badge
            itemView.findViewById<TextView>(R.id.tvEventTitle).text = mapping.title
            itemView.findViewById<TextView>(R.id.tvEventDetails).text = event.description
            itemView.findViewById<TextView>(R.id.tvEventDate).text = formatDate(event.createdAt)

            val amountView = itemView.findViewById<TextView>(R.id.tvEventAmount)
            if (event.amount > 0) {
                amountView.text = "${formatAmount(event.amount)} ₽"
                amountView.visibility = View.VISIBLE
            } else {
                amountView.visibility = View.GONE
            }

            layoutTimeline.addView(itemView)
        }
    }

    private fun mapEventType(type: String): EventDisplay {
        return when (type.lowercase()) {
            "fuel" -> EventDisplay("Заправка", "АЗ")
            "repair" -> EventDisplay("Ремонт", "ТО")
            "trip" -> EventDisplay("Поездка", "КМ")
            "issue" -> EventDisplay("Повреждение", "!")
            "condition" -> EventDisplay("Состояние", "СТ")
            else -> EventDisplay("Событие", "•")
        }
    }

    private fun formatDate(createdAt: String?): String {
        if (createdAt.isNullOrBlank()) {
            return ""
        }

        return try {
            val datePart = createdAt.substringBefore("T").takeIf { it.length == 10 } ?: createdAt
            val parsedDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(datePart)
            if (parsedDate == null) {
                datePart
            } else {
                SimpleDateFormat("d MMM", Locale("ru")).format(parsedDate).replace(".", "")
            }
        } catch (_: Exception) {
            createdAt.substringBefore("T")
        }
    }

    private fun formatAmount(amount: Int): String {
        return "%,d".format(Locale.US, amount).replace(',', ' ')
    }

    private data class EventDisplay(
        val title: String,
        val badge: String,
    )
}

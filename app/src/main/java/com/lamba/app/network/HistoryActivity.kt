package com.lamba.app.network

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.lamba.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryActivity : AppCompatActivity() {

    private lateinit var tvTotalSpent: TextView
    private lateinit var tvFuelSpent: TextView
    private lateinit var layoutTimeline: LinearLayout
    private lateinit var btnAddEvent: Button
    private lateinit var progressBar: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        tvTotalSpent = findViewById(R.id.tvTotalSpent)
        tvFuelSpent = findViewById(R.id.tvFuelSpent)
        layoutTimeline = findViewById(R.id.layoutTimeline)
        btnAddEvent = findViewById(R.id.btnAddEvent)
        progressBar = findViewById(R.id.progressBar)

        btnAddEvent.setOnClickListener { showAddEventDialog() }

        executeMvpFlow()
    }

    private fun executeMvpFlow() {
        progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            val savedUserId = SessionManager.getUserId(this@HistoryActivity)
            if (savedUserId != null) {
                loadDataFromBackend(savedUserId)
                return@launch
            }

            try {
                val authResponse = RetrofitClient.apiService.login(
                    LoginRequest(username = "demo", password = "demo")
                )

                val demoUserId = authResponse.body()?.userId
                if (authResponse.isSuccessful && authResponse.body()?.success == true && demoUserId != null) {
                    SessionManager.saveUserId(this@HistoryActivity, demoUserId)
                    loadDataFromBackend(demoUserId)
                } else {
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                    }
                    showToast("Ошибка авторизации бэкенда")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                }
                showToast("Не удалось подключиться к бэкенду: ${e.message}")
            }
        }
    }

    private suspend fun loadDataFromBackend(userId: Int?) {
        try {
            val statsResponse = RetrofitClient.apiService.getStats(userId)
            val eventsResponse = RetrofitClient.apiService.getEvents(userId)

            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE

                if (statsResponse.isSuccessful && statsResponse.body() != null) {
                    val stats = statsResponse.body()!!
                    val totalSpent = stats.fuelExpenses + stats.repairExpenses

                    tvTotalSpent.text = "Общие расходы: $totalSpent ₽"
                    tvFuelSpent.text = "Из них на топливо: ${stats.fuelExpenses} ₽"
                } else {
                    showToast("Не удалось загрузить статистику")
                }

                if (eventsResponse.isSuccessful && eventsResponse.body() != null) {
                    layoutTimeline.removeAllViews()
                    val events = eventsResponse.body()!!

                    for (event in events) {
                        val itemView = layoutInflater.inflate(android.R.layout.simple_list_item_2, layoutTimeline, false)
                        val text1 = itemView.findViewById<TextView>(android.R.id.text1)
                        val text2 = itemView.findViewById<TextView>(android.R.id.text2)

                        text1.text = "${event.description} — ${event.amount} ₽"
                        text2.text = "Тип: ${event.type} | Пробег: ${event.mileage} км"

                        layoutTimeline.addView(itemView)
                    }
                } else {
                    showToast("Не удалось загрузить историю")
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE
            }
            showToast("Ошибка загрузки данных")
        }
    }

    private fun showAddEventDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Добавить событие")

        val context = this
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val etTitle = EditText(context).apply {
            hint = "Название (например, Заправка Лукойл)"
        }
        val etType = EditText(context).apply {
            hint = "Тип (например, Заправка)"
        }
        val etAmount = EditText(context).apply {
            hint = "Сумма расходов (₽)"
        }
        val etMileage = EditText(context).apply {
            hint = "Пробег (можно оставить пустым)"
        }

        layout.addView(etTitle)
        layout.addView(etType)
        layout.addView(etAmount)
        layout.addView(etMileage)
        builder.setView(layout)

        builder.setPositiveButton("Сохранить") { dialog, _ ->
            val title = etTitle.text.toString()
            val type = etType.text.toString()
            val amount = etAmount.text.toString().toIntOrNull() ?: 0
            val mileage = etMileage.text.toString().toIntOrNull() ?: 125000

            if (title.isNotBlank() && type.isNotBlank()) {
                val event = Event(
                    type = Event.mapUiTypeToBackend(type),
                    description = title,
                    amount = amount,
                    mileage = mileage
                )

                sendNewEventToBackend(event)
            } else {
                Toast.makeText(context, "Заполните название и тип события", Toast.LENGTH_SHORT).show()
            }

            dialog.dismiss()
        }

        builder.setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun sendNewEventToBackend(event: Event) {
        progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = SessionManager.getUserId(this@HistoryActivity)
                val response = RetrofitClient.apiService.createEvent(event, userId)

                if (response.isSuccessful) {
                    loadDataFromBackend(userId)
                    showToast("Событие сохранено")
                } else {
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                    }
                    showToast("Бэкенд отклонил сохранение")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                }
                showToast("Ошибка при отправке события")
            }
        }
    }

    private fun showToast(message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(this@HistoryActivity, message, Toast.LENGTH_LONG).show()
        }
    }
}

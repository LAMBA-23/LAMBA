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
import com.lamba.app.network.Event
import com.lamba.app.network.LoginRequest
import com.lamba.app.network.RetrofitClient
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

        // Инициализация UI компонентов
        tvTotalSpent = findViewById(R.id.tvTotalSpent)
        tvFuelSpent = findViewById(R.id.tvFuelSpent)
        layoutTimeline = findViewById(R.id.layoutTimeline)
        btnAddEvent = findViewById(R.id.btnAddEvent)
        progressBar = findViewById(R.id.progressBar)

        // Кнопка вызова формы (Диалогового окна)
        btnAddEvent.setOnClickListener { showAddEventDialog() }

        // Запуск логики MVP v0: Авторизация -> Загрузка данных
        executeMvpFlow()
    }

    private fun executeMvpFlow() {
        progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Запрос к /auth/login (демо-вход по требованиям MVP)
                val authResponse = RetrofitClient.apiService.login(LoginRequest("demo", "password"))
                if (authResponse.isSuccessful) {
                    loadDataFromBackend()
                } else {
                    showToast("Ошибка авторизации бэкенда")
                }
            } catch (e: Exception) {
                showToast("Не удалось подключиться к бэкенду: ${e.message}")
            }
        }
    }

    private suspend fun loadDataFromBackend() {
        try {
            val statsResponse = RetrofitClient.apiService.getStats()
            val eventsResponse = RetrofitClient.apiService.getEvents()

            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE

                if (statsResponse.isSuccessful && statsResponse.body() != null) {
                    val stats = statsResponse.body()!!
                    tvTotalSpent.text = "Общие расходы: ${stats.totalSpent} ₽"
                    tvFuelSpent.text = "Из них на топливо: ${stats.fuelSpent} ₽"
                }

                if (eventsResponse.isSuccessful && eventsResponse.body() != null) {
                    layoutTimeline.removeAllViews()
                    val events = eventsResponse.body()!!

                    for (event in events) {
                        val itemView = layoutInflater.inflate(android.R.layout.simple_list_item_2, null)
                        val text1 = itemView.findViewById<TextView>(android.R.id.text1)
                        val text2 = itemView.findViewById<TextView>(android.R.id.text2)

                        text1.text = "${event.title} — ${event.amount} ₽"
                        text2.text = "Тип: ${event.type} | Дата: ${event.date}"
                        layoutTimeline.addView(itemView)
                    }
                }
            }
        } catch (e: Exception) {
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

        val etTitle = EditText(context).apply { hint = "Название (например, Заправка Лукойл)" }
        val etType = EditText(context).apply { hint = "Тип (например, Заправка)" }
        val etAmount = EditText(context).apply { hint = "Сумма расходов (₽)" }

        layout.addView(etTitle)
        layout.addView(etType)
        layout.addView(etAmount)
        builder.setView(layout)

        builder.setPositiveButton("Сохранить") { dialog, _ ->
            val title = etTitle.text.toString()
            val type = etType.text.toString()
            val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0

            if (title.isNotBlank() && type.isNotBlank()) {
                sendNewEventToBackend(Event(title = title, type = type, amount = amount, date = "2026-06-13"))
            } else {
                Toast.makeText(context, "Заполните все поля!", Toast.LENGTH_SHORT).show()
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
                val response = RetrofitClient.apiService.createEvent(event)
                if (response.isSuccessful) {
                    loadDataFromBackend()
                    showToast("Событие сохранено!")
                } else {
                    showToast("Бэкенд отклонил сохранение")
                }
            } catch (e: Exception) {
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
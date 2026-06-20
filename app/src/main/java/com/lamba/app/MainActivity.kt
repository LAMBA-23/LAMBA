package com.lamba.app

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.lamba.app.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userId = intent.getIntExtra("USER_ID", -1)

        val tvCarName = findViewById<TextView>(R.id.tvCarName)
        val tvCarInfo = findViewById<TextView>(R.id.tvCarInfo)
        val btnTalkToCar = findViewById<ConstraintLayout>(R.id.btnTalkToCar)
        val btnHistory = findViewById<ConstraintLayout>(R.id.btnHistory)
        val navChat = findViewById<LinearLayout>(R.id.navChat)

        btnTalkToCar.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }

        navChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }

        btnHistory.setOnClickListener {
            val intent = Intent(this, com.lamba.app.network.HistoryActivity::class.java)
            startActivity(intent)
        }

        loadVehicleData(tvCarName, tvCarInfo)
    }

    private fun loadVehicleData(tvCarName: TextView, tvCarInfo: TextView) {
        if (userId == -1) {
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
                        tvCarName.text = "${vehicle.brand} ${vehicle.model}"
                        tvCarInfo.text = "${vehicle.currentMileage} км • ${vehicle.productionYear}"
                    } else if (response.code() == 404) {
                        val intent = Intent(this@MainActivity, AddVehicleActivity::class.java)
                        intent.putExtra("USER_ID", userId)
                        startActivity(intent)
                        finish()
                    } else {
                        tvCarName.text = "Ошибка загрузки"
                        tvCarInfo.text = "Попробуйте позже"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvCarName.text = "Автомобиль не добавлен"
                    tvCarInfo.text = "Добавьте автомобиль, чтобы начать"
                }
            }
        }
    }
}

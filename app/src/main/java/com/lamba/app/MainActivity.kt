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

        userId = intent.getIntExtra("USER_ID", SessionStore.getUserId(this))

        val btnTalkToCar = findViewById<ConstraintLayout>(R.id.btnTalkToCar)
        val btnHistory = findViewById<ConstraintLayout>(R.id.btnHistory)
        val navChat = findViewById<LinearLayout>(R.id.navChat)
        val tvCarName = findViewById<TextView>(R.id.tvCarName)
        val tvCarInfo = findViewById<TextView>(R.id.tvCarInfo)

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

        loadVehicle(tvCarName, tvCarInfo)
    }

    private fun loadVehicle(tvCarName: TextView, tvCarInfo: TextView) {
        if (userId == -1) {
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
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Не удалось загрузить данные автомобиля",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}

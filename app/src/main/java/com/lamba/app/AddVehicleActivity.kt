package com.lamba.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.lamba.app.network.RetrofitClient
import com.lamba.app.network.VehicleRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddVehicleActivity : AppCompatActivity() {

    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_vehicle)

        userId = intent.getIntExtra("USER_ID", -1)
        if (userId == -1) {
            Toast.makeText(this, "Ошибка: пользователь не определён", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val etBrand = findViewById<EditText>(R.id.etBrand)
        val etModel = findViewById<EditText>(R.id.etModel)
        val etYear = findViewById<EditText>(R.id.etYear)
        val etMileage = findViewById<EditText>(R.id.etMileage)
        val btnSave = findViewById<AppCompatButton>(R.id.btnSave)
        val tvError = findViewById<TextView>(R.id.tvError)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnSave.setOnClickListener {
            val brand = etBrand.text.toString().trim()
            val model = etModel.text.toString().trim()
            val yearStr = etYear.text.toString().trim()
            val mileageStr = etMileage.text.toString().trim()

            tvError.visibility = View.GONE

            if (brand.isEmpty()) {
                tvError.text = "Введите марку автомобиля"
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (model.isEmpty()) {
                tvError.text = "Введите модель автомобиля"
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (yearStr.isEmpty()) {
                tvError.text = "Введите год выпуска"
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (mileageStr.isEmpty()) {
                tvError.text = "Введите текущий пробег"
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            val year = yearStr.toIntOrNull()
            if (year == null || year < 1886 || year > 2100) {
                tvError.text = "Год выпуска должен быть от 1886 до 2100"
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            val mileage = mileageStr.toIntOrNull()
            if (mileage == null || mileage < 0) {
                tvError.text = "Пробег не может быть отрицательным"
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            btnSave.isEnabled = false
            progressBar.visibility = View.VISIBLE

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val request = VehicleRequest(
                        userId = userId,
                        brand = brand,
                        model = model,
                        productionYear = year,
                        currentMileage = mileage
                    )
                    val response = RetrofitClient.apiService.createVehicle(request)

                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        if (response.isSuccessful) {
                            Toast.makeText(
                                this@AddVehicleActivity,
                                "Автомобиль успешно добавлен!",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this@AddVehicleActivity, MainActivity::class.java)
                            intent.putExtra("USER_ID", userId)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                            finish()
                        } else {
                            btnSave.isEnabled = true
                            val errorMsg = when (response.code()) {
                                409 -> "У пользователя уже есть автомобиль. В MVP v1 допускается только один автомобиль."
                                422 -> "Проверьте правильность введённых данных"
                                else -> "Ошибка при сохранении: ${response.code()}"
                            }
                            tvError.text = errorMsg
                            tvError.visibility = View.VISIBLE
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        btnSave.isEnabled = true
                        tvError.text = "Не удалось подключиться к бэкенду"
                        tvError.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}

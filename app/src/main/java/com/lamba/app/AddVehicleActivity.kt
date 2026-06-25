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
            Toast.makeText(this, "Пользователь не определён", Toast.LENGTH_SHORT).show()
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

        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        btnSave.setOnClickListener {
            val brand = etBrand.text.toString().trim()
            val model = etModel.text.toString().trim()
            val year = etYear.text.toString().trim().toIntOrNull()
            val mileage = etMileage.text.toString().trim().toIntOrNull()

            tvError.visibility = View.GONE

            when {
                brand.isEmpty() -> {
                    showError(tvError, "Введите марку автомобиля")
                    return@setOnClickListener
                }
                model.isEmpty() -> {
                    showError(tvError, "Введите модель автомобиля")
                    return@setOnClickListener
                }
                year == null || year < 1886 || year > 2100 -> {
                    showError(tvError, "Год выпуска должен быть от 1886 до 2100")
                    return@setOnClickListener
                }
                mileage == null || mileage < 0 -> {
                    showError(tvError, "Пробег не может быть отрицательным")
                    return@setOnClickListener
                }
            }
            val productionYear = year ?: return@setOnClickListener
            val currentMileage = mileage ?: return@setOnClickListener

            btnSave.isEnabled = false
            progressBar.visibility = View.VISIBLE

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitClient.apiService.createVehicle(
                        VehicleRequest(
                            userId = userId,
                            brand = brand,
                            model = model,
                            productionYear = productionYear,
                            currentMileage = currentMileage,
                        ),
                    )

                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        if (response.isSuccessful) {
                            openMainFlow()
                        } else {
                            btnSave.isEnabled = true
                            showError(
                                tvError,
                                when (response.code()) {
                                    409 -> "У пользователя уже есть автомобиль"
                                    422 -> "Проверьте введённые данные"
                                    else -> "Ошибка сохранения: ${response.code()}"
                                },
                            )
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        btnSave.isEnabled = true
                        showError(tvError, "Не удалось подключиться к бэкенду")
                    }
                }
            }
        }
    }

    private fun showError(tvError: TextView, message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    private fun openMainFlow() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("USER_ID", userId)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}

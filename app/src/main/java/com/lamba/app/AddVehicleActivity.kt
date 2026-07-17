package com.lamba.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.widget.ArrayAdapter
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
        val etBrand = findViewById<AutoCompleteTextView>(R.id.etBrand)
        val etModel = findViewById<AutoCompleteTextView>(R.id.etModel)
        val etYear = findViewById<EditText>(R.id.etYear)
        val etMileage = findViewById<EditText>(R.id.etMileage)
        val btnSave = findViewById<AppCompatButton>(R.id.btnSave)
        val tvError = findViewById<TextView>(R.id.tvError)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        val brands = resources.getStringArray(R.array.popular_car_brands)
        val brandAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, brands)
        etBrand.setAdapter(brandAdapter)

        val brandToModelsMap = mapOf(
            "Audi" to R.array.audi_models,
            "BMW" to R.array.bmw_models,
            "Chevrolet" to R.array.chevrolet_models,
            "Citroën" to R.array.citroen_models,
            "Ford" to R.array.ford_models,
            "Honda" to R.array.honda_models,
            "Hyundai" to R.array.hyundai_models,
            "Kia" to R.array.kia_models,
            "Lada" to R.array.lada_models,
            "Lexus" to R.array.lexus_models,
            "Mazda" to R.array.mazda_models,
            "Mercedes-Benz" to R.array.mercedes_benz_models,
            "Mitsubishi" to R.array.mitsubishi_models,
            "Nissan" to R.array.nissan_models,
            "Opel" to R.array.opel_models,
            "Peugeot" to R.array.peugeot_models,
            "Renault" to R.array.renault_models,
            "Seat" to R.array.seat_models,
            "Skoda" to R.array.skoda_models,
            "Subaru" to R.array.subaru_models,
            "Suzuki" to R.array.suzuki_models,
            "Tesla" to R.array.tesla_models,
            "Toyota" to R.array.toyota_models,
            "Volkswagen" to R.array.volkswagen_models,
            "Volvo" to R.array.volvo_models,
            "Geely" to R.array.geely_models,
            "Chery" to R.array.chery_models,
            "Great Wall" to R.array.great_wall_models,
            "Porsche" to R.array.porsche_models,
            "Land Rover" to R.array.land_rover_models,
            "Jaguar" to R.array.jaguar_models,
            "Infiniti" to R.array.infiniti_models,
            "Acura" to R.array.acura_models,
            "Genesis" to R.array.genesis_models,
        )

        etBrand.setOnItemClickListener { _, _, _, _ ->
            val selectedBrand = etBrand.text.toString().trim()
            val modelsArrayId = brandToModelsMap[selectedBrand]
            if (modelsArrayId != null) {
                val models = resources.getStringArray(modelsArrayId)
                val modelAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, models)
                etModel.setAdapter(modelAdapter)
                etModel.setText("")
            }
        }

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

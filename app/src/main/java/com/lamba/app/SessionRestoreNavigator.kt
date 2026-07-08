package com.lamba.app

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.lamba.app.network.RetrofitClient
import com.lamba.app.network.Vehicle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object SessionRestoreNavigator {
    fun restore(
        activity: Activity,
        userId: Int,
        onStart: (() -> Unit)? = null,
        onComplete: (() -> Unit)? = null,
        onError: (() -> Unit)? = null,
    ) {
        onStart?.invoke()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val vehicleResponse = RetrofitClient.apiService.getVehicle(userId)

                withContext(Dispatchers.Main) {
                    onComplete?.invoke()

                    val vehicle = vehicleResponse.body()
                    when {
                        vehicleResponse.isSuccessful && vehicle != null && !isPlaceholderVehicle(vehicle) -> {
                            openMainFlow(activity, userId)
                        }

                        vehicleResponse.isSuccessful || vehicleResponse.code() == 404 -> {
                            openVehicleSetup(activity, userId)
                        }

                        else -> {
                            onError?.invoke()
                            Toast.makeText(
                                activity,
                                "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u0440\u043e\u0432\u0435\u0440\u0438\u0442\u044c \u0434\u0430\u043d\u043d\u044b\u0435 \u0430\u0432\u0442\u043e\u043c\u043e\u0431\u0438\u043b\u044f",
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                    }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    onComplete?.invoke()
                    onError?.invoke()
                    Toast.makeText(
                        activity,
                        "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u043e\u0434\u043a\u043b\u044e\u0447\u0438\u0442\u044c\u0441\u044f \u043a \u0431\u044d\u043a\u0435\u043d\u0434\u0443",
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }
        }
    }

    private fun isPlaceholderVehicle(vehicle: Vehicle): Boolean {
        return vehicle.brand == "Not set" &&
            vehicle.model == "Not set" &&
            vehicle.productionYear == 0 &&
            vehicle.currentMileage == 0
    }

    private fun openVehicleSetup(activity: Activity, userId: Int) {
        val intent = Intent(activity, AddVehicleActivity::class.java)
        intent.putExtra("USER_ID", userId)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        activity.startActivity(intent)
        activity.finish()
    }

    private fun openMainFlow(activity: Activity, userId: Int) {
        val intent = Intent(activity, MainActivity::class.java)
        intent.putExtra("USER_ID", userId)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        activity.startActivity(intent)
        activity.finish()
    }
}

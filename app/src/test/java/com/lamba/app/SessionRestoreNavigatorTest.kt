package com.lamba.app

import com.lamba.app.network.Vehicle
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionRestoreNavigatorTest {

    @Test
    fun restoreOpensMainFlowForSavedUserWithVehicle() {
        val vehicle = Vehicle(
            id = 7,
            brand = "Toyota",
            model = "Camry",
            productionYear = 2023,
            currentMileage = 50000,
        )

        val destination = SessionRestoreNavigator.resolveDestination(
            statusCode = 200,
            isSuccessful = true,
            vehicle = vehicle,
        )

        assertEquals(SessionRestoreNavigator.RestoreDestination.MAIN_FLOW, destination)
    }

    @Test
    fun restoreOpensVehicleSetupForMissingVehicle() {
        val destination = SessionRestoreNavigator.resolveDestination(
            statusCode = 404,
            isSuccessful = false,
            vehicle = null,
        )

        assertEquals(SessionRestoreNavigator.RestoreDestination.VEHICLE_SETUP, destination)
    }

    @Test
    fun restoreTreatsPlaceholderVehicleAsMissingVehicle() {
        val placeholderVehicle = Vehicle(
            id = 7,
            brand = "Not set",
            model = "Not set",
            productionYear = 0,
            currentMileage = 0,
        )

        val destination = SessionRestoreNavigator.resolveDestination(
            statusCode = 200,
            isSuccessful = true,
            vehicle = placeholderVehicle,
        )

        assertEquals(SessionRestoreNavigator.RestoreDestination.VEHICLE_SETUP, destination)
    }

    @Test
    fun restoreReturnsErrorForBackendFailure() {
        val destination = SessionRestoreNavigator.resolveDestination(
            statusCode = 500,
            isSuccessful = false,
            vehicle = null,
        )

        assertEquals(SessionRestoreNavigator.RestoreDestination.ERROR, destination)
    }
}

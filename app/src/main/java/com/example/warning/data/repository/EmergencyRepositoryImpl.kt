package com.example.warning.data.repository

import android.R.attr.data
import android.util.Log
import com.example.warning.data.remote.Dto.EmergencyRequestDto
import com.example.warning.data.remote.api.EmergencyApi
import com.example.warning.domain.model.EmergencyLocation
import com.example.warning.domain.model.EmergencySendResult
import com.example.warning.domain.repository.EmergencyRepository
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class EmergencyRepositoryImpl @Inject constructor(
    private val functions: FirebaseFunctions
) : EmergencyRepository {

    override suspend fun sendEmergency (location: EmergencyLocation, senderId: String): EmergencySendResult {
        // Domain model -> DTO dönüşümü
        val request = EmergencyRequestDto(
            latitude = location.latitude,
            longitude = location.longitude,
            senderId = senderId
        )

        return try {
            val result = functions
                .getHttpsCallable("sendEmergency") // index.js'deki exports adı
                .call(data)
                .await() // kotlinx-coroutines-play-services kütüphanesi gerekir

            val resMap = result.data as Map<*, *>
            EmergencySendResult(
                successCount = (resMap["successCount"] as Int),
                failureCount = (resMap["failureCount"] as Int)
            )
        } catch (e: Exception) {
            Log.e("WarningError", "Hata: ${e.message}")
            EmergencySendResult(0, 1)
        }
    }

}
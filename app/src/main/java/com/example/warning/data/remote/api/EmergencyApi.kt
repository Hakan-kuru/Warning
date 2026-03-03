package com.example.warning.data.remote.api

import com.example.warning.data.remote.Dto.EmergencyRequestDto
import com.example.warning.data.remote.Dto.EmergencyResponseDto
import retrofit2.http.Body
import retrofit2.http.POST


/**
 * Retrofit arayüzü: HTTP isteklerini tanımladığımız yer.
 * (Retrofit: HTTP REST API çağrıları için kullanılan kütüphane)
 */
interface EmergencyApi {

    @POST(".") // Örnek endpoint, backend'de ne tanımlarsan
    suspend fun sendEmergency(
        @Body request: EmergencyRequestDto
    ): EmergencyResponseDto
}
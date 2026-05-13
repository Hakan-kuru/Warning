package com.hakankuru.yanimda.domain.usecase

import android.util.Log
import com.hakankuru.yanimda.data.remote.Dto.ContactDto
import com.hakankuru.yanimda.domain.repository.FirebaseRepository
import com.hakankuru.yanimda.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

sealed class AddContactResult {
    object Idle : AddContactResult()
    object Loading : AddContactResult()
    data class NotFound(val message: String = "Kullanıcı bulunamadı") : AddContactResult()
    object Success : AddContactResult()
    data class Error(val message: String) : AddContactResult()
}

class AddContactUseCase @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val profileRepository: ProfileRepository
) {
    suspend fun execute(phone: String, country: String): Flow<AddContactResult> = flow {
        emit(AddContactResult.Loading)

        val exists = firebaseRepository.isRegistered(phone)
        if (!exists) {
            emit(AddContactResult.NotFound())
            return@flow
        }

        val owner = profileRepository.getCurrentUserOnce()
        Log.d("AddContactUseCase", "getCurrentUserOnce() result: $owner")
        if (owner == null) {
            Log.e("AddContactUseCase", "Mevcut kullanıcı bulunamadı - getCurrentUserOnce() null döndü")
            emit(AddContactResult.Error("Mevcut kullanıcı bulunamadı"))
            return@flow
        }
        
        Log.d("AddContactUseCase", "Owner found: phoneNumber=${owner.phoneNumber}, id=${owner.id}")

        // addingId olarak telefon numarasını kullan (ownerPhone ile aynı olmalı)
        val addingUserId = owner.id
        if (addingUserId.isNullOrEmpty()) {
            emit(AddContactResult.Error("Kullanıcı telefon numarası bulunamadı"))
            return@flow
        }

        val dto = ContactDto(
            id = "", // Firestore tarafı id oluşturacak
            phone = phone,
            country = country,
            name = "Bekleyen İstek",
            profilePhoto = null,
            ownerProfilePhoto = owner.profilePhoto,
            ownerPhone = owner.phoneNumber,
            ownerCountry = owner.country,
            ownerName = owner.name,
            addingId = addingUserId,
            addedId = null,
            isActiveUser = true,
            specialMessage = null,
            isLocationSend = false,
            tag = null,
            isTop = false,
            isConfirmed = false,
            date = System.currentTimeMillis()
        )

        val remoteOk = firebaseRepository.addContact(dto)
        if (!remoteOk) {
            emit(AddContactResult.Error("Sunucuya kaydedilemedi"))
            return@flow
        }
        emit(AddContactResult.Success)
    }
}



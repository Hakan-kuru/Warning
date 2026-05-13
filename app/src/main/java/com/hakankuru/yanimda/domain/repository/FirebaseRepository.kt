package com.hakankuru.yanimda.domain.repository

import com.hakankuru.yanimda.data.remote.Dto.UserDto
import com.hakankuru.yanimda.domain.model.Profile

interface FirebaseRepository {


    suspend fun startEmergencyHistoryListener(userId: String)
    fun stopEmergencyHistoryListener()

    //Kayıt        => Firebase (succes) -> local
    suspend fun getUser(phone: String): UserDto?
    suspend fun addUser(user: Profile): Boolean
    suspend fun addContact(contact: com.hakankuru.yanimda.data.remote.Dto.ContactDto): Boolean
    suspend fun isRegistered(phone: String): Boolean

    // Contact actions (by phone for legacy, by id for new flow)
    suspend fun setContactTop(ownerPhone: String, contactPhone: String, isTop: Boolean): Boolean
    suspend fun deleteContact(ownerPhone: String, contactPhone: String): Boolean
    suspend fun setContactTopById(contactId: String, isTop: Boolean): Boolean
    suspend fun deleteContactById(contactId: String): Boolean

    /**
     * Güncellenen FCM token'ı Firebase'e kaydeder
     */
    suspend fun updateFCMToken(userId: String, token: String): Boolean

    /**
     * Kullanıcının tüm Firestore verilerini (profil + contacts) siler
     */
    suspend fun deleteAccount(userId: String, phoneNumber: String): Boolean

    // Linked actions (operate on contact doc id viewed as linked)
    suspend fun confirmLinked(
        contactId: String,
        phone: String,
        country: String,
        name: String
    ): Boolean
    suspend fun deleteLinked(contactId: String): Boolean

    /*Listener    ->  firebaseService -> local
      |
      -> Start                                   */
    suspend fun startLinkedListener(phone: String)
    suspend fun startContactListener(phone: String)
    suspend fun startUserListener(phone: String)
//    -> Stop
    fun stopContactListener()
    fun stopUserListener()
    fun stopLinkedListener()
}
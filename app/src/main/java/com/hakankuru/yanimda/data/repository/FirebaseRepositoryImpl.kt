package com.hakankuru.yanimda.data.repository

import android.util.Log
import com.hakankuru.yanimda.data.mapper.toDto
import com.hakankuru.yanimda.data.remote.Dto.UserDto
import com.hakankuru.yanimda.data.remote.Dto.ContactDto
import com.hakankuru.yanimda.data.remote.service.FirestoreService
import com.hakankuru.yanimda.data.remote.listener.ContactRealtimeSyncManager
import com.hakankuru.yanimda.data.remote.listener.EmergencyHistorySyncManager
import com.hakankuru.yanimda.data.remote.listener.LinkedRealtimeSyncManager
import com.hakankuru.yanimda.data.remote.listener.UserRealtimeSyncManager
import com.hakankuru.yanimda.domain.model.Profile
import com.hakankuru.yanimda.domain.repository.FirebaseRepository
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class FirebaseRepositoryImpl @Inject constructor(
    private val firestoreService: FirestoreService,
    private val syncManagerUser: UserRealtimeSyncManager,
    private val syncLinked: LinkedRealtimeSyncManager,
    private val syncContact: ContactRealtimeSyncManager,
    private val syncEmergencyHistory: EmergencyHistorySyncManager //Inject
) : FirebaseRepository {

    override suspend fun startEmergencyHistoryListener(userId: String) {
        syncEmergencyHistory.startListening(userId)
    }

    override fun stopEmergencyHistoryListener() {
        syncEmergencyHistory.stopListening()
    }

    override suspend fun updateFCMToken(userId: String, token: String): Boolean {
        return try {
            firestoreService.updateFCMToken(userId, token)
        } catch (e: CancellationException) {
            Log.w("Firebase Service", "Coroutine iptal edildi: $e")
            throw e
        } catch (e: Exception) {
            Log.w("Firebase Service", "updateFCMToken hata: ${e}")
            false
        }
    }

    override suspend fun deleteAccount(userId: String, phoneNumber: String): Boolean {
        return try {
            firestoreService.deleteAccount(userId, phoneNumber)
        } catch (e: CancellationException) {
            Log.w("Firebase Service", "Coroutine iptal edildi: $e")
            throw e
        } catch (e: Exception) {
            Log.w("Firebase Service", "deleteAccount hata: ${e}")
            false
        }
    }

    override suspend fun getUser(phone: String): UserDto?{
        return firestoreService.getProfile(phone)
    }

    override suspend fun addUser(user: Profile): Boolean {
        return try {
            firestoreService.registerUser(user.toDto())
        }catch (e: CancellationException) {
            Log.w("Firestore Service","Coroutine iptal edildi: $e")
            throw e // iptali tekrar yukarı at
        }catch (e: Exception){
            Log.w("Firestore Service","addUser fun'da bir problemden dolayı fale döndü ${e}")
            false
        }
    }

    override suspend fun isRegistered(phone: String): Boolean {
        return firestoreService.isUserRegistered(phone)
    }

    override suspend fun addContact(contact: ContactDto): Boolean {
        return try {
            firestoreService.addContact(contact)
        } catch (e: CancellationException) {
            Log.w("Firestore Service","Coroutine iptal edildi: $e")
            throw e
        } catch (e: Exception) {
            Log.w("Firestore Service","addContact hata: ${e}")
            false
        }
    }

    override suspend fun setContactTop(ownerPhone: String, contactPhone: String, isTop: Boolean): Boolean {
        return try {
            firestoreService.updateContactFields(ownerPhone, contactPhone, mapOf("isTop" to isTop))
        } catch (e: CancellationException) {
            Log.w("Firestore Service","Coroutine iptal edildi: $e")
            throw e
        } catch (e: Exception) {
            Log.w("Firestore Service","setContactTop hata: ${e}")
            false
        }
    }

    override suspend fun deleteContact(ownerPhone: String, contactPhone: String): Boolean {
        return try {
            firestoreService.deleteContactByOwnerAndPhone(ownerPhone, contactPhone)
        } catch (e: CancellationException) {
            Log.w("Firestore Service","Coroutine iptal edildi: $e")
            throw e
        } catch (e: Exception) {
            Log.w("Firestore Service","deleteContact hata: ${e}")
            false
        }
    }

    override suspend fun setContactTopById(contactId: String, isTop: Boolean): Boolean {
        return try {
            firestoreService.updateContactById(contactId, mapOf("isTop" to isTop))
        } catch (e: CancellationException) {
            Log.w("Firestore Service","Coroutine iptal edildi: $e")
            throw e
        } catch (e: Exception) {
            Log.w("Firestore Service","setContactTopById hata: ${e}")
            false
        }
    }

    override suspend fun deleteContactById(contactId: String): Boolean {
        return try {
            firestoreService.deleteContactById(contactId)
        } catch (e: CancellationException) {
            Log.w("Firestore Service","Coroutine iptal edildi: $e")
            throw e
        } catch (e: Exception) {
            Log.w("Firestore Service","deleteContactById hata: ${e}")
            false
        }
    }

    override suspend fun confirmLinked(contactId: String, phone: String, country: String, name: String): Boolean {
        return try {
            val confirmingUser = firestoreService.getProfile(phone)
            val confirmingUserId = confirmingUser?.id
            // Update only profile-derived fields and confirmation status
            val fields = mapOf(
                "phone" to phone,
                "country" to country,
                "name" to name,
                "isConfirmed" to true,
                // addedId: onaylayan kullanıcının userId'si
                "addedId" to confirmingUserId
            )
            firestoreService.updateContactById(contactId, fields)
        } catch (e: CancellationException) {
            Log.w("Firestore Service","Coroutine iptal edildi: $e")
            throw e
        } catch (e: Exception) {
            Log.w("Firestore Service","confirmLinked hata: ${e}")
            false
        }
    }

    override suspend fun deleteLinked(contactId: String): Boolean {
        return try {
            firestoreService.deleteContactById(contactId)
        } catch (e: CancellationException) {
            Log.w("Firestore Service","Coroutine iptal edildi: $e")
            throw e
        } catch (e: Exception) {
            Log.w("Firestore Service","deleteLinked hata: ${e}")
            false
        }
    }

    //Start
    override suspend fun startContactListener(phone: String){
        syncContact.startListening(phone)
    }
    override suspend fun startUserListener(phone: String){
        syncManagerUser.startListening(phone )
    }
    override suspend fun startLinkedListener(phone: String){
        syncLinked.startListening(phone)
    }

    //Stop
    override fun stopContactListener(){
        syncContact.stopListening()
    }
    override fun stopUserListener(){
        syncManagerUser.stopListening()
    }
    override fun stopLinkedListener(){
        syncLinked.stopListening()
    }
}
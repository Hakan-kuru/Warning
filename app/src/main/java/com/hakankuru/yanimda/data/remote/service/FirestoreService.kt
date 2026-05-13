package com.hakankuru.yanimda.data.remote.service

import android.util.Log
import com.hakankuru.yanimda.data.remote.Dto.ContactDto
import com.hakankuru.yanimda.data.remote.Dto.LinkedDto
import com.hakankuru.yanimda.data.remote.Dto.UserDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException
import kotlin.jvm.java

class FirestoreService(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    //table Linked

    //tableContact


    suspend fun addContact(contactDto: ContactDto): Boolean {
        return try {
            // 1) Phone check: hedef kullanici kayitli mi?
            val targetProfileQuery = firestore.collection("profiles")
                .whereEqualTo("phoneNumber", contactDto.phone)
                .limit(1)
                .get()
                .await()
            if (targetProfileQuery.isEmpty) {
                Log.w("ServiceAddContact", "Hedef kullanıcı bulunamadı: ${contactDto.phone}")
                return false
            }

            // 2) Mükerrer kontrol: aynı owner için aynı phone zaten var mı?
            val duplicate = firestore.collection("contacts")
                .whereEqualTo("ownerPhone", contactDto.ownerPhone)
                .whereEqualTo("phone", contactDto.phone)
                .limit(1)
                .get()
                .await()
            if (!duplicate.isEmpty) {
                Log.w("ServiceAddContact", "Aynı kişi zaten ekli: ${contactDto.ownerPhone} -> ${contactDto.phone}")
                return true // idempotent davran: mevcutsa başarı say
            }

            // 3) addingId otomatik: ekleyen taraf ownerPhone olarak işaretlensin
            val payload = contactDto.copy(
                addingId = contactDto.addingId ?: contactDto.ownerPhone,
                // addedId, onaylanınca dolacak
            )

            // 4) Firebase'in id üretmesi için add kullan
            val newDoc = firestore.collection("contacts")
                .add(payload)
                .await()

            // 5) Belge içine id alanını geri yaz (okuma kolaylığı)
            firestore.collection("contacts")
                .document(newDoc.id)
                .update(mapOf("id" to newDoc.id))
                .await()

            true
        } catch (e: CancellationException) {
            Log.w("Service", "Coroutine iptal edildi")
            throw e
        } catch (e: Exception) {
            Log.e("ServiceAddContact", "Hata: ${e.message}", e)
            false
        }
    }

    // Update only specified fields for a contact document matched by ownerPhone + phone
    suspend fun updateContactFields(ownerPhone: String, contactPhone: String, fields: Map<String, Any?>): Boolean {
        return try {
            val query = firestore.collection("contacts")
                .whereEqualTo("ownerPhone", ownerPhone)
                .whereEqualTo("phone", contactPhone)
                .limit(1)
                .get()
                .await()

            val doc = query.documents.firstOrNull() ?: return false
            firestore.collection("contacts")
                .document(doc.id)
                .update(fields)
                .await()
            true
        } catch (e: CancellationException) {
            Log.w("Service", "Coroutine iptal edildi")
            throw e
        } catch (e: Exception) {
            Log.e("ServiceUpdateContact", "Hata: ${e.message}", e)
            false
        }
    }

    // Delete contact by ownerPhone + phone
    suspend fun deleteContactByOwnerAndPhone(ownerPhone: String, contactPhone: String): Boolean {
        return try {
            val query = firestore.collection("contacts")
                .whereEqualTo("ownerPhone", ownerPhone)
                .whereEqualTo("phone", contactPhone)
                .limit(1)
                .get()
                .await()

            val doc = query.documents.firstOrNull() ?: return false
            firestore.collection("contacts")
                .document(doc.id)
                .delete()
                .await()
            true
        } catch (e: CancellationException) {
            Log.w("Service", "Coroutine iptal edildi")
            throw e
        } catch (e: Exception) {
            Log.e("ServiceDeleteContact", "Hata: ${e.message}", e)
            false
        }
    }

    suspend fun updateName(userId: String, newName: String) {
        firestore.collection("profiles")
            .document(userId)
            .update("name", newName)
            .await()
    }
    // table User
    fun uploadUser(userDto: UserDto?, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        if (userDto == null) {
            onError(IllegalArgumentException("userDto is null"))
            return
        }

        // Legacy function: keep behavior if used elsewhere, but prefer registerUser for new flow
        firestore.collection("profiles")
            .add(userDto)
            .addOnSuccessListener { ref ->
                ref.update(mapOf("id" to ref.id))
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onError(it) }
            }
            .addOnFailureListener { onError(it) }
    }
    /**
     * Update the FCM token for a user identified by their Firestore document ID.
     */
    suspend fun updateFCMToken(userId: String, token: String): Boolean {
        return try {
            firestore.collection("profiles")
                .document(userId)
                .update("fcmToken", token) // Firestore'da 'fcmToken' alanını günceller
                .await()
            Log.i("ServiceUpdateToken", "FCM token başarıyla güncellendi: $userId")
            true
        } catch (e: CancellationException) {
            Log.w("Service", "Coroutine iptal edildi")
            throw e
        } catch (e: Exception) {
            Log.e("ServiceUpdateToken", "FCM token güncelleme hatası: ${e.message}", e)
            false
        }
    }
    suspend fun isUserRegistered(phoneNumber: String): Boolean {
        return try {
            val snapshot = firestore.collection("profiles")
                .whereEqualTo("phoneNumber", phoneNumber)
                .limit(1) // sadece 1 kayıt yeterli
                .get()
                .await()
            !snapshot.isEmpty // varsa true, yoksa false // kullanıcı varsa true, yoksa false
        } catch (e: Exception) {
            Log.w("Firestore", "Kullanıcı kontrol edilirken hata: $e")
            false
        }
    }


    suspend fun registerUser(userDto: UserDto): Boolean{
        return try {
            // Firestore generated id
            val ref = firestore.collection("profiles")
                .add(userDto)
                .await()
            // write back id field for lookups
            ref.update(mapOf("id" to ref.id)).await()
            Log.i("addUser", "Başarılı: ${userDto.phoneNumber} -> ${ref.id}")
            true // başarılı olursa true döner
        }
        catch (e: CancellationException) {
            Log.w("Service", "Coroutine iptal edildi")
            throw e
        }
        catch (e: Exception) {
            Log.w("Service",e)
            false // hata varsa false döner
        }

    }

    suspend fun getProfile(phoneNumber: String): UserDto? {
        return try {
            val snapshot = firestore.collection("profiles")
                .whereEqualTo("phoneNumber", phoneNumber)
                .limit(1)
                .get()
                .await()
            val doc = snapshot.documents.firstOrNull()
            Log.d("Service", "Firebase'den veri çekildi: ${doc?.data}")
            return doc?.toObject(UserDto::class.java)
        }catch (e: CancellationException) {
            // Job iptal edilmişse burası normal, hata gibi göstermemek daha iyi
            Log.d("ServiceGet", "Coroutine iptal edildi.")
            throw e // tekrar fırlat, aksi halde coroutine hiyerarşisi bozulur
        }
        catch (e: Exception) {
            Log.e("ServiceGet", "HATA: ${e.message}", e)
            throw e
        }
    }

    suspend fun getLinked(phone: String): List<LinkedDto> {
        val snapshot = firestore
            .collection("contacts")
            .whereEqualTo("phone", phone)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(ContactDto::class.java)?.let { entity ->
                LinkedDto(
                    id = doc.id,
                    phone = entity.ownerPhone,  // linked tarafı için karşı taraf owner oluyor
                    country = entity.ownerCountry,
                    name = entity.ownerName,
                    profilePhoto = entity.ownerProfilePhoto,
                    ownerPhone = entity.phone,
                    date = entity.date,
                    isConfirmed = entity.isConfirmed
                )
            }
        }
    }

    suspend fun getContacts(ownerPhone: String): List<ContactDto> {
        val snapshot = firestore
            .collection("contacts")
            .whereEqualTo("ownerPhone", ownerPhone)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(ContactDto::class.java)?.let { entity ->
                ContactDto(
                    id = doc.id,
                    phone = entity.phone,
                    country = entity.country,
                    name = entity.name,
                    profilePhoto = entity.profilePhoto,
                    ownerProfilePhoto = entity.ownerProfilePhoto,
                    ownerPhone = entity.ownerPhone,
                    ownerCountry = entity.ownerCountry,
                    ownerName = entity.ownerName,
                    addingId = entity.addingId,
                    addedId = entity.addedId,
                    isActiveUser = entity.isActiveUser,
                    specialMessage = entity.specialMessage,
                    isLocationSend = entity.isLocationSend,
                    tag = entity.tag,
                    isTop = entity.isTop,
                    isConfirmed = entity.isConfirmed,
                    date = entity.date
                )
            }
        }
    }

    // Generic update by document id on contacts
    suspend fun updateContactById(contactId: String, fields: Map<String, Any?>): Boolean {
        return try {
            if (contactId.isBlank()) return false
            firestore.collection("contacts")
                .document(contactId)
                .update(fields)
                .await()
            true
        } catch (e: CancellationException) {
            Log.w("Service", "Coroutine iptal edildi")
            throw e
        } catch (e: Exception) {
            Log.e("ServiceUpdateById", "Hata: ${e.message}", e)
            false
        }
    }

    suspend fun deleteContactById(contactId: String): Boolean {
        return try {
            if (contactId.isBlank()) return false
            firestore.collection("contacts")
                .document(contactId)
                .delete()
                .await()
            true
        } catch (e: CancellationException) {
            Log.w("Service", "Coroutine iptal edildi")
            throw e
        } catch (e: Exception) {
            Log.e("ServiceDeleteById", "Hata: ${e.message}", e)
            false
        }
    }

    /**
     * Kullanıcının hesabını tamamen siler:
     * 1) Firestore'daki profil belgesi
     * 2) Kullanıcının oluşturduğu tüm contacts (ownerPhone)
     * 3) Kullanıcının dahil olduğu tüm contacts (phone)
     */
    suspend fun deleteAccount(userId: String, phoneNumber: String): Boolean {
        return try {
            // 1) Profil belgesini sil
            firestore.collection("profiles")
                .document(userId)
                .delete()
                .await()
            Log.i("ServiceDeleteAccount", "Profil silindi: $userId")

            // 2) Kullanıcının oluşturduğu contacts'ları sil (ownerPhone)
            val ownedContacts = firestore.collection("contacts")
                .whereEqualTo("ownerPhone", phoneNumber)
                .get()
                .await()
            for (doc in ownedContacts.documents) {
                doc.reference.delete().await()
            }
            Log.i("ServiceDeleteAccount", "${ownedContacts.size()} owned contact silindi")

            // 3) Kullanıcının phone olarak geçtiği contacts'ları sil
            val linkedContacts = firestore.collection("contacts")
                .whereEqualTo("phone", phoneNumber)
                .get()
                .await()
            for (doc in linkedContacts.documents) {
                doc.reference.delete().await()
            }
            Log.i("ServiceDeleteAccount", "${linkedContacts.size()} linked contact silindi")

            true
        } catch (e: CancellationException) {
            Log.w("ServiceDeleteAccount", "Coroutine iptal edildi")
            throw e
        } catch (e: Exception) {
            Log.e("ServiceDeleteAccount", "Hesap silme hatası: ${e.message}", e)
            false
        }
    }
}

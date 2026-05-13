package com.hakankuru.yanimda.data.local

import android.content.Context
import android.provider.ContactsContract
import com.hakankuru.yanimda.domain.model.PhoneContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Cihazın rehberini (ContactsContract) okuyarak [PhoneContact] listesi döner.
 *
 * NİÇİN ayrı utility? ViewModel veya Repository'de ContentResolver işlemleri
 * karışıklığa yol açar; bu util saf ve test edilebilir bir şekilde bunu soyutlar.
 */
object ContactsReader {

    // Desteklenen ülke kodları: numara bu prefix ile başlıyorsa ayır
    private val KNOWN_COUNTRY_CODES = listOf("+90", "+1", "+44", "+49", "+33", "+31", "+39", "+34")

    /**
     * IO dispatcher'da rehberi okur, isim + telefon numarası olan kişileri döner.
     * Aynı kişinin birden fazla numarası varsa her biri ayrı [PhoneContact] olarak gelir.
     */
    suspend fun readContacts(context: Context): List<PhoneContact> = withContext(Dispatchers.IO) {
        val contacts = mutableListOf<PhoneContact>()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        )

        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val name = it.getString(nameIndex)?.trim() ?: continue
                val rawNumber = it.getString(numberIndex)?.trim() ?: continue

                val (countryCode, cleanNumber) = parsePhoneNumber(rawNumber)

                // Sadece anlamlı uzunluktaki numaraları al (en az 7 hane)
                if (cleanNumber.length >= 7) {
                    contacts.add(
                        PhoneContact(
                            name = name,
                            phoneNumber = cleanNumber,
                            countryCode = countryCode
                        )
                    )
                }
            }
        }

        // Aynı isim+numara çiftini tekilleştir
        contacts.distinctBy { it.name + it.phoneNumber }
    }

    /**
     * Ham numarayı ülke koduna ve temiz numaraya ayırır.
     *
     * Örnekler:
     *  "+90 (555) 123 45 67" → ("+90", "5551234567")
     *  "05551234567"         → ("+90", "5551234567")   ← 0 ile başlayan TR numaraları
     *  "5551234567"          → ("+90", "5551234567")
     *  "+1 555 123 4567"     → ("+1",  "5551234567")
     */
    private fun parsePhoneNumber(rawNumber: String): Pair<String, String> {
        // Sadece rakam ve + bırak
        val normalized = rawNumber.replace(Regex("[^\\d+]"), "")

        // Bilinen ülke kodu ile başlıyor mu?
        for (code in KNOWN_COUNTRY_CODES) {
            val digitsOnly = code.replace("+", "")
            if (normalized.startsWith(code)) {
                val number = normalized.removePrefix(code)
                return Pair(code, number)
            }
            // Örn: "905551234567" → +90 prefix'i rakam olarak
            if (normalized.startsWith(digitsOnly) && normalized.length > digitsOnly.length + 6) {
                val number = normalized.removePrefix(digitsOnly)
                return Pair("+$digitsOnly", number)
            }
        }

        // 0 ile başlıyorsa Türkiye numarası varsay (0555... → 555...)
        if (normalized.startsWith("0") && normalized.length >= 10) {
            return Pair("+90", normalized.removePrefix("0"))
        }

        // Varsayılan: Türkiye
        return Pair("+90", normalized)
    }
}

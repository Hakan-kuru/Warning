package com.hakankuru.yanimda.domain.model

/**
 * Cihazın rehberinden okunan kişiyi temsil eder.
 *
 * @param name          Kişinin adı (rehberdeki görünen ad)
 * @param phoneNumber   Temizlenmiş numara — ülke kodu çıkarılmış, sadece rakamlar
 *                      Örn: "+90 (555) 123 45 67" → "5551234567"
 * @param countryCode   Otomatik ayrıştırılan ülke kodu ("+90", "+1" vb.)
 *                      Numara ülke kodu içermiyorsa varsayılan "+90" atanır.
 */
data class PhoneContact(
    val name: String,
    val phoneNumber: String,
    val countryCode: String = "+90"
)

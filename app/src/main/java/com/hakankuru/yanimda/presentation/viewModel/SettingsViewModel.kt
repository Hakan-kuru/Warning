package com.hakankuru.yanimda.presentation.viewModel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hakankuru.yanimda.domain.repository.FirebaseRepository
import com.hakankuru.yanimda.domain.repository.ThemeRepository
import com.hakankuru.yanimda.domain.repository.ProfileRepository
import com.hakankuru.yanimda.domain.repository.EmergencyHistoryRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeRepository: ThemeRepository,
    private val profileRepository: ProfileRepository,
    private val firebaseRepository: FirebaseRepository,
    private val emergencyHistoryRepository: EmergencyHistoryRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val isDarkTheme = themeRepository.isDarkTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _isDeleteAccountLoading = MutableStateFlow(false)
    val isDeleteAccountLoading: StateFlow<Boolean> = _isDeleteAccountLoading.asStateFlow()

    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            themeRepository.setDarkTheme(isDark)
        }
    }

    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch {
            FirebaseAuth.getInstance().signOut()
            firebaseRepository.stopUserListener()
            firebaseRepository.stopContactListener()
            firebaseRepository.stopLinkedListener()
            firebaseRepository.stopEmergencyHistoryListener()
            profileRepository.clearAllData()
            emergencyHistoryRepository.clearEmergencyHistory()
            onLogout()
        }
    }

    /**
     * Kullanıcının tüm verilerini siler:
     * 1) Tüm listener'ları durdurur
     * 2) Firestore'dan profil + contacts belgelerini siler
     * 3) Firebase Auth kullanıcısını siler (re-authentication gerektirmez, yeni oturum)
     * 4) Local Room DB'yi temizler
     */
    fun deleteAccount(onDeleted: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isDeleteAccountLoading.value = true
            try {
                val currentUser = profileRepository.getCurrentUserOnce()
                val userId = currentUser?.id
                val phoneNumber = currentUser?.phoneNumber

                if (userId == null || phoneNumber == null) {
                    onError("Kullanıcı bilgisi alınamadı")
                    _isDeleteAccountLoading.value = false
                    return@launch
                }

                // 1) Tüm listener'ları durdur
                firebaseRepository.stopUserListener()
                firebaseRepository.stopContactListener()
                firebaseRepository.stopLinkedListener()
                firebaseRepository.stopEmergencyHistoryListener()

                // 2) Firestore'dan sil
                val firestoreSuccess = firebaseRepository.deleteAccount(userId, phoneNumber)
                if (!firestoreSuccess) {
                    onError("Veriler silinirken hata oluştu. Lütfen tekrar deneyin.")
                    _isDeleteAccountLoading.value = false
                    return@launch
                }

                // 3) Local Room DB'yi temizle
                profileRepository.clearAllData()
                emergencyHistoryRepository.clearEmergencyHistory()

                // 4) Firebase Auth kullanıcısını sil
                try {
                    FirebaseAuth.getInstance().currentUser?.delete()?.await()
                } catch (e: Exception) {
                    // Auth silme başarısız olsa bile local + Firestore temizlendi, devam et
                    Log.w("SettingsVM", "Auth kullanıcı silme başarısız (zaten temizlendi): ${e.message}")
                }

                onDeleted()
            } catch (e: Exception) {
                Log.e("SettingsVM", "deleteAccount hatası: ${e.message}", e)
                onError("Beklenmeyen bir hata oluştu: ${e.message}")
            } finally {
                _isDeleteAccountLoading.value = false
            }
        }
    }

    fun openAppSystemSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}



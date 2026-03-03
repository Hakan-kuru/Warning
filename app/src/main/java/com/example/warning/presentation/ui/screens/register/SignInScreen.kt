package com.example.warning.presentation.ui.screens.register

import com.example.warning.presentation.viewModel.RegistrationViewModel
import com.example.warning.presentation.viewModel.VerificationStep
import com.example.warning.presentation.viewModel.VerificationViewModel

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.warning.domain.model.Contact
import com.example.warning.presentation.ui.theme.AppColorScheme
import com.example.warning.presentation.viewModel.ContactListenerViewmodel
import com.example.warning.presentation.viewModel.ProfileListenerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    navController: NavHostController,
    registrationViewModel: RegistrationViewModel = hiltViewModel(),
    verificationViewModel: VerificationViewModel = hiltViewModel(),
    userview: ProfileListenerViewModel = hiltViewModel(),
    contactview: ContactListenerViewmodel= hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Ülke kodu dropdown için state
    var expanded by remember { mutableStateOf(false) }
    var selectedCountryCode by remember { mutableStateOf("+90") }
    val countryCodes = listOf("+90", "+1", "+44", "+49")

    // Telefon numarası
    var phoneNumber by remember { mutableStateOf("") }

    // Doğrulama adımları için state
    var step by remember { mutableStateOf(VerificationStep.EnterPhone) }

    // SMS kodu
    var smsCode by remember { mutableStateOf("") }

    // Hata/başarı mesajları
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Geri sayım süresi (2 dakika = 120 saniye)
    var timeLeft by remember { mutableStateOf(120) }
    val timerRunning = step == VerificationStep.EnterCode

    // Timer’ı çalıştır
    LaunchedEffect(timerRunning) {
        if (timerRunning) {
            timeLeft = 120
            while (timeLeft > 0) {
                delay(1000)
                timeLeft--
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
<<<<<<< Updated upstream
        when (step) {
            VerificationStep.EnterPhone -> {
                // 🔽 Ülke kodu seçimi
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
=======
        // Animated Icon
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer { rotationZ = rotation }
                    .background(
                        Brush.sweepGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary,
                                MaterialTheme.colorScheme.primary,
                            )
                        ),
                        CircleShape
                    )
            )

            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Login,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        Text(
            text = "Hoş Geldiniz",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Hesabınıza giriş yapın",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnterPhoneContent(
    state: SignInUiState,
    onExpandedChange: (Boolean) -> Unit,
    onCountrySelected: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onRequestCodeClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Telefon Numaranız",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            // Başlığın kartın üst sınırına çok yapışmaması için margin eklendi
            modifier = Modifier
                .padding(top = 8.dp)

        )

        Text(
            text = "SMS ile doğrulama kodu göndereceğiz.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Country Code Dropdown
        PremiumCountryDropdown(
            selectedCode = state.selectedCountryCode,
            expanded = state.expanded,
            onExpandedChange = onExpandedChange,
            onCountrySelected = onCountrySelected
        )

        // Phone Number Input
        PremiumPhoneInput(
            phone = state.phoneNumber,
            onPhoneChange = onPhoneNumberChange
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Submit Button
        PremiumButton(
            text = "Kod Gönder",
            icon = Icons.Default.Send,
            onClick = onRequestCodeClick,
            enabled = state.phoneNumber.length >= 10
        )
    }
}

@Composable
private fun EnterCodeContent(
    state: SignInUiState,
    onSmsCodeChange: (String) -> Unit,
    onVerifyClick: () -> Unit,
    onResendClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Success Icon
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                tonalElevation = 4.dp
            ) {
                Icon(
                    Icons.Default.MarkEmailRead,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(16.dp).size(32.dp)
                )
            }
        }

        Text(
            text = "Kod Gönderildi",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Telefonunuza gelen 6 haneli kodu girin",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // SMS Code Input
        PremiumCodeInput(
            code = state.smsCode,
            onCodeChange = onSmsCodeChange
        )

        // Timer or Resend
        if (state.timeLeft > 0) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
>>>>>>> Stashed changes
                ) {
                    OutlinedTextField(
                        value = selectedCountryCode,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Ülke Kodu") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        countryCodes.forEach { code ->
                            DropdownMenuItem(
                                text = { Text(code) },
                                onClick = {
                                    selectedCountryCode = code
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 📱 Telefon numarası girişi
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Telefon Numarası") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 🔘 Giriş yap butonu
                Button(
                    onClick = {
                        if (phoneNumber.length != 10) {
                            // ❌ Numara geçersiz
                            errorMessage = "Telefon numarası 10 hane olmalı"
                        } else {
                            coroutineScope.launch {
                                val exists = registrationViewModel.checkingUser(phoneNumber).await()
                                if (exists) {
                                    // ✅ Kullanıcı var → SMS gönder
                                    verificationViewModel.sendVerificationCode(
                                        selectedCountryCode + phoneNumber,
                                        context as Activity,
                                        onSuccess = { step = VerificationStep.EnterCode },
                                        onFailure = { errorMessage = "Kod gönderilemedi" }
                                    )
                                } else {
                                    errorMessage = "Kullanıcı bulunamadı"
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Giriş Yap", color = AppColorScheme.neutralLight)
                }
            }

            VerificationStep.EnterCode -> {
                // 🔑 SMS doğrulama kodu
                OutlinedTextField(
                    value = smsCode,
                    onValueChange = { smsCode = it },
                    label = { Text("SMS Kodu") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        verificationViewModel.verifyCode(smsCode)
                        if (verificationViewModel.isVerified == true) {
                            // ✅ Başarılı giriş

                            step = VerificationStep.Verified
                        } else {
                            errorMessage = verificationViewModel.errorMessage
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Doğrula", color = AppColorScheme.neutralLight)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ⏱ Süre ve tekrar gönderme
                if (timeLeft > 0) {
                    Text("Kalan süre: ${timeLeft}s", color = AppColorScheme.secondary)
                } else {
                    TextButton(
                        onClick = {
                            verificationViewModel.sendVerificationCode(
                                selectedCountryCode + phoneNumber,
                                context as Activity,
                                onSuccess = { timeLeft = 120 },
                                onFailure = { errorMessage = "Tekrar gönderilemedi" }
                            )
                        }
                    ) {
                        Text("Tekrar Gönder", color = AppColorScheme.info)
                    }
                }
            }

            VerificationStep.Verified -> {
                Text("Giriş başarılı!", color = AppColorScheme.success)
                Log.i("signIn","giriş başarılı")
                userview.startUserListener(phoneNumber)
                contactview.startContactListener(phoneNumber)
                navController.navigate("main") {
                    popUpTo("signIn") { inclusive = true }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ⚠️ Hata mesajı
        errorMessage?.let {
            Text(it, color = AppColorScheme.error)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 🔗 Kayıt Ol yönlendirme
        TextButton(onClick = {
            navController.navigate("signup") {
                popUpTo("signin") { inclusive = true }
            }
        }) {
            Text("Kayıt Ol", color = AppColorScheme.info)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignInScreenPreview() {
    // Preview için sahte NavController
    val navController = rememberNavController()

    // Temaya uygun çalışması için MaterialTheme ile sarmaladık
    MaterialTheme {
        SignInScreen(navController = navController)
    }
}

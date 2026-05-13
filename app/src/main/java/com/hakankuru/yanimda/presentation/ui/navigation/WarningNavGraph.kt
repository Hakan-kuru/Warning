package com.hakankuru.yanimda.presentation.ui.navigation

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.hakankuru.yanimda.data.local.ContactsReader
import com.hakankuru.yanimda.presentation.ui.components.ContactPickerBottomSheet
import com.hakankuru.yanimda.presentation.ui.screens.MainScreen
import com.hakankuru.yanimda.domain.usecase.AddContactResult
import com.hakankuru.yanimda.domain.usecase.ContactActionResult
import com.hakankuru.yanimda.domain.usecase.LinkedActionResult
import com.hakankuru.yanimda.presentation.ui.screens.*
import com.hakankuru.yanimda.presentation.ui.screens.register.SignInScreen
import com.hakankuru.yanimda.presentation.ui.screens.register.SignInUiState
import com.hakankuru.yanimda.presentation.ui.screens.register.SignUpScreen
import com.hakankuru.yanimda.presentation.viewModel.ContactActionsViewModel
import com.hakankuru.yanimda.presentation.viewModel.ContactListenerViewmodel
import com.hakankuru.yanimda.presentation.viewModel.EmergencyMessageViewModel
import com.hakankuru.yanimda.presentation.viewModel.LinkedActionsViewModel
import com.hakankuru.yanimda.presentation.viewModel.ProfileListenerViewModel
import com.hakankuru.yanimda.presentation.viewModel.RegistrationViewModel
import com.hakankuru.yanimda.presentation.viewModel.VerificationStep
import com.hakankuru.yanimda.presentation.viewModel.VerificationViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun WarningNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = modifier
    ) {
        // Splash Screen
        composable(Routes.SPLASH) {
            SplashScreen(navController = navController)
        }

        // Giriş Ekranı
        composable(Routes.SIGN_IN) {
            val registrationViewModel: RegistrationViewModel = hiltViewModel()
            val verificationViewModel: VerificationViewModel = hiltViewModel()
            val profileListenerViewModel: ProfileListenerViewModel = hiltViewModel()
            val contactListenerViewModel: ContactListenerViewmodel = hiltViewModel()

            // Ekran durumunu hoist eden sade state
            var countryExpanded by remember { mutableStateOf(false) }
            var selectedCountryCode by remember { mutableStateOf("+90") }
            var phoneNumber by remember { mutableStateOf("") }
            var step by remember { mutableStateOf(VerificationStep.EnterPhone) }
            var smsCode by remember { mutableStateOf("") }
            var errorMessage by remember { mutableStateOf<String?>(null) }
            var timeLeft by remember { mutableStateOf(120) }
            val timerRunning = step == VerificationStep.EnterCode

            // SMS kodu beklenirken geri sayım – sadece nav graph seviyesinde yönetilir
            LaunchedEffect(timerRunning) {
                if (timerRunning) {
                    timeLeft = 120
                    while (timeLeft > 0 && step == VerificationStep.EnterCode) {
                        delay(1000)
                        timeLeft--
                    }
                }
            }

            // Observe verification state to trigger navigation
            LaunchedEffect(verificationViewModel.isVerified) {
                if (verificationViewModel.isVerified == true) {
                    profileListenerViewModel.startUserListener(phoneNumber)
                    contactListenerViewModel.startContactListener(phoneNumber)
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.SIGN_IN) { inclusive = true }
                    }
                } else if (verificationViewModel.isVerified == false) {
                    errorMessage = verificationViewModel.errorMessage
                }
            }

            SignInScreen(
                state = SignInUiState(
                    expanded = countryExpanded,
                    selectedCountryCode = selectedCountryCode,
                    phoneNumber = phoneNumber,
                    step = step,
                    smsCode = smsCode,
                    errorMessage = errorMessage,
                    timeLeft = timeLeft,
                    isLoading = verificationViewModel.isLoading
                ),
                onExpandedChange = { countryExpanded = it },
                onCountrySelected = { selectedCountryCode = it },
                onPhoneNumberChange = { phoneNumber = it },
                onRequestCodeClick = {
                    if (phoneNumber.length != 10) {
                        errorMessage = "Telefon numarası 10 hane olmalı"
                    } else {
                        scope.launch {
                            val exists = registrationViewModel.checkingUser(phoneNumber).await()
                            if (exists) {
                                verificationViewModel.sendVerificationCode(
                                    selectedCountryCode + phoneNumber,
                                    context as Activity,
                                    onSuccess = {
                                        step = VerificationStep.EnterCode
                                        errorMessage = null
                                    },
                                    onFailure = {
                                        errorMessage = "Kod gönderilemedi"
                                    }
                                )
                            } else {
                                errorMessage = "Kullanıcı bulunamadı"
                            }
                        }
                    }
                },
                onSmsCodeChange = { smsCode = it },
                onVerifyClick = {
                    verificationViewModel.verifyCode(smsCode)
                },
                onResendClick = {
                    verificationViewModel.sendVerificationCode(
                        selectedCountryCode + phoneNumber,
                        context as Activity,
                        onSuccess = {
                            timeLeft = 120
                            errorMessage = null
                        },
                        onFailure = { errorMessage = "Tekrar gönderilemedi" }
                    )
                },
                onErrorDismiss = { errorMessage = null },
                onSignUpClick = {
                    navController.navigate(Routes.SIGN_UP) {
                        popUpTo(Routes.SIGN_IN) { inclusive = true }
                    }
                }
            )
        }

        // Kayıt Ekranı
        composable(Routes.SIGN_UP) {
            // TODO: SignUpScreen de stateless olacak şekilde benzer biçimde hoist edilebilir
            SignUpScreen(navController = navController)
        }

        // Ana Ekran
        composable(Routes.MAIN) {
            val profileViewModel: ProfileListenerViewModel = hiltViewModel()
            val contactViewModel: ContactListenerViewmodel = hiltViewModel()
            val emergencyViewModel: EmergencyMessageViewModel = hiltViewModel()

            val profile by profileViewModel.profileState.collectAsState()
            val contacts by contactViewModel.contacts.collectAsState()
            val emergencyState by emergencyViewModel.emergencyMessageState.collectAsState()
            val stats by emergencyViewModel.statsState.collectAsState()

            MainScreen(
                profile = profile,
                contactCount = contacts.size,
                stats = stats,
                emergencyState = emergencyState,
                onNotificationsClick = { navController.navigate(Routes.emergencyHistory("ALL")) },
                onContactsClick = { navController.navigate(Routes.CONTACTS) },
                onDrawerDestinationClick = { route -> navController.navigate(route) },
                onEmergencyClick = { emergencyViewModel.sendEmergencyMessage() },
                onEmergencyDialogDismiss = { emergencyViewModel.resetState() }
            )
        }

        // Profil Ekranı
        composable(Routes.PROFILE) {
            ProfileRoute(navController = navController)
        }

        // Ayarlar Ekranı
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onLogout = {
                    // ✅ FIX: Logout sonrası SignIn'e yönlendir, back stack'i temizle
                    navController.navigate(Routes.SIGN_IN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onDeleteAccount = {
                    // Hesap silme sonrası da SignIn'e yönlendir
                    navController.navigate(Routes.SIGN_IN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Bağlantılar/Kişiler Ekranı
        composable(Routes.CONTACTS) {
            val contactListenerViewModel: ContactListenerViewmodel = hiltViewModel()
            val contactActionsViewModel: ContactActionsViewModel = hiltViewModel()
            val linkedActionsViewModel: LinkedActionsViewModel = hiltViewModel()

            val contacts by contactListenerViewModel.contacts.collectAsState()
            val linked by contactListenerViewModel.linked.collectAsState()
            val actionState by contactActionsViewModel.uiState.collectAsState()
            val linkedActionState by linkedActionsViewModel.uiState.collectAsState()

            // Hata / başarı mesajlarını merkezi olarak yönet
            LaunchedEffect(actionState.lastResult) {
                when (val r = actionState.lastResult) {
                    is ContactActionResult.Success -> {
                        Toast.makeText(context, "İşlem başarılı", Toast.LENGTH_SHORT).show()
                        contactListenerViewModel.loadContact()
                    }
                    is ContactActionResult.Error -> {
                        Toast.makeText(context, r.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }

            LaunchedEffect(linkedActionState.lastResult) {
                when (val r = linkedActionState.lastResult) {
                    is LinkedActionResult.Success -> {
                        Toast.makeText(context, "İşlem başarılı", Toast.LENGTH_SHORT).show()
                        contactListenerViewModel.loadLinked()
                    }
                    is LinkedActionResult.Error -> {
                        Toast.makeText(context, r.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }

            var isRefreshing by remember { mutableStateOf(false) }

            ContactLinkedScreen(
                contacts = contacts,
                linked = linked,
                isRefreshing = isRefreshing,
                isContactLoading = { contact -> actionState.loadingContactId == contact.id },
                isLinkedLoading = { link -> linkedActionState.loadingLinkedId == link.id },
                onBack = { navController.popBackStack() },
                onAddContactClick = { navController.navigate(Routes.ADD_CONTACT) },
                onRefresh = {
                    scope.launch {
                        isRefreshing = true
                        contactListenerViewModel.loadContact()
                        contactListenerViewModel.loadLinked()
                        delay(1000)
                        isRefreshing = false
                    }
                },
                onToggleTop = { contact ->
                    contactActionsViewModel.toggleTop(contact.id, contact.isTop)
                },
                onDeleteContact = { contact ->
                    contactActionsViewModel.delete(contact.id)
                },
                onAcceptLinked = { link -> linkedActionsViewModel.accept(link.id) },
                onDeleteLinked = { link -> linkedActionsViewModel.delete(link.id) }
            )
        }

        // Kişi Ekleme Ekranı
        composable(Routes.ADD_CONTACT) {
            val contactViewModel: ContactListenerViewmodel = hiltViewModel()
            val registrationViewModel: RegistrationViewModel = hiltViewModel()

            var country by remember { mutableStateOf("+90") }
            var phone by remember { mutableStateOf("") }
            var isDropdownExpanded by remember { mutableStateOf(false) }

            // ──── Rehber İzni + BottomSheet State ────
            var showContactPicker by remember { mutableStateOf(false) }
            var showPermissionDialog by remember { mutableStateOf(false) }
            var deviceContacts by remember { mutableStateOf(listOf<com.hakankuru.yanimda.domain.model.PhoneContact>()) }
            var isContactsLoading by remember { mutableStateOf(false) }

            // İzin launcher: kullanıcı izni verirse/reddederse
            val contactsPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    // İzin verildi → rehberi oku ve BottomSheet aç
                    scope.launch {
                        isContactsLoading = true
                        showContactPicker = true
                        deviceContacts = ContactsReader.readContacts(context)
                        isContactsLoading = false
                    }
                } else {
                    Toast.makeText(context, "Rehber erişimi reddedildi", Toast.LENGTH_SHORT).show()
                }
            }

            // AddContactResult akışını merkezi dinle ve Toast göster
            LaunchedEffect(Unit) {
                contactViewModel.addContactState.collect { state ->
                    when (state) {
                        is AddContactResult.Success -> {
                            Toast.makeText(context, "Kişi eklendi", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                        is AddContactResult.Error -> {
                            Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                        }
                        is AddContactResult.NotFound -> {
                            Toast.makeText(context, "Kullanıcı bulunamadı", Toast.LENGTH_SHORT).show()
                        }
                        else -> {}
                    }
                }
            }

            AddContactScreen(
                state = AddContactUiState(
                    country = country,
                    phone = phone,
                    isDropdownExpanded = isDropdownExpanded
                ),
                onBack = { navController.popBackStack() },
                onNavigateHome = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                },
                onCountryDropdownToggle = {
                    isDropdownExpanded = !isDropdownExpanded
                },
                onCountrySelected = { selected ->
                    country = selected
                    isDropdownExpanded = false
                },
                onPhoneChange = { newPhone -> phone = newPhone },
                onSubmitClick = {
                    scope.launch {
                        try {
                            val exists = registrationViewModel.checkingUser(phone).await()
                            if (!exists) {
                                Toast.makeText(context, "Kullanıcı bulunamadı", Toast.LENGTH_SHORT).show()
                            } else {
                                contactViewModel.addContact(phone, country)
                            }
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                e.message ?: "Hata oluştu",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                onPickFromContacts = {
                    // İzin var mı kontrol et
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_CONTACTS
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasPermission) {
                        // İzin zaten var → doğrudan oku ve aç
                        scope.launch {
                            isContactsLoading = true
                            showContactPicker = true
                            deviceContacts = ContactsReader.readContacts(context)
                            isContactsLoading = false
                        }
                    } else {
                        // İzin yok → iste
                        contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    }
                }
            )

            // Rehber BottomSheet
            if (showContactPicker) {
                ContactPickerBottomSheet(
                    contacts = deviceContacts,
                    isLoading = isContactsLoading,
                    onContactSelected = { selected ->
                        // Seçilen kişinin numarasını ve ülke kodunu state'e aktar
                        phone = selected.phoneNumber
                        country = selected.countryCode
                    },
                    onDismiss = { showContactPicker = false }
                )
            }
        }


        // Acil Durum Geçmişi
        composable(
            route = Routes.EMERGENCY_HISTORY_ROUTE,
            arguments = listOf(
                navArgument("filterType") {
                    type = NavType.StringType
                    defaultValue = "ALL"
                }
            )
        ) {
            EmergencyHistoryScreen(navController = navController)
        }
        // Gelen Acil Durum Detayı
        composable(
            route = Routes.INCOMING_DETAIL,
            arguments = listOf(
                navArgument("messageId") { type = NavType.StringType },
                navArgument("currentUserId") { type = NavType.StringType }
            )
        ) {
            IncomingEmergencyDetailScreen(navController = navController)
        }

        // Giden Acil Durum Detayı
        composable(
            route = Routes.OUTGOING_DETAIL,
            arguments = listOf(
                navArgument("messageId") { type = NavType.StringType },
                navArgument("currentUserId") { type = NavType.StringType }
            )
        ) {
            OutgoingEmergencyDetailScreen(navController = navController)
        }
    }
}
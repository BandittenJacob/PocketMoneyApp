package com.jmp.pocketmoneyapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.jmp.pocketmoneyapp.data.repository.BiometricAuthManager
import com.jmp.pocketmoneyapp.data.repository.LocaleManager
import com.jmp.pocketmoneyapp.data.repository.PreferencesManager
import com.jmp.pocketmoneyapp.navigation.NavGraph
import com.jmp.pocketmoneyapp.ui.auth.PinEntryScreen
import com.jmp.pocketmoneyapp.ui.theme.PocketMoneyAppTheme
import com.jmp.pocketmoneyapp.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : FragmentActivity() {
    private lateinit var localeManager: LocaleManager

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun attachBaseContext(newBase: Context) {
        localeManager = LocaleManager(newBase)
        val context = localeManager.setLocale(localeManager.selectedLanguage)
        super.attachBaseContext(context)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request POST_NOTIFICATIONS on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        enableEdgeToEdge()
        setContent {
            PocketMoneyAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    BiometricAuthWrapper(
                        activity = this,
                        content = {
                            NavGraph(navController = navController)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BiometricAuthWrapper(
    activity: FragmentActivity,
    content: @Composable () -> Unit
) {
    val authViewModel: AuthViewModel = viewModel()
    val preferencesManager = remember { PreferencesManager(activity) }
    val biometricManager = remember { BiometricAuthManager(activity) }

    val isAuthenticated = remember { Firebase.auth.currentUser != null }
    val isBiometricEnabled = remember { preferencesManager.isBiometricEnabled }
    val biometricAvailable = remember { biometricManager.isBiometricAvailable().isAvailable }

    var isUnlocked by remember { mutableStateOf(!isAuthenticated) }
    var pinError by remember { mutableStateOf<String?>(null) }
    var showPinEntry by remember { mutableStateOf(isAuthenticated) }
    var tryBiometricFirst by remember { mutableStateOf(isAuthenticated && isBiometricEnabled && biometricAvailable) }
    // Tracks whether the user has successfully unlocked at least once — prevents double-triggering
    // biometric on first launch (the LaunchedEffect below already handles that case).
    var hasBeenUnlocked by remember { mutableStateOf(!isAuthenticated) }

    val lifecycleOwner = LocalLifecycleOwner.current

    // Lock the app when it goes to background; re-trigger biometric when returning
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    // Only lock when actually signed in
                    if (isUnlocked && Firebase.auth.currentUser != null) {
                        isUnlocked = false
                        showPinEntry = true
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    // hasBeenUnlocked guard prevents double-triggering biometric on first launch
                    if (hasBeenUnlocked && !isUnlocked && isBiometricEnabled && biometricAvailable && Firebase.auth.currentUser != null) {
                        biometricManager.authenticate(
                            activity = activity,
                            title = "Unlock App",
                            subtitle = "Use biometric to unlock",
                            negativeButtonText = "Use PIN",
                            onSuccess = {
                                isUnlocked = true
                                showPinEntry = false
                            },
                            onError = { _ -> /* Stay on PIN screen */ },
                            onFailed = { /* Stay on PIN screen */ }
                        )
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Try biometric authentication first if enabled and available (initial unlock only)
    LaunchedEffect(tryBiometricFirst) {
        if (tryBiometricFirst) {
            biometricManager.authenticate(
                activity = activity,
                title = "Unlock App",
                subtitle = "Use biometric to unlock",
                negativeButtonText = "Use PIN",
                onSuccess = {
                    isUnlocked = true
                    hasBeenUnlocked = true
                    showPinEntry = false
                },
                onError = { _ ->
                    showPinEntry = true
                },
                onFailed = {
                    showPinEntry = true
                }
            )
            tryBiometricFirst = false
        }
    }

    when {
        isUnlocked -> {
            content()
        }
        showPinEntry -> {
            PinEntryScreen(
                onPinEntered = { pin ->
                    authViewModel.verifyPin(pin) { isValid ->
                        if (isValid) {
                            pinError = null
                            isUnlocked = true
                            hasBeenUnlocked = true
                            showPinEntry = false
                        } else {
                            pinError = "Incorrect PIN"
                        }
                    }
                },
                onUseBiometric = {
                    if (isBiometricEnabled && biometricAvailable) {
                        biometricManager.authenticate(
                            activity = activity,
                            title = "Unlock App",
                            subtitle = "Use biometric to unlock",
                            negativeButtonText = "Cancel",
                            onSuccess = {
                                isUnlocked = true
                                hasBeenUnlocked = true
                                showPinEntry = false
                            },
                            onError = { _ ->
                                pinError = "Biometric authentication failed"
                            },
                            onFailed = {
                                pinError = "Biometric authentication failed"
                            }
                        )
                    }
                },
                showBiometricOption = isBiometricEnabled && biometricAvailable,
                errorMessage = pinError
            )
        }
        else -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

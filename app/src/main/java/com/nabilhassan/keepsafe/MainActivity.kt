package com.nabilhassan.keepsafe

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nabilhassan.keepsafe.screens.*
import com.nabilhassan.keepsafe.security.BiometricAuth
import com.nabilhassan.keepsafe.settings.AppLanguage
import com.nabilhassan.keepsafe.settings.LocaleHelper
import com.nabilhassan.keepsafe.settings.SettingsManager
import com.nabilhassan.keepsafe.ui.theme.KeepSafeTheme

class MainActivity : FragmentActivity() {

    private val viewModel: ScanViewModel by viewModels()

    private val notifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun attachBaseContext(newBase: Context) {
        val lang = SettingsManager.getLanguage(newBase)
        val ctx = if (lang == AppLanguage.SYSTEM) newBase
                  else LocaleHelper.wrap(newBase, lang.code)
        super.attachBaseContext(ctx)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            var themeVersion by remember { mutableIntStateOf(0) }
            val currentTheme = remember(themeVersion) { SettingsManager.getTheme(this) }

            KeepSafeTheme(theme = currentTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppRoot(
                        viewModel = viewModel,
                        activity = this,
                        onThemeChanged = { themeVersion++ }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppRoot(
    viewModel: ScanViewModel,
    activity: FragmentActivity,
    onThemeChanged: () -> Unit
) {
    var showCustomSplash by remember { mutableStateOf(true) }
    var showWelcome by remember { mutableStateOf(false) }
    var isUnlocked by remember {
        mutableStateOf(!BiometricAuth.isEnabled(activity))
    }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAbout by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    // ---- شاشة Splash ----
    AnimatedVisibility(
        visible = showCustomSplash,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        SplashContent {
            showCustomSplash = false
            showWelcome = true
        }
    }

    if (showCustomSplash) return

    // ---- بوابة البصمة ----
    if (!isUnlocked) {
        BiometricGate(activity = activity) { isUnlocked = true }
        return
    }

    // ---- رسالة الترحيب ----
    if (showWelcome) {
        WelcomeDialog(onDismiss = { showWelcome = false })
    }

    // ---- صفحة عن التطبيق ----
    if (showAbout) {
        AboutScreen(onBack = { showAbout = false })
        return
    }

    // ---- الهيكل الرئيسي ----
    Scaffold(
        bottomBar = {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 0.dp
            ) {
                val tabs = listOf(
                    Triple("روابط", Icons.Filled.Search, 0),
                    Triple("APK", Icons.Filled.Android, 1),
                    Triple("بريد", Icons.Filled.Email, 2),
                    Triple("SMS", Icons.Filled.Sms, 3),
                    Triple("سبام", Icons.Filled.Warning, 4),
                    Triple("Wi-Fi", Icons.Filled.Wifi, 5),
                    Triple("QR", Icons.Filled.QrCodeScanner, 6),
                    Triple("VPN", Icons.Filled.Shield, 7),
                    Triple("حماية", Icons.Filled.ChildCare, 8),
                    Triple("إحصائيات", Icons.Filled.BarChart, 9),
                    Triple("تصدير", Icons.Filled.Download, 10),
                    Triple("السجل", Icons.Filled.History, 11),
                    Triple("إعدادات", Icons.Filled.Settings, 12)
                )
                tabs.forEach { (label, icon, index) ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(label, fontSize = 10.sp) },
                        icon = {
                            Icon(icon, contentDescription = label,
                                modifier = Modifier.padding(2.dp))
                        }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> when (val s = state) {
                    is ScanUiState.Success -> ResultScreen(
                        report = s.report,
                        onBack = { viewModel.reset() }
                    )
                    else -> ScanScreen(
                        onScan = { viewModel.scan(it) },
                        isLoading = s is ScanUiState.Loading,
                        errorMessage = (s as? ScanUiState.Error)?.message
                    )
                }
                1 -> ApkAnalysisScreen(onBack = { selectedTab = 0 })
                2 -> EmailScanScreen(onBack = { selectedTab = 0 })
                3 -> SmsScanScreen(onBack = { selectedTab = 0 })
                4 -> SpamCheckScreen(onBack = { selectedTab = 0 })
                5 -> WifiScannerScreen(onBack = { selectedTab = 0 })
                6 -> QrScannerScreen(
                    onBack = { selectedTab = 0 },
                    onUrlScanned = { url ->
                        viewModel.scan(url)
                        selectedTab = 0
                    }
                )
                7 -> VpnToggleScreen(onBack = { selectedTab = 0 })
                8 -> ChildProtectionScreen(onBack = { selectedTab = 0 })
                9 -> StatsScreen(
                    dao = remember {
                        ThreatDatabase.getInstance(context).scanHistoryDao()
                    },
                    onBack = { selectedTab = 0 }
                )
                10 -> ExportScreen(onBack = { selectedTab = 0 })
                11 -> HistoryScreen(
                    dao = remember {
                        ThreatDatabase.getInstance(context).scanHistoryDao()
                    },
                    onBack = { selectedTab = 0 }
                )
                12 -> SettingsScreen(
                    onBack = { selectedTab = 0 },
                    onThemeChanged = onThemeChanged,
                    onOpenAbout = { showAbout = true }
                )
            }
        }
    }
}

@Composable
private fun BiometricGate(activity: FragmentActivity, onUnlocked: () -> Unit) {
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var prompted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!prompted) {
            prompted = true
            BiometricAuth.authenticate(activity,
                onSuccess = { onUnlocked() },
                onFailure = { errorMsg = it })
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Icon(Icons.Filled.Fingerprint, contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(96.dp))
        Spacer(Modifier.size(20.dp))
        Text("التطبيق مقفل", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("App is locked", fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Spacer(Modifier.size(24.dp))
        Button(onClick = {
            BiometricAuth.authenticate(activity,
                onSuccess = { onUnlocked() },
                onFailure = { errorMsg = it })
        }) {
            Icon(Icons.Filled.Fingerprint, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("المحاولة مجددًا | Retry")
        }
        errorMsg?.let {
            Spacer(Modifier.size(16.dp))
            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }
    }
}
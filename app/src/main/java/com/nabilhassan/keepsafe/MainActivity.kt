package com.nabilhassan.keepsafe

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
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
import com.nabilhassan.keepsafe.family.FamilySettings
import com.nabilhassan.keepsafe.security.BiometricAuth
import com.nabilhassan.keepsafe.security.PinLock
import com.nabilhassan.keepsafe.security.PinLockScreen
import com.nabilhassan.keepsafe.services.TrafficScannerVpnService
import com.nabilhassan.keepsafe.settings.AppLanguage
import com.nabilhassan.keepsafe.settings.LocaleHelper
import com.nabilhassan.keepsafe.settings.SettingsManager
import com.nabilhassan.keepsafe.ui.theme.KeepSafeTheme

class MainActivity : FragmentActivity() {

    private val viewModel: ScanViewModel by viewModels()

    private var sharedUrl by mutableStateOf<String?>(null)

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

        extractSharedUrl(intent)
        startProtectionIfAuto()

        setContent {
            var themeVersion by remember { mutableIntStateOf(0) }
            val currentTheme = remember(themeVersion) { SettingsManager.getTheme(this) }

            KeepSafeTheme(theme = currentTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppRoot(
                        viewModel = viewModel,
                        activity = this,
                        sharedUrl = sharedUrl,
                        onSharedConsumed = { sharedUrl = null },
                        onThemeChanged = { themeVersion++ }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        extractSharedUrl(intent)
    }

    /** A link shared from another app ("Share" → Keep Safe) is scanned before the user opens it. */
    private fun extractSharedUrl(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND) return
        val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return
        val m = Regex("""https?://\S+""").find(text)
        sharedUrl = m?.value ?: text.trim().takeIf { it.contains('.') && !it.contains(' ') }
    }

    private fun startProtectionIfAuto() {
        if (!FamilySettings.autoStart || TrafficScannerVpnService.running.value) return
        if (VpnService.prepare(this) != null) return
        try {
            startService(Intent(this, TrafficScannerVpnService::class.java)
                .setAction(TrafficScannerVpnService.ACTION_START))
        } catch (_: Exception) { }
    }
}

private const val TAB_HOME = 13
private const val TAB_SECURITY = 14
private const val TAB_LEAK = 15

@Composable
private fun AppRoot(
    viewModel: ScanViewModel,
    activity: FragmentActivity,
    sharedUrl: String?,
    onSharedConsumed: () -> Unit,
    onThemeChanged: () -> Unit
) {
    var showCustomSplash by remember { mutableStateOf(true) }
    var showWelcome by remember { mutableStateOf(false) }
    var isUnlocked by remember {
        mutableStateOf(!BiometricAuth.isEnabled(activity) && !PinLock.isSet(activity))
    }
    var selectedTab by remember { mutableIntStateOf(TAB_HOME) }
    val beginner by FamilySettings.beginner.collectAsStateWithLifecycle()
    var showAbout by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

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

    if (!isUnlocked) {
        if (PinLock.isSet(activity)) PinLockScreen(activity) { isUnlocked = true }
        else BiometricGate(activity = activity) { isUnlocked = true }
        return
    }

    if (showWelcome) {
        WelcomeDialog(onDismiss = { showWelcome = false })
    }

    if (showAbout) {
        AboutScreen(onBack = { showAbout = false })
        return
    }

    LaunchedEffect(sharedUrl) {
        sharedUrl?.let {
            viewModel.scan(it)
            selectedTab = 0
            onSharedConsumed()
        }
    }

    Scaffold(
        bottomBar = {
            val tabs = if (beginner) listOf(
                    Triple("الرئيسية", Icons.Filled.Home, TAB_HOME),
                    Triple("روابط", Icons.Filled.Search, 0),
                    Triple("الأسرة", Icons.Filled.ChildCare, 8),
                    Triple("فحص الأمان", Icons.Filled.VerifiedUser, TAB_SECURITY),
                    Triple("إعدادات", Icons.Filled.Settings, 12)
                ) else listOf(
                    Triple("الرئيسية", Icons.Filled.Home, TAB_HOME),
                    Triple("روابط", Icons.Filled.Search, 0),
                    Triple("APK", Icons.Filled.Android, 1),
                    Triple("بريد", Icons.Filled.Email, 2),
                    Triple("SMS", Icons.Filled.Sms, 3),
                    Triple("سبام", Icons.Filled.Warning, 4),
                    Triple("Wi-Fi", Icons.Filled.Wifi, 5),
                    Triple("QR", Icons.Filled.QrCodeScanner, 6),
                    Triple("VPN", Icons.Filled.Shield, 7),
                    Triple("الأسرة", Icons.Filled.ChildCare, 8),
                    Triple("فحص الأمان", Icons.Filled.VerifiedUser, TAB_SECURITY),
                    Triple("كلمات المرور", Icons.Filled.Key, TAB_LEAK),
                    Triple("إحصائيات", Icons.Filled.BarChart, 9),
                    Triple("تصدير", Icons.Filled.Download, 10),
                    Triple("السجل", Icons.Filled.History, 11),
                    Triple("إعدادات", Icons.Filled.Settings, 12)
                )
            ScrollableTabRow(
                selectedTabIndex = tabs.indexOfFirst { it.third == selectedTab }.coerceAtLeast(0),
                edgePadding = 0.dp
            ) {
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
            val home = { selectedTab = TAB_HOME }
            when (selectedTab) {
                TAB_HOME -> HomeScreen(onNavigate = { selectedTab = it })
                TAB_SECURITY -> SecurityCheckScreen(onBack = home)
                TAB_LEAK -> LeakCheckScreen(onBack = home)
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
                1 -> ApkAnalysisScreen(onBack = home)
                2 -> EmailScanScreen(onBack = home)
                3 -> SmsScanScreen(onBack = home)
                4 -> SpamCheckScreen(onBack = home)
                5 -> WifiScannerScreen(onBack = home)
                6 -> QrScannerScreen(
                    onBack = home,
                    onUrlScanned = { url ->
                        viewModel.scan(url)
                        selectedTab = 0
                    }
                )
                7 -> VpnToggleScreen(onBack = home)
                8 -> ChildProtectionScreen(onBack = home)
                9 -> StatsScreen(
                    dao = remember {
                        ThreatDatabase.getInstance(context).scanHistoryDao()
                    },
                    onBack = home
                )
                10 -> ExportScreen(onBack = home)
                11 -> HistoryScreen(
                    dao = remember {
                        ThreatDatabase.getInstance(context).scanHistoryDao()
                    },
                    onBack = home
                )
                12 -> SettingsScreen(
                    onBack = home,
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
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.Fingerprint, contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(96.dp))
        Spacer(Modifier.height(20.dp))
        Text("التطبيق مقفل", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("App is locked", fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Spacer(Modifier.height(24.dp))
        Button(onClick = {
            BiometricAuth.authenticate(activity,
                onSuccess = { onUnlocked() },
                onFailure = { errorMsg = it })
        }) {
            Icon(Icons.Filled.Fingerprint, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("المحاولة مجددًا | Retry")
        }
        errorMsg?.let {
            Spacer(Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }
    }
}

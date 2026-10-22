package com.nabilhassan.keepsafe.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nabilhassan.keepsafe.security.PinGuard
import com.nabilhassan.keepsafe.security.PinGuardDialog
import com.nabilhassan.keepsafe.security.rememberPinGuard
import com.nabilhassan.keepsafe.services.TrafficScannerVpnService

/** Starts/stops the protection VPN, asking for the system consent and the PIN when needed. */
class VpnController(
    private val context: Context,
    private val requestConsent: (Intent) -> Unit,
    val guard: PinGuard
) {
    fun startService() {
        context.startService(Intent(context, TrafficScannerVpnService::class.java)
            .setAction(TrafficScannerVpnService.ACTION_START))
    }

    fun start() {
        val intent = VpnService.prepare(context)
        if (intent != null) requestConsent(intent) else startService()
    }

    fun stop() = guard.run {
        context.startService(Intent(context, TrafficScannerVpnService::class.java)
            .setAction(TrafficScannerVpnService.ACTION_STOP))
    }

    fun set(on: Boolean) = if (on) start() else stop()
}

@Composable
fun rememberVpnController(): VpnController {
    val context = LocalContext.current
    val guard = rememberPinGuard()
    var controller by remember { mutableStateOf<VpnController?>(null) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) controller?.startService()
    }
    val c = remember { VpnController(context, { launcher.launch(it) }, guard) }
    controller = c
    PinGuardDialog(guard)
    return c
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VpnToggleScreen(onBack: () -> Unit) {
    val enabled by TrafficScannerVpnService.running.collectAsStateWithLifecycle()
    val vpn = rememberVpnController()

    Column(Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("الحماية الدائمة | VPN", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(40.dp))
        Card(modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (enabled) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f)
                else MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Shield, contentDescription = null,
                    tint = if (enabled) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(72.dp))
                Spacer(Modifier.height(16.dp))
                Text(if (enabled) "الحماية مُفعّلة" else "الحماية مُعطّلة",
                    fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("يحجب النطاقات الخبيثة عبر DNS دون التأثير على سرعة الإنترنت",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(24.dp))
                Switch(checked = enabled, onCheckedChange = { vpn.set(it) })
            }
        }
    }
}

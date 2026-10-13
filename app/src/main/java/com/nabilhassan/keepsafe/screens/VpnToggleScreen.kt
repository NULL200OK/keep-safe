package com.nabilhassan.keepsafe.screens

import android.app.Activity
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
import com.nabilhassan.keepsafe.services.TrafficScannerVpnService
import com.nabilhassan.keepsafe.ui.theme.SafeGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VpnToggleScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            context.startService(Intent(context, TrafficScannerVpnService::class.java)
                .setAction(TrafficScannerVpnService.ACTION_START))
            enabled = true
        }
    }

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
                containerColor = if (enabled) SafeGreen.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Shield, contentDescription = null,
                    tint = if (enabled) SafeGreen else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(72.dp))
                Spacer(Modifier.height(16.dp))
                Text(if (enabled) "الحماية مُفعّلة" else "الحماية مُعطّلة",
                    fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(24.dp))
                Switch(checked = enabled, onCheckedChange = { on ->
                    if (on) {
                        val intent = VpnService.prepare(context)
                        if (intent != null) launcher.launch(intent)
                        else {
                            context.startService(Intent(context,
                                TrafficScannerVpnService::class.java)
                                .setAction(TrafficScannerVpnService.ACTION_START))
                            enabled = true
                        }
                    } else {
                        context.startService(Intent(context,
                            TrafficScannerVpnService::class.java)
                            .setAction(TrafficScannerVpnService.ACTION_STOP))
                        enabled = false
                    }
                })
            }
        }
    }
}
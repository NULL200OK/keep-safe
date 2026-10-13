package com.nabilhassan.keepsafe.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.nabilhassan.keepsafe.WifiNetworkInfo
import com.nabilhassan.keepsafe.WifiSecurityScanner
import com.nabilhassan.keepsafe.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiScannerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var networks by remember { mutableStateOf<List<WifiNetworkInfo>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }

    fun doScan() {
        scope.launch {
            loading = true
            networks = withContext(Dispatchers.IO) {
                WifiSecurityScanner.scanNetworks(context)
            }
            loading = false
        }
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { if (it) doScan() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("فحص Wi-Fi | Wi-Fi Scanner", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) doScan()
                else launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Filled.Wifi, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("فحص الشبكات | Scan", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(networks, key = { it.bssid }) { n ->
                val color = when {
                    n.riskScore >= 81 -> CriticalMagenta
                    n.riskScore >= 51 -> DangerRed
                    n.riskScore >= 21 -> WarningOrange
                    else -> SafeGreen
                }
                Card(shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface)) {
                    Row(Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Wifi, contentDescription = null,
                            tint = color, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(n.ssid, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("${n.security} | ${n.bssid}", fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Text("${n.riskScore}", fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold, color = color)
                    }
                }
            }
        }
    }
}
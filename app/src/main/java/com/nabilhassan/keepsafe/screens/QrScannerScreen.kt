package com.nabilhassan.keepsafe.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(onBack: () -> Unit, onUrlScanned: (String) -> Unit) {
    val context = LocalContext.current
    var scanned by remember { mutableStateOf<String?>(null) }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.let {
            scanned = it
            onUrlScanned(it)
        }
    }

    fun openScanner() {
        val opts = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("امسح | Scan")
            setBeepEnabled(false)
            setOrientationLocked(false)
        }
        scanLauncher.launch(opts)
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { if (it) openScanner() }

    Column(Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("ماسح QR | QR Scanner", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(30.dp))
        Button(
            onClick = {
                if (ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) openScanner()
                else permLauncher.launch(Manifest.permission.CAMERA)
            },
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) {
            Text("افتح الكاميرا | Open Camera", fontWeight = FontWeight.Bold)
        }
        scanned?.let {
            Spacer(Modifier.height(20.dp))
            Card(shape = MaterialTheme.shapes.medium) {
                Text(it, Modifier.padding(16.dp), fontSize = 14.sp)
            }
        }
    }
}
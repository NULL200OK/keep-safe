package com.nabilhassan.keepsafe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(onScan: (String) -> Unit, isLoading: Boolean, errorMessage: String?) {
    var url by remember { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))
        Text("🛡 أمان | Keep Safe", fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary)
        Text("كاشف الاحتيال | Fraud Detector", fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Spacer(Modifier.height(30.dp))
        OutlinedTextField(
            value = url, onValueChange = { url = it },
            label = { Text("أدخل الرابط | Enter URL") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = {
                    clipboard.getText()?.text?.let { url = it.trim() }
                }) { Icon(Icons.Filled.ContentPaste, contentDescription = "Paste") }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { onScan(url.trim()) },
            enabled = !isLoading && url.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp),
                    color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                Spacer(Modifier.width(12.dp))
                Text("جارٍ الفحص... | Scanning...")
            } else {
                Icon(Icons.Filled.Search, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("فحص | Scan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
        if (errorMessage != null) {
            Spacer(Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(errorMessage, modifier = Modifier.padding(14.dp),
                    color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
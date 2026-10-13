package com.nabilhassan.keepsafe.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nabilhassan.keepsafe.SmsPhishingResult
import com.nabilhassan.keepsafe.SmsPhishingScanner
import com.nabilhassan.keepsafe.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsScanScreen(onBack: () -> Unit) {
    var sender by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<SmsPhishingResult?>(null) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("فحص SMS | SMS Scanner", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = sender, onValueChange = { sender = it },
            label = { Text("المرسل | Sender") },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = body, onValueChange = { body = it },
            label = { Text("الرسالة | Message") },
            modifier = Modifier.fillMaxWidth().height(150.dp),
            shape = RoundedCornerShape(14.dp))
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { result = SmsPhishingScanner.analyzeSms(sender, body) },
            enabled = sender.isNotBlank() || body.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Filled.Sms, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("فحص | Scan", fontWeight = FontWeight.Bold)
        }
        result?.let { r ->
            Spacer(Modifier.height(20.dp))
            val color = if (r.isPhishing) DangerRed else SafeGreen
            Card(colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("${(r.probability * 100).toInt()}%",
                        fontSize = 28.sp, fontWeight = FontWeight.Bold, color = color)
                    Text(r.verdictAr, color = color)
                    Text(r.verdictEn, fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
            }
        }
    }
}
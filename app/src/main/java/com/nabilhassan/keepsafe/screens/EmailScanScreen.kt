package com.nabilhassan.keepsafe.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nabilhassan.keepsafe.SmsPhishingScanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailScanScreen(onBack: () -> Unit) {
    var subject by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("فحص البريد | Email Scanner", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = subject, onValueChange = { subject = it },
            label = { Text("الموضوع | Subject") },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = body, onValueChange = { body = it },
            label = { Text("النص | Body") },
            modifier = Modifier.fillMaxWidth().height(180.dp),
            shape = RoundedCornerShape(14.dp))
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                val r = SmsPhishingScanner.analyzeSms(subject, body)
                resultText = if (r.isPhishing)
                    "⚠️ تصيّد محتمل | Possible phishing"
                else "✅ طبيعي | Legitimate"
            },
            enabled = subject.isNotBlank() || body.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Filled.Email, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("فحص | Scan", fontWeight = FontWeight.Bold)
        }
        resultText?.let {
            Spacer(Modifier.height(20.dp))
            Card(shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface)) {
                Text(it, Modifier.padding(16.dp), fontSize = 14.sp)
            }
        }
    }
}
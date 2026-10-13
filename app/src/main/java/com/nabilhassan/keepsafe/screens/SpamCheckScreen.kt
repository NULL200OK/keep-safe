package com.nabilhassan.keepsafe.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nabilhassan.keepsafe.SpamChecker
import com.nabilhassan.keepsafe.SpamResult
import com.nabilhassan.keepsafe.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpamCheckScreen(onBack: () -> Unit) {
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<SpamResult?>(null) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("فاحص السبام | Spam Checker", fontSize = 20.sp,
                fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = input, onValueChange = { input = it },
            label = { Text("أدخل نصًا للفحص | Enter text") },
            modifier = Modifier.fillMaxWidth().height(150.dp),
            shape = RoundedCornerShape(14.dp)
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { result = SpamChecker.checkLocal(input) },
            enabled = input.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Filled.Search, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("فحص | Check", fontWeight = FontWeight.Bold)
        }
        result?.let { r ->
            Spacer(Modifier.height(20.dp))
            val color = if (r.isSpam) DangerRed else SafeGreen
            Card(modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))) {
                Column(Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${(r.probability * 100).toInt()}%",
                        fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = color)
                    Text(r.verdictAr, fontSize = 18.sp,
                        fontWeight = FontWeight.Bold, color = color)
                    Text(r.verdictEn, fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
            }
        }
    }
}
package com.nabilhassan.keepsafe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashContent(onComplete: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2200)
        onComplete()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.Security, contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(96.dp))
        Spacer(Modifier.height(20.dp))
        Text("أمان", fontSize = 42.sp, fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary)
        Text("Keep Safe", fontSize = 26.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface)

        Spacer(Modifier.height(24.dp))

        Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            shape = RoundedCornerShape(12.dp)) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("تصميم وتطوير", fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Text("نبيل حسن", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)
                Text("Nabil Hassan", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Spacer(Modifier.height(6.dp))
                Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(50)) {
                    Text("NULL200OK",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 3.dp),
                        fontSize = 11.sp, fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("خبير الأمن السيبراني | مطوّر برمجيات | ذكاء اصطناعي",
            fontSize = 11.sp, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Spacer(Modifier.height(40.dp))
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary, strokeWidth = 3.dp,
            modifier = Modifier.size(32.dp))
    }
}
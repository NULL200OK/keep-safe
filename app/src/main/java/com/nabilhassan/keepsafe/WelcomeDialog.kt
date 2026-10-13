package com.nabilhassan.keepsafe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("ابدأ | Start", fontWeight = FontWeight.Bold)
            }
        },
        icon = {
            Icon(Icons.Filled.Security, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp))
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("تطبيق أمان", fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp, color = MaterialTheme.colorScheme.primary)
                Text("Keep Safe", fontWeight = FontWeight.Bold,
                    fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                Spacer(Modifier.height(12.dp))
                Text("هذا التطبيق من تصميم وتطوير:",
                    fontSize = 14.sp, textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                Text("Designed & Developed by:",
                    fontSize = 12.sp, textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(10.dp))
                Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("نبيل حسن", fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
                        Text("Nabil Hassan", fontWeight = FontWeight.Bold,
                            fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(8.dp))
                        Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(50)) {
                            Text("NULL200OK",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                fontSize = 12.sp, fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text("خبير الأمن السيبراني\nمطوّر برمجيات\nمتخصص في الذكاء الاصطناعي",
                    fontSize = 14.sp, fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center, lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(6.dp))
                Text("Cybersecurity Expert\nSoftware Developer\nAI Specialist",
                    fontSize = 12.sp, textAlign = TextAlign.Center, lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                Spacer(Modifier.height(10.dp))
                Text("🛡 يفحص الروابط ويكشف الاحتيال\n🛡 Detects phishing & fraud",
                    fontSize = 12.sp, textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
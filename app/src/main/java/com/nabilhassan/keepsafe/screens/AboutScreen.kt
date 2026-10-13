package com.nabilhassan.keepsafe.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nabilhassan.keepsafe.ui.theme.SafeGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("عن التطبيق | About", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(24.dp))

        Surface(
            modifier = Modifier.size(120.dp).clip(CircleShape),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Security, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(70.dp))
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("أمان", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary)
        Text("Keep Safe", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("الإصدار | Version 1.0.0", fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

        Spacer(Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))) {
            Column(Modifier.padding(20.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text("تصميم وتطوير | Designed by", fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(8.dp))
                Text("نبيل حسن", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary)
                Text("Nabil Hassan", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text("خبير الأمن السيبراني\nمطوّر برمجيات\nمتخصص في الذكاء الاصطناعي",
                    fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 20.sp)
                Spacer(Modifier.height(12.dp))

                Surface(color = SafeGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(50)) {
                    Text("NULL200OK",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        fontSize = 14.sp, fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 3.sp, color = SafeGreen)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        AboutLink(Icons.Filled.Code, "المستودع على GitHub", "GitHub",
            "github.com/NULL2000K/keep-safe") {
            context.startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("https://github.com/NULL2000K/keep-safe")))
        }

        AboutLink(Icons.Filled.Person, "حساب المطوّر", "Profile",
            "github.com/NULL2000K") {
            context.startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("https://github.com/NULL2000K")))
        }

        Spacer(Modifier.height(20.dp))
        Text("رخصة MIT | MIT License", fontSize = 12.sp,
            fontWeight = FontWeight.Bold)
        Text("صُنع بـ ❤️ — NULL200OK", fontSize = 11.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

        Spacer(Modifier.height(30.dp))
    }
}

@Composable
private fun AboutLink(
    icon: ImageVector, titleAr: String, titleEn: String,
    subtitle: String, onClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(titleAr, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(titleEn, fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text(subtitle, fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
            }
        }
    }
}
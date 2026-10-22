package com.nabilhassan.keepsafe.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nabilhassan.keepsafe.family.BlockLog
import com.nabilhassan.keepsafe.security.SecurityChecker
import com.nabilhassan.keepsafe.services.TrafficScannerVpnService

/** Simple dashboard for non-expert users: one switch, one number, a few big buttons. */
@Composable
fun HomeScreen(onNavigate: (Int) -> Unit) {
    val context = LocalContext.current
    val enabled by TrafficScannerVpnService.running.collectAsStateWithLifecycle()
    val logVersion by BlockLog.version.collectAsStateWithLifecycle()
    val vpn = rememberVpnController()

    val today = remember(logVersion) { BlockLog.todayTotal() }
    val week = remember(logVersion) { BlockLog.weekTotal() }
    val warnings = remember(enabled) { SecurityChecker.bypassWarnings(context) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("أمان | Keep Safe", fontSize = 24.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)
        Text("حمايتك في مكان واحد", fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(20.dp))

        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (enabled) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f)
                else MaterialTheme.colorScheme.surfaceVariant)) {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Shield, contentDescription = null,
                    tint = if (enabled) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp))
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (enabled) "أنت محمي" else "الحماية متوقفة",
                        fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(if (enabled) "يحجب المواقع الضارة وغير اللائقة"
                    else "فعّلها ليحجب التطبيق المواقع الضارة", fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = enabled, onCheckedChange = { vpn.set(it) })
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("محجوب اليوم", today.toString(), Modifier.weight(1f))
            StatCard("محجوب هذا الأسبوع", week.toString(), Modifier.weight(1f))
        }

        warnings.forEach {
            Spacer(Modifier.height(12.dp))
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f))) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, contentDescription = null,
                        tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(10.dp))
                    Text(it, fontSize = 13.sp)
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("ماذا تريد أن تفعل؟", fontSize = 16.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        BigAction(Icons.Filled.VerifiedUser, "فحص أمان هاتفي", "درجة من 100 مع خطوات إصلاح") {
            onNavigate(14)
        }
        BigAction(Icons.Filled.Search, "هل هذا الرابط آمن؟", "الصق الرابط أو شاركه إلى التطبيق") {
            onNavigate(0)
        }
        BigAction(Icons.Filled.ChildCare, "حماية الأسرة", "المستوى، الجدول الزمني، قوائم الحجب") {
            onNavigate(8)
        }
        BigAction(Icons.Filled.Sms, "رسالة أو مكالمة مشبوهة؟", "افحص النص قبل أن تثق به") {
            onNavigate(3)
        }
        BigAction(Icons.Filled.Key, "هل تسرّبت كلمة مروري؟", "فحص خاص لا يرسل كلمتك") {
            onNavigate(15)
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun BigAction(icon: ImageVector, title: String, sub: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(14.dp))
            Column {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(sub, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

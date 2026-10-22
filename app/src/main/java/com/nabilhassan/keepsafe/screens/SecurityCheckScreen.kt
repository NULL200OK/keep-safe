package com.nabilhassan.keepsafe.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nabilhassan.keepsafe.security.CheckReport
import com.nabilhassan.keepsafe.security.SecurityChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityCheckScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var report by remember { mutableStateOf<CheckReport?>(null) }
    var round by remember { mutableIntStateOf(0) }

    LaunchedEffect(round) {
        report = null
        report = withContext(Dispatchers.IO) { SecurityChecker.run(context) }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("فحص أمان الهاتف | Security Check", fontSize = 20.sp,
                fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))

        val r = report
        if (r == null) {
            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        val color = when {
            r.score >= 80 -> MaterialTheme.colorScheme.tertiary
            r.score >= 50 -> MaterialTheme.colorScheme.secondary
            else -> MaterialTheme.colorScheme.error
        }
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))) {
            Column(Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${r.score}/100", fontSize = 44.sp, fontWeight = FontWeight.Bold,
                    color = color)
                Text(
                    when {
                        r.score >= 80 -> "وضعك الأمني ممتاز"
                        r.score >= 50 -> "جيد، لكن هناك ما يمكن تحسينه"
                        else -> "هاتفك معرّض للخطر — أصلح النقاط الحمراء"
                    }, fontSize = 14.sp)
            }
        }
        Spacer(Modifier.height(12.dp))

        r.items.sortedBy { it.ok }.forEach { item ->
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (item.ok) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                        contentDescription = null,
                        tint = if (item.ok) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(item.detail, fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    val action = item.fixAction
                    if (!item.ok && action != null) {
                        TextButton(onClick = {
                            try {
                                context.startActivity(Intent(action)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            } catch (_: Exception) { }
                        }) { Text("إصلاح") }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = { round++ }, modifier = Modifier.fillMaxWidth()) {
            Text("إعادة الفحص | Re-scan")
        }
    }
}

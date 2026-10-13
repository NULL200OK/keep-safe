package com.nabilhassan.keepsafe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nabilhassan.keepsafe.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(report: UrlReport, onBack: () -> Unit) {
    val score = report.risk.score
    val color = when {
        score >= 81 -> CriticalMagenta
        score >= 51 -> DangerRed
        score >= 21 -> WarningOrange
        else -> SafeGreen
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("التقرير | Report", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))
        ) {
            Column(Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$score / 100", fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold, color = color)
                Spacer(Modifier.height(10.dp))
                Text(report.risk.verdictAr, fontSize = 20.sp,
                    fontWeight = FontWeight.Bold, color = color)
                Text(report.risk.verdictEn, fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
            }
        }
        Spacer(Modifier.height(16.dp))
        InfoCard("الرابط | URL", report.originalUrl)
        InfoCard("المضيف | Host", report.host)
        InfoCard("النطاق | Domain", report.registeredDomain)

        if (report.risk.reasons.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text("الأسباب | Reasons", fontSize = 16.sp,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            report.risk.reasons.forEach { (ar, en) ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(ar, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface)
                        Text(en, fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(label: String, value: String) {
    if (value.isBlank()) return
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(label, fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
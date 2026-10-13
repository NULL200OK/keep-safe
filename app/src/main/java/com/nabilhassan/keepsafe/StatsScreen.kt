package com.nabilhassan.keepsafe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nabilhassan.keepsafe.ui.theme.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(dao: ScanHistoryDao, onBack: () -> Unit) {
    val total by dao.getTotalScans().collectAsStateWithLifecycle(initialValue = 0)
    val dangerous by dao.getDangerousScans().collectAsStateWithLifecycle(initialValue = 0)
    val safe by dao.getSafeScans().collectAsStateWithLifecycle(initialValue = 0)
    val avg by dao.getAverageScore().collectAsStateWithLifecycle(initialValue = 0.0)

    val weekAgo = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }.timeInMillis
    }
    val weekScans by dao.getScansSince(weekAgo).collectAsStateWithLifecycle(initialValue = 0)

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("الإحصائيات | Statistics", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("إجمالي", "$total", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
            StatCard("مهددة", "$dangerous", DangerRed, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("آمنة", "$safe", SafeGreen, Modifier.weight(1f))
            StatCard("متوسط", "${(avg ?: 0.0).toInt()}%", WarningOrange, Modifier.weight(1f))
        }
        Spacer(Modifier.height(20.dp))
        StatCard("آخر 7 أيام", "$weekScans", SafeGreen, Modifier.fillMaxWidth())
    }
}

@Composable
private fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))) {
        Column(Modifier.padding(16.dp)) {
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(label, fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
        }
    }
}
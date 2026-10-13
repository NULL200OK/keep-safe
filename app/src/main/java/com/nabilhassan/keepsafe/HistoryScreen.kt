package com.nabilhassan.keepsafe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nabilhassan.keepsafe.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(dao: ScanHistoryDao, onBack: () -> Unit) {
    val history by dao.getAll().collectAsStateWithLifecycle(initialValue = emptyList())
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("السجل | History", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { scope.launch { dao.clearAll() } }) {
                Icon(Icons.Filled.Delete, contentDescription = "Clear")
            }
        }
        if (history.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا يوجد سجل | No history",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(history, key = { it.id }) { item ->
                    val color = when {
                        item.score >= 81 -> CriticalMagenta
                        item.score >= 51 -> DangerRed
                        item.score >= 21 -> WarningOrange
                        else -> SafeGreen
                    }
                    Card(shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface)) {
                        Row(Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Text("${item.score}", fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold, color = color)
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(item.host, fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                                Text(item.verdictAr, fontSize = 12.sp, color = color)
                            }
                            IconButton(onClick = {
                                scope.launch { dao.deleteById(item.id) }
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                            }
                        }
                    }
                }
            }
        }
    }
}
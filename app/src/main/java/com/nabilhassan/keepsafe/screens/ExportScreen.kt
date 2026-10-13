package com.nabilhassan.keepsafe.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.nabilhassan.keepsafe.ThreatDatabase
import com.nabilhassan.keepsafe.export.CsvExporter
import com.nabilhassan.keepsafe.export.PdfExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("تصدير | Export", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(30.dp))
        Button(
            onClick = {
                scope.launch {
                    val file = withContext(Dispatchers.IO) {
                        val items = ThreatDatabase.getInstance(context)
                            .scanHistoryDao().getAllOnce()
                        PdfExporter.exportHistory(context, items)
                    }
                    shareFile(context, file)
                    status = "✅ PDF"
                }
            },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Filled.PictureAsPdf, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("تصدير PDF", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                scope.launch {
                    val file = withContext(Dispatchers.IO) {
                        val items = ThreatDatabase.getInstance(context)
                            .scanHistoryDao().getAllOnce()
                        CsvExporter.exportHistory(context, items)
                    }
                    shareFile(context, file)
                    status = "✅ CSV"
                }
            },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Icon(Icons.Filled.TableChart, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("تصدير CSV", fontWeight = FontWeight.Bold)
        }
        status?.let {
            Spacer(Modifier.height(20.dp))
            Text(it, color = MaterialTheme.colorScheme.primary)
        }
    }
}

private fun shareFile(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = if (file.extension == "pdf") "application/pdf" else "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share"))
}
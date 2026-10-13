package com.nabilhassan.keepsafe.export

import android.content.Context
import android.os.Environment
import com.nabilhassan.keepsafe.ScanHistory
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object CsvExporter {
    fun exportHistory(context: Context, items: List<ScanHistory>): File {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
        val file = File(dir, "keep_safe_${System.currentTimeMillis()}.csv")
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        file.bufferedWriter().use { w ->
            w.write("ID,Type,URL,Host,Score,VerdictAR,VerdictEN,Date\n")
            for (item in items) {
                w.write("${item.id},${item.scanType},\"${item.url}\",\"${item.host}\"," +
                        "${item.score},\"${item.verdictAr}\",\"${item.verdictEn}\"," +
                        "${df.format(Date(item.scannedAt))}\n")
            }
        }
        return file
    }
}
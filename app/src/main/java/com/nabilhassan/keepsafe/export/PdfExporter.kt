package com.nabilhassan.keepsafe.export

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.nabilhassan.keepsafe.ScanHistory
import java.io.File
import java.io.FileOutputStream

object PdfExporter {
    fun exportHistory(context: Context, items: List<ScanHistory>): File {
        val doc = PdfDocument()
        val paint = Paint().apply { textSize = 12f }
        val info = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = doc.startPage(info)
        val canvas = page.canvas
        var y = 40f
        paint.textSize = 18f
        canvas.drawText("Keep Safe — NULL200OK", 30f, y, paint)
        y += 30f
        paint.textSize = 11f
        for (item in items) {
            if (y > 800f) break
            canvas.drawText("${item.scanType} | ${item.host} | ${item.score}", 30f, y, paint)
            y += 18f
        }
        doc.finishPage(page)
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
        val file = File(dir, "keep_safe_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }
}
package com.pamneuroncraft.jobapplicationtracker.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.pamneuroncraft.jobapplicationtracker.domain.repository.ExportManager
import java.io.File

class AndroidExportManager(
    private val context: Context
) : ExportManager {
    override fun shareCsv(content: String, fileName: String) {
        val cacheDir = context.cacheDir
        val file = File(cacheDir, fileName)
        file.writeText(content)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val shareIntent = Intent.createChooser(intent, "Export Jobs CSV").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        context.startActivity(shareIntent)
    }
}

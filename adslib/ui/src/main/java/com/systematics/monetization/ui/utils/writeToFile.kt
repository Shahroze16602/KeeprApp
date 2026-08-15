package com.systematics.monetization.ui.utils

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

fun writeToFile(
    context: Context,
    filename: String,
    data: String,
    folder: String = "AdsLib",
) {
    try {
        // Create (or get) the subdirectory inside internal storage
        val dir = File(context.filesDir, folder)
        if (!dir.exists()) {
            dir.mkdir() // or dir.mkdirs() if you need nested directories
        }

        // Create the file inside the subdirectory
        val file = File(dir, filename)
        val fileOutputStream = FileOutputStream(file)

        // Write data
        fileOutputStream.write(data.toByteArray())
        fileOutputStream.close()

        println("Data written to file successfully at: ${file.absolutePath}")
    } catch (e: IOException) {
        e.printStackTrace()
    }
}
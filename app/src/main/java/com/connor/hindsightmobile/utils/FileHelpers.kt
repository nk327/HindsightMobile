package com.connor.hindsightmobile.utils

import android.content.Context
import java.io.File
import java.io.FileOutputStream

fun getImageFiles(directory: File): List<File> {
    return directory.listFiles { _, name -> name.endsWith(".webp") }?.toList() ?: emptyList()
}

fun getUnprocessedScreenshotsDirectory(context: Context): File {
    val directory = File(context.filesDir, "unprocessed_screenshot_images")
    if (!directory.exists()) directory.mkdirs() // Ensure the directory exists
    return directory
}

fun parseScreenshotFilePath(filePath: String): Pair<String?, Long?> {
    val fileName = filePath.substringAfterLast("/")
    val parts = fileName.removeSuffix(".webp").split("_")

    return if (parts.size == 2) {
        val application = parts[0]
        val timestamp = parts[1].toLongOrNull()
        Pair(application, timestamp)
    } else {
        Pair(null, null)
    }
}

fun getAssetFile(context: Context, assetName: String): File {
    val file = File(context.filesDir, assetName)
    if (!file.exists()) {
        context.assets.open(assetName).use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }
    return file
}
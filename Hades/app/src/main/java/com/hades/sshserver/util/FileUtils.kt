package com.hades.sshserver.util

import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ln
import kotlin.math.pow

object FileUtils {
    /**
     * Format file size in human-readable format
     */
    fun formatFileSize(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val units = arrayOf("KB", "MB", "GB", "TB")
        val exp = (ln(bytes.toDouble()) / ln(1024.0)).toInt()
        val pre = units[exp - 1]
        return String.format("%.1f %s", bytes / 1024.0.pow(exp.toDouble()), pre)
    }

    /**
     * Format date/time for display
     */
    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /**
     * Get file extension from filename
     */
    fun getFileExtension(filename: String): String {
        val lastDot = filename.lastIndexOf('.')
        return if (lastDot > 0 && lastDot < filename.length - 1) {
            filename.substring(lastDot + 1).lowercase()
        } else {
            ""
        }
    }

    /**
     * Check if file is an image
     */
    fun isImageFile(mimeType: String?): Boolean {
        return mimeType?.startsWith("image/") == true
    }

    /**
     * Check if file is a video
     */
    fun isVideoFile(mimeType: String?): Boolean {
        return mimeType?.startsWith("video/") == true
    }

    /**
     * Check if file is an audio file
     */
    fun isAudioFile(mimeType: String?): Boolean {
        return mimeType?.startsWith("audio/") == true
    }

    /**
     * Check if file is a document
     */
    fun isDocumentFile(mimeType: String?): Boolean {
        return mimeType?.startsWith("text/") == true ||
                mimeType?.contains("pdf") == true ||
                mimeType?.contains("document") == true ||
                mimeType?.contains("msword") == true ||
                mimeType?.contains("ms-excel") == true ||
                mimeType?.contains("ms-powerpoint") == true
    }
}

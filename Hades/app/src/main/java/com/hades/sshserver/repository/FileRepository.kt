package com.hades.sshserver.repository

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import com.hades.sshserver.data.FileItem
import com.hades.sshserver.data.NavigationLocation
import com.hades.sshserver.data.LocationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FileRepository(private val context: Context) {

    /**
     * Get list of files in a directory
     */
    suspend fun getFilesInDirectory(path: String): List<FileItem> = withContext(Dispatchers.IO) {
        try {
            val directory = File(path)
            if (!directory.exists() || !directory.isDirectory) {
                return@withContext emptyList()
            }

            directory.listFiles()?.mapNotNull { file ->
                try {
                    FileItem(
                        name = file.name,
                        path = file.absolutePath,
                        uri = Uri.fromFile(file),
                        isDirectory = file.isDirectory,
                        size = if (file.isDirectory) 0 else file.length(),
                        lastModified = file.lastModified(),
                        mimeType = getMimeType(file),
                        isHidden = file.isHidden,
                        canRead = file.canRead(),
                        canWrite = file.canWrite()
                    )
                } catch (e: Exception) {
                    null
                }
            }?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Create a new directory
     */
    suspend fun createDirectory(parentPath: String, name: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val newDir = File(parentPath, name)
            newDir.mkdir()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Delete a file or directory
     */
    suspend fun deleteFile(fileItem: FileItem): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(fileItem.path)
            file.deleteRecursively()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Rename a file or directory
     */
    suspend fun renameFile(fileItem: FileItem, newName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(fileItem.path)
            val newFile = File(file.parent, newName)
            file.renameTo(newFile)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Copy a file to a destination
     */
    suspend fun copyFile(fileItem: FileItem, destinationPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val sourceFile = File(fileItem.path)
            val destFile = File(destinationPath, fileItem.name)
            sourceFile.copyRecursively(destFile, overwrite = false)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Move a file to a destination
     */
    suspend fun moveFile(fileItem: FileItem, destinationPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val sourceFile = File(fileItem.path)
            val destFile = File(destinationPath, fileItem.name)
            sourceFile.renameTo(destFile)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Get navigation locations
     */
    fun getNavigationLocations(): List<NavigationLocation> {
        val externalStorage = Environment.getExternalStorageDirectory().absolutePath
        return listOf(
            NavigationLocation(
                "Internal Storage",
                externalStorage,
                LocationType.INTERNAL_STORAGE
            ),
            NavigationLocation(
                "Downloads",
                "$externalStorage/Download",
                LocationType.DOWNLOADS
            ),
            NavigationLocation(
                "Documents",
                "$externalStorage/Documents",
                LocationType.DOCUMENTS
            ),
            NavigationLocation(
                "Pictures",
                "$externalStorage/Pictures",
                LocationType.PICTURES
            ),
            NavigationLocation(
                "Videos",
                "$externalStorage/Movies",
                LocationType.VIDEOS
            ),
            NavigationLocation(
                "Audio",
                "$externalStorage/Music",
                LocationType.AUDIO
            )
        )
    }

    /**
     * Get MIME type for a file
     */
    private fun getMimeType(file: File): String? {
        if (file.isDirectory) return null
        val extension = file.extension
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }
}

package com.hades.sshserver.data

import android.net.Uri

/**
 * Represents a file or directory in the file system
 */
data class FileItem(
    val name: String,
    val path: String,
    val uri: Uri,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
    val mimeType: String?,
    val isHidden: Boolean = false,
    val canRead: Boolean = true,
    val canWrite: Boolean = true
)

/**
 * Represents a storage volume (internal/external)
 */
data class StorageVolume(
    val name: String,
    val path: String,
    val totalSpace: Long,
    val freeSpace: Long,
    val isRemovable: Boolean,
    val isPrimary: Boolean
)

/**
 * Represents a quick access location in navigation drawer
 */
data class NavigationLocation(
    val name: String,
    val path: String,
    val type: LocationType
)

enum class LocationType {
    ROOT,
    INTERNAL_STORAGE,
    SD_CARD,
    DOWNLOADS,
    DOCUMENTS,
    PICTURES,
    VIDEOS,
    AUDIO,
    RECENT,
    FAVORITES
}

/**
 * Sort order for file list
 */
enum class SortOrder {
    NAME_ASC,
    NAME_DESC,
    DATE_ASC,
    DATE_DESC,
    SIZE_ASC,
    SIZE_DESC,
    TYPE
}

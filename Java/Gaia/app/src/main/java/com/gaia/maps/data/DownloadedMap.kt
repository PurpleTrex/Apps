package com.gaia.maps.data

import org.osmdroid.util.GeoPoint

/**
 * Downloaded map region
 */
data class DownloadedMap(
    val id: String,
    val name: String,
    val boundingBox: BoundingBox,
    val downloadDate: Long,
    val sizeBytes: Long,
    val isComplete: Boolean = true
)

data class BoundingBox(
    val north: Double,
    val south: Double,
    val east: Double,
    val west: Double
) {
    fun contains(point: GeoPoint): Boolean {
        return point.latitude in south..north && 
               point.longitude in west..east
    }
}

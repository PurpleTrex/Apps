package com.gaia.maps.data

import org.osmdroid.util.GeoPoint
import java.util.Date

/**
 * User-submitted incident report
 */
data class IncidentReport(
    val id: String,
    val type: IncidentType,
    val location: GeoPoint,
    val description: String,
    val timestamp: Date = Date(),
    val expiresAt: Date? = null
)

enum class IncidentType {
    TRAFFIC_ACCIDENT,
    CONSTRUCTION,
    POLICE_ACTIVITY,
    WEATHER_HAZARD,
    ROAD_CLOSURE,
    OTHER
}

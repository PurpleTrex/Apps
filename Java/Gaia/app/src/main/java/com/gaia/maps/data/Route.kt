package com.gaia.maps.data

import org.osmdroid.util.GeoPoint

/**
 * Route information
 */
data class Route(
    val waypoints: List<GeoPoint>,
    val distance: Double, // in meters
    val duration: Long, // in seconds
    val instructions: List<RouteInstruction>,
    val isUserDrawn: Boolean = false
)

data class RouteInstruction(
    val text: String,
    val distance: Double,
    val location: GeoPoint,
    val type: InstructionType
)

enum class InstructionType {
    START,
    TURN_LEFT,
    TURN_RIGHT,
    CONTINUE_STRAIGHT,
    ARRIVE
}

package com.gaia.maps.data

/**
 * Navigation preferences for route calculation
 */
data class NavigationPreferences(
    val routeType: RouteType = RouteType.FASTEST,
    val avoidHighways: Boolean = false,
    val avoidTolls: Boolean = false,
    val preferResidential: Boolean = false,
    val preferInterstate: Boolean = false,
    val sensitiveMode: Boolean = false
)

enum class RouteType {
    FASTEST,
    SHORTEST,
    SCENIC
}

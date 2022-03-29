package no.uio.ifi.team16.stim.data

import no.uio.ifi.team16.stim.util.LatLong

/**
 * The weather at a location at a given time
 */
data class Weather(
    val position: LatLong,
    val temperature: Double,
    val humidity: Double
)
package no.uio.ifi.team16.stim.data

import no.uio.ifi.team16.stim.util.LatLong

data class Municipality(val id: String, val sites: List<Site>)
data class County(val id: String, val sites: List<Site>)
data class ProductionArea(
    val id: String,
    val sites: List<Site>
) //distinct from ProdArea, this one contains sites

data class ProdArea(
    val prodAreaCode: Int,
    val prodAreaName: String,
    val prodAreaStatus: ProdAreaStatus
)

enum class ProdAreaStatus { Red, Yellow, Green }

data class AreaPlacement(
    val municipalityCode: Int,
    val municipalityName: String,
    val countyCode: Int,
    val prodArea: ProdArea?
)

data class BorderPoint(val id: Int, val index: Int, val longitude: Double, val latitude: Double)

data class SiteBorder(val points: List<BorderPoint>)

data class Site(
    val id: Int,
    val name: String,
    val latLong: LatLong,
    val placement: AreaPlacement?,
    val capacity: Double,
    val placementType: String?,
    val waterType: String?
) {
    /**
     * Current weather forecast at site, null if not loaded yet
     */
    var weatherForecast: WeatherForecast? = null
}
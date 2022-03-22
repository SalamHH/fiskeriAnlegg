package no.uio.ifi.team16.stim.data

import no.uio.ifi.team16.stim.util.LatLng

data class Sites(val sites: List<Site>)

/* schema from fiskeridirektoratet
Site {
    siteId	integer($int64)
    versionId	integer($int64)
    siteNr	integer($int32)
    name	string
    placementType	string
    placementTypeValue	string
    waterType	string
    waterTypeValue	string
    firstClearanceTime	string
    firstClearanceType	string
    firstClearanceTypeValue	string
    latitude	number($double)
    longitude	number($double)
    capacity	number($double)
    tempCapacity	number($double)
    capacityUnitType	string
    placement	AreaPlacement{...}
    speciesLimitations	[...]
    connections	[...]
    version	VersionDetail{...}
}
*/

/*BorderPoint{
    id	integer($int64)
    index	integer($int32)
    latitude	number($double)
    longitude	number($double)
}*/
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
    val latLng: LatLng,
    val placement: AreaPlacement?
) {
    /**
     * @see InfectiousPressure.getConcentration(LatLng)
     */
    fun getInfectiousPressure(infectiousPressure: InfectiousPressure): Float {
        return infectiousPressure.getConcentration(latLng)
    }

    /**
     * @see InfectiousPressure.getConcentration(LatLng, Int)
     */
    fun getInfectiousPressure(infectiousPressure: InfectiousPressure, weeksFromNow: Int): Float {
        return infectiousPressure.getConcentration(latLng, weeksFromNow)
    }
}

package no.uio.ifi.team16.stim.data

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

data class BorderPoint(val id: Int, val index: Int, val longitude: Double, val latitude: Double)

data class SiteBorder(val points: List<BorderPoint>)

data class Site(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double
) // val placement : AreaPlacement)
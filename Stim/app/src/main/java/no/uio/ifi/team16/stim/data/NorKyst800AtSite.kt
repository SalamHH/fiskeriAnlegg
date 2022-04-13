package no.uio.ifi.team16.stim.data

import no.uio.ifi.team16.stim.util.DoubleArray4D
import no.uio.ifi.team16.stim.util.Options
import no.uio.ifi.team16.stim.util.get

/**
 * Class representing NorKyst800 data at a specific site.
 *
 * Note that this norkyst, contrary to the "general one" is relatively indexed when asking for temperature etc
 * That is, if you ask for getTempperature(x=-1,y=-1) you ge the temperature in the grid cell to the
 * SOUTHWEST of the site.
 */
data class NorKyst800AtSite(
    val siteId: Int,
    val norKyst800: NorKyst800
) {
    val TAG = "NORKYST800AtSite"

    val radius = Options.norKyst800AtSiteRadius

    ///////////////
    // UTILITIES //
    ///////////////
    override fun toString() =
        "NorKyst800AtSite: \n" +
                "\tsite: $siteId\n" +
                "\tnorkyst: $norKyst800\n"

    fun getTemperature(): Double? = getTemperature(0, 0, 0, 0)
    fun getTemperature(y: Int, x: Int): Double? = getTemperature(0, 0, y, x)
    fun getTemperature(depth: Int, time: Int, y: Int, x: Int): Double? =
        norKyst800.temperature.get(depth, time, radius + y, radius + x)
            ?: averageOf(depth, time, norKyst800.temperature)

    fun getSalinity(): Double? = getSalinity(0, 0, 0, 0)
    fun getSalinity(y: Int, x: Int): Double? = getSalinity(0, 0, y, x)
    fun getSalinity(depth: Int, time: Int, y: Int, x: Int): Double? =
        norKyst800.salinity.get(depth, time, radius + y, radius + x)
            ?: averageOf(depth, time, norKyst800.salinity)

    ///////////////
    // UTILITIES //
    ///////////////
    /**
     * take out all non-null, then average them
     */
    fun averageOf(depth: Int, time: Int, arr: DoubleArray4D): Double = arr[depth][time]
        .flatMap { row -> row.toList() }
        .filterNotNull()
        .let { elements ->
            elements.fold(0.0) { acc, element ->
                acc + element
            } / elements.count()
        }
}
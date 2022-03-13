package no.uio.ifi.team16.stim.data.dataLoader

import no.uio.ifi.team16.stim.data.NorKyst800

/**
 * DataLoader for infectious pressure data.
 *
 * Data is loaded through load(...) and returned as an InfectiousPressure, with the
 * concentration of salmon louse represented as a grid.
 **/
class NorKyst800DataLoader : THREDDSDataLoader() {
    private val TAG = "NorKyst800DataLoader"
    override val url = "?????????????????????"

    /**
     * load the entire dataset
     */
    fun load(): NorKyst800? = load(-90f, 90f, 0.001f, -90f, 90f, 0.001f)

    /**
     * return data between latitude from/to, and latitude from/to, with given resolution.
     * Uses minimum of given and possible resolution.
     * crops to dataset if latitudes or longitudes exceed the dataset.
     *
     * TODO:
     * in general, finding the distance between two points of latitude/longitude is "hard",
     * need to find a library(netcdf probably has it) for doing this.
     * until then resolution is set to max(ie uses every datapoint between range)
     *
     * @param latitudeFrom smallest latitude to get data from
     * @param latitudeTo largest latitude to get data from
     * @param latitudeResolution resolution of latitude. A latitude resolution of 0.001 means that
     * the data is sampled from latitudeFrom to latitudeTo with 0.001 latitude between points
     * @param longitudeFrom smallest longitude to get data from
     * @param longitudeTo largest longitude to get data from
     * @param latitudeResolution resolution of longitude.
     * @return NorKyst800 data in the prescribed data range, primarily stream and wave data(?)
     */
    fun load(
        latitudeFrom: Float,
        latitudeTo: Float,
        latitudeResolution: Float,
        longitudeFrom: Float,
        longitudeTo: Float,
        longitudeResolution: Float
    ): NorKyst800? = throw NotImplementedError()
}
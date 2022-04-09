package no.uio.ifi.team16.stim.data.dataLoader

//import thredds.catalog.ThreddsMetadata
import no.uio.ifi.team16.stim.data.NorKyst800
import no.uio.ifi.team16.stim.util.LatLong
import ucar.ma2.ArrayDouble
import ucar.ma2.ArrayInt

/**
 * DataLoader for data related tot he norkyst800 model.
 * Temperature, salinity, water velocity etc
 **/
class NorKyst800DataLoader : THREDDSDataLoader() {
    private val TAG = "NorKyst800DataLoader"
    val resolution = "500"
    val depthResolution = "15"
    val timeResolution = "41"
    val yRange = "0:${resolution}:901"
    val xRange = "0:${resolution}:901"
    val depthRange = "0:${depthResolution}:15"
    val timeRange = "0:${timeResolution}:42"

    //                 "https://thredds.met.no/thredds/fileServer/fou-hi/norkyst800m-1h/NorKyst-800m_ZDEPTHS_his.fc.2022031000.nc"
    val url =
        "dods://thredds.met.no/thredds/dodsC/fou-hi/norkyst800m-1h/NorKyst-800m_ZDEPTHS_his.an.2022031700.nc"
    //"thredds:resolve:https://thredds.met.no/thredds/catalog/fou-hi/norkyst800m-1h/catalog.xml#" +
    //"norkyst800m_1h_files/NorKyst-800m_ZDEPTHS_his.an.2022031700.nc"
    //"http://thredds.met.no/thredds/fileServer/fou-hi/norkyst800m-1h/NorKyst-800m_ZDEPTHS_his.an.2022031700.nc"
    /*"https://thredds.met.no/thredds/fileServer/fou-hi/norkyst800m-1h/NorKyst-800m_ZDEPTHS_his.fc.2022031700.nc?" +
                "depth[${depthRange}]," +
                "lat[${yRange}][${xRange}]," +
                "lon[${yRange}][${xRange}]," +
                "salinity[${timeRange}][${depthRange}][${yRange}][${xRange}]," +
                "temperature[${timeRange}][${depthRange}][${yRange}][${xRange}]," +
                "time[${timeRange}]," +
                "u[${timeRange}][${depthRange}][${yRange}][${xRange}]," +
                "v[${timeRange}][${depthRange}][${yRange}][${xRange}]," +
                "w[${timeRange}][${depthRange}][${yRange}][${xRange}]"*/

    /**
     * load the "entire" dataset
     * 388234558 byte allocation with 4194304 free bytes and 193MB until OOM
     * 385873886 byte allocation with 4194304 free bytes and 194MB until OOM
     * 385808110 byte allocation with 4194304 free bytes and 194MB until OOM
     */
    //fun load(): NorKyst800? = load(-90f, 90f, 0.001f, -90f, 90f, 0.001f)

    /**
     * return data between latitude from/to, and latitude from/to, with given resolution.
     * Uses minimum of given and possible resolution.
     * crops to dataset if latitudes or longitudes exceed the dataset.
     *
     * TODO:
     * in general, finding the distance between two points of latitude/longitude is "hard",[${timeRage}][${depthRange}][${yRange}][${xRange}]
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
        latLongUpperLeft: LatLong,
        latLongLowerRight: LatLong,
        latitudeResolution: Int,
        longitudeResolution: Int,
        depthRange: IntProgression,
        timeRange: IntProgression
    ): NorKyst800? = NorKyst800(
        //depth
        ArrayDouble.factory(
            doubleArrayOf(
                0.0, 5.0
            )
        ) as ArrayDouble,
        //latitude
        ArrayDouble.factory(
            doubleArrayOf(
                5.216333, 4.96755,
                5.102933, 5.090733
            )
        ).reshape(intArrayOf(2, 2)) as ArrayDouble,
        //logitude
        ArrayDouble.factory(
            doubleArrayOf(
                59.371233, 60.339883,
                59.88065, 60.054317
            )
        ).reshape(intArrayOf(2, 2)) as ArrayDouble,
        //salinitiy
        ArrayInt.factory(
            intArrayOf(
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16
            )
        ).reshape(intArrayOf(2, 2, 2, 2)) as ArrayInt,
        //temp
        ArrayInt.factory(
            intArrayOf(
                32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47
            )
        ).reshape(intArrayOf(2, 2, 2, 2)) as ArrayInt,
        //time
        ArrayDouble.factory(
            doubleArrayOf(
                100.0, 200.0
            )
        ).reshape(intArrayOf(2)) as ArrayDouble,
        //u
        ArrayInt.factory(
            intArrayOf(
                64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79
            )
        ).reshape(intArrayOf(2, 2, 2, 2)) as ArrayInt,
        //v
        ArrayInt.factory(
            intArrayOf(
                80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95
            )
        ).reshape(intArrayOf(2, 2, 2, 2)) as ArrayInt,
        //w
        ArrayInt.factory(
            intArrayOf(
                96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111
            )
        ).reshape(intArrayOf(2, 2, 2, 2)) as ArrayInt
    )
}
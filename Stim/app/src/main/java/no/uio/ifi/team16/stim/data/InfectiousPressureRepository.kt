package no.uio.ifi.team16.stim.data

/**
 * Repository for infectious pressure data.
 * The Single Source of Truth(SST) for the viewmodel.
 * This is the class that should be queried for data when a class in the model layer wants a set of data.
 *
 * The repository can load data and return it, return cached data or provide mocked data(for testing)
 *
 */
class InfectiousPressureRepository {
    private val dataSource = InfectiousPressureDataloader()
    private var cache : InfectiousPressure? = null  //hold data if loaded before
    private var dirty : Boolean = true              //whether the data in cache is "dirty"/not up-to-date

    private val mockData =
        InfectiousPressure(Grid(0), listOf(), listOf(), 3, Grid(6), Grid(8))

    private val mocked = false //TODO should not be hardcoded, should only be used in testing

    /*get the data.
    * If in testmode, return the testdata and store it in the cache
    * otherwise;
    * if the cache is not up to date, load the data anew,
    * otherwise just return the data in the cache.
    */
    fun getData() : InfectiousPressure {
        if (mocked) {
            cache = mockData
            dirty = false
        } else if (dirty) {
            cache = dataSource.load()
            dirty = false
        }
        return cache?:mockData //TODO do not return fake data if error in connection!
    }
}
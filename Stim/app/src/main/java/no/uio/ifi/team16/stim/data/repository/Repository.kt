package no.uio.ifi.team16.stim.data.repository

/**
 * A GENERAL ABSTRACT CLASS FOR REPOSITORIES OF NULLABLE DATA
 * should be inherited by all(?) repositories: InfectiousPressureRepository, NorKyst800Repository,
 * WeatherRepository, and maaaaaybe SitesRepository(might not be nullable).
 *
 * The Single Source of Truth(SST) for the viewmodel.
 * This is the class that should be queried for data when a class in the model layer wants a set of data.
 *
 * The repository can load data and return it, return cached data or provide mocked data(for testing)
 *
 * If constructed without parameters, it behaves normally.
 *
 * If constructed with a dataset it uses test behaviour; always returning
 * the given data for every query
 *
 * TODO: implement system to check if cache is not up-to-date
 * TODO: implement methods to get data from appropriate slices, so we don't load everything everytime.
 */
abstract class Repository<D, S> {
    private val TAG = "Repository"
    protected abstract val dataSource: S //must be set in subclass
    protected var cache: D? = null       //hold data if loaded before
    protected var dirty: Boolean         //whether the data in cache is "dirty"/not up-to-date
    protected val mocked: Boolean

    constructor() {
        mocked = false
        dirty = true
    }

    //make a mocked repository
    constructor(mockedData: D?) {
        mocked = true
        dirty = false
        cache = mockedData
    }


    /**
     * get the data.
     * If in testmode(mocked data), return the testdata
     * otherwise;
     * if the cache is not up to date(dirty), load the data anew,
     * otherwise just return the data in the cache.
     *
     * @return mocked, cached or newly loaded data.
     */
    abstract fun getData(): D?
}
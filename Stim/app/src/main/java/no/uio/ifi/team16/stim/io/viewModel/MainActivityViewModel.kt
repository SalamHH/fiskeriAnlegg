package no.uio.ifi.team16.stim.io.viewModel

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import no.uio.ifi.team16.stim.data.*
import no.uio.ifi.team16.stim.data.repository.*
import no.uio.ifi.team16.stim.util.LatLong
import no.uio.ifi.team16.stim.util.Options

class MainActivityViewModel : ViewModel() {

    private val TAG = "MainActivityViewModel"
    private lateinit var prefrences: SharedPreferences

    //REPOSITORIES
    private val infectiousPressureRepository = InfectiousPressureRepository()
    private val infectiousPressureTimeSeriesRepository = InfectiousPressureTimeSeriesRepository()
    private val sitesRepository = SitesRepository() //municipalities, favourite sites
    private val norKyst800Repository = NorKyst800Repository()
    private val norKyst800AtSiteRepository = NorKyst800AtSiteRepository()
    private val addressRepository = AddressRepository()
    private val weatherRepository = WeatherRepository()

    //MUTABLE LIVE DATA
    private val infectiousPressureData = MutableLiveData<InfectiousPressure?>()
    private val infectiousPressureTimeSeriesData: MutableMap<Site, MutableLiveData<InfectiousPressureTimeSeries?>> =
        mutableMapOf()
    private val municipalityData = MutableLiveData<Municipality?>()
    private val favouriteSitesData = MutableLiveData<MutableList<Site>?>()
    private val norKyst800Data = MutableLiveData<NorKyst800?>()
    private val norKyst800AtSiteData = mutableMapOf<Site, MutableLiveData<NorKyst800AtSite?>>()
    private val municipalityNrData = MutableLiveData<String?>()
    private val currentSitesData = MutableLiveData<List<Site>?>()
    private var lineDataSet = MutableLiveData<LineDataSet?>(null)
    private val weatherData = MutableLiveData<WeatherForecast?>()

    //
    //private val WeatherRepository = WeatherRepository()
    //private val WeatherData  = MutableLiveData<Weather>()

    ///////////// used to get the mutablelivedata, which again is probably used
    // GETTERS // to attach listeners etc in activities.
    /////////////
    //note that for maps, getting on a value of the map, say site or municipalitycode,
    //will create a mutablelivedata at the entry if there is none.
    fun getInfectiousPressureData(): LiveData<InfectiousPressure?> {
        return infectiousPressureData
    }

    fun getNorKyst800Data(): LiveData<NorKyst800?> {
        return norKyst800Data
    }

    fun getNorKyst800AtSiteData(site: Site): LiveData<NorKyst800AtSite?> {
        return norKyst800AtSiteData.getOrPut(site) {
            MutableLiveData()
        }
    }

    fun getMunicipalityData(): LiveData<Municipality?> {
        return municipalityData
    }

    fun getWeatherData(): LiveData<WeatherForecast?> {
        return weatherData
    }

    fun getInfectiousPressureTimeSeriesData(site: Site): LiveData<InfectiousPressureTimeSeries?> {
        return infectiousPressureTimeSeriesData.getOrPut(site) {
            MutableLiveData()
        }
    }

    fun getLineDataSet(): LiveData<LineDataSet?> {
        return lineDataSet
    }

    fun getMunicipalityNr(): LiveData<String?> {
        return municipalityNrData
    }

    fun getFavouriteSitesData(): LiveData<MutableList<Site>?> {
        return favouriteSitesData
    }

    fun loadPrefrences(preferences: SharedPreferences) {
        prefrences = preferences
    }

    fun getCurrentSitesData(): LiveData<List<Site>?> {///nyyy
        return currentSitesData
    }

    ///////////// used to load the data from its source, does ot return the data but puts it
    // LOADERS // into its corresponding MutableLiveData container.
    ///////////// The posting will wake the observer of that data.
    fun loadDefaultInfectiousPressure() {
        viewModelScope.launch(Dispatchers.IO) {
            val loaded =
                infectiousPressureRepository.getDefault() //either loaded, retrieved from cache or faked
            //invokes the observer
            infectiousPressureData.postValue(loaded)
        }
    }

    fun loadInfectiousPressureTimeSeriesAtSite(site: Site) {
        viewModelScope.launch(Dispatchers.IO) {
            infectiousPressureTimeSeriesData.getOrPut(site) {
                MutableLiveData()
            }.postValue(
                infectiousPressureTimeSeriesRepository.getDataAtSite(
                    site,
                    Options.infectiousPressureTimeSeriesSpan
                ) //either loaded, retrieved from cache or faked
            )
        }
    }

    fun loadNorKyst800() {
        viewModelScope.launch(Dispatchers.IO) {
            val loaded =
                norKyst800Repository.getDefaultData() //either loaded, retrieved from cache or faked
            //invokes the observer
            norKyst800Data.postValue(loaded)
        }
    }

    fun loadNorKyst800AtSite(site: Site) {
        viewModelScope.launch(Dispatchers.IO) {
            norKyst800AtSiteData.getOrPut(site) {
                MutableLiveData()
            }.postValue(
                norKyst800AtSiteRepository.getDataAtSite(site)
            )
        }
    }

    /**
     * Load the 100 first sited from the given municipality
     */
    fun loadSitesAtMunicipality(municipalityCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val loaded = sitesRepository.getMunicipality(municipalityCode)
            //invokes the observer
            municipalityData.postValue(loaded)
        }
    }

    fun loadFavouriteSites() {
        viewModelScope.launch(Dispatchers.IO) {
            val loaded = sitesRepository.getFavouriteSites(prefrences.getStringSet(Options.FAVOURITES, null))
            //invokes the observer
            favouriteSitesData.postValue(loaded)
        }
    }

    fun loadWeatherAtSite(site: Site) {
        viewModelScope.launch(Dispatchers.IO) {
            val forecast = weatherRepository.getWeatherForecast(site)
            weatherData.postValue(forecast)
        }
    }

    fun loadMunicipalityNr(latLong: LatLong) {
        viewModelScope.launch(Dispatchers.IO) {
            val nr = addressRepository.getMunicipalityNr(latLong)
            municipalityNrData.postValue(nr)
        }
    }

    /**
     * Processes a search string from the map.
     * input is numeric -> searches by municipality number
     * if not, searches municipality name first, then by site name
     */
    fun doMapSearch(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (query.matches(Regex("^[0-9]+\$"))) {
                // search is purely numeric, so this is probably municipality number
                loadSitesAtMunicipality(query)
            } else {
                // string search, check first if this is the name of a municipality
                val municipalityNr = addressRepository.searchMunicipalityNr(query)
                if (municipalityNr != null) {
                    municipalityNrData.postValue(municipalityNr)
                } else {
                    // not a municipality, search for sites by name
                    val sites = sitesRepository.getSitesByName(query)
                    currentSitesData.postValue(sites)
                }
            }
        }
    }

    //Methods for communicating chosen Site between fragments

    private var site: Site? = null

    fun setCurrentSite(new: Site) {
        site = new
    }

    fun getCurrentSite(): Site? {
        return site
    }

    companion object {
        const val CHART_LABEL = "INFECTION_CHART"
    }

    private val infectionData = mutableListOf<Entry>()

    fun registerFavouriteSite(site: Site) {
        favouriteSitesData.value.let { favouriteSites ->
            favouriteSites?.add(site)
            favouriteSitesData.postValue(favouriteSites)
        }
        prefrences.edit().apply{
            val editStringSet = prefrences.getStringSet(Options.FAVOURITES, emptySet())?.toMutableSet()
            if (editStringSet != null) {
                editStringSet.add(site.nr.toString())
                putStringSet(Options.FAVOURITES, editStringSet.toSet())
            }
            apply()
        }
    }

    fun removeFavouriteSite(site: Site) {
        favouriteSitesData.value.let { favouriteSites ->
            favouriteSites?.remove(site)
            favouriteSitesData.postValue(favouriteSites)
        }
        prefrences.edit().apply{
            val editStringSet = prefrences.getStringSet(Options.FAVOURITES, emptySet())?.toMutableSet()
            if (editStringSet != null) {
                editStringSet.remove(site.nr.toString())
                putStringSet(Options.FAVOURITES, editStringSet.toSet())
            }
            apply()
        }
    }

    //TODO: only for debug
    fun registerFavouriteSite(sites: List<Site>) {
        favouriteSitesData.value.let { favouriteSites ->
            sites.forEach() { site ->
                favouriteSites?.add(site)
            }
            favouriteSitesData.postValue(favouriteSites)
        }
    }

    fun getFavouriteSitesStringSet(): Set<String> {
        val favSet = mutableSetOf<String>()
        favouriteSitesData.value?.forEach {
            favSet.add(it.nr.toString())
        }
        return favSet
    }

    /**
     * Empties the cache so the app uses less memory
     */
    fun clearCache() {
        infectiousPressureRepository.clearCache()
        infectiousPressureTimeSeriesRepository.clearCache()
        sitesRepository.clearCache()
        norKyst800Repository.clearCache()
        norKyst800AtSiteRepository.clearCache()
        addressRepository.clearCache()
        weatherRepository.clearCache()
    }
}
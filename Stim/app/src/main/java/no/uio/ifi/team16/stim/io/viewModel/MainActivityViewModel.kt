package no.uio.ifi.team16.stim.io.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import no.uio.ifi.team16.stim.data.*
import no.uio.ifi.team16.stim.data.repository.InfectiousPressureRepository
import no.uio.ifi.team16.stim.data.repository.InfectiousPressureTimeSeriesRepository
import no.uio.ifi.team16.stim.data.repository.NorKyst800Repository
import no.uio.ifi.team16.stim.data.repository.SitesRepository
import no.uio.ifi.team16.stim.util.LatLng

class MainActivityViewModel : ViewModel() {
    private val TAG = "MainActivityViewModel"
    private val infectiousPressureRepository = InfectiousPressureRepository()
    private val infectiousPressureData = MutableLiveData<InfectiousPressure?>()

    private val infectiousPressureTimeSeriesRepository = InfectiousPressureTimeSeriesRepository()
    private val infectiousPressureTimeSeriesData =
        MutableLiveData<Map<Int, InfectiousPressureTimeSeries>>()

    private val sitesRepository = SitesRepository()
    private val sitesData = MutableLiveData<Sites?>()

    private val norKyst800Repository = NorKyst800Repository()
    private val norKyst800Data = MutableLiveData<NorKyst800?>()

    //
    //private val WeatherRepository = WeatherRepository()
    //private val WeatherData  = MutableLiveData<Weather>()

    ///////////// used to get the mutablelivedata, which again is probably used
    // GETTERS // to attach listeners etc in activities.
    /////////////
    fun getInfectiousPressureData(): MutableLiveData<InfectiousPressure?> {
        return infectiousPressureData
    }

    fun getNorKyst800Data(): MutableLiveData<NorKyst800?> {
        return norKyst800Data
    }

    fun getSitesData(): MutableLiveData<Sites?> {
        return sitesData
    }

    fun getWeatherData() {
        throw NotImplementedError()
    }

    ///////////// used to load the data from its source, does ot return the data but puts it
    // LOADERS // into its corresponding MutableLiveData container.
    ///////////// The setting will wake the observer of that data.
    fun loadInfectiousPressure() {
        //InfectiousPressure can be loaded asynchronously(probably),
        //if not use runblocking { }
        viewModelScope.launch(Dispatchers.IO) {
            //runBlocking {
            Log.d(TAG, "loading infectiousdata to viewmodel")
            val loaded =
                infectiousPressureRepository.getSomeData() //either loaded, retrieved from cache or faked
            Log.d(TAG, "loading infectiousdata to viewmodel - DONE")
            //invokes the observer
            infectiousPressureData.postValue(loaded)
        }
    }

    fun loadInfectiousPressureTimeSeriesAtSite(site: Site) {
        //InfectiousPressure can be loaded asynchronously(probably),
        //if not use runblocking { }
        viewModelScope.launch(Dispatchers.IO) {
            //runBlocking {
            Log.d(TAG, "loading infectioustimeseriesdata to viewmodel")
            val loaded =
                infectiousPressureTimeSeriesRepository.getDataAtSite(
                    site,
                    8
                ) //either loaded, retrieved from cache or faked
            Log.d(TAG, "loading infectioustimeseriesdata to viewmodel - DONE")
            //invokes the observer
            infectiousPressureTimeSeriesData.postValue(loaded)
            Log.d(TAG, loaded[site.id].toString())
        }
    }

    fun loadNorKyst800() {
        viewModelScope.launch(Dispatchers.IO) {
            //runBlocking {
            Log.d(TAG, "loading infectiousdata to viewmodel")
            val loaded =
                norKyst800Repository.getData() //either loaded, retrieved from cache or faked
            Log.d(TAG, "loading infectiousdata to viewmodel - DONE")
            //invokes the observer
            norKyst800Data.postValue(loaded)
        }
    }

    /**
     * Load the 100 first sited from the given municipality
     */
    fun loadSites(municipalityCode: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "loading sites to viewmodel")
            val loaded =
                sitesRepository.getData(municipalityCode) //either loaded, retrieved from cache or faked
            Log.d(TAG, "loading sites to viewmodel - DONE")
            //invokes the observer
            sitesData.postValue(loaded)
        }
    }

    fun loadWeather() {
        throw NotImplementedError()
    }

    //Methods for communicating chosen Site between fragments

    private var site = Site(0, "Test", LatLng(0.0, 0.0), null) //In case setCurrentSite fails

    fun setCurrentSite(new : Site) {
        site = new
    }

    fun getCurrentSite() : Site {
        return site
    }

}
package no.uio.ifi.team16.stim.io.viewModel

import android.util.Log
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
    private val infectiousPressureRepository = InfectiousPressureRepository()
    private val infectiousPressureData = MutableLiveData<InfectiousPressure?>()

    private val infectiousPressureTimeSeriesRepository = InfectiousPressureTimeSeriesRepository()
    private val infectiousPressureTimeSeriesData =
        MutableLiveData<Map<Int, InfectiousPressureTimeSeries>>()

    private val sitesRepository = SitesRepository()
    private val sitesData = MutableLiveData<Sites?>()

    private val norKyst800Repository = NorKyst800Repository()
    private val norKyst800Data = MutableLiveData<NorKyst800?>()

    private val addressRepository = AddressRepository()
    private val addressData = MutableLiveData<String?>()

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

    fun getInfectiousPressureTimeSeriesData(): MutableLiveData<Map<Int, InfectiousPressureTimeSeries>> {
        return infectiousPressureTimeSeriesData
    }

    fun getLineDataSet(): LiveData<LineDataSet> {
        return lineDataSet
    }

    fun getMunicipalityNr(): LiveData<String?> {
        return addressData
    }

    ///////////// used to load the data from its source, does ot return the data but puts it
    // LOADERS // into its corresponding MutableLiveData container.
    ///////////// The setting will wake the observer of that data.
    fun loadInfectiousPressure() {
        //InfectiousPressure can be loaded asynchronously(probably),
        //if not use runblocking { }
        viewModelScope.launch(Dispatchers.IO) {
            //runBlocking {
            val loaded =
                infectiousPressureRepository.getSomeData() //either loaded, retrieved from cache or faked
            Log.d(TAG, "LOADED infectiousPressure")
            //invokes the observer
            infectiousPressureData.postValue(loaded)
        }
    }

    fun loadInfectiousPressureTimeSeriesAtSite(site: Site) {
        //InfectiousPressure can be loaded asynchronously(probably),
        //if not use runblocking { }
        viewModelScope.launch(Dispatchers.IO) {
            //runBlocking {
            //Log.d(TAG, "loading infectioustimeseriesdata to viewmodel")
            val loaded =
                infectiousPressureTimeSeriesRepository.getDataAtSite(
                    site,
                    Options.infectiousPressureTimeSeriesSpan
                ) //either loaded, retrieved from cache or faked
            Log.d(TAG, "LOADED - infectioustimeseries at ${site.name}")
            //invokes the observer
            infectiousPressureTimeSeriesData.postValue(loaded)
        }
    }

    fun loadNorKyst800() {
        viewModelScope.launch(Dispatchers.IO) {
            //runBlocking {
            //Log.d(TAG, "loading infectiousdata to viewmodel")
            val loaded =
                norKyst800Repository.getData() //either loaded, retrieved from cache or faked
            Log.d(TAG, "LOADED - norKyst800")
            //invokes the observer
            norKyst800Data.postValue(loaded)
        }
    }

    /**
     * Load the 100 first sited from the given municipality
     */
    fun loadSites(municipalityCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "loading sites to viewmodel")
            //either loaded, retrieved from cache or faked
            val loaded = sitesRepository.getData(municipalityCode)
            Log.d(TAG, "loading sites to viewmodel - DONE")
            //invokes the observer
            sitesData.postValue(loaded)
        }
    }

    fun loadWeather() {
        throw NotImplementedError()
    }

    fun loadMunicipalityNumber(latLong: LatLong) {
        viewModelScope.launch(Dispatchers.IO) {
            val nr = addressRepository.getMunicipalityNr(latLong)
            addressData.postValue(nr)
        }
    }

    //Methods for communicating chosen Site between fragments

    private var site = Options.fakeSite

    fun setCurrentSite(new: Site) {
        site = new
    }

    fun getCurrentSite(): Site {
        return site
    }

    companion object {
        const val CHART_LABEL = "INFECTION_CHART"
    }

    private val infectionData = mutableListOf<Entry>()
    private var _lineDataSet = MutableLiveData(LineDataSet(infectionData, CHART_LABEL))
    private var lineDataSet: LiveData<LineDataSet> = _lineDataSet

    fun setLineDataSet(lDataSet: LineDataSet) {
        lineDataSet = MutableLiveData(lDataSet)
    }
}
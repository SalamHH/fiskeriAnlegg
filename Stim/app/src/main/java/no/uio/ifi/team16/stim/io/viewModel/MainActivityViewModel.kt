package no.uio.ifi.team16.stim.io.viewModel

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.gms.common.api.internal.ActivityLifecycleObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import no.uio.ifi.team16.stim.InfectionFragment
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

    /*fun loadSiteContamination() {
        Log.d(TAG, "loading timeSeriesData to viewmodel")
        loadInfectiousPressureTimeSeriesAtSite(getCurrentSite())
        Log.d(TAG, "loading timeSeriesData to viewmodel - DONE")
        val loaded = infectiousPressureTimeSeriesData.value
        Log.d("load TS", loaded?.get(getCurrentSite().id)?.siteId.toString())
        Log.d(TAG, "got timeSeriesData from viewmodel - DONE")
        //infectionData.add(Entry(loaded.value))
    }*/


    companion object {
        const val CHART_LABEL = "INFECTION_CHART"
    }

    private val infectionData = mutableListOf<Entry>()
    private var _lineDataSet = MutableLiveData(LineDataSet(infectionData, CHART_LABEL))
    private var lineDataSet: LiveData<LineDataSet> = _lineDataSet

    fun setLineDataSet(lDataSet: MutableLiveData<LineDataSet>) {
        lineDataSet = lDataSet
    }

    /*
    init {
        infectionData.add(Entry(0f, 5f))
        infectionData.add(Entry(1f, 4f))
        infectionData.add(Entry(2f, 7f))
        infectionData.add(Entry(3f, 8f))
        infectionData.add(Entry(4f, 6f))

        _lineDataSet.value = LineDataSet(infectionData, CHART_LABEL)

    }*/

}
package no.uio.ifi.team16.stim.io.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import no.uio.ifi.team16.stim.data.InfectiousPressure
import no.uio.ifi.team16.stim.data.Sites
import no.uio.ifi.team16.stim.data.repository.InfectiousPressureRepository
import no.uio.ifi.team16.stim.data.repository.SitesRepository

class MainActivityViewModel : ViewModel() {
    private val TAG = "MainActivityViewModel"
    private val infectiousPressureRepository = InfectiousPressureRepository()
    private val infectiousPressureData = MutableLiveData<InfectiousPressure?>()

    private val sitesRepository = SitesRepository()
    private val sitesData = MutableLiveData<Sites?>()

    //NOT YET IMPLEMENTED
    //private val NorKyst800Repository = NorKyst800Repository()
    //private val NorKyst800Data  = MutableLiveData<NorKyst800>()
    //
    //private val WeatherRepository = WeatherRepository()
    //private val WeatherData  = MutableLiveData<Weather>()

    ///////////// used to get the mutablelivedata, which again is probably used
    // GETTERS // to attach listeners etc in activities.
    /////////////
    fun getInfectiousPressureData(): MutableLiveData<InfectiousPressure?> {
        return infectiousPressureData
    }

    fun getNorKyst800Data() {
        throw NotImplementedError()
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
            Log.d(TAG, "loading infectiousdata to viewmodel")
            val loaded =
                infectiousPressureRepository.getSomeData() //either loaded, retrieved from cache or faked
            Log.d(TAG, "loading infectiousdata to viewmodel - DONE")
            //invokes the observer
            infectiousPressureData.postValue(loaded)
        }
    }

    fun loadNorKyst800() {
        throw NotImplementedError()
    }

    fun loadSites() {
        viewModelScope.launch(Dispatchers.Main) {
            Log.d(TAG, "loading sites to viewmodel")
            val loaded =
                sitesRepository.getData() //either loaded, retrieved from cache or faked
            Log.d(TAG, "loading sites to viewmodel - DONE")
            //invokes the observer
            sitesData.postValue(loaded)
        }
    }

    fun loadWeather() {
        throw NotImplementedError()
    }
}
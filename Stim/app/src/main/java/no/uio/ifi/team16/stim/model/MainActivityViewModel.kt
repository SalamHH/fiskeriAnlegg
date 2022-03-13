package no.uio.ifi.team16.stim.model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import no.uio.ifi.team16.stim.data.InfectiousPressure
import no.uio.ifi.team16.stim.data.repository.InfectiousPressureRepository

class MainActivityViewModel : ViewModel() {
    private val TAG = "MainActivityViewModel"
    private val infectiousPressureRepository = InfectiousPressureRepository()
    private val infectiousPressureData  = MutableLiveData<InfectiousPressure>()

    fun getInfectiousPressureData() : MutableLiveData<InfectiousPressure> { return infectiousPressureData }

    fun loadInfectiousPressure() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "loading infectiousdata to viewmodel")
            val loaded = infectiousPressureRepository.getData() //either loaded, retrieved from cache or faked
            Log.d("MADE", loaded.toString() )
            Log.d(TAG, "loading infectiousdata to viewmodel - DONE")
            //TODO: do not set here!
            //infectiousPressureData.value = loaded //invokes observer
        }
    }
}
package no.uio.ifi.team16.stim.model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import no.uio.ifi.team16.stim.data.InfectiousPressure
import no.uio.ifi.team16.stim.data.InfectiousPressureRepository

class MainActivityViewModel : ViewModel() {
    private val infectiousPressureRepository = InfectiousPressureRepository()
    private val infectiousPressureData  = MutableLiveData<InfectiousPressure>()

    fun getInfectiousPressureData() : MutableLiveData<InfectiousPressure> { return infectiousPressureData }

    fun loadInfectiousPressure() {
        GlobalScope.launch(Dispatchers.Main) {
            val loaded = infectiousPressureRepository.getData() //either loaded, retrieved from cache or faked
            Log.d("LOADED", "infectiousdata from repository")
            infectiousPressureData.value = loaded               //invokes observer
        }
    }
}
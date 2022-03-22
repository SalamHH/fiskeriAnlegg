package no.uio.ifi.team16.stim.io.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import no.uio.ifi.team16.stim.data.repository.AddressRepository
import no.uio.ifi.team16.stim.util.LatLng

class MapActivityViewModel : ViewModel() {

    private val addressRepository = AddressRepository()
    private val livedata = MutableLiveData<String?>()

    fun getMunicipalityNr(): LiveData<String?> {
        return livedata
    }

    fun load(latLng: LatLng) {
        viewModelScope.launch(Dispatchers.IO) {
            val nr = addressRepository.getMunicipalityNr(latLng)
            livedata.postValue(nr)
        }
    }
}
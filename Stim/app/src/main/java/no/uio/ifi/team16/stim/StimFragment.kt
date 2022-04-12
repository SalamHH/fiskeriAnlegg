package no.uio.ifi.team16.stim

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

abstract class StimFragment : Fragment() {


    companion object {
        var currentSite: String? = null
        var hasLocationPermission = false
    }

    /**
     * Check if you can use the user's location
     */
    fun checkLocationPermission(): Boolean {
        activity?.let {
            val fineLocation = ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_COARSE_LOCATION)
            val coarseLocation = ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_COARSE_LOCATION)
            return fineLocation == PackageManager.PERMISSION_GRANTED || coarseLocation == PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    /**
     * Ask for permission to show user location
     */
    fun requestPermission(callback: (result: Boolean) -> Unit) {
        val contract = ActivityResultContracts.RequestMultiplePermissions()
        val request = registerForActivityResult(contract) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                    callback(true)
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                    callback(true)
                }
                else -> callback(false)
            }
        }

        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        request.launch(permissions)
    }
}
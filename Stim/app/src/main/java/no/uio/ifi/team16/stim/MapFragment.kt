package no.uio.ifi.team16.stim

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import no.uio.ifi.team16.stim.databinding.FragmentMapBinding
import no.uio.ifi.team16.stim.io.viewModel.MainActivityViewModel

/**
 * Map fragment
 */
class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnCameraMoveListener {

    private val TAG = "MapFragment"
    private lateinit var map: GoogleMap
    private lateinit var binding: FragmentMapBinding
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        binding = FragmentMapBinding.inflate(layoutInflater)

        val mapFragment = SupportMapFragment.newInstance()
        activity?.supportFragmentManager?.beginTransaction()?.add(R.id.mapView, mapFragment)?.commit()

        mapFragment.getMapAsync(this)

        // todo sjekk om dette er nødvending
        val owner: LifecycleOwner = if (activity != null) {
            activity as FragmentActivity
        } else {
            viewLifecycleOwner
        }
        viewModel.getMunicipalityNr().observe(owner) { nr ->
            if (nr != null) {
                binding.nrView.text = "Kommunenr: $nr"
                viewModel.loadSites(nr)
            }
        }

        binding.toSitesBtn.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_mapFragment_to_sitesFromMapFragment)
        }

        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setOnCameraMoveListener(this)
        val bounds = LatLngBounds(LatLng(60.0, 10.0), LatLng(60.5, 10.5))
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 10)
        map.moveCamera(cameraUpdate)
    }

    override fun onCameraMove() {
        Log.d(TAG, "camera moved to ${map.cameraPosition}")
        val center =
            no.uio.ifi.team16.stim.util.LatLng(map.cameraPosition.target.latitude, map.cameraPosition.target.longitude)
        viewModel.load(center)
    }
}
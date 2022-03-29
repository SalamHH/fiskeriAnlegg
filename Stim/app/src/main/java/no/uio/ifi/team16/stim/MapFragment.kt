package no.uio.ifi.team16.stim

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import no.uio.ifi.team16.stim.data.Sites
import no.uio.ifi.team16.stim.databinding.FragmentMapBinding
import no.uio.ifi.team16.stim.io.viewModel.MainActivityViewModel
import no.uio.ifi.team16.stim.util.LatLong

/**
 * Map fragment
 */
class MapFragment : StimFragment(), OnMapReadyCallback {

    private val TAG = "MapFragment"
    private lateinit var map: GoogleMap
    private lateinit var binding: FragmentMapBinding
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = FragmentMapBinding.inflate(layoutInflater)

        val mapFragment = SupportMapFragment.newInstance()
        activity?.supportFragmentManager?.beginTransaction()?.add(R.id.mapView, mapFragment)?.commit()

        mapFragment.getMapAsync(this)

        // Observe municipality number
        viewModel.getMunicipalityNr().observe(getLifecycleOwner(), this::onMunicipalityUpdate)

        // Observe sites and place them on the map
        viewModel.getSitesData().observe(getLifecycleOwner(), this::onSiteUpdate)

        binding.toSitesBtn.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_mapFragment_to_sitesFromMapFragment)
        }

        binding.syncBtn.setOnClickListener {
            onRefresh()
        }

        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val bounds = LatLngBounds(LatLng(60.0, 10.0), LatLng(60.5, 10.5))
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 10)
        map.moveCamera(cameraUpdate)
    }

    private fun onMunicipalityUpdate(nr: String?) {
        if (nr != null) {
            viewModel.loadSites(nr)
        }
    }

    private fun onSiteUpdate(sites: Sites?) {
        if (sites != null) {
            binding.numSites.text = "Antall anlegg: ${sites.sites.size}"

            for (site in sites.sites) {
                val markerOptions = MarkerOptions()
                markerOptions.title(site.name)
                markerOptions.position(site.latLong.toGoogle())
                map.addMarker(markerOptions)
            }
        }
    }

    private fun onRefresh() {
        val center = LatLong.fromGoogle(map.cameraPosition.target)
        viewModel.loadMunicipalityNumber(center)
    }
}
package no.uio.ifi.team16.stim

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import no.uio.ifi.team16.stim.data.Site
import no.uio.ifi.team16.stim.data.Sites
import no.uio.ifi.team16.stim.databinding.FragmentMapBinding
import no.uio.ifi.team16.stim.io.adapter.RecycleViewAdapter
import no.uio.ifi.team16.stim.io.viewModel.MainActivityViewModel
import no.uio.ifi.team16.stim.util.LatLong
import no.uio.ifi.team16.stim.util.Options

/**
 * Map fragment
 */
class MapFragment : StimFragment(), OnMapReadyCallback {

    private val TAG = "MapFragment"
    private lateinit var map: GoogleMap
    private lateinit var binding: FragmentMapBinding
    private val viewModel: MainActivityViewModel by activityViewModels()
    private var mapReady = false
    private var mapBounds: CameraPosition? = null

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMapBinding.inflate(layoutInflater)

        val mapFragment = SupportMapFragment.newInstance()
        activity?.supportFragmentManager?.beginTransaction()?.add(R.id.mapView, mapFragment)?.commit()

        mapFragment.getMapAsync(this)

        // Observe municipality number
        viewModel.getMunicipalityNr().observe(viewLifecycleOwner, this::onMunicipalityUpdate)

        binding.toSitesBtn.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_mapFragment_to_sitesFromMapFragment)
        }

        binding.syncBtn.setOnClickListener {
            onRefresh()
        }

        //Bottom Sheet behavior
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.setPeekHeight(200, true)
        bottomSheetBehavior.isDraggable = true
        bottomSheetBehavior.isHideable = false

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = RecycleViewAdapter(Options.fakeSites, this::adapterOnClick)
        binding.recyclerView.adapter = adapter

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapBounds = map.cameraPosition
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        mapReady = true

        mapBounds?.let { bounds ->
            // Move to last camera position
            val update = CameraUpdateFactory.newCameraPosition(bounds)
            map.moveCamera(update)
        } ?: run {
            // Move to start position (Todo: make this the user location)
            val bounds = LatLngBounds(LatLng(60.0, 10.0), LatLng(60.5, 10.5))
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 10)
            map.moveCamera(cameraUpdate)
        }

        // Observe sites and place them on the map
        viewModel.getSitesData().observe(viewLifecycleOwner, this::onSiteUpdate)
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

    /*
   When an item in the RecyclerView is clicked it updates the viewModels currentSite to the Site that was clicked
   and then it navigates to the fragment that fetches this Site and displays information about it */
    private fun adapterOnClick(site: Site) {
        //TODO
    }
}
package no.uio.ifi.team16.stim

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import no.uio.ifi.team16.stim.data.Municipality
import no.uio.ifi.team16.stim.data.Site
import no.uio.ifi.team16.stim.databinding.FragmentMapBinding
import no.uio.ifi.team16.stim.io.adapter.RecycleViewAdapter
import no.uio.ifi.team16.stim.io.viewModel.MainActivityViewModel
import no.uio.ifi.team16.stim.util.LatLong

/**
 * Map fragment
 */
class MapFragment : StimFragment(), OnMapReadyCallback, GoogleMap.OnCameraMoveListener {

    private val TAG = "MapFragment"
    private lateinit var map: GoogleMap
    private lateinit var binding: FragmentMapBinding
    private val viewModel: MainActivityViewModel by activityViewModels()
    private var mapReady = false
    private var mapBounds: CameraPosition? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var SearchView: SearchView
    private var zoomLevel = 12F

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        if (!checkLocationPermission()) {
            requestPermission {
                hasLocationPermission = it
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        binding = FragmentMapBinding.inflate(layoutInflater)

        val mapFragment = SupportMapFragment.newInstance()
        activity?.supportFragmentManager?.beginTransaction()?.add(R.id.mapView, mapFragment)?.commit()

        mapFragment.getMapAsync(this)

        // Observe municipality number
        viewModel.getMunicipalityNr().observe(viewLifecycleOwner, this::onMunicipalityUpdate)

        //Bottom Sheet behavior
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.setPeekHeight(200, true)
        bottomSheetBehavior.isDraggable = true
        bottomSheetBehavior.isHideable = false

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = RecycleViewAdapter(listOf(), this::adapterOnClick, requireActivity())
        binding.recyclerView.adapter = adapter


        val spinner = binding.spinner

        //populere spinneren fra array
        ArrayAdapter.createFromResource(
            requireActivity(),
            R.array.searchChoices,
            android.R.layout.simple_spinner_item
        ).also { arrayAdapter ->
            // Specify the layout to use when the list of choices appears
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = arrayAdapter
        }

        var filterChoice = 1 //1=muncode, 2=site name...

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (spinner.selectedItem) {
                    "municipality code" -> {
                        filterChoice = 1
                    }
                    "site name" -> {
                        filterChoice = 2

                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        binding.syncBtn.setOnClickListener {
            onRefresh()
        }

        //nytt
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                if (p0 != null) {
                    map.clear()
                    if (filterChoice == 1) {
                        Log.d("choice:", "muncode")
                        searchMunNr(p0)
                    } else if (filterChoice == 2) {
                        Log.d("choice:", "site name")
                        searchName(p0)

                    }

                }

                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return false
            }

        })

        return binding.root
    }

    fun searchMunNr(munNr: String) {
        map.clear()

        // Check that the input is numeric only
        if (!munNr.matches(Regex("^[0-9]+\$"))) {
            return
        }

        currentMunicipalityNr = munNr
        viewModel.loadSitesAtMunicipality(munNr)
        viewModel.getMunicipalityData().observe(viewLifecycleOwner) { municipality ->
            if (municipality != null && municipality.sites.isNotEmpty()) {
                onSiteUpdate(municipality.sites)

                // Move camera to arbitrary site in municipality
                val firstSite = municipality.sites[0]
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(firstSite.latLong.toGoogle(), zoomLevel)
                map.animateCamera(cameraUpdate)

                val adapter = RecycleViewAdapter(municipality.sites, this::adapterOnClick, requireActivity())
                binding.recyclerView.adapter = adapter
            }
        }
    }

    fun searchName(name: String) {
        map.clear()
        viewModel.loadSiteByName(name)
        viewModel.getCurrentSiteData().observe(viewLifecycleOwner) { site ->
            if (site != null) {
                onSiteUpdate(listOf(site))
                currentMunicipalityNr = null
                currentSite = site.name

                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(site.latLong.toGoogle(), zoomLevel)
                map.animateCamera(cameraUpdate)
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapBounds = map.cameraPosition
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        mapReady = true

        map.setOnCameraMoveListener(this::onCameraMove)

        mapBounds?.let { bounds ->
            // Move to last camera position
            val update = CameraUpdateFactory.newCameraPosition(bounds)
            map.moveCamera(update)
        } ?: run {
            map.moveCamera(getInitialCameraPosition())
        }

        if (checkLocationPermission()) {
            map.isMyLocationEnabled = true
            // todo må kanskje lage en LocationProvider her
        }
    }

    private fun onMunicipalityUpdate(nr: String?) {
        if (nr != null) {
            currentMunicipalityNr = nr
            viewModel.loadSitesAtMunicipality(nr)
        }
        //todo - update headertext in bottomsheet to kommunenavn/nr
    }

    private fun onSiteUpdate(sites: List<Site>?) {
        if (sites != null && mapReady) {

            for (site in sites) {
                val markerOptions = MarkerOptions()
                markerOptions.title(site.name)
                markerOptions.position(site.latLong.toGoogle())
                map.addMarker(markerOptions)
            }

            //update municipality in bottomsheet
            val adapter = RecycleViewAdapter(sites, this::adapterOnClick, requireActivity())
            binding.recyclerView.adapter = adapter
        }
    }

    private fun onRefresh() {
        val center = LatLong.fromGoogle(map.cameraPosition.target)
        viewModel.loadMunicipalityNumber(center)
    }

    /**
     * When an item in the recyclerview is clicked, navigate to that site
     */
    private fun adapterOnClick(site: Site) {
        viewModel.setCurrentSite(site)
        view?.findNavController()?.navigate(R.id.action_mapFragment_to_siteInfoFragment)
    }

    /**
     * Hent kartposisjon som viser sør-Norge
     */
    private fun getInitialCameraPosition(): CameraUpdate {
        val coordinates = LatLng(61.42888648306541, 8.68770383298397)
        return CameraUpdateFactory.newLatLngZoom(coordinates, 5.5639253F)
    }

    override fun onCameraMove() {
        zoomLevel = map.cameraPosition.zoom
    }
}
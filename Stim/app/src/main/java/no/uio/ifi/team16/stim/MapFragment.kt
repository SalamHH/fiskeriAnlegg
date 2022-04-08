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
import com.google.android.gms.maps.model.LatLngBounds
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
class MapFragment : StimFragment(), OnMapReadyCallback {

    private val TAG = "MapFragment"
    private lateinit var map: GoogleMap
    private lateinit var binding: FragmentMapBinding
    private val viewModel: MainActivityViewModel by activityViewModels()
    private var mapReady = false
    private var mapBounds: CameraPosition? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var SearchView: SearchView

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

        // Observe sites and place them on the map
        viewModel.getMunicipalityData().observe(viewLifecycleOwner, this::onSiteUpdate)


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
        currentMunicipalityNr = munNr
        viewModel.loadSitesAtMunicipality(munNr)
        viewModel.getMunicipalityData().observe(viewLifecycleOwner) {
            onSiteUpdate((it))
            if (it != null && it.sites.isNotEmpty()) {
                val bounds = LatLngBounds(
                    LatLng(it.sites[0].latLong.lat, it.sites[0].latLong.lng),
                    LatLng(it.sites[0].latLong.lat + 0.5, it.sites[0].latLong.lng + 0.5)
                )
                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 10)
                map.moveCamera(cameraUpdate)

                // todo fix hva it gjør her
                val adapter = RecycleViewAdapter(it, this::adapterOnClick, requireActivity())
                binding.recyclerView.adapter = adapter
            }
        }
    }

    fun searchName(name: String) {
        map.clear()
        // todo dette må refaktoreres
        viewModel.loadSitesByName(name)
        viewModel.getSitesDataName().observe(viewLifecycleOwner) {
            onSiteUpdate((it))
            if (it != null && it.sites.isNotEmpty()) {
                currentMunicipalityNr = null//it.sites[0].placement?.municipalityCode.toString()
                currentSite = (it.sites[0].name)

                val bounds = LatLngBounds(
                    LatLng(it.sites[0].latLong.lat, it.sites[0].latLong.lng),
                    LatLng(it.sites[0].latLong.lat + 0.5, it.sites[0].latLong.lng + 0.5)
                )
                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 10)
                map.moveCamera(cameraUpdate)
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

        mapBounds?.let { bounds ->
            // Move to last camera position
            val update = CameraUpdateFactory.newCameraPosition(bounds)
            map.moveCamera(update)
        }

        // Observe municipality and place them on the map
        viewModel.getMunicipalityData().observe(viewLifecycleOwner, this::onSiteUpdate)

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

    private fun onSiteUpdate(municipality: Municipality?) {
        if (municipality != null && mapReady) {
            binding.numSites.text = "Antall anlegg: ${municipality.sites.size}"

            for (site in municipality.sites) {
                val markerOptions = MarkerOptions()
                markerOptions.title(site.name)
                markerOptions.position(site.latLong.toGoogle())
                map.addMarker(markerOptions)
            }

            //update municipality in bottomsheet
            val adapter =
                RecycleViewAdapter(municipality.sites, this::adapterOnClick, requireActivity())
            binding.recyclerView.adapter = adapter
        }
    }

    private fun onRefresh() {
        val center = LatLong.fromGoogle(map.cameraPosition.target)
        viewModel.loadMunicipalityNr(center)
    }

    /**
     * When an item in the recyclerview is clicked, navigate to that site
     */
    private fun adapterOnClick(site: Site) {
        viewModel.setCurrentSite(site)
        view?.findNavController()?.navigate(R.id.action_mapFragment_to_siteInfoFragment)
    }
}

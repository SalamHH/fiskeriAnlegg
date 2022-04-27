package no.uio.ifi.team16.stim

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import no.uio.ifi.team16.stim.data.Municipality
import no.uio.ifi.team16.stim.data.Site
import no.uio.ifi.team16.stim.databinding.FragmentMapBinding
import no.uio.ifi.team16.stim.io.adapter.RecycleViewAdapter
import no.uio.ifi.team16.stim.io.viewModel.MainActivityViewModel
import no.uio.ifi.team16.stim.util.LatLong
import no.uio.ifi.team16.stim.util.capitalizeEachWord

/**
 * Map fragment
 */
class MapFragment : StimFragment(), OnMapReadyCallback, GoogleMap.OnCameraMoveListener, SearchView.OnQueryTextListener {

    private val TAG = "MapFragment"
    private lateinit var map: GoogleMap
    private lateinit var binding: FragmentMapBinding
    private val viewModel: MainActivityViewModel by activityViewModels()
    private var mapReady = false
    private var mapBounds: CameraPosition? = null
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

        binding = FragmentMapBinding.inflate(layoutInflater)

        val mapFragment = SupportMapFragment.newInstance()
        activity?.supportFragmentManager?.beginTransaction()?.add(R.id.mapView, mapFragment)?.commit()

        mapFragment.getMapAsync(this)

        // Observe municipality number
        viewModel.getMunicipalityNr().observe(viewLifecycleOwner, this::onMunicipalityNrRecieved)

        // Observe municipality
        viewModel.getMunicipalityData().observe(viewLifecycleOwner, this::onMunicipalityUpdate)

        // Observe individual site (searched for by name)
        viewModel.getCurrentSitesData().observe(viewLifecycleOwner, this::onSiteSearchComplete)

        //Bottom Sheet behavior
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.setPeekHeight(200, true)
        bottomSheetBehavior.isDraggable = true
        bottomSheetBehavior.isHideable = false

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = RecycleViewAdapter(listOf(), listOf(), this::adapterOnClick, this::favoriteOnClick, requireActivity())
        binding.recyclerView.adapter = adapter

        binding.syncBtn.setOnClickListener {
            onRefresh()
        }

        binding.searchView.setOnQueryTextListener(this)

        return binding.root
    }

    /**
     * Called when the user searches for something
     */
    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null && query.isNotBlank()) {
            closeKeyboard()
            map.clear()
            if (query.matches(Regex("^[0-9]+\$"))) {
                // Numeric input, search for municipality number
                viewModel.loadSitesAtMunicipality(query)
            } else {
                // Letters in input, search for site name
                viewModel.loadSitesByName(query)
            }
            return true
        }
        return false
    }

    /**
     * Called when the fragment goes out of focus
     */
    override fun onDestroyView() {
        super.onDestroyView()
        mapBounds = map.cameraPosition
    }

    /**
     * Called when the map is visible
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        mapReady = true

        map.setOnCameraMoveListener(this::onCameraMove)
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style))

        mapBounds?.let { bounds ->
            // Move to last camera position
            val update = CameraUpdateFactory.newCameraPosition(bounds)
            map.moveCamera(update)
        } ?: run {
            map.moveCamera(getInitialCameraPosition())
        }

        if (checkLocationPermission()) {
            map.isMyLocationEnabled = true
        }
    }

    /**
     * Called when a site has been searched for and found
     */
    private fun onSiteSearchComplete(sites: List<Site>?) {
        if (sites != null) {
            onSiteUpdate(sites)
            currentSite = sites[0].name
            val adapter =
                RecycleViewAdapter(sites, listOf(), this::adapterOnClick, this::favoriteOnClick, requireActivity())
            binding.recyclerView.adapter = adapter

            binding.headerBtmSheet.text = "Liste over anlegg"

            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(sites[0].latLong.toGoogle(), 5F)
            map.animateCamera(cameraUpdate)
        }
    }



    /**
     * Called when the ViewModel has found a municipality number
     */
    private fun onMunicipalityNrRecieved(nr: String?) {
        if (nr != null) {
            viewModel.loadSitesAtMunicipality(nr)
        }
    }

    /**
     * Called when the sites in a municipality has been loaded
     */
    private fun onMunicipalityUpdate(municipality: Municipality?) {
        if (mapReady && municipality != null && municipality.sites.isNotEmpty()) {


            onSiteUpdate(municipality.sites)

            // Move camera to arbitrary site in municipality
            val firstSite = municipality.sites[0]
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(firstSite.latLong.toGoogle(), zoomLevel)
            map.animateCamera(cameraUpdate)

            var favSites = emptyList<Site>()
            viewModel.getFavouriteSitesData().observe(viewLifecycleOwner) {
                if (it != null) {
                    favSites = it.toList()
                }
            }
            val adapter = RecycleViewAdapter(municipality.sites, favSites, this::adapterOnClick, this::favoriteOnClick, requireActivity())
            binding.recyclerView.adapter = adapter

            binding.headerBtmSheet.text = getString(
                R.string.bottomsheet_formatted_header,
                firstSite.placement?.municipalityName?.capitalizeEachWord()
            )
        }
    }

    /**
     * Called when the ViewModel has loaded some sites
     */
    private fun onSiteUpdate(sites: List<Site>?) {
        if (sites != null && mapReady) {
            for (site in sites) {
                val markerOptions = MarkerOptions()
                markerOptions.title(site.name)

                markerOptions.position(site.latLong.toGoogle())
                map.addMarker(markerOptions)
                site.placement?.let { Log.d("munname", it.municipalityName) }
            }
        }

        map.setOnMarkerClickListener ( object: GoogleMap.OnMarkerClickListener{
            override fun onMarkerClick(mark: Marker): Boolean {
                Log.d("hh", mark.title.toString())
                viewModel.loadSitesByName(mark.title.toString())
                val site=viewModel.getCurrentSiteData().value
               // viewModel.loadSitesAtMunicipality(site?.placement?.municipalityCode.toString())



                if (site != null) {
                    viewModel.setCurrentSite(site)
                }
                view?.findNavController()?.navigate(R.id.action_mapFragment_to_siteInfoFragment)


                return false
            }
        } )
    }

    /**
     * Called when the user pressed the load button
     */
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

    private fun favoriteOnClick(site : Site, checked : Boolean) {
        if (checked) viewModel.registerFavouriteSite(site)
        else viewModel.removeFavouriteSite(site)
    }

    /**
     * Hent kartposisjon som viser sør-Norge
     */
    private fun getInitialCameraPosition(): CameraUpdate {
        val coordinates = LatLng(61.42888648306541, 8.68770383298397)
        return CameraUpdateFactory.newLatLngZoom(coordinates, 5.5639253F)
    }

    /**
     * Called when the map is moved
     */
    override fun onCameraMove() {
        zoomLevel = map.cameraPosition.zoom
    }

    /**
     * Called on text input in search field, does nothing
     */
    override fun onQueryTextChange(newText: String?): Boolean {
        return false
    }
}
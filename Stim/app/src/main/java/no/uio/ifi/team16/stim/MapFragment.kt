package no.uio.ifi.team16.stim

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import no.uio.ifi.team16.stim.data.Municipality
import no.uio.ifi.team16.stim.data.Site
import no.uio.ifi.team16.stim.databinding.FragmentMapBinding
import no.uio.ifi.team16.stim.io.adapter.RecycleViewAdapter
import no.uio.ifi.team16.stim.io.viewModel.MainActivityViewModel
import no.uio.ifi.team16.stim.util.LatLong
import kotlin.math.roundToInt


/**
 * Map fragment
 */
class MapFragment : StimFragment(), OnMapReadyCallback, GoogleMap.OnCameraMoveListener {

    companion object {
        private const val TAG = "MapFragment"
        private const val MIN_ZOOM_FOR_MAP_SEARCH = 6F
    }

    private lateinit var map: GoogleMap
    private lateinit var binding: FragmentMapBinding
    private lateinit var speedDial: SpeedDialView
    private var heatMapUsed: SpeedDialActionItem? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private var locationClient: FusedLocationProviderClient? = null
    private val viewModel: MainActivityViewModel by activityViewModels()
    private var mapReady = false
    private var mapBounds: CameraPosition? = null
    private var zoomLevel = 12F
    private val markerMap: MutableMap<Marker, Site> = mutableMapOf()
    private var usingHeatmap = false
    private var doSiteSearchOnMovement = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (!checkLocationPermission()) {
            requestPermission { hasPermission ->
                if (hasPermission) {
                    onLocationPermissionGranted()
                }
            }
        } else {
            onLocationPermissionGranted()
        }

        binding = FragmentMapBinding.inflate(layoutInflater)
        speedDial = binding.speedDial

        val mapFragment = SupportMapFragment.newInstance()
        activity?.supportFragmentManager?.beginTransaction()?.add(R.id.mapView, mapFragment)?.commit()

        mapFragment.getMapAsync(this)

        // Observe municipality
        viewModel.getMunicipalityData().observe(viewLifecycleOwner, this::onMunicipalityUpdate)

        // Observe individual site (searched for by name)
        viewModel.getCurrentSitesData().observe(viewLifecycleOwner, this::onSiteSearchComplete)

        //Bottom Sheet behavior
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.setPeekHeight(230, true)
        bottomSheetBehavior.isDraggable = true
        bottomSheetBehavior.isHideable = false

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter =
            RecycleViewAdapter(
                listOf(),
                listOf(),
                this::adapterOnClick,
                this::favoriteOnClick,
                requireActivity()
            )
        binding.recyclerView.adapter = adapter

        setHasOptionsMenu(true)



        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_toolbar, menu)
        val mSearch = menu.findItem(R.id.search)
        val mSearchView = mSearch.actionView as SearchView
        mSearchView.queryHint = "Søk her"
        mSearchView.setIconifiedByDefault(false)
        mSearchView.setBackgroundResource(R.drawable.long_circle)

        mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            /**
             * Called when the user searches for something
             */
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null && query.isNotBlank()) {
                    doSiteSearchOnMovement = false
                    closeKeyboard()
                    map.clear()
                    markerMap.clear()
                    viewModel.doMapSearch(query)
                    return true
                }
                return false
            }

            /**
             * Called on text input in search field, does nothing
             */
            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })

        super.onCreateOptionsMenu(menu, inflater)
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
        map.setOnCameraIdleListener(this::onCameraIdle)
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style))
        map.uiSettings.isMyLocationButtonEnabled = false
        map.setOnCameraMoveStartedListener(this::onCameraMoveStarted)

        mapBounds?.let { bounds ->
            // Move to last camera position
            val update = CameraUpdateFactory.newCameraPosition(bounds)
            map.moveCamera(update)
        } ?: run {
            map.moveCamera(getInitialCameraPosition())
        }

        if (checkLocationPermission()) {
            map.isMyLocationEnabled = true
            setMapToUserLocation()
        }

        closeKeyboard()
        initSpeedDial(requireContext(), googleMap)
    }

    /**
     * Called when a site has been searched for and found
     */
    private fun onSiteSearchComplete(sites: List<Site>?) {
        if (sites != null) {
            onSiteUpdate(sites)
            val adapter =
                RecycleViewAdapter(
                    sites,
                    listOf(),
                    this::adapterOnClick,
                    this::favoriteOnClick,
                    requireActivity()
                )
            binding.recyclerView.adapter = adapter

            bottomSheetBehavior.setPeekHeight(400, true)
            bottomSheetBehavior.isDraggable = true
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED

            binding.openHeaderBottomsheet.kommuneText.text = getString(R.string.blank)

            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(sites[0].latLong.toGoogle(), zoomLevel)
            map.animateCamera(cameraUpdate)
        }
    }

    /**
     * Called when the sites in a municipality has been loaded
     */
    private fun onMunicipalityUpdate(municipality: Municipality?) {
        if (mapReady && municipality != null && municipality.sites.isNotEmpty()) {
            onSiteUpdate(municipality.sites)

            if (!doSiteSearchOnMovement) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                if (municipality.sites.size > 1) {
                    // Move camera to fit all sites
                    try {
                        val bounds = LatLngBounds.builder()
                        for (site in municipality.sites) {
                            bounds.include(site.latLong.toGoogle())
                        }
                        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 200))
                    } catch (e: Exception) {
                        Log.e(TAG, "Error occured while animating map: ", e)
                    }
                } else {
                    // One site, move to it
                    val site = municipality.sites[0]
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(site.latLong.toGoogle(), zoomLevel)
                    map.animateCamera(cameraUpdate)
                }
            }

            var favSites = emptyList<Site>()
            viewModel.getFavouriteSitesData().observe(viewLifecycleOwner) {
                if (it != null) {
                    favSites = it.toList()
                }
            }
            val adapter = RecycleViewAdapter(
                municipality.sites,
                favSites,
                this::adapterOnClick,
                this::favoriteOnClick,
                requireActivity()
            )
            binding.recyclerView.adapter = adapter

            //get municipality name in bottom header
            val firstSite = municipality.sites[0]
            binding.openHeaderBottomsheet.kommuneText.text = firstSite.placement?.municipalityName
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
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.fish_marker))
                markerOptions.position(site.latLong.toGoogle())
                val marker = map.addMarker(markerOptions)
                if (marker != null) {
                    markerMap[marker] = site
                }
            }
        }

        binding.openHeaderBottomsheet.infoText.text = sites?.size.toString()

        map.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
            override fun onMarkerClick(mark: Marker): Boolean {
                val site = markerMap[mark]

                if (site != null) {
                    viewModel.setCurrentSite(site)
                    view?.findNavController()?.navigate(R.id.action_mapFragment_to_siteInfoFragment)
                    return true
                }

                return false
            }
        })
    }

    /**
     * Called when the user starts moving the map
     */
    private fun onCameraMoveStarted(reason: Int) {
        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            closeKeyboard()
            doSiteSearchOnMovement = true
        }
    }

    /**
     * Called when the user has stopped moving the map
     */
    private fun onCameraIdle() {
        if (doSiteSearchOnMovement && zoomLevel >= MIN_ZOOM_FOR_MAP_SEARCH) {
            val center = LatLong.fromGoogle(map.cameraPosition.target)
            viewModel.loadSitesAtLocation(center)
        }
        if (usingHeatmap) {
            heatMapUsed?.let { heatMap ->
                drawHeatmap(heatMap, map)
            }
        }
    }

    /**
     * When an item in the recyclerview is clicked, navigate to that site
     */
    private fun adapterOnClick(site: Site) {
        viewModel.setCurrentSite(site)
        view?.findNavController()?.navigate(R.id.action_mapFragment_to_siteInfoFragment)
    }

    /**
     * Adds or removes a site from favourites
     */
    private fun favoriteOnClick(site: Site, checked: Boolean) {
        if (checked) {
            viewModel.registerFavouriteSite(site)
        } else {
            viewModel.removeFavouriteSite(site)
        }
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

    }

    /**
     * Called when access to user location is granted
     */
    private fun onLocationPermissionGranted() {
        locationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    /**
     * Set the map to the users current location
     */
    @SuppressLint("MissingPermission")
    fun setMapToUserLocation() {
        locationClient?.let { client ->
            client.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))
                    onCameraIdle()
                }
            }
        }
    }


    enum class HeatmapType { INFECTIOUSPRESSURE, SALINITY, TEMPERATURE, VELOCITY, NONE }

    var tileOverlay: TileOverlay? = null

    private fun initSpeedDial(context: Context, googleMap: GoogleMap) {

        //val xRange = fromClosedRange(0,2602,50) //IMPORTANT: THESE ARE THE CORRECT WAYS TO INDEX!
        //val yRange = fromClosedRange(0,902,50)

        speedDial.addActionItem(
            SpeedDialActionItem.Builder(R.id.heatmap_infectiousPressure, R.drawable.mdi___virus)
                .setFabBackgroundColor(ContextCompat.getColor(context, R.color.dark_skyblue))
                .setFabImageTintColor(ContextCompat.getColor(context, R.color.white))
                .create()
        )
        speedDial.addActionItem(
            SpeedDialActionItem.Builder(R.id.heatmap_salinity, R.drawable.salt)
                .setFabBackgroundColor(ContextCompat.getColor(context, R.color.dark_skyblue))
                .setFabImageTintColor(ContextCompat.getColor(context, R.color.white))
                .create()
        )
        speedDial.addActionItem(
            SpeedDialActionItem.Builder(R.id.heatmap_temperature, R.drawable.farevarsel)
                .setFabBackgroundColor(ContextCompat.getColor(context, R.color.dark_skyblue))
                .setFabImageTintColor(ContextCompat.getColor(context, R.color.white))
                .create()
        )
        speedDial.addActionItem(
            SpeedDialActionItem.Builder(R.id.heatmap_velocity, R.drawable.down_icon)
                .setFabBackgroundColor(ContextCompat.getColor(context, R.color.dark_skyblue))
                .setFabImageTintColor(ContextCompat.getColor(context, R.color.white))
                .create()
        )

        speedDial.setOnActionSelectedListener(SpeedDialView.OnActionSelectedListener { actionItem ->
            heatMapUsed = actionItem
            usingHeatmap = true
            val result = drawHeatmap(actionItem, googleMap)
            speedDial.close()
            result
        })
    }

    private fun drawHeatmap(actionItem: SpeedDialActionItem, googleMap: GoogleMap): Boolean {
        tileOverlay?.remove()
        when (actionItem.id) {
            R.id.heatmap_infectiousPressure -> {
                Toast.makeText(
                    context,
                    "Speed Dial 'infectiousPressure' klikket!",
                    Toast.LENGTH_LONG
                ).show()
                // INFECTIOUSPRESSURE HEATMAP
                Log.d(TAG, "USING INFECTIOUSPRESSURE")
                viewModel.getInfectiousPressureData()
                    .observe(viewLifecycleOwner) { infectiousPressure ->
                        val z = googleMap.cameraPosition.zoom
                        val screenBound = googleMap.projection.visibleRegion.latLngBounds

                        val n =
                            Math.max(1 + 20 * (z - 9.884714) / (4.992563 - 9.884714), 1.0).toInt()
                        val scale = 50.0
                        infectiousPressure?.let {
                            val data = it.getHeatMapData(screenBound, n)
                            val heatMapProvider = HeatmapTileProvider.Builder()
                                .weightedData(data) // load our weighted data
                                .radius((scale).roundToInt()) // finn måte å gjøre om til 800x800 m
                                .build()
                            tileOverlay = googleMap.addTileOverlay(
                                TileOverlayOptions().tileProvider(heatMapProvider)
                            )
                        }
                    }
                return true // false will close it without animation
            }
        }
        return false
    }
}
package no.uio.ifi.team16.stim

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
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
import no.uio.ifi.team16.stim.data.Site
import no.uio.ifi.team16.stim.data.Sites
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
    private lateinit var SearchView: androidx.appcompat.widget.SearchView

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

        // Observe sites and place them on the map
        viewModel.getSitesData().observe(viewLifecycleOwner, this::onSiteUpdate)


        //Bottom Sheet behavior
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.setPeekHeight(200, true)
        bottomSheetBehavior.isDraggable = true
        bottomSheetBehavior.isHideable = false

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = RecycleViewAdapter(Sites(listOf()), this::adapterOnClick, requireActivity())
        binding.recyclerView.adapter = adapter


        /* val spinner = binding.spinner

         //populere spinneren fra array
         ArrayAdapter.createFromResource(
             requireActivity()!!,
             R.array.searchChoices,
             android.R.layout.simple_spinner_item
         ).also { adapter ->
             // Specify the layout to use when the list of choices appears
             adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
             // Apply the adapter to the spinner
             spinner.adapter = adapter
         }


         spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
             override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                 when(spinner.selectedItem){
                     "municipality code" ->{
                         Log.d("heei","test2")
                     }
                     "site name" ->{
                         Log.d("heei","test")

                     }

                 }
             }

             override fun onNothingSelected(parent: AdapterView<*>?) {
             }
         }*/

        binding.syncBtn.setOnClickListener {
            onRefresh()
        }

        //nytt
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                if (p0 != null) {
                   // searchName(p0) fjern comment for å søke etter navn til site
                    searchMunNr(p0)
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
        currentMunicipalityNr = munNr
        viewModel.loadSites(munNr)
        viewModel.getSitesData().observe(viewLifecycleOwner) {
            onSiteUpdate((it))
            if (it != null && it.sites.isNotEmpty()) {
                val bounds = LatLngBounds(
                    LatLng(it.sites[0].latLong.lat, it.sites[0].latLong.lng),
                    LatLng(it.sites[0].latLong.lat + 0.5, it.sites[0].latLong.lng + 0.5)
                )
                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 10)
                map.moveCamera(cameraUpdate)

                val adapter = RecycleViewAdapter(it, this::adapterOnClick, requireActivity())
                binding.recyclerView.adapter = adapter
            }
        }
    }

    fun searchName(name: String){

        viewModel.loadSitesByName(name)
        viewModel.getSitesDataName().observe(viewLifecycleOwner){
            onSiteUpdate((it))
            if (it != null) {
                currentMunicipalityNr= null//it.sites[0].placement?.municipalityCode.toString()
                currentSite=(it.sites[0].name)

                val bounds = LatLngBounds(LatLng(it.sites[0].latLong.lat, it.sites[0].latLong.lng), LatLng(it.sites[0].latLong.lat+0.5, it.sites[0].latLong.lng+0.5))
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
            currentMunicipalityNr = nr
            viewModel.loadSites(nr)
        }
        //todo - update headertext in bottomsheet to kommunenavn/nr
    }

    private fun onSiteUpdate(sites: Sites?) {

        if (sites != null) {
            binding.numSites.text = "Antall anlegg: ${sites.sites.size}"

            for (site in sites.sites) {
                if (mapReady) {
                    val markerOptions = MarkerOptions()
                    markerOptions.title(site.name)
                    markerOptions.position(site.latLong.toGoogle())
                    map.addMarker(markerOptions)
                }
            }

            //update sites in bottomsheet
            val adapter = RecycleViewAdapter(sites, this::adapterOnClick, requireActivity())
            binding.recyclerView.adapter = adapter
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
        viewModel.setCurrentSite(site)
        view?.findNavController()?.navigate(R.id.action_mapFragment_to_siteInfoFragment)
    }
}



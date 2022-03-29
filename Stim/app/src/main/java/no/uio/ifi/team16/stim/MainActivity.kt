package no.uio.ifi.team16.stim

import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import no.uio.ifi.team16.stim.databinding.ActivityMainBinding
import no.uio.ifi.team16.stim.io.viewModel.MainActivityViewModel
import no.uio.ifi.team16.stim.util.Options


class MainActivity : StimActivity() {

    private val TAG = "MainActivity"
    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Navigation control for fragments
        val navController = this.findNavController(R.id.myNavHostFragment)
        NavigationUI.setupActionBarWithNavController(this,navController)

        //initial load of data
        viewModel.loadNorKyst800()
        viewModel.loadInfectiousPressure()
        viewModel.loadSites(Options.fakeMunicipality)

        /**
         * DISSE METODENE TRENGS IKKE FOR RECYCLEVIEW MEN KANSKJE NOE ANNET??
         */
        /*
        //observe infectious pressure
        viewModel.getInfectiousPressureData().observe(this) { infectiousPressure ->
            Log.d("INVOKED", "observer of infectiousPressure")
            println("THE INFECTIOUS PRESSURE OBSERVED IS \n" + infectiousPressure.toString())
            //set adapter of recyclerview
            recycleview.adapter = RecycleViewAdapter(
                this,
                viewModel.getSitesData().value ?: Sites(listOf()),
                infectiousPressure,
                viewModel.getNorKyst800Data().value
            )

            //Log.d(TAG, infectiousPressure?.getLatitude(4,4).toString())
            Log.d(TAG, infectiousPressure?.getLatitude(4, 4).toString())
            Log.d(TAG, infectiousPressure?.getLongitude(4, 4).toString())
            val lat = infectiousPressure!!.getLatitude(4, 4)
            val lng = infectiousPressure!!.getLongitude(4, 4)
            Log.d(TAG, infectiousPressure?.getEtaRho(4).toString())
            Log.d(TAG, infectiousPressure?.getXiRho(4).toString())
            Log.d(TAG, infectiousPressure.project(lat, lng).toString())
            val crsFactory = CRSFactory()
            val stereographicProjection = crsFactory.createFromParameters(
                null,
                "+proj=stere +ellps=WGS84 +lat_0=90.0 +lat_ts=60.0 +x_0=3192800 +y_0=1784000 +lon_0=70"
            )
            val latLngCRT = stereographicProjection.createGeographic()
            val ctFactory = CoordinateTransformFactory()
            val latLngToStereo: CoordinateTransform =
                ctFactory.createTransform(latLngCRT, stereographicProjection)
            // `result` is an output parameter to `transform()`
            // `result` is an output parameter to `transform()`
            val result = ProjCoordinate()
            latLngToStereo.transform(ProjCoordinate(lng.toDouble(), lat.toDouble()), result)
            Log.d(TAG, "\n\n\n" + result.toString() + "\n\n\n")
        }



        //observe norKyst800
        viewModel.getNorKyst800Data().observe(this) { norKyst800 ->
            Log.d("INVOKED", "observer of norkystPressure")
            println("THE NORKYST800 OBSERVED IS \n" + norKyst800.toString())
            //set adapter of recyclerview
            recycleview.adapter = RecycleViewAdapter(
                this,
                viewModel.getSitesData().value ?: Sites(listOf()),
                viewModel.getInfectiousPressureData().value,
                norKyst800
            )
        }

         */
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.myNavHostFragment)
        return navController.navigateUp()
    }
}
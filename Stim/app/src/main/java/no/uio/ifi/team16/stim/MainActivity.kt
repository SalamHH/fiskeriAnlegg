package no.uio.ifi.team16.stim

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.recyclerview.widget.RecyclerView
import no.uio.ifi.team16.stim.data.Sites
import no.uio.ifi.team16.stim.io.viewModel.MainActivityViewModel
import no.uio.ifi.team16.stim.io.viewModel.RecycleViewAdapter
import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.CoordinateTransform
import org.locationtech.proj4j.CoordinateTransformFactory
import org.locationtech.proj4j.ProjCoordinate


class MainActivity : StimActivity() {
    val TAG = "MainActivity"
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /********************
         *  RECYCLEVIEW *
         ********************/

        setContentView(R.layout.recycleview)
        val recycleview = findViewById<RecyclerView>(R.id.recyclerview)

        /*************
         * OBSERVERS *
         *************/
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

        //observe sites
        viewModel.getSitesData().observe(this) { sites ->
            Log.d("INVOKED", "observer of sites")
            println("THE SITES OBSERVED ARE \n" + sites.toString())

            recycleview.adapter = RecycleViewAdapter(
                this,
                sites ?: Sites(listOf()),
                viewModel.getInfectiousPressureData().value,
                viewModel.getNorKyst800Data().value
            )
        }

        //observe sites
        viewModel.getSitesData().observe(this) { sites ->
            Log.d("INVOKED", "observer of infectiousPressure")
            println("THE SITES OBSERVED ARE \n" + sites.toString())

            //do something with the sites data.
        }

        //initial load of data
        viewModel.loadNorKyst800()
        viewModel.loadInfectiousPressure()
        viewModel.loadSites()
    }
}
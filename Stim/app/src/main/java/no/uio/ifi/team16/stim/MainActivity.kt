package no.uio.ifi.team16.stim

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import no.uio.ifi.team16.stim.model.viewModel.MainActivityViewModel
import no.uio.ifi.team16.stim.model.viewModel.RecycleViewAdapter
import no.uio.ifi.team16.stim.io.viewModel.MainActivityViewModel

class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /********************
         * TEST RECYCLEVIEW *
         ********************/

        setContentView(R.layout.recycleview)
        val recycleview = findViewById<RecyclerView>(R.id.recyclerview)
        recycleview.adapter = RecycleViewAdapter(null)

        /*************
         * OBSERVERS *
         *************/
        //observe infectious pressure
        viewModel.getInfectiousPressureData().observe(this) { infectiousPressure ->
            Log.d("INVOKED", "observer of infectiousPressure")
            println("THE INFECTIOUS PRESSURE OBSERVED IS \n" + infectiousPressure.toString())

            //do something with the infectious pressure data.

            //RECYCLEVIEWTEST
            Log.d("RECYCLEVIEW_TEST :", "Trying to add adapter")
            recycleview.adapter = RecycleViewAdapter(infectiousPressure)
            Log.d("RECYCLEVIEW_TEST: ", "Adapter added")

        }

        //observe sites
        viewModel.getSitesData().observe(this) { sites ->
            Log.d("INVOKED", "observer of infectiousPressure")
            println("THE SITES OBSERVED ARE \n" + sites.toString())

            //do something with the sites data.
        }

        //initial load of data
        viewModel.loadInfectiousPressure()
        viewModel.loadSites()
    }
}
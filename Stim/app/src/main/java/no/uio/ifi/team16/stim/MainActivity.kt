package no.uio.ifi.team16.stim

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import no.uio.ifi.team16.stim.data.Sites
import no.uio.ifi.team16.stim.io.viewModel.MainActivityViewModel
import no.uio.ifi.team16.stim.io.viewModel.RecycleViewAdapter

class MainActivity : AppCompatActivity() {
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
                infectiousPressure
            )
        }

        //observe sites
        viewModel.getSitesData().observe(this) { sites ->
            Log.d("INVOKED", "observer of infectiousPressure")
            println("THE SITES OBSERVED ARE \n" + sites.toString())
            
            recycleview.adapter = RecycleViewAdapter(
                this,
                sites ?: Sites(listOf()),
                viewModel.getInfectiousPressureData().value
            )
        }

        //initial load of data
        viewModel.loadInfectiousPressure()
        viewModel.loadSites()
    }
}
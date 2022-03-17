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
                infectiousPressure,
                viewModel.getNorKyst800Data().value
            )
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

        //initial load of data
        viewModel.loadNorKyst800()
        viewModel.loadInfectiousPressure()
        viewModel.loadSites()
    }
}
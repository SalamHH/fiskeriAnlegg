package no.uio.ifi.team16.stim

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
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

        //kjører recycleview med dummy data
        recycleview.adapter = RecycleViewAdapter()

        /*************
         * OBSERVERS *
         *************/
        //observe infectious pressure
        viewModel.getInfectiousPressureData().observe(this) { infectiousPressure ->
            Log.d("INVOKED", "observer of infectiousPressure")
            println("THE INFECTIOUS PRESSURE OBSERVED IS \n" + infectiousPressure.toString())

            //do something with the infectious pressure data.
        }

        //initial load of data
        viewModel.loadInfectiousPressure()
    }
}
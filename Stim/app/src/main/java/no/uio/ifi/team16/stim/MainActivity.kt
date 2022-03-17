package no.uio.ifi.team16.stim

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import no.uio.ifi.team16.stim.data.Site
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
        /**
         * Dummy data for å teste
         */
        val dummySite1 = Site(0, "TUHOLMANE Ø", 59.371233, 5.216333)
        val dummySite2 = Site(1, "TJAJNELUOKTA", 67.892433, 16.236718)
        val dummySite3 = Site(2, "JØRSTADSKJERA", 59.2955, 5.938617)

        val dummyList: List<Site> = listOf(dummySite1, dummySite2, dummySite3)

        recycleview.adapter = RecycleViewAdapter(dummyList)

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
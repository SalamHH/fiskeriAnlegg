package no.uio.ifi.team16.stim

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import no.uio.ifi.team16.stim.model.MainActivityViewModel

class MainActivity : AppCompatActivity() {
    private val viewModel : MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*************
         * OBSERVERS *
         *************/
        //observe parties
        viewModel.getInfectiousPressureData().observe(this){ infectiousPressure ->
            Log.d("INVOKED: ", "observer of infectiousPressure")
        }

        //initial load of data
        viewModel.loadInfectiousPressure()
    }
}
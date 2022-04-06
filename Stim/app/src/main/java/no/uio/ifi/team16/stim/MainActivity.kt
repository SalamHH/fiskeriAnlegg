package no.uio.ifi.team16.stim

import android.os.Bundle
import android.util.Log
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

        //window.statusBarColor = Color.TRANSPARENT
        //window.navigationBarColor = Color.TRANSPARENT
        //window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Navigation control for fragments
        val navController = this.findNavController(R.id.myNavHostFragment)
        NavigationUI.setupActionBarWithNavController(this, navController)

        //initial load of data
        viewModel.loadNorKyst800()
        viewModel.loadInfectiousPressure()
        viewModel.loadSites(Options.fakeMunicipality)

        /**observe norKyst800
        viewModel.getInfectiousPressureTimeSeriesData().observe(this) { inf ->
        Log.d(TAG, "observed change in infectiousPressureTimeseries")
        }
         **/

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.myNavHostFragment)
        return navController.navigateUp()
    }
}
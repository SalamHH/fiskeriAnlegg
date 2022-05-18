package no.uio.ifi.team16.stim

import android.content.ComponentCallbacks2
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import no.uio.ifi.team16.stim.databinding.ActivityMainBinding
import no.uio.ifi.team16.stim.io.viewModel.MainActivityViewModel
import no.uio.ifi.team16.stim.util.Options


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var prefrences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.skyblue)

        prefrences = getSharedPreferences(Options.SHARED_PREFERENCES_KEY, MODE_PRIVATE)

        // Navigation control for fragments
        setSupportActionBar(binding.toolbar)
        val navController = this.findNavController(R.id.myNavHostFragment)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.favoriteSitesFragment,
                R.id.mapFragment,
                R.id.appInfoFragment,
                R.id.tutorialFragment
            ),
            binding.drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        //initial load of data
        viewModel.loadPrefrences(prefrences)
        viewModel.loadNorKyst800() // todo hva brukes dette til?
        viewModel.loadInfectiousPressure() // todo hva brukes dette til?
        viewModel.loadFavouriteSites()

        //setup periodical loading of data
        /*Timer().schedule(
            object : TimerTask() {
                override fun run() {
                    Log.d(TAG, "Reloading stale datasets")
                    viewModel.loadNorKyst800Anew()
                    /*TODO: infectiouspressure updates once a week, but it is impossible to predict when, maybe
                    update every hour just in case? or just ignore and assume app does not live beyond an update cycle*/
                }
            },
            (Calendar.getInstance().run {
                60 * 60 - (60 * get(Calendar.MINUTE) + get(Calendar.SECOND)) //seconds until next hour
            } * 1000).toLong(), //milliseconds until next hour
            60 * 60 * 1000
        )*/
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL) {
            Log.w(TAG, "Tømmer cache pga. lite minne!")
            Options.decreaseDataResolution()
            viewModel.clearCache()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.myNavHostFragment).navigateUp(appBarConfiguration)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(findNavController(R.id.myNavHostFragment))
                || super.onOptionsItemSelected(item)
    }
}
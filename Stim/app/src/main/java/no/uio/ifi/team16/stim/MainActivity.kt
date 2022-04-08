package no.uio.ifi.team16.stim

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import no.uio.ifi.team16.stim.databinding.ActivityMainBinding
import no.uio.ifi.team16.stim.io.viewModel.MainActivityViewModel
import no.uio.ifi.team16.stim.util.Options


class MainActivity : StimActivity() {

    private val TAG = "MainActivity"
    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration : AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Navigation control for fragments
        setSupportActionBar(binding.toolbar)
        val navController = this.findNavController(R.id.myNavHostFragment)
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.favoriteSitesFragment, R.id.mapFragment),
            binding.drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)


        //initial load of data
        viewModel.loadNorKyst800()
        viewModel.loadInfectiousPressure()
        viewModel.loadFavouriteSites()
        viewModel.loadSitesAtMunicipality(Options.fakeMunicipality)

    }

    override fun onSupportNavigateUp(): Boolean {
       return findNavController(R.id.myNavHostFragment).navigateUp(appBarConfiguration)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(findNavController(R.id.myNavHostFragment))
                || super.onOptionsItemSelected(item)
    }
}
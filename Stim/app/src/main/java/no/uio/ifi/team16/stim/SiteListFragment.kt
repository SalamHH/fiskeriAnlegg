package no.uio.ifi.team16.stim

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import no.uio.ifi.team16.stim.data.Site
import no.uio.ifi.team16.stim.data.Sites
import no.uio.ifi.team16.stim.databinding.FragmentSiteListBinding
import no.uio.ifi.team16.stim.io.adapter.RecycleViewAdapter
import no.uio.ifi.team16.stim.io.viewModel.MainActivityViewModel

class SiteListFragment : Fragment() {

    private lateinit var binding: FragmentSiteListBinding
    private val viewModel: MainActivityViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSiteListBinding.inflate(inflater, container, false)

        /********************
         *  RECYCLEVIEW *
         ********************/

        val adapter = RecycleViewAdapter(Sites(listOf()), this::adapterOnClick)
        binding.recyclerview.adapter = adapter

        //observe sites
        viewModel.getSitesData().observe(viewLifecycleOwner) { sites ->
            Log.d("INVOKED", "observer of sites")
            println("THE SITES OBSERVED ARE \n" + sites.toString())

            if (sites != null) {
                adapter.sites = sites
                adapter.notifyDataSetChanged()
            }
        }
        return binding.root
    }

    /*
    When an item in the RecyclerView is clicked it updates the viewModels currentSite to the Site that was clicked
    and then it navigates to the fragment that fetches this Site and displays information about it */
    private fun adapterOnClick(site : Site) {
        viewModel.setCurrentSite(site)
        view?.findNavController()?.navigate(R.id.action_siteListFragment_to_siteInfoFragment)

    }
}
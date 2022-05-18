package no.uio.ifi.team16.stim

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import no.uio.ifi.team16.stim.data.Site
import no.uio.ifi.team16.stim.databinding.FragmentFavoriteSitesBinding
import no.uio.ifi.team16.stim.io.adapter.RecycleViewAdapter
import no.uio.ifi.team16.stim.io.viewModel.MainActivityViewModel


class FavoriteSitesFragment : StimFragment() {

    private lateinit var binding: FragmentFavoriteSitesBinding
    private val viewModel: MainActivityViewModel by activityViewModels()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentFavoriteSitesBinding.inflate(inflater, container, false)

        /********************
         *  RECYCLEVIEW *
         ********************/

        val adapter = RecycleViewAdapter(listOf(), listOf(), this::adapterOnClick, this::favoriteOnClick, requireActivity())
        binding.recyclerview.adapter = adapter

        //observe municipality
        viewModel.getFavouriteSitesData().observe(viewLifecycleOwner) { sites ->

            if (sites != null) {
                adapter.sites = sites
                adapter.favs = sites
                adapter.notifyDataSetChanged()
            }
            binding.noFavoriteSites.isVisible = sites?.size == 0
        }
        return binding.root
    }

    /*
    When an item in the RecyclerView is clicked it updates the viewModels currentSite to the Site that was clicked
    and then it navigates to the fragment that fetches this Site and displays information about it */
    private fun adapterOnClick(site : Site) {
        viewModel.setCurrentSite(site)
        view?.findNavController()?.navigate(R.id.action_favoriteSitesFragment_to_siteInfoFragment)
    }

    private fun favoriteOnClick(site : Site, checked : Boolean) {
        if (checked) viewModel.registerFavouriteSite(site)
        else viewModel.removeFavouriteSite(site)
    }
}
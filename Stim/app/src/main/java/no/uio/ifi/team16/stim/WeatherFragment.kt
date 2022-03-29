package no.uio.ifi.team16.stim

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import no.uio.ifi.team16.stim.databinding.FragmentWeatherBinding
import no.uio.ifi.team16.stim.io.viewModel.MainActivityViewModel

class WeatherFragment : StimFragment() {

    private lateinit var binding: FragmentWeatherBinding
    private val viewModel: MainActivityViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentWeatherBinding.inflate(inflater, container, false)

        val site = viewModel.getCurrentSite()


        return binding.root

    }
}
package no.uio.ifi.team16.stim

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import no.uio.ifi.team16.stim.databinding.FragmentSaltBinding
import no.uio.ifi.team16.stim.io.viewModel.MainActivityViewModel

class SaltFragment : Fragment() {

    private lateinit var binding: FragmentSaltBinding
    private val viewModel: MainActivityViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSaltBinding.inflate(inflater, container, false)

        val site =  viewModel.getCurrentSite()


        return binding.root

    }
}
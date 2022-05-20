package no.uio.ifi.team16.stim.fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import no.uio.ifi.team16.stim.databinding.FragmentAppInfoBinding

class AppInfoFragment : Fragment() {

    private lateinit var binding: FragmentAppInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAppInfoBinding.inflate(inflater, container, false)

        return binding.root
    }

}
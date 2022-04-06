package no.uio.ifi.team16.stim

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import no.uio.ifi.team16.stim.databinding.FragmentGeneralInfoBinding
import no.uio.ifi.team16.stim.io.viewModel.MainActivityViewModel

class GeneralInfoFragment : Fragment() {

    private lateinit var binding: FragmentGeneralInfoBinding
    private val viewModel: MainActivityViewModel by activityViewModels()

    /**
     * Fragment for siden i appen som gir genrell info om sites
     */

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGeneralInfoBinding.inflate(inflater, container, false)

        val site = viewModel.getCurrentSite()

        //header
        binding.header.text = ("OM ${site.name}")

        //bilde

        val imagewidth = 800
        val imageheight = 200

        val info = requireActivity().packageManager.getApplicationInfo(
            requireActivity().packageName,
            PackageManager.GET_META_DATA
        )
        val metadata = info.metaData
        val mapsApiKey = metadata.getString("com.google.android.geo.API_KEY")

        Glide.with(requireActivity())
            .load("http://maps.google.com/maps/api/staticmap?center=${site.latLng.lat},${site.latLng.lng}&zoom=16&size=${imagewidth}x${imageheight}&maptype=satellite&key=$mapsApiKey")
            .placeholder(android.R.drawable.ic_menu_gallery.toDrawable())
            .error(android.R.drawable.ic_menu_gallery.toDrawable())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .dontAnimate()
            .into(binding.imageViewOverview)

        //Vanntemperatur og saltholdighet

        viewModel.loadNorKyst800()

        viewModel.getNorKyst800Data().observe(viewLifecycleOwner) {
            if (it != null) {
                binding.temperatureTextview.text = "${it.getTemperature(site.latLng).toString()}°"
                binding.saltTextview.text = it.getSalinity(site.latLng).toString()
            }
        }

        //posisjon
        binding.posisjonView.text = "${site.latLng.lat}, ${site.latLng.lng}"

        //anleggsnummer
        binding.anleggsnrView.text = site.id.toString()

        //plassering
        binding.plasseringView.text = site.placementType ?: "-----"

        //kapasitet
        binding.kapasitetView.text = site.capacity.toString()

        //vanntype
        binding.vannTypeView.text = site.waterType ?: "-----"

        //kommune
        binding.prodOmraadeView.text = site.placement?.municipalityName ?: "-----"

        return binding.root

    }
}
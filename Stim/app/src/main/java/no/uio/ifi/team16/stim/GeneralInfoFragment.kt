package no.uio.ifi.team16.stim

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
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
            .load("https://maps.google.com/maps/api/staticmap?center=${site.latLong.lat},${site.latLong.lng}&zoom=16&size=${imagewidth}x${imageheight}&maptype=satellite&key=$mapsApiKey")
            .placeholder(android.R.drawable.ic_menu_gallery.toDrawable())
            .error(android.R.drawable.ic_menu_gallery.toDrawable())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .dontAnimate()
            .into(binding.imageViewOverview)

        //Vanntemperatur og saltholdighet

        viewModel.loadNorKyst800AtSite(site)

        viewModel.getNorKyst800AtSiteData(site).observe(viewLifecycleOwner) {
            it?.apply {
                //forecast data is also available in the norkyst object! (about 66 hours, time indexes hours)
                binding.temperatureTextview.text = "%4.1f".format(getTemperature()) + "°"
                binding.saltTextview.text = "%4.1f".format(getSalinity())
            } ?: run {
                binding.temperatureTextview.text = "N/A"
                binding.saltTextview.text = "N/A"
            }
        }

        //posisjon
        binding.posisjonView.text = "${site.latLong.lat}, ${site.latLong.lng}"

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
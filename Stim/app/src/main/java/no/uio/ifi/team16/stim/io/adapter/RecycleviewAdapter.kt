package no.uio.ifi.team16.stim.io.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.ToggleButton
import androidx.recyclerview.widget.RecyclerView
import no.uio.ifi.team16.stim.R
import no.uio.ifi.team16.stim.data.Site
import no.uio.ifi.team16.stim.data.StaticMapImageLoader


class RecycleViewAdapter(
    var sites: List<Site>,
    var favs: List<Site>,
    private val onClick: (Site) -> Unit,
    private val favOnClick: (Site, Boolean) -> Unit,
    val context: Context
) :
    RecyclerView.Adapter<RecycleViewAdapter.ViewHolder>() {

    /**
     * Recycleview som skal ta inn en liste over alle anlegg
     * TODO - vurdere om alle anlegg har plass (?) evt legge til range/neste side eln
     *
     * Benytter seg av layoutene:
     * Recycleview_element.xml
     * Recycleview.xml
     */

    private val TAG = "_RECYCLERVIEW"

    private val imageLoader = StaticMapImageLoader(context)

    /**
     * Oppretter viewholder med alle views i element
     */

    inner class ViewHolder(view: View, val onClick: (Site) -> Unit) : RecyclerView.ViewHolder(view) {
        val nameView: TextView = view.findViewById(R.id.textview_name)
        val locationView: TextView = view.findViewById(R.id.textview_location)
        val pictureView: ImageView = view.findViewById(R.id.imageView_overview)
        val favoriteButton: ToggleButton = view.findViewById(R.id.favoriteButton)

        private var site: Site? = null

        init {
            view.setOnClickListener { site?.let { onClick(it) } }
        }

        fun bind(s: Site) {
            site = s
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.recycleview_element_overview, viewGroup, false)

        return ViewHolder(view, onClick)
    }

    /**
     * Setter data inn i view
     */
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        val site = sites[position]

        viewHolder.bind(site)
        viewHolder.nameView.text = site.name
        viewHolder.locationView.text = "Kommune: " + site.placement?.municipalityName ?: ""

        imageLoader.loadSiteImage(site, viewHolder.pictureView)

        viewHolder.favoriteButton.isChecked = favs.contains(site)

        viewHolder.favoriteButton.setOnClickListener {
            favOnClick(site, viewHolder.favoriteButton.isChecked)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = sites.size
}
package no.uio.ifi.team16.stim.io.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import no.uio.ifi.team16.stim.R
import no.uio.ifi.team16.stim.data.Site
import no.uio.ifi.team16.stim.data.Sites

class RecycleViewAdapter(sites: Sites, private val onClick: (Site) -> Unit) :
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
    private val sites: List<Site> = sites.sites

    /**
     * Oppretter viewholder med alle views i element
     */

    class ViewHolder(view: View, val onClick : (Site) -> Unit) : RecyclerView.ViewHolder(view) {
        val nameView: TextView
        val locationView: TextView
        private var site: Site? = null

        init {
            nameView = view.findViewById(R.id.textview_name)
            locationView = view.findViewById(R.id.textview_location)
            view.setOnClickListener { site?.let { onClick(it) } }
        }

        fun bind(s: Site) { site = s }
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
        viewHolder.locationView.text = site.latLng.toString()

        Log.d(TAG, "data lagt inn")
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = sites.size
}
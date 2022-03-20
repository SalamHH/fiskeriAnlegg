package no.uio.ifi.team16.stim.io.viewModel

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import no.uio.ifi.team16.stim.R
import no.uio.ifi.team16.stim.data.InfectiousPressure
import no.uio.ifi.team16.stim.data.NorKyst800
import no.uio.ifi.team16.stim.data.Sites

class RecycleViewAdapter(
    private val context: Context,
    private var sites: Sites,
    private val infectiousPressure: InfectiousPressure?,
    private val norKyst800: NorKyst800?
) : RecyclerView.Adapter<RecycleViewAdapter.ViewHolder>() {

    /**
     * Recycleview som skal ta inn en liste over alle anlegg
     * TODO - vurdere om alle anlegg har plass (?) evt legge til range/neste side eln
     *
     * Benytter seg av layoutene:
     * Recycleview_element.xml
     * Recycleview.xml
     */

    private val TAG = "RECYCLEVIEW"

    /**
     * Oppretter viewholder med alle views i element
     */

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameView: TextView
        val locationView: TextView
        //val infectiousPressureView: TextView
        //val velocityView: TextView
        //val salinityView: TextView

        init {
            // Define click listener for the ViewHolder's View.
            nameView = view.findViewById(R.id.textview_name)
            locationView = view.findViewById(R.id.textview_location)
            //latitudeView = view.findViewById(R.id.textView_latitude)
            //infectiousPressureView = view.findViewById(R.id.textView_infectiousPressure)
            //velocityView = view.findViewById(R.id.textView_velocity)
            //salinityView = view.findViewById(R.id.textView_salinity)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.recycleview_element_overview, viewGroup, false)

        return ViewHolder(view)
    }

    /**
     * Setter data inn i view
     */

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val site = sites.sites[position]
        viewHolder.nameView.text = site.name
        viewHolder.locationView.text = site.latLng.toString()
        //set norkyst800, or infectiouspressure can be set here to some view

        Log.d(TAG, "data lagt inn")
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = sites.sites.size
}
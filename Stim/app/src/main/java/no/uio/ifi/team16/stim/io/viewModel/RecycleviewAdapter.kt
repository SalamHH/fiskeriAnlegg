package no.uio.ifi.team16.stim.io.viewModel

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import no.uio.ifi.team16.stim.R
import no.uio.ifi.team16.stim.data.Site

class RecycleViewAdapter(val sites: List<Site>) :
    RecyclerView.Adapter<RecycleViewAdapter.ViewHolder>() {

    /**
     * Recycleview som skal ta inn en liste over alle anlegg
     * TODO - vurdere om alle anlegg har plass (?) evt legge til range/neste side eln
     *
     * Benytter seg av layoutene:
     * Recycleview_element_overview.xml
     * Recycleview.xml
     */

    private val TAG = "RECYCLEVIEW_TEST: "

    /**
     * Oppretter viewholder med alle views i element
     * TODO - sette inn location i textview når det er implementert
     */

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView

        //val location: TextView
        val image: ImageView

        init {
            name = view.findViewById(R.id.textview_name)
            image = view.findViewById(R.id.imageView_overview)
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
     * //TODO - legge inn location i dataklassen
     */

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        viewHolder.name.text = sites.get(position).name
        //viewHolder.location.text = dummyList.get(position).location

        Log.d(TAG, "data lagt inn")
    }

    // Return the size of your dataset (invoked by the layout manager)

    override fun getItemCount() = sites.size
}
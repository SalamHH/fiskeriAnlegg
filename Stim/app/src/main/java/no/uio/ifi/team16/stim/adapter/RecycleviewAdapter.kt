package no.uio.ifi.team16.stim.model.viewModel

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import no.uio.ifi.team16.stim.R
import no.uio.ifi.team16.stim.data.InfectiousPressure
import ucar.nc2.NCdumpW

class RecycleViewAdapter(var infectpressDataset: InfectiousPressure?) :
    RecyclerView.Adapter<RecycleViewAdapter.ViewHolder>() {

    /**
     * Recycleview som (til nå) tar inn InfectiousPressure
     *
     * For å vise info fra flere anlegg i en liste
     *
     * Benytter seg av layoutene:
     * Recycleview_element.xml
     * Recycleview.xml
     */

    private val TAG = "RECYCLEVIEW_TEST: "

    /**
     * Oppretter viewholder. Bruker bare textview for å gjøre d enkelt (for nå)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewTest: TextView

        init {
            // Define click listener for the ViewHolder's View.
            textViewTest = view.findViewById(R.id.textView_test)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.recycleview_element, viewGroup, false)

        return ViewHolder(view)
    }

    /**
     * Metode som setter inn data
     *
     * /TODO - metode tar inn kordinater til hvilke grids den skal vise
     * /TODO - metoder i InfectiousPressurse som returnerer info (mer enn toString)
     */
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        //Setter til consentration
        viewHolder.textViewTest.text = NCdumpW.toString(infectpressDataset?.concentration)

        Log.d(TAG, "data lagt inn")
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        if (infectpressDataset?.concentration?.size == null) {
            return 0
        }
        return infectpressDataset!!.concentration.size.toInt()
    }
}
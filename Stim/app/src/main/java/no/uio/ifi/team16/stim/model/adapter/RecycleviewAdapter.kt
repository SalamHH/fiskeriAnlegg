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
     * Dummy data for å teste å ta inn kordinater fra et oppdrettsanlegg og vise smittetallet derifra
     */

    data class DummyAnlegg(
        var nr: Int,
        var navn: String,
        var latitude: Double,
        var longitude: Double
    )

    val dummyAnlegg1 = DummyAnlegg(0, "TUHOLMANE Ø", 59.371233, 5.216333)
    val dummyAnlegg2 = DummyAnlegg(1, "TJAJNELUOKTA", 67.892433, 16.236718)
    val dummyAnlegg3 = DummyAnlegg(2, "JØRSTADSKJERA", 59.2955, 5.938617)

    val dummyList: List<DummyAnlegg> = listOf(dummyAnlegg1, dummyAnlegg2, dummyAnlegg3)

    /**
     * Metode som setter inn data
     *
     * /TODO - metode tar inn kordinater til hvilke grids den skal vise
     * /TODO - metoder i InfectiousPressurse som returnerer info (mer enn toString)
     */
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {


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
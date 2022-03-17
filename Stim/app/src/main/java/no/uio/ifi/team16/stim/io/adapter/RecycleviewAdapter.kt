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
     * Dummy data for å teste
     */

    private data class DummySite(
        var nr: Int,
        var name: String,
        var latitude: Double,
        var longitude: Double
    )

    private val dummyAnlegg1 = DummySite(0, "TUHOLMANE Ø", 59.371233, 5.216333)
    private val dummyAnlegg2 = DummySite(1, "TJAJNELUOKTA", 67.892433, 16.236718)
    private val dummyAnlegg3 = DummySite(2, "JØRSTADSKJERA", 59.2955, 5.938617)

    private val dummyList: List<DummySite> = listOf(dummyAnlegg1, dummyAnlegg2, dummyAnlegg3)



    /**
     * Oppretter viewholder med alle views i element
     */

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameView: TextView
        val longitudeView: TextView
        val latitudeView: TextView
        val infectiousPressureView: TextView
        val velocityView: TextView
        val salinityView: TextView

        init {
            // Define click listener for the ViewHolder's View.
            nameView = view.findViewById(R.id.textView_name)
            longitudeView = view.findViewById(R.id.textView_longitude)
            latitudeView = view.findViewById(R.id.textView_latitude)
            infectiousPressureView = view.findViewById(R.id.textView_infectiousPressure)
            velocityView = view.findViewById(R.id.textView_velocity)
            salinityView = view.findViewById(R.id.textView_salinity)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.recycleview_element, viewGroup, false)

        return ViewHolder(view)
    }

    /**
     * Setter data inn i view
     * //TODO - endre fra dummy data når ekte data er tilgjenlig
     */

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.nameView.text = sites.sites.get(position).name
        val latLng = sites.sites.get(position).latLng
        viewHolder.latitudeView.text = latLng.lat.toString()
        viewHolder.longitudeView.text = latLng.lng.toString()
        viewHolder.infectiousPressureView.text =
            infectiousPressure?.getConcentration(latLng).toString()
        viewHolder.velocityView.text =
            norKyst800?.getVelocity(latLng)?.first.toString() //TODO second, third in other views
        viewHolder.salinityView.text =
            norKyst800?.getSalinity(latLng).toString()

        Log.d(TAG, "data lagt inn")
    }

    // Return the size of your dataset (invoked by the layout manager)
    //TODO - endre fra dummy data når ekte data er tilgjenlig
    override fun getItemCount() = sites.sites.size
}
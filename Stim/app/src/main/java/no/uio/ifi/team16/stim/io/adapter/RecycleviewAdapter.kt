package no.uio.ifi.team16.stim.io.adapter

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.FutureTarget
import no.uio.ifi.team16.stim.R
import no.uio.ifi.team16.stim.data.Site
import no.uio.ifi.team16.stim.data.Sites


class RecycleViewAdapter(
    var sites: Sites,
    private val onClick: (Site) -> Unit,
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

    /**
     * Oppretter viewholder med alle views i element
     */

    class ViewHolder(view: View, val onClick : (Site) -> Unit) : RecyclerView.ViewHolder(view) {
        val nameView: TextView
        val locationView: TextView
        val pictureView: ImageView
        private var site: Site? = null

        init {
            nameView = view.findViewById(R.id.textview_name)
            locationView = view.findViewById(R.id.textview_location)
            pictureView = view.findViewById(R.id.imageView_overview)
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
        val info = context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        )
        val metadata = info.metaData
        val mapsApiKey = metadata.getString("com.google.android.geo.API_KEY")

        val site = sites.sites[position]

        viewHolder.bind(site)
        viewHolder.nameView.text = site.name
        viewHolder.locationView.text = site.latLong.toString()

        val imagewidth = 800
        val imageheight = 200

        Glide.with(context)
            .load("http://maps.google.com/maps/api/staticmap?center=${site.latLong.lat},${site.latLong.lng}&zoom=16&size=${imagewidth}x${imageheight}&maptype=satellite&key=$mapsApiKey")
            .placeholder(android.R.drawable.ic_menu_gallery.toDrawable())
            .error(android.R.drawable.ic_menu_gallery.toDrawable())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .dontAnimate()
            .into(viewHolder.pictureView)

        Log.d(TAG, "data lagt inn")
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = sites.sites.size
}
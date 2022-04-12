package no.uio.ifi.team16.stim.io.adapter

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import no.uio.ifi.team16.stim.R
import no.uio.ifi.team16.stim.data.Site


class RecycleViewAdapter(
    var sites: List<Site>,
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

    inner class ViewHolder(view: View, val onClick: (Site) -> Unit) : RecyclerView.ViewHolder(view) {
        val nameView: TextView
        val locationView: TextView
        val pictureView: ImageView
        val favoriteButton: Button

        private var site: Site? = null

        init {
            nameView = view.findViewById(R.id.textview_name)
            locationView = view.findViewById(R.id.textview_location)
            pictureView = view.findViewById(R.id.imageView_overview)
            favoriteButton = view.findViewById(R.id.favoriteButton)
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
        viewHolder.locationView.text = site.latLong.toString()

        Glide.with(context)
            .load(getImageUrl(site))
            .placeholder(android.R.drawable.ic_menu_gallery.toDrawable())
            .error(android.R.drawable.ic_menu_gallery.toDrawable())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .dontAnimate()
            .listener(
                object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e(TAG, "failed to load image")
                        return true
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d(TAG, "Succesfully loaded image")
                        return false
                    }
                }
            )
            .into(viewHolder.pictureView)

        Log.d(TAG, "data lagt inn")

        viewHolder.favoriteButton.setOnClickListener {
            //find viewmodel, and use its registerFavourtieSite(site)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = sites.size

    private val mapsApiKey by lazy {
        val info = context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        )
        info.metaData.getString("com.google.android.geo.API_KEY")
    }

    /**
     * Hent URL til bilde av site
     */
    private fun getImageUrl(site: Site): String {

        val imagewidth = 1000
        val imageheight = 1000

        return Uri.parse("https://maps.google.com/maps/api/staticmap").buildUpon().apply {
            appendQueryParameter("center", "${site.latLong.lat},${site.latLong.lng}")
            appendQueryParameter("zoom", "16")
            appendQueryParameter("size", "${imagewidth}x${imageheight}")
            appendQueryParameter("maptype", "satellite")
            appendQueryParameter("key", mapsApiKey)
        }.toString()
    }
}
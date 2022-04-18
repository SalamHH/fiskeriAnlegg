package no.uio.ifi.team16.stim.data

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.ImageView
import androidx.core.graphics.drawable.toDrawable
import com.bumptech.glide.Glide

/**
 * Henter bilder av sites fra Google Maps Static API
 */
class StaticMapImageLoader(private val context: Context) {

    /**
     * Last inn bilde av en gitt site til et gitt imageview
     */
    fun loadSiteImage(site: Site, imageView: ImageView) {
        Glide.with(context)
            .load(getImageUrl(site))
            .placeholder(android.R.drawable.ic_menu_gallery.toDrawable())
            .error(android.R.drawable.ic_menu_gallery.toDrawable())
            .dontAnimate()
            .into(imageView)
    }

    /**
     * API-nøkkel til Google Maps
     */
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

        return Uri.parse("https://maps.google.com/maps/api/staticmap").buildUpon().apply {
            appendQueryParameter("center", "${site.latLong.lat},${site.latLong.lng}")
            appendQueryParameter("zoom", "16")
            appendQueryParameter("size", "${IMAGE_WIDTH}x${IMAGE_HEIGHT}")
            appendQueryParameter("maptype", "satellite")
            appendQueryParameter("key", mapsApiKey)
        }.toString()
    }

    companion object {
        private const val IMAGE_WIDTH = 800
        private const val IMAGE_HEIGHT = 300
    }
}
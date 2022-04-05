package no.uio.ifi.team16.stim.util

import org.locationtech.proj4j.CoordinateTransform
import org.locationtech.proj4j.ProjCoordinate

/**
 * Extensions for CoordinateTransform to project from latlngs to indexes(when rounded!) in a grid
 */

fun CoordinateTransform.project(lat: Float, lng: Float): Pair<Float, Float> =
    transform(ProjCoordinate(lng.toDouble(), lat.toDouble()), ProjCoordinate(0.0, 0.0)).let { p ->
        Pair(p.y.toFloat(), p.x.toFloat())
    }

fun CoordinateTransform.project(latLng: LatLong): Pair<Float, Float> =
    project(latLng.lat.toFloat(), latLng.lng.toFloat())

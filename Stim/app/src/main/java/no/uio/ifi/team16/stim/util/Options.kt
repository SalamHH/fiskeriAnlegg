package no.uio.ifi.team16.stim.util

import no.uio.ifi.team16.stim.data.Site

class Options {
    companion object {
        //INFECTIOUSPRESSURE - DATALOADER
        const val infectiousPressureStepX =
            50 //amount of steps between data points, 1=use entire x axis
        const val infectiousPressureStepY =
            50 //amount of steps between data points, 1=use entire y axis

        //SITES - DATALOADER
        const val sitesRange = "0-99"

        //MAINACTIVITY
        const val fakeMunicipality = 4615
        val fakeSite = Site(0, "bingbong", LatLng(61.12341234, 4.23), null)
    }
}
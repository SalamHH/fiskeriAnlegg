package no.uio.ifi.team16.stim.util

import no.uio.ifi.team16.stim.data.Site

class Options {
    companion object {
        //INFECTIOUSPRESSURE - DATALOADER
        const val infectiousPressureStepX =
            50 //amount of steps between data points, 1=use entire x axis
        const val infectiousPressureStepY =
            50 //amount of steps between data points, 1=use entire y axis

        //InfectiousPressureTimeseries
        const val infectiousPressureTimeSeriesSpan =
            8 //how many weeks from now to load in a timeseries

        //SITES - DATALOADER
        const val sitesRange = "0-99"

        //MAINACTIVITY
        const val initialMunicipality = "4615"
        val fakeSite = Site(420, "fakevik", LatLong(1.0, 2.0), null, 3.141592, null, null)
        val initialFavouriteSites: MutableList<Site> = mutableListOf()
    }
}
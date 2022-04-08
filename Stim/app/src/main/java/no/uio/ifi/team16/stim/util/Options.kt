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
        const val fakeMunicipality = "4615"
        val fakeSite = Site(-1, "BingBong", LatLong(61.12341234, 4.23), null, 1.0, null, null)
        private val skjetve = Site(0, "Skjetve", LatLong(61.12341234, 4.23), null, 1.0, null, null)
        private val spikkestad =
            Site(1, "Spikkestad", LatLong(54.12234, 6.73), null, 2.0, null, null)
        private val foldal = Site(2, "Foldal", LatLong(62.1412341, 5.28), null, 3.0, null, null)
        private val helvete = Site(3, "Helvete", LatLong(66.6, 4.20), null, 4.0, null, null)
        val initialFavouriteSites =
            mutableListOf(skjetve, spikkestad, foldal, helvete) //SHOULD ACTUALLY BE EMPTY!
    }
}
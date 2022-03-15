package no.uio.ifi.team16.stim.data.dataLoader

import no.uio.ifi.team16.stim.data.Site
import no.uio.ifi.team16.stim.data.Sites

/**
 * Load Sites
 */
class SitesDataLoader {
    protected val url: String = "https://api.fiskeridir.no/pub-aqua/api/v1/sites?range=0-9"

    //load some data TODO figure out slicing
    fun load(): Sites? = mockLoad()


    private fun mockLoad(): Sites {
        return Sites(
            listOf(
                Site(0, "Bingbong", 24.024409, 32.1234124),
                Site(34, "Skiptvet", 3.1234234, 9.34234),
                Site(666, "Helvete", 6.666, 6.666),
            )
        )
    }
}
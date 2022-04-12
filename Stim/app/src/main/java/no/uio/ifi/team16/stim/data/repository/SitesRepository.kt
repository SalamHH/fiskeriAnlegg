package no.uio.ifi.team16.stim.data.repository

import no.uio.ifi.team16.stim.data.Municipality
import no.uio.ifi.team16.stim.data.Site
import no.uio.ifi.team16.stim.data.dataLoader.SitesDataLoader
import no.uio.ifi.team16.stim.util.Options

/**
 * Repository for sites.
 */
class SitesRepository {

    private val TAG = "SitesRepository"
    private val dataSource = SitesDataLoader()

    /**
     * Maps municipality-code to site list
     */
    private val municipalityCache: MutableMap<String, Municipality?> = mutableMapOf()

    /**
     * Maps sitename to site object
     */
    private val nameCache: MutableMap<String, Site?> = mutableMapOf()

    //todo: store as int then load? sitedata on memory might be incorrect
    var favouriteSites: MutableList<Site> = mutableListOf()

    /**
     * load the municipality at the given municipalitycode
     */
    suspend fun getMunicipality(municipalityCode: String): Municipality? {
        var municipality = municipalityCache[municipalityCode]
        if (municipality != null) {
            return municipality
        }
        municipality = dataSource.loadMunicipality(municipalityCode)

        if(municipality != null) {
            municipalityCache[municipalityCode] = municipality

            for(site in municipality.sites) {
                nameCache[site.name] = site
            }
        }

        return municipality
    }

    suspend fun getFavouriteSites(): List<Site>? {
        // TODO: load from memory
        return Options.initialFavouriteSites
    }

    /**
     * Load the site with the given name
     */
    suspend fun getDataByName(name: String): Site? {
        var site = nameCache[name]
        if (site != null) {
            return site
        }
        site = dataSource.loadDataByName(name)
        nameCache[name] = site
        return site
    }
}
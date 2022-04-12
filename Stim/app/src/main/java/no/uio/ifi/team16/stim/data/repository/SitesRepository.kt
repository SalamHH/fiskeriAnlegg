package no.uio.ifi.team16.stim.data.repository

import no.uio.ifi.team16.stim.data.Sites
import no.uio.ifi.team16.stim.data.dataLoader.SitesDataLoader

/**
 * Repository for sites.
 *
 * The cache maps a municipalitynumber to a collection of sites
 */
class SitesRepository {
    private val TAG = "SitesRepository"
    private val dataSource = SitesDataLoader()

    /**
     * Maps municipality-code to site list
     */
    private val municipalityCache: MutableMap<String, Sites?> = mutableMapOf()

    /**
     * Maps sitename to site object
     * TODO: Fix this to be a Map<String, Site>
     */
    private val nameCache: MutableMap<String, Sites?> = mutableMapOf()

    /**
     * load the sites at the given municipalitycode
     */
    suspend fun getData(municipalityCode: String): Sites? {
        var sites = municipalityCache[municipalityCode]
        if (sites != null) {
            return sites
        }
        sites = dataSource.loadDataByMunicipalityCode(municipalityCode)

        if (sites != null) {
            municipalityCache[municipalityCode] = sites
            for (site in sites.sites) {
                // todo this makes no sense
                nameCache[site.name] = sites
            }
        }

        return sites
    }

    /**
     * Load the site with the given name
     */
    suspend fun getDataByName(name: String): Sites? {
        var sites = nameCache[name]
        if (sites != null) {
            return sites
        }
        sites = dataSource.loadDataByName(name)
        nameCache[name] = sites
        return sites
    }
}
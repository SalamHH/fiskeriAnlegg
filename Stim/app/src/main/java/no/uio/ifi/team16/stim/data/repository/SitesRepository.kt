package no.uio.ifi.team16.stim.data.repository

import no.uio.ifi.team16.stim.data.Municipality
import no.uio.ifi.team16.stim.data.Site
import no.uio.ifi.team16.stim.data.dataLoader.SitesDataLoader

/**
 * Repository for municipality.
 *
 * The cache maps a municipalitynumber to a collection of municipality
 */
class SitesRepository {
    private val TAG = "SitesRepository"
    private val dataSource = SitesDataLoader()

    /**
     * Maps municipality-code to site list
     */
    private val municipalityCache: MutableMap<String, Municipality?> = mutableMapOf()

    /**
     * Maps sitename to list of sites
     */
    private val nameCache: MutableMap<String, MutableSet<Site>?> = mutableMapOf()

    /**
     * Maps sitenr to site object
     */
    private val siteNrCache: MutableMap<Int, Site?> = mutableMapOf()

    /**
     * load the municipality at the given municipalitycode
     */
    suspend fun getMunicipality(municipalityCode: String): Municipality? {
        var municipality = municipalityCache[municipalityCode]
        if (municipality != null) {
            return municipality
        }
        municipality = dataSource.loadDataByMunicipalityCode(municipalityCode)

        if(municipality != null) {
            municipalityCache[municipalityCode] = municipality
            for(site in municipality.sites) {
                siteNrCache[site.nr] = site
            }
        }
        return municipality
    }

    suspend fun getFavouriteSites(favourites: Set<String>?): MutableList<Site> {
        val list = mutableListOf<Site>()
        favourites?.forEach { siteNr ->
            val loadedSite = getSiteByNr(siteNr.toInt())
            if (loadedSite != null) list.add(loadedSite)
        }
        return list
    }

    suspend fun getSitesByName(name: String): List<Site>? {
        val sites = nameCache[name]
        if (sites != null) {
            return sites.toList()
        }
        val sitesList = dataSource.loadSitesByName(name)

        if (sitesList != null) {
            nameCache[name] = sitesList.toMutableSet()

            // lagre sites i nr-cache også
            for (site in sitesList) {
                siteNrCache[site.nr] = site
            }
        }

        return sitesList
    }

    private suspend fun getSiteByNr(nr: Int): Site? {
        var site = siteNrCache[nr]
        if (site != null) {
            return site
        }
        site = dataSource.loadDataByNr(nr.toString())
        siteNrCache[nr] = site

        return site
    }
}
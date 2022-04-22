package no.uio.ifi.team16.stim.data.repository

import android.util.Log
import no.uio.ifi.team16.stim.data.Municipality
import no.uio.ifi.team16.stim.data.Site
import no.uio.ifi.team16.stim.data.dataLoader.SitesDataLoader
import no.uio.ifi.team16.stim.util.Options

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
     * Maps sitename to site object
     */
    private val nameCache: MutableMap<String, Site?> = mutableMapOf()
    private val nameListCache: MutableMap<String, List<Site>?> = mutableMapOf()//ny


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
                nameCache[site.name] = site
            }
        }
        return municipality
    }

    suspend fun getFavouriteSites(favourites: Set<String>?): MutableList<Site> {
        val list = mutableListOf<Site>()
        favourites?.forEach { siteName ->
            val loadedSite = getDataByName(siteName)
            if (loadedSite != null) list.add(loadedSite)
        }
        return list
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



    suspend fun getSitesByName(name: String): List<Site>? {
        var site = nameListCache[name]
        if (site != null) {
            return site
        }
        site = dataSource.loadSitByName(name)
        nameListCache[name] = site
        return site
    }
}
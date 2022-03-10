package no.uio.ifi.team16.stim.data

import android.util.Log
import java.io.IOException;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayFloat
import ucar.ma2.InvalidRangeException
import ucar.nc2.Variable
import ucar.nc2.dataset.NetcdfDataset

/*
* Dataloader for infectious pressure data.
* TODO: further documentation
* TODO: refactor to gridDataset(throws same errors as original solution...)
* */
class InfectiousPressureDataloader {
    private val TAG          = "InfectiousPressureDataloader"
    private val logger       = LoggerFactory.getLogger(this.javaClass)
    private val url : String = "http://thredds.nodc.no:8080/thredds/fileServer/smittepress_new2018/agg_OPR_2022_9.nc"

    protected val maxX       = 902  //TODO: read from data
    protected val maxY       = 2602 // -..-
    protected val separation = 800  //separation(in meters) between points(in real life)
    protected val steps      = 100  //increment between datapoints TODO: supply by user

    protected val startX = 0
    protected val stopX  = maxX
    protected val stepX  = steps

    protected val startY = 0
    protected val stopY  = maxY
    protected val stepY  = steps

    /**
     * load infectious pressure
     *
     * OUT:
     * if data was successfully loaded, a InfectiousPressure object, otherwise null
     */
    fun load() : InfectiousPressure? {
        var out : InfectiousPressure? = null //TODO: clean up nulls
        Log.d("TRYING TO OPEN", url)
            try {
                    Log.d("OPENING", url)
                    val ncfile = NetcdfDataset.openDataset(url).let { ncfile ->
                        // Do cool stuff here
                        println("SUCCESS - OPENDAP URL OPENED")
                        println(ncfile)

                        //lets make some infectious pressure
                        //Variables are data that are NOT READ YET
                        val concentrations: Variable = ncfile.findVariable("C10")?:return null
                        val eta_rhos: Variable = ncfile.findVariable("eta_rho")?:return null
                        val xi_rhos: Variable = ncfile.findVariable("xi_rho")?:return null
                        val lat: Variable = ncfile.findVariable("lat")?:return null
                        val lon: Variable = ncfile.findVariable("lon")?:return null
                        val time : Variable = ncfile.findVariable("time")?:return null
                        val gridMapping: Variable = ncfile.findVariable("grid_mapping")?:return null

                        //ranges have format start:END:STEP (which of course, is different from opendap:))
                        val readRangeX = "${startX}:${stopX}:${stepX}"
                        val readRangeY = "${startY}:${stopY}:${stepY}"
                        val readRange2 = "$readRangeX,$readRangeY"
                        val readRange3 = "0,$readRange2"

                        // note that this way of reading does not apply scale or offset
                        // see variable attributes "scale_factor" and "add_offset"
                        //TODO: change unsafe casts
                        out = InfectiousPressure(
                            concentrations.read(readRange3) as ArrayFloat,
                            eta_rhos.read(readRangeX) as ArrayFloat,
                            xi_rhos.read(readRangeY) as ArrayFloat,
                            lat.read(readRange2) as ArrayFloat,
                            lon.read(readRange2) as ArrayFloat,
                            time.readScalarFloat(),
                            gridMapping.readScalarInt()
                        )

                        ncfile.close()
                        Log.d("DONE", url)
                    }
            } catch (e: IOException) {
                // Handle less-cool exceptions here
                logger.error("ERROR", e)
            } catch (e: InvalidRangeException) {
                logger.error("ERROR", e)
            } catch (e : NullPointerException) {
                Log.e(TAG, "ERROR: a Variable might be read as null, are you sure you are using the cirrect url/dataset?")
                logger.error("ERROR", e)
            }

            NetcdfDataset.shutdown()
            Log.d(TAG, " load - DONE")
            return out
    }
}
package no.uio.ifi.team16.stim.data

import android.util.Log
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDatasets;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.write.Ncdump;
import ucar.unidata.util.Format.d

/**
 * DATA FORMAT:
 * Dataset {
Grid {
ARRAY:
Float32 C10[time = 1][eta_rho = 902][xi_rho = 2602];
MAPS:
Float64 time[time = 1];
Float32 eta_rho[eta_rho = 902];
Float32 xi_rho[xi_rho = 2602];
} C10;
Float32 eta_rho[eta_rho = 902];
Int32 grid_mapping;
Grid {
ARRAY:
Float32 lat[eta_rho = 902][xi_rho = 2602];
MAPS:
Float32 eta_rho[eta_rho = 902];
Float32 xi_rho[xi_rho = 2602];
} lat;
Grid {
ARRAY:
Float32 lon[eta_rho = 902][xi_rho = 2602];
MAPS:
Float32 eta_rho[eta_rho = 902];
Float32 xi_rho[xi_rho = 2602];
} lon;
Float64 time[time = 1];
Float32 xi_rho[xi_rho = 2602];
} smittepress_new2018/agg_OPR_2022_9.nc;
 */

/*
* Dataloader for infectious pressure data.
*
* Non-functional. The examples on mets coursepage DONT WORK!
*
* Not using the prefix gives a NoSuchFieldError. Seems to be an error in httpsrequest
*
* Using the prefix requires url with a catalog.xml#<id> in it, which the urls we use dont have
* */
class InfectiousPressureDataloader {
    protected val logger = LoggerFactory.getLogger(this.javaClass)
    //TODO: the url is not correct, and might need the 'thredds:' prefix.
    protected val url : String = "thredds://thredds.met.no/thredds/catalog/fou-hi/norkyst800m-1h/catalog.xml#norkyst800m_1h_files/NorKyst-800m_ZDEPTHS_his.fc.2022031000.nc"
                               //"thredds:thredds.met.no/thredds/dodsC/fou-hi/norkyst800m-1h/NorKyst-800m_ZDEPTHS_his.fc.2022031000.nc.ascii?X%5B0:1:10%5D,Y%5B0:1:10%5D"

    fun load() : InfectiousPressure? {
        Log.d("TRYING TO OPEN", url)
        try {
            NetcdfDatasets.openFile(url, null).use { ncfile ->
                Log.d("SUCCESS","OPENDAP URL OPENED")
                Log.d("GOT:", ncfile.toString())

                //val v: Variable = ncfile.findVariable("temperature")
                //if (v == null) return
                // direct indexing (ranges)
                // note that this way of reading does not apply scale or offset
                // see variable attributes "scale_factor" and "add_offset"
                // the argument for read(...) is specifying a range of data per dimension (order is t, z, y, x)
                //val data: Array = v.read("2,0:2,200:203,199")
                //val arrayStr: String = Ncdump.printArray(data, "temperature_selection", null)
                //println(arrayStr)

                ncfile.close()
            }
        } catch (e: IOException) {
            // Handle less-cool exceptions here
            logger.error("ERROR", e)
        } catch (e: InvalidRangeException) {
            logger.error("ERROR", e)
        }
        NetcdfDatasets.shutdown()
        return null
    }
}
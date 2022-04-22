package no.uio.ifi.team16.stim.data.dataLoader.parser

import android.util.Log
import no.uio.ifi.team16.stim.data.NorKyst800
import no.uio.ifi.team16.stim.util.NullableDoubleArray4D

/**
 * Welcome to regex hell!!
 */
class NorKyst800RegexParser {
    val TAG = "NORKYST800PARSER"

    ///////////////////////
    // REGEXES (REGEXI?) //
    ///////////////////////
    //finds attribute data start, captures dimensions(group 0,1,2,3) and data(group 4)
    //only read the attribute itself, not the mappings included. i.e. starts with attribute.attribute
    private val dataRegex: (String) -> Regex = { str ->
        Regex("${str}\\.${str}(?:\\[(.*?)\\])(?:\\[(.*?)\\])(?:\\[(.*?)\\])(?:\\[(.*?)\\])\\n((?:.*\\n)+?)\\n")
    }

    //parse 1D data, which has a slightly different format
    private val dataRegex1D: (String) -> Regex = { str ->
        Regex("$str(?:\\[(.*?)\\])?(?:\\[(.*?)\\])?(?:\\[(.*?)\\])?(?:\\[(.*?)\\])?\\n(.*\\n)*?\\n")
    }

    //finds start of a row, captures the row contents
    private val arrayRowRegex = Regex("""(?:\[.*?\]\[.*?\]\[.*?\])?(?:, )?(.+?)\n""")

    //capture a single entry (in a row)
    private val entryRegex = Regex(""" ?(.+?)(?:,|$)""")

    /////////////
    // PARSING //
    /////////////
    /**
     * Take an ascii response from the norkystdataset and parse it into a norkyst-object
     *
     * @param response ascii response from the norjkyst dataset.
     * @return corresponding NorKyst800 object.
     */
    fun parse(response: String): NorKyst800? {
        // midlertidig fix pga. koden under er litt treg :-)
        //if (BuildConfig.DEBUG) return null
        //get filler values TODO get from data itself, fram .dds
        val salinityFillValue = -32767
        val temperatureFillValue = -32767
        val uFillValue = -32767
        val vFillValue = -32767
        val wFillValue = 1.0E37


        val depth = make1DDoubleArrayOf("depth", response) ?: run {
            Log.e(TAG, "Failed to read <depth> from NorKyst800")
            return@parse null
        }
        val salinity =
            make4DDoubleArrayOf("salinity", response, 0.001, 30.0, salinityFillValue) ?: run {
                Log.e(TAG, "Failed to read <salinity> from NorKyst800")
                return@parse null
            }
        val temperature =
            make4DDoubleArrayOf("temperature", response, 0.01, 0.0, temperatureFillValue) ?: run {
                Log.e(TAG, "Failed to read <temperature> from NorKyst800")
                return@parse null
            }
        val time = make1DDoubleArrayOf("time", response) ?: run {
            Log.e(TAG, "Failed to read <time> from NorKyst800")
            return@parse null
        }
        val velocity = Triple(
            NullableDoubleArray4D(
                make4DDoubleArrayOf("u", response, 0.001, 0.0, uFillValue) ?: run {
                    Log.e(TAG, "Failed to read <u> from NorKyst800")
                    return@parse null
                },
                uFillValue.toDouble()
            ),
            NullableDoubleArray4D(
                make4DDoubleArrayOf("v", response, 0.001, 0.0, vFillValue) ?: run {
                    Log.e(TAG, "Failed to read <v> from NorKyst800")
                    return@parse null
                },
                vFillValue.toDouble()
            ),
            NullableDoubleArray4D(
                make4DDoubleArrayOfW("w", response, 1.0, 0.0, wFillValue) ?: run {
                    Log.e(TAG, "Failed to read <w> from NorKyst800")
                    return@parse null
                },
                wFillValue
            )
        )

        return NorKyst800(
            depth,
            NullableDoubleArray4D(salinity, salinityFillValue.toDouble()),
            NullableDoubleArray4D(temperature, temperatureFillValue.toDouble()),
            time,
            velocity
        )
    }

    /**
     * Thredds stores a float as a special value if the value is actually nan,
     * For this particular dataset, the value is the fillvalue -32767.
     * Fill value varies from dataset to dataset, and variable to variable, so this is actually not safe!
     * But it is impossible to find from the ascii response alone
     */
    private fun isTHREDDSFiller(unscaledValue: Int): Boolean = (unscaledValue == -32767)

    /**
     * try to parse out a 4D intarray from an ascii opendap response,
     * returns null if any parsing fails.
     * The array contains null where the data is not available(ie where there are filler values)
     */
    private fun make4DDoubleArrayOf(
        attribute: String,
        response: String,
        scaleFactor: Double,
        offset: Double,
        fillValue: Int
    ): Array<Array<Array<DoubleArray>>>? =
        dataRegex(attribute).find(response, 0)?.let { match ->
            //parse dimensions
            val dT = match.groupValues.getOrNull(1)?.toInt() ?: run {
                Log.e(TAG, "Failed to read <time-dimension-size> from 4DArray")
                return null
            }
            val dD = match.groupValues.getOrNull(2)?.toInt() ?: run {
                Log.e(TAG, "Failed to read <depth-dimension-size> from 4DArray")
                return null
            }
            val dY = match.groupValues.getOrNull(3)?.toInt() ?: run {
                Log.e(TAG, "Failed to read <y-dimension-size> from 4DArray")
                return null
            }
            val dX = match.groupValues.getOrNull(4)?.toInt() ?: run {
                Log.e(TAG, "Failed to read <x-dimension-size> from 4DArray")
                return null
            }
            //parse the data
            val dataString = match.groupValues.getOrNull(5) ?: run {
                Log.e(TAG, "Failed to read <data-section> from 4DArray")
                return null
            }

            val dataSequence =
                readRowsOf4DIntArray(dataString, scaleFactor, offset, fillValue.toDouble())
            //we have Sequence<Sequence<Int>>, where the inner sequence is a row.
            //now, reshape it to Array<Array<Array<FloatArray>>>
            Array(dT) { ti ->
                Array(dD) { di ->
                    Array(dY) { yi ->
                        DoubleArray(dX) { xi ->
                            dataSequence.elementAt(ti * dD * dY + di * dY + yi)
                                .elementAt(xi) //element is guaranteed to exist(if indexing done properly!)
                        }
                    }
                }
            }
        }

    /**
     * try to parse out a 1D DoubleArray from an ascii opendap response,
     * returns null if any parsing fails.
     */
    private fun make1DDoubleArrayOf(attribute: String, response: String): DoubleArray? =
        dataRegex1D(attribute).find(response, 0)?.let { match ->
            //parse dimensions
            val dT = match.groupValues.getOrNull(1)?.toInt() ?: run {
                Log.e(TAG, "Failed to read <dimension-size> from 1DArray")
                return null
            }
            //parse the data
            val dataString = match.groupValues.getOrNull(5) ?: run {
                Log.e(TAG, "Failed to read <data-section> from 1DArray")
                return null
            }
            //match all rows, for each one parse out entries
            val dataSequence = arrayRowRegex.findAll(dataString, 0).map { rowMatch ->
                entryRegex.findAll(rowMatch.groupValues.getOrElse(1) { "" }, 0).map { entryMatch ->
                    entryMatch.groupValues.getOrNull(1)?.toDouble()
                }
            }

            DoubleArray(dT) { id ->
                dataSequence.elementAtOrNull(0)?.elementAtOrNull(id) ?: run {
                    Log.e(TAG, "Failed to read an entry in data-section from 1DArray")
                    return null
                }
            }
        }


    /**
     * for some reason the velocity w variable has a double as a fillervalue,
     * so we have to make an entirely separate function for it. Yay!
     */
    private fun make4DDoubleArrayOfW(
        attribute: String,
        response: String,
        scaleFactor: Double,
        offset: Double,
        fillValue: Double
    ): Array<Array<Array<DoubleArray>>>? =
        dataRegex(attribute).find(response, 0)?.let { match ->
            //parse dimensions
            val dT = match.groupValues.getOrNull(1)?.toInt() ?: run {
                Log.e(TAG, "Failed to read <time-dimension-size> from 4DArray")
                return null
            }
            val dD = match.groupValues.getOrNull(2)?.toInt() ?: run {
                Log.e(TAG, "Failed to read <depth-dimension-size> from 4DArray")
                return null
            }
            val dY = match.groupValues.getOrNull(3)?.toInt() ?: run {
                Log.e(TAG, "Failed to read <y-dimension-size> from 4DArray")
                return null
            }
            val dX = match.groupValues.getOrNull(4)?.toInt() ?: run {
                Log.e(TAG, "Failed to read <x-dimension-size> from 4DArray")
                return null
            }
            //parse the data
            val dataString = match.groupValues.getOrNull(5) ?: run {
                Log.e(TAG, "Failed to read <data-section> from 4DArray")
                return null
            }

            val dataSequence = readRowsOf4DIntArray(response, scaleFactor, offset, fillValue)

            //we have Sequence<Sequence<Int>>, where the inner sequence is a row.
            //now, reshape it to Array<Array<Array<FloatArray>>>
            Array(dT) { ti ->
                Array(dD) { di ->
                    Array(dY) { yi ->
                        DoubleArray(dX) { xi ->
                            dataSequence.elementAt(ti * dD * dY + di * dY + yi)
                                .elementAt(xi) //element is guaranteed to exist(if indexing done properly!)
                        }
                    }
                }
            }
        }

    fun readRowsOf4DIntArray(
        str: String,
        scaleFactor: Double,
        offset: Double,
        fillValue: Double
    ): List<List<Double>> {
        return str.split("\n")
            .map { row ->
                row.drop(1)
                    .split(", ")
                    .map { entry ->
                        val parsed = entry.toDouble()
                        if (parsed == fillValue) {
                            parsed
                        } else {
                            parsed * scaleFactor + offset
                        }
                    }
            }
    }
}
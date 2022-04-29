package no.uio.ifi.team16.stim.data.dataLoader.parser

import android.util.Log
import no.uio.ifi.team16.stim.data.NorKyst800
import no.uio.ifi.team16.stim.util.NullableFloatArray4D
import no.uio.ifi.team16.stim.util.mapAsync

/**
 * Welcome to regex hell!!
 */
class NorKyst800RegexParser {
    companion object Parser {
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

        //capture a variable and its list of attributes of das-response
        private val dasVariableRegex = Regex("""    (.*?) \{\n((?:.*\n)*?) *?\}""")
        private val dasAttributeRegex = Regex("""        (.+?) (.+?) (.+);""")
        /////////////
        // PARSING //
        /////////////
        /**
         * Take an ascii response from the norkystdataset and parse it into a norkyst-object
         *
         * @param response ascii response from the norjkyst dataset.
         * @return corresponding NorKyst800 object.
         */
        suspend fun parse(response: String): NorKyst800? {
            //get filler values TODO get from data itself, fram .dds

            val salinityFillValue = -32767
            val temperatureFillValue = -32767
            val uFillValue = -32767
            val vFillValue = -32767
            val wFillValue = 1.0E37f

            val depth = make1DFloatArrayOf("depth", response) ?: run {
                Log.e(TAG, "Failed to read <depth> from NorKyst800")
                return null
            }
            val salinity =
                makeNullable4DFloatArrayOf("salinity", response, 0.001f, 30.0f, salinityFillValue)
                    ?: run {
                        Log.e(TAG, "Failed to read <salinity> from NorKyst800")
                        return null
                    }
            val temperature =
                makeNullable4DFloatArrayOf(
                    "temperature",
                    response,
                    0.01f,
                    0.0f,
                    temperatureFillValue
                )
                    ?: run {
                        Log.e(TAG, "Failed to read <temperature> from NorKyst800")
                        return null
                    }
            val time = make1DFloatArrayOf("time", response) ?: run {
                Log.e(TAG, "Failed to read <time> from NorKyst800")
                return null
            }
            val velocity = Triple(
                makeNullable4DFloatArrayOf("u", response, 0.001f, 0.0f, uFillValue) ?: run {
                    Log.e(TAG, "Failed to read <u> from NorKyst800")
                    return null
                },
                makeNullable4DFloatArrayOf("v", response, 0.001f, 0.0f, vFillValue) ?: run {
                    Log.e(TAG, "Failed to read <v> from NorKyst800")
                    return null
                },
                makeNullable4DFloatArrayOfW("w", response, 1.0f, 0.0f, wFillValue) ?: run {
                    Log.e(TAG, "Failed to read <w> from NorKyst800")
                    return null
                }
            )


            return NorKyst800(
                depth,
                salinity,
                temperature,
                time,
                velocity,
                projection
            )
        }


        //all indexing is guaranteed to exist by virtue of the regex capture patterns
        /**
         * Takes a DAS response(from opendap), and parses its variables and their attributes toa map, mapping
         * the variables name to a sequence of that variable attributes. Each attribute is represented
         * as a triple, the first being the attributes type, the second its name, and the third its value.
         *
         * @param das: das response from opendap, as a string
         * @return map from variable name to variables attribute
         */
        fun parseDas(das: String): Map<String, Sequence<Triple<String, String, String>>> =
            dasVariableRegex.findAll(das).associate { match ->
                match.groupValues[1] to
                        dasAttributeRegex.findAll(match.groupValues[2]).map { attributeMatch ->
                            Triple(
                                attributeMatch.groupValues[1],
                                attributeMatch.groupValues[2],
                                attributeMatch.groupValues[3]
                            )
                        }
            }

        /**
         * takes the attribute declaration of a variable and returns a map from the name of its attributes to
         * pairs of the attributes type and value(as strings)
         *
         * @param variableDas: attributes of a variable in the das response
         * @return map from attribute names to attribute type and value
         */
        fun parseVariableAttributes(variableDas: Sequence<Triple<String, String, String>>): Map<String, Pair<String, String>> =
            variableDas.associate { (type, name, value) ->
                name to Pair(type, value)
            }


        ///////////////////
        // MAKE 1D ARRAY //
        ///////////////////
        /**
         * try to parse out a 1D FloatArray from an ascii opendap response,
         * returns null if any parsing fails.
         */
        fun make1DFloatArrayOf(attribute: String, response: String): FloatArray? =
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
                    entryRegex.findAll(rowMatch.groupValues.getOrElse(1) { "" }, 0)
                        .map { entryMatch ->
                            entryMatch.groupValues.getOrNull(1)?.toFloat()
                        }
                }

                FloatArray(dT) { id ->
                    dataSequence.elementAtOrNull(0)?.elementAtOrNull(id) ?: run {
                        Log.e(TAG, "Failed to read an entry in data-section from 1DArray")
                        return null
                    }
                }
            }


    ///////////////////
    // MAKE 4D ARRAY //
    ///////////////////
    /**
     * try to parse out a 4D intarray from an ascii opendap response,
     * returns null if any parsing fails.
     * The array contains null where the data is not available(ie where there are filler values)
     */
    suspend fun makeNullable4DFloatArrayOf(
        attribute: String,
        response: String,
        fso: Triple<Int, Float, Float>
    ): NullableFloatArray4D? =
        dataRegex(attribute).find(response, 0)?.let { match ->
            val (fillValue, scale, offset) = fso
            //parse dimensions
            val dT = match.groupValues.getOrNull(1)?.toInt() ?: run {
                Log.e(TAG, "Failed to read <time-dimension-size> from 4DArray")
                return@makeNullable4DFloatArrayOf null
            }
            val dD = match.groupValues.getOrNull(2)?.toInt() ?: run {
                Log.e(TAG, "Failed to read <depth-dimension-size> from 4DArray")
                return@makeNullable4DFloatArrayOf null
            }
            val dY = match.groupValues.getOrNull(3)?.toInt() ?: run {
                Log.e(TAG, "Failed to read <y-dimension-size> from 4DArray")
                return@makeNullable4DFloatArrayOf null
            }
            val dX = match.groupValues.getOrNull(4)?.toInt() ?: run {
                Log.e(TAG, "Failed to read <x-dimension-size> from 4DArray")
                return@makeNullable4DFloatArrayOf null
            }
            //parse the data
            val dataString = match.groupValues.getOrNull(5) ?: run {
                Log.e(TAG, "Failed to read <data-section> from 4DArray")
                return@makeNullable4DFloatArrayOf null
            }
            //read the rows of ints, apply scale, offset and fillvalues to get the floats
            readRowsOf4DIntArray(dT, dD, dY, dX, dataString, scale, offset, fillValue)
        }


        /**
         * for some reason the velocity w variable has a double as a fillervalue,
         * so we have to make an entirely separate function for it. Yay!
         */
        suspend fun makeNullable4DFloatArrayOfW(
            attribute: String,
            response: String,
            fso: Triple<Float, Float, Float>
        ): NullableFloatArray4D? =
            dataRegex(attribute).find(response, 0)?.let { match ->
                val (fillValue, scale, offset) = fso
                //parse dimensions
                val dT = match.groupValues.getOrNull(1)?.toInt() ?: run {
                    Log.e(TAG, "Failed to read <time-dimension-size> from 4DArray")
                    return@makeNullable4DFloatArrayOfW null
                }
                val dD = match.groupValues.getOrNull(2)?.toInt() ?: run {
                    Log.e(TAG, "Failed to read <depth-dimension-size> from 4DArray")
                    return@makeNullable4DFloatArrayOfW null
                }
                val dY = match.groupValues.getOrNull(3)?.toInt() ?: run {
                    Log.e(TAG, "Failed to read <y-dimension-size> from 4DArray")
                    return@makeNullable4DFloatArrayOfW null
                }
                val dX = match.groupValues.getOrNull(4)?.toInt() ?: run {
                    Log.e(TAG, "Failed to read <x-dimension-size> from 4DArray")
                    return@makeNullable4DFloatArrayOfW null
                }
                //parse the data
                val dataString = match.groupValues.getOrNull(5) ?: run {
                    Log.e(TAG, "Failed to read <data-section> from 4DArray")
                    return@makeNullable4DFloatArrayOfW null
                }

                //val dataSequence = readRowsOf4DFloatArray(dataString, scale, offset, fillValue)

                readRowsOf4DFloatArray(dT, dD, dY, dX, dataString, scale, offset, fillValue)
        }


    ///////////////////////////// opendap stores data as ints or floats. most are ints which scaling
    // READ INT OR FLOAT ARRAY // is applied to, but w-values are stored as floats
    /////////////////////////////
    /**
     * read a string of 4D data of ints to a 4D array of floats(after applying scale and offset)
     */
    private suspend fun readRowsOf4DIntArray(
        dT: Int,
        dD: Int,
        dY: Int,
        dX: Int,
        str: String,
        scale: Float,
        offset: Float,
        fillValue: Int
    ): NullableFloatArray4D =
        str.split("\n")
            .dropLast(1) //drop empty row
            .chunked(dD * dY) //chunk into time-slices, there should be dT of them
            .mapAsync(16) { timeChunk -> //list of string-rows representing depth, y, x at a given time
                timeChunk.chunked(dY)
                    .map { depthChunk -> //list of string-rows representing a xy grid at given time, depth
                        //read depthChunk, a XY grid, to a 2D array
                        depthChunk.map { row ->
                            row.split(", ")
                                .drop(1) //drop indexes
                                .map { entry ->
                                    val parsed = entry.toInt()
                                    if (parsed == fillValue) {
                                        null
                                    } else {
                                        parsed * scale + offset
                                    }
                                }
                                .toTypedArray()
                        }.toTypedArray()
                    }.toTypedArray()
            }.toTypedArray()


    /**
     * read a string of 4D data of floats to a 4D array of floats(after applying scale and offset)
     */
    private suspend fun readRowsOf4DFloatArray(
        dT: Int,
        dD: Int,
        dY: Int,
        dX: Int,
        str: String,
        scale: Float,
        offset: Float,
        fillValue: Float
    ): NullableFloatArray4D =
        str.split("\n")
            .dropLast(1) //drop empty row
            .chunked(dD * dY) //chunk into time-slices, there should be dT of them
            .mapAsync(16) { timeChunk -> //list of string-rows representing depth, y, x at a given time
                timeChunk.chunked(dY)
                    .map { depthChunk -> //list of string-rows representing a xy grid at given time, depth
                        //read depthChunk, a XY grid, to a 2D array
                        depthChunk.map { row ->
                            row.split(", ")
                                .drop(1) //drop indexes
                                .map { entry ->
                                    val parsed = entry.toFloat()
                                    if (parsed == fillValue) {
                                        null
                                    } else {
                                        parsed * scale + offset
                                    }
                                }.toTypedArray()
                        }.toTypedArray()
                    }.toTypedArray()
            }.toTypedArray()
    }
}
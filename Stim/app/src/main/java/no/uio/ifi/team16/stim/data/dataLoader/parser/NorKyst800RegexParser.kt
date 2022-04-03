package no.uio.ifi.team16.stim.data.dataLoader.parser

import no.uio.ifi.team16.stim.data.NorKyst800

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
    fun parse(response: String): NorKyst800? {
        val depth = make1DDoubleArrayOf("depth", response) ?: return null
        val salinity = make4DDoubleArrayOf("salinity", response, 0.001, 30.0) ?: return null
        val temperature = make4DDoubleArrayOf("temperature", response, 0.01, 0.0) ?: return null
        val time = make1DDoubleArrayOf("time", response) ?: return null
        val velocity = Triple(
            make4DDoubleArrayOf("u", response, 0.001, 0.0) ?: return null,
            make4DDoubleArrayOf("v", response, 0.001, 0.0) ?: return null,
            make4DDoubleArrayOf("w", response, 0.001, 0.0) ?: return null
        )
        return NorKyst800(
            depth,
            salinity,
            temperature,
            time,
            velocity
        )
    }

    /**
     * try to parse out a 4D intarray from an ascii opendap response,
     * returns null if any parsing fails.
     */
    private fun make4DDoubleArrayOf(
        attribute: String,
        response: String,
        scaleFactor: Double,
        offset: Double
    ): Array<Array<Array<DoubleArray>>>? =
        dataRegex(attribute).find(response, 0)?.let { match ->
            //parse dimensions
            val dT = match.groupValues.getOrNull(1)?.toInt() ?: return null
            val dD = match.groupValues.getOrNull(2)?.toInt() ?: return null
            val dY = match.groupValues.getOrNull(3)?.toInt() ?: return null
            val dX = match.groupValues.getOrNull(4)?.toInt() ?: return null
            //parse the data
            val dataString = match.groupValues.getOrNull(5) ?: return null
            //match all rows, for each one parse out entries
            val dataSequence = arrayRowRegex.findAll(dataString, 0).toList().map { rowMatch ->
                entryRegex.findAll(rowMatch.groupValues.getOrElse(1) { "" }, 0).toList()
                    .map { entryMatch ->
                        (entryMatch.groupValues.getOrNull(1)?.toDouble()
                            ?: return null) * scaleFactor - offset
                    }
            }
            //we have Sequence<Sequence<Int>>, where the inner sequence is a row
            //now, reshape it to Array<Array<Array<FloatArray>>>
            Array(dT) { ti ->
                Array(dD) { di ->
                    Array(dY) { yi ->
                        DoubleArray(dX) { xi ->
                            dataSequence.elementAt(ti * dD * dY + di * dY + yi)
                                .elementAtOrNull(xi)
                                ?: return null
                        }
                    }
                }
            }
        }
    /**
     * try to parse out a 4D intarray from an ascii opendap response,
     * returns null if any parsing fails.
     */
    private fun make1DDoubleArrayOf(attribute: String, response: String): DoubleArray? =
        dataRegex1D(attribute).find(response, 0)?.let { match ->
            //parse dimensions
            val dT = match.groupValues.getOrNull(1)?.toInt() ?: return null
            //parse the data
            val dataString = match.groupValues.getOrNull(5) ?: return null
            //match all rows, for each one parse out entries
            val dataSequence = arrayRowRegex.findAll(dataString, 0).map { rowMatch ->
                entryRegex.findAll(rowMatch.groupValues.getOrElse(1) { "" }, 0).map { entryMatch ->
                    entryMatch.groupValues.getOrNull(1)?.toDouble()
                }
            }
            //we have Sequence<Sequence<Int>>, where the inner sequence is a row
            //now, reshape it to Array<Array<Array<FloatArray>>>
            DoubleArray(dT) { id ->
                dataSequence.elementAtOrNull(0)?.elementAtOrNull(id) ?: return null
            }
        }
}
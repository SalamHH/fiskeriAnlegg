package no.uio.ifi.team16.stim.data.dataLoader.parser

import no.uio.ifi.team16.stim.data.NorKyst800

/**
 * Welcome to regex hell!!
 */
class NorKyst800RegexParser {

    ///////////////////////
    // REGEXES (REGEXI?) //
    ///////////////////////
    //finds attribute data start, captures dimensions(group 0,1,2,3) and data(group 4)
    //only read the attribute itself, not the mappings included. i.e. starts with attribute.attribute
    private val dataRegex: (String) -> Regex = { str ->
        Regex(
            """^${str}\.${str}""" +
                    //"""^(?P<attribute>\w+)\.((?P=attribute))""" +   //attribute start (name, capture 0)
                    """(?:\[(.*?)\])?""" + //dimension 1 (capture 0)
                    """(?:\[(.*?)\])?""" + //dimension 2 (capture 1)
                    """(?:\[(.*?)\])?""" + //dimension 3 (capture 2)
                    """(?:\[(.*?)\])?""" + //dimension 4 (capture 3)
                    """$(^.*$\n)*?\n"""    //data        (capture 4)
        )
    }

    //finds start of a row, captures the row contents
    private val arrayRowRegex = Regex("""^(?:\[.*?\])?(?:\[.*?\])?(?:\[.*?\]), (.*?)$""")

    //capture a single entry (in a row)
    private val entryRegex = Regex(""" (.+?)(?:,|$)""")

    /////////////
    // PARSING //
    /////////////
    fun parse(response: String): NorKyst800? {
        val depth = make1DDoubleArrayOf("depth", response) ?: return null
        val salinity = make4DIntArrayOf("salinity", response) ?: return null
        val temperature = make4DIntArrayOf("temperature", response) ?: return null
        val time = make1DDoubleArrayOf("time", response) ?: return null
        val velocity = Triple(
            make4DIntArrayOf("u", response) ?: return null,
            make4DIntArrayOf("v", response) ?: return null,
            make4DIntArrayOf("w", response) ?: return null
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
    private fun make4DIntArrayOf(
        attribute: String,
        response: String
    ): Array<Array<Array<IntArray>>>? =
        dataRegex(attribute).find(response, 0)?.let { match ->
            //parse dimensions
            val dT = match.groupValues.getOrNull(0)?.toInt() ?: return null
            val dD = match.groupValues.getOrNull(1)?.toInt() ?: return null
            val dY = match.groupValues.getOrNull(2)?.toInt() ?: return null
            val dX = match.groupValues.getOrNull(3)?.toInt() ?: return null
            //parse the data
            val dataString = match.groupValues.getOrNull(4) ?: return null
            //match all rows, for each one parse out entries
            val dataSequence = arrayRowRegex.findAll(dataString, 0).map { rowMatch ->
                entryRegex.findAll(rowMatch.groupValues.getOrElse(0) { "" }, 0).map { entryMatch ->
                    (entryMatch.groupValues.getOrElse(0) { "-1" }).toInt()
                }
            }
            //we have Sequence<Sequence<Int>>, where the inner sequence is a row
            //now, reshape it to Array<Array<Array<FloatArray>>>
            Array(dT) { ti ->
                Array(dD) { di ->
                    Array(dY) { yi ->
                        IntArray(dX) { xi ->
                            dataSequence
                                .elementAtOrNull(xi)
                                ?.elementAt(ti * dD * dY * dX + di * dY * dX + yi * dX)
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
        dataRegex(attribute).find(response, 0)?.let { match ->
            //parse dimensions
            val dT = match.groupValues.getOrNull(0)?.toInt() ?: return null
            //parse the data
            val dataString = match.groupValues.getOrNull(4) ?: return null
            //match all rows, for each one parse out entries
            val dataSequence = arrayRowRegex.findAll(dataString, 0).map { rowMatch ->
                entryRegex.findAll(rowMatch.groupValues.getOrElse(0) { "" }, 0).map { entryMatch ->
                    (entryMatch.groupValues.getOrElse(0) { "-1" }).toDouble()
                }
            }
            //we have Sequence<Sequence<Int>>, where the inner sequence is a row
            //now, reshape it to Array<Array<Array<FloatArray>>>
            DoubleArray(dT) { id ->
                dataSequence.elementAtOrNull(0)?.elementAtOrNull(id) ?: return null
            }
        }
}
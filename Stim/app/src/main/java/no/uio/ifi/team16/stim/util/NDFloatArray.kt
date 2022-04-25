package no.uio.ifi.team16.stim.util

import ucar.ma2.ArrayFloat
import ucar.ma2.ArrayInt

/**
 * TODO: optimize access by implementing ND arrays as a single array which stores its dimensions.
 */


///////////////////////////////
// typealiases for ND arrays //
///////////////////////////////
typealias FloatArray1D = FloatArray
typealias FloatArray2D = Array<FloatArray1D>
typealias FloatArray3D = Array<FloatArray2D>
typealias FloatArray4D = Array<FloatArray3D>

typealias IntArray1D = IntArray
typealias IntArray2D = Array<IntArray1D>
typealias IntArray3D = Array<IntArray2D>
typealias IntArray4D = Array<IntArray3D>

////////////////////////////////////
// classes for ND nullable arrays //
////////////////////////////////////
/*
When working with thredds-datasets, some grids have filler values, like intarrays having -32767
at entries where there is no data.
 */
typealias NullableFloatArray1D = Array<Float?>
typealias NullableFloatArray2D = Array<NullableFloatArray1D>
typealias NullableFloatArray3D = Array<NullableFloatArray2D>
typealias NullableFloatArray4D = Array<NullableFloatArray3D>

//there is no use for nullable int arrays - yet.

///////////////////////////
// getters for ND arrays // Note that these have explicit return-values
///////////////////////////
fun FloatArray2D.get(row: Int, column: Int): Float = this[row][column]
fun FloatArray3D.get(depth: Int, row: Int, column: Int): Float = this[depth][row][column]
fun FloatArray4D.get(depth: Int, time: Int, row: Int, column: Int): Float =
    this[depth][time][row][column]

fun IntArray2D.get(row: Int, column: Int): Int = this[row][column]
fun IntArray3D.get(depth: Int, row: Int, column: Int): Int = this[depth][row][column]
fun IntArray4D.get(depth: Int, time: Int, row: Int, column: Int): Int =
    this[depth][time][row][column]

////////////////////////////////////
// getters for ND nullable arrays // Note that these have nullable return-values
////////////////////////////////////
fun NullableFloatArray2D.get(row: Int, column: Int): Float? = this[row][column]
fun NullableFloatArray3D.get(depth: Int, row: Int, column: Int): Float? = this[depth][row][column]
fun NullableFloatArray4D.get(depth: Int, time: Int, row: Int, column: Int): Float? =
    this[depth][time][row][column]

//////////////////////////////////
//"pretty" prints for ND arrays //
//////////////////////////////////
fun FloatArray1D.prettyPrint(): String = fold("[") { acc, arr ->
    "$acc, " + "%10.7f".format(arr)
} + "]"

fun FloatArray2D.prettyPrint(): String =
    foldIndexed("") { row, acc, arr -> "$acc[$row]${arr.prettyPrint()}" }

fun FloatArray3D.prettyPrint(): String =
    foldIndexed("[") { i, acc, arr -> "$acc\n${arr.prettyPrint()}" } + "],"

fun FloatArray4D.prettyPrint(): String =
    foldIndexed("[") { i, acc, arr -> "$acc\n${arr.prettyPrint()}" } + "]"

/////////////////////////////////////////////////
//"pretty" prints for nullable ND Float arrays //
/////////////////////////////////////////////////

fun NullableFloatArray1D.prettyPrint(): String = fold("[") { acc, arr ->
    "$acc, " +
            (if (arr == null) "N/A  " else "%5.2f".format(arr))
} + "]"

fun NullableFloatArray2D.prettyPrint(): String =
    foldIndexed("") { row, acc2, arr2 ->
        "$acc2[$row]" +
                arr2.fold("[") { acc, arr ->
                    "$acc, " +
                            (if (arr == null) "N/A  " else "%5.2f".format(arr))
                } + "]"
    }

fun NullableFloatArray3D.prettyPrint(): String =
    foldIndexed("[") { i, acc3, arr3 ->
        "$acc3\n" +
                arr3.foldIndexed("") { row, acc2, arr2 ->
                    "$acc2[$row]" +
                            arr2.fold("[") { acc, arr ->
                                "$acc, " +
                                        (if (arr == null) "N/A  " else "%5.2f".format(arr))
                            } + "]"
                }
    } + "],"

fun NullableFloatArray4D.prettyPrint(): String =
    foldIndexed("[") { j, acc4, arr4 ->
        "$acc4\n" +
                arr4.foldIndexed("[") { i, acc3, arr3 ->
                    "$acc3\n" +
                            arr3.foldIndexed("") { row, acc2, arr2 ->
                                "$acc2[$row]" +
                                        arr2.fold("[") { acc, arr ->
                                            "$acc, " +
                                                    (if (arr == null) "N/A  " else "%5.2f".format(
                                                        arr
                                                    ))
                                        } + "]"
                            }
                } + "],"
    } + "]"


///////////////////////////////////////////////////////////
// EXTENDING NETCDF NDARRAYS WITH CASTS TO EXPLICIT TYPE //
///////////////////////////////////////////////////////////
/*
 * we need functions for converting from ArrayInt to ND arrays of int, and ArrayFLoat to ND FloatArray
 */
/////////////////////////////
// ARRAYINT TO ND INTARRAY //
/////////////////////////////
/** cast netcdfs version of an array to the kind of ND arrays we use */
fun ArrayInt.to1DIntArray(): IntArray1D = this.copyTo1DJavaArray() as IntArray

/** cast netcdfs version of an array to the kind of ND arrays we use */
fun ArrayInt.to2DIntArray(): IntArray2D {
    val asIntArray =
        this.copyToNDJavaArray() as Array<IntArray> //unchecked cast, but guaranteed to be Array<IntArray>
    return Array(asIntArray.size) { row ->
        asIntArray[row]
    }
}

/** cast netcdfs version of an array to the kind of ND arrays we use */
fun ArrayInt.to3DIntArray(): IntArray3D {
    val asIntArray =
        this.copyToNDJavaArray() as Array<Array<IntArray>> //unchecked cast, but guaranteed to be Array<Array<IntArray>>
    return Array(asIntArray.size) { i ->
        Array(asIntArray[i].size) { j ->
            asIntArray[i][j]
        }
    }
}

/** cast netcdfs version of an array to the kind of ND arrays we use */
fun ArrayInt.to4DIntArray(): IntArray4D {
    val asIntArray =
        this.copyToNDJavaArray() as Array<Array<Array<IntArray>>> //guaranteed
    return Array(asIntArray.size) { i ->
        Array(asIntArray[i].size) { j ->
            Array(asIntArray[i][j].size) { k ->
                asIntArray[i][j][k]
            }
        }
    }
}

/////////////////////////////////
// ARRAYFLOAT TO ND FLOATARRAY //
/////////////////////////////////
/** cast netcdfs version of an array to the kind of ND arrays we use */
fun ArrayFloat.to1DFloatArray(): FloatArray1D = this.copyTo1DJavaArray() as FloatArray

/** cast netcdfs version of an array to the kind of ND arrays we use */
fun ArrayFloat.to2DFloatArray(): FloatArray2D {
    val asFloatArray =
        this.copyToNDJavaArray() as Array<FloatArray> //unchecked cast, but guaranteed to be Array<FloatArray>
    return Array(asFloatArray.size) { row ->
        asFloatArray[row]
    }
}

/** cast netcdfs version of an array to the kind of ND arrays we use */
fun ArrayFloat.to3DFloatArray(): FloatArray3D {
    val asFloatArray =
        this.copyToNDJavaArray() as Array<Array<FloatArray>> //unchecked cast, but guaranteed to be Array<Array<FloatArray>>
    return Array(asFloatArray.size) { i ->
        Array(asFloatArray[i].size) { j ->
            asFloatArray[i][j]
        }
    }
}

/** cast netcdfs version of an array to the kind of ND arrays we use */
fun ArrayFloat.to4DFloatArray(): FloatArray4D {
    val asFloatArray =
        this.copyToNDJavaArray() as Array<Array<Array<FloatArray>>> //guaranteed
    return Array(asFloatArray.size) { i ->
        Array(asFloatArray[i].size) { j ->
            Array(asFloatArray[i][j].size) { k ->
                asFloatArray[i][j][k]
            }
        }
    }
}

///////////////////////////////////////////////////////////
// EXTENDING NETCDF NDARRAYS WITH CASTS TO NULLABLE TYPE //
///////////////////////////////////////////////////////////

/** cast netcdfs version of an array to the kind of ND arrays we use */
/*
fun ArrayDouble.toNullable1DFloatArray(fillValue : Double): NullableFloatArray1D =
    NullableFloatArray1D(this.to1DDoubleArray(), fillValue)
fun ArrayDouble.toNullable2DFloatArray(fillValue : Double): NullableFloatArray2D =
    NullableDoubleArray2D(this.to2DDoubleArray(), fillValue)
fun ArrayDouble.toNullable3DFloatArray(fillValue : Double): NullableFloatArray3D =
    NullableDoubleArray3D(this.to3DDoubleArray(), fillValue)
fun ArrayDouble.toNullable4DFloatArray(fillValue : Double): NullableFloatArray4D =
    NullableDoubleArray4D(this.to4DDoubleArray(), fillValue)

fun ArrayFloat.toNullable1DFloatArray(fillValue : Float): NullableFloatArray1D =
    NullableFloatArray1D(this.to1DFloatArray(), fillValue)
fun ArrayFloat.toNullable2DFloatArray(fillValue : Float): NullableFloatArray2D =
    NullableFloatArray2D(this.to2DFloatArray(), fillValue)
fun ArrayFloat.toNullable3DFloatArray(fillValue : Float): NullableFloatArray3D =
    NullableFloatArray3D(this.to3DFloatArray(), fillValue)
fun ArrayFloat.toNullable4DFloatArray(fillValue : Float): NullableFloatArray4D =
    NullableFloatArray4D(this.to4DFloatArray(), fillValue)

fun ArrayInt.toNullable1DIntArray(fillValue : Int): NullableIntArray1D =
    NullableIntArray1D(this.to1DIntArray(), fillValue)
fun ArrayInt.toNullable2DIntArray(fillValue : Int): NullableIntArray2D =
    NullableIntArray2D(this.to2DIntArray(), fillValue)
fun ArrayInt.toNullable3DIntArray(fillValue : Int): NullableIntArray3D =
    NullableIntArray3D(this.to3DIntArray(), fillValue)
fun ArrayInt.toNullable4DIntArray(fillValue : Int): NullableIntArray4D =
    NullableIntArray4D(this.to4DIntArray(), fillValue)
    */

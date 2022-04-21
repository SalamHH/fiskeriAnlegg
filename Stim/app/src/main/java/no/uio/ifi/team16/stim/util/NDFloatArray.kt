package no.uio.ifi.team16.stim.util

import ucar.ma2.ArrayDouble
import ucar.ma2.ArrayFloat
import ucar.ma2.ArrayInt


///////////////////////////////
// typealiases for ND arrays //
///////////////////////////////
typealias FloatArray1D = FloatArray
typealias FloatArray2D = Array<FloatArray1D>
typealias FloatArray3D = Array<FloatArray2D>
typealias FloatArray4D = Array<FloatArray3D>

typealias DoubleArray1D = DoubleArray
typealias DoubleArray2D = Array<DoubleArray1D>
typealias DoubleArray3D = Array<DoubleArray2D>
typealias DoubleArray4D = Array<DoubleArray3D>

typealias IntArray1D = IntArray
typealias IntArray2D = Array<IntArray1D>
typealias IntArray3D = Array<IntArray2D>
typealias IntArray4D = Array<IntArray3D>

////////////////////////////////////
// classes for ND nullable arrays //
////////////////////////////////////
/*
When working with thredds-datasets, some grids have filler values, like intarrays having -32767
at entries where there is no data. We could use an array of nullable ints, but a better approach is
using the primitive representation and wrapping it in a class that can tell whether the
entries in the primitive data are fillervalues.

Since we are working on primitives there is not much we can do with generics here.
 */
data class NullableIntArray1D(val data : IntArray1D, val fillValue: Int)
data class NullableFloatArray1D(val data : FloatArray1D, val fillValue: Float)
data class NullableDoubleArray1D(val data : DoubleArray1D, val fillValue: Double)

data class NullableFloatArray2D(val data : FloatArray2D, val fillValue: Float)
data class NullableFloatArray3D(val data : FloatArray3D, val fillValue: Float)
data class NullableFloatArray4D(val data : FloatArray4D, val fillValue: Float)

data class NullableDoubleArray2D(val data : DoubleArray2D, val fillValue: Double)
data class NullableDoubleArray3D(val data : DoubleArray3D, val fillValue: Double)
data class NullableDoubleArray4D(val data : DoubleArray4D, val fillValue: Double)

data class NullableIntArray2D(val data : IntArray2D, val fillValue: Int)
data class NullableIntArray3D(val data : IntArray3D, val fillValue: Int)
data class NullableIntArray4D(val data : IntArray4D, val fillValue: Int)

///////////////////////////
// getters for ND arrays // Note that these have explicit return-values
///////////////////////////
fun FloatArray2D.get(row: Int, column: Int): Float = this[row][column]
fun FloatArray3D.get(depth: Int, row: Int, column: Int): Float = this[depth][row][column]
fun FloatArray4D.get(depth: Int, time: Int, row: Int, column: Int): Float =
    this[depth][time][row][column]

fun DoubleArray2D.get(row: Int, column: Int): Double = this[row][column]
fun DoubleArray3D.get(depth: Int, row: Int, column: Int): Double = this[depth][row][column]
fun DoubleArray4D.get(depth: Int, time: Int, row: Int, column: Int): Double =
    this[depth][time][row][column]

fun IntArray2D.get(row: Int, column: Int): Int = this[row][column]
fun IntArray3D.get(depth: Int, row: Int, column: Int): Int = this[depth][row][column]
fun IntArray4D.get(depth: Int, time: Int, row: Int, column: Int): Int =
    this[depth][time][row][column]

////////////////////////////////////
// getters for ND nullable arrays // Note that these have nullable return-values
////////////////////////////////////
fun NullableFloatArray1D.get(entry: Int): Float? =
    this.data[entry].let { value ->
        if (value == this.fillValue) null else value
    }
fun NullableFloatArray2D.get(row: Int, column: Int): Float? =
    this.data[row][column].let { value ->
        if (value == this.fillValue) null else value
    }
fun NullableFloatArray3D.get(depth: Int, row: Int, column: Int): Float? =
    this.data[depth][row][column].let { value ->
        if (value == this.fillValue) null else value
    }
fun NullableFloatArray4D.get(depth: Int, time: Int, row: Int, column: Int): Float? =
    this.data[depth][time][row][column].let { value ->
        if (value == this.fillValue) null else value
    }

fun NullableDoubleArray1D.get(index : Int): Double? =
    this.data[index].let { value ->
        if (value == this.fillValue) null else value
    }
fun NullableDoubleArray2D.get(row: Int, column: Int): Double? =
    this.data[row][column].let { value ->
        if (value == this.fillValue) null else value
    }
fun NullableDoubleArray3D.get(depth: Int, row: Int, column: Int): Double? =
    this.data[depth][row][column].let { value ->
        if (value == this.fillValue) null else value
    }
fun NullableDoubleArray4D.get(depth: Int, time: Int, row: Int, column: Int): Double? =
    this.data[depth][time][row][column].let { value ->
        if (value == this.fillValue) null else value
    }

fun NullableIntArray1D.get(index: Int): Int? =
    this.data[index].let { value ->
        if (value == this.fillValue) null else value
    }
fun NullableIntArray2D.get(row: Int, column: Int): Int? =
    this.data[row][column].let { value ->
        if (value == this.fillValue) null else value
    }
fun NullableIntArray3D.get(depth: Int, row: Int, column: Int): Int? =
    this.data[depth][row][column].let { value ->
        if (value == this.fillValue) null else value
    }
fun NullableIntArray4D.get(depth: Int, time: Int, row: Int, column: Int): Int? =
    this.data[depth][time][row][column].let { value ->
        if (value == this.fillValue) null else value
    }

//////////////////////////////////
//"pretty" prints for ND arrays //
//////////////////////////////////
fun FloatArray1D.prettyPrint(): String = fold("[") { acc, arr ->
    "$acc, " + "%5.2f".format(arr)
} + "]"

fun FloatArray2D.prettyPrint(): String =
    foldIndexed("") { row, acc, arr -> "$acc[$row]${arr.prettyPrint()}" }

fun FloatArray3D.prettyPrint(): String =
    foldIndexed("[") { i, acc, arr -> "$acc\n${arr.prettyPrint()}" } + "],"

fun FloatArray4D.prettyPrint(): String =
    foldIndexed("[") { i, acc, arr -> "$acc\n${arr.prettyPrint()}" } + "]"


fun DoubleArray1D.prettyPrint(): String = fold("[") { acc, arr ->
    "$acc, " + "%5.2f".format(arr)
} + "]"

fun DoubleArray2D.prettyPrint(): String =
    foldIndexed("") { row, acc, arr -> "$acc[$row]${arr.prettyPrint()}\n" }

fun DoubleArray3D.prettyPrint(): String =
    foldIndexed("[") { i, acc, arr -> "$acc\n${arr.prettyPrint()}" } + "],\n"

fun DoubleArray4D.prettyPrint(): String =
    foldIndexed("[") { i, acc, arr -> "$acc\n${arr.prettyPrint()}" } + "]"


/////////////////////////////////////////////////
//"pretty" prints for nullable ND Float arrays //
/////////////////////////////////////////////////
fun NullableFloatArray1D.prettyPrint(): String = data.fold("[") { acc, arr ->
    "$acc, " +
            (if (arr == fillValue) "N/A  " else "%5.2f".format(arr))
} + "]"

fun NullableFloatArray2D.prettyPrint(): String =
    data.foldIndexed("") { row, acc2, arr2 -> "$acc2[$row]" +
        arr2.fold("[") { acc, arr ->
            "$acc, " +
                    (if (arr == fillValue) "N/A  " else "%5.2f".format(arr))
        } + "]"
    }

fun NullableFloatArray3D.prettyPrint(): String =
    data.foldIndexed("[") { i, acc3, arr3 -> "$acc3\n" +
            arr3.foldIndexed("") { row, acc2, arr2 -> "$acc2[$row]" +
                    arr2.fold("[") { acc, arr ->
                        "$acc, " +
                                (if (arr == fillValue) "N/A  " else "%5.2f".format(arr))
                    } + "]"
            }
    } + "],"

fun NullableFloatArray4D.prettyPrint(): String =
    data.foldIndexed("[") { j, acc4, arr4 -> "$acc4\n" +
            arr4.foldIndexed("[") { i, acc3, arr3 -> "$acc3\n" +
                    arr3.foldIndexed("") { row, acc2, arr2 -> "$acc2[$row]" +
                        arr2.fold("[") { acc, arr ->
                            "$acc, " +
                                    (if (arr == fillValue) "N/A  " else "%5.2f".format(arr))
                        } + "]"
                    }
            } + "],"
    } + "]"

//////////////////////////////////////////////////
//"pretty" prints for nullable ND Double arrays //
//////////////////////////////////////////////////
fun NullableDoubleArray1D.prettyPrint(): String = data.fold("[") { acc, arr ->
    "$acc, " +
            (if (arr == fillValue) "N/A  " else "%5.2f".format(arr))
} + "]"

fun NullableDoubleArray2D.prettyPrint(): String =
    data.foldIndexed("") { row, acc2, arr2 -> "$acc2[$row]" +
            arr2.fold("[") { acc, arr ->
                "$acc, " +
                        (if (arr == fillValue) "N/A  " else "%5.2f".format(arr))
            } + "]"
    }

fun NullableDoubleArray3D.prettyPrint(): String =
    data.foldIndexed("[") { i, acc3, arr3 -> "$acc3\n" +
            arr3.foldIndexed("") { row, acc2, arr2 -> "$acc2[$row]" +
                    arr2.fold("[") { acc, arr ->
                        "$acc, " +
                                (if (arr == fillValue) "N/A  " else "%5.2f".format(arr))
                    } + "]"
            }
    } + "],"

fun NullableDoubleArray4D.prettyPrint(): String =
    data.foldIndexed("[") { j, acc4, arr4 -> "$acc4\n" +
            arr4.foldIndexed("[") { i, acc3, arr3 -> "$acc3\n" +
                    arr3.foldIndexed("") { row, acc2, arr2 -> "$acc2[$row]" +
                            arr2.fold("[") { acc, arr ->
                                "$acc, " +
                                        (if (arr == fillValue) "N/A  " else "%5.2f".format(arr))
                            } + "]"
                    }
            } + "],"
    } + "]"

///////////////////////////////////////////////////////////
// EXTENDING NETCDF NDARRAYS WITH CASTS TO EXPLICIT TYPE //
///////////////////////////////////////////////////////////
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

/** cast netcdfs version of an array to the kind of ND arrays we use */
fun ArrayDouble.to1DDoubleArray(): DoubleArray1D = this.copyTo1DJavaArray() as DoubleArray

/** cast netcdfs version of an array to the kind of ND arrays we use */
fun ArrayDouble.to2DDoubleArray(): DoubleArray2D {
    val asDoubleArray =
        this.copyToNDJavaArray() as Array<DoubleArray> //unchecked cast, but guaranteed to be Array<DoubleArray>
    return Array(asDoubleArray.size) { row ->
        asDoubleArray[row]
    }
}

/** cast netcdfs version of an array to the kind of ND arrays we use */
fun ArrayDouble.to3DDoubleArray(): DoubleArray3D {
    val asDoubleArray =
        this.copyToNDJavaArray() as Array<Array<DoubleArray>> //unchecked cast, but guaranteed to be Array<Array<DoubleArray>>
    return Array(asDoubleArray.size) { i ->
        Array(asDoubleArray[i].size) { j ->
            asDoubleArray[i][j]
        }
    }
}

/** cast netcdfs version of an array to the kind of ND arrays we use */
fun ArrayDouble.to4DDoubleArray(): DoubleArray4D {
    val asDoubleArray =
        this.copyToNDJavaArray() as Array<Array<Array<DoubleArray>>> //guaranteed
    return Array(asDoubleArray.size) { i ->
        Array(asDoubleArray[i].size) { j ->
            Array(asDoubleArray[i][j].size) { k ->
                asDoubleArray[i][j][k]
            }
        }
    }
}

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
fun ArrayDouble.toNullable1DDoubleArray(fillValue : Double): NullableDoubleArray1D =
    NullableDoubleArray1D(this.to1DDoubleArray(), fillValue)
fun ArrayDouble.toNullable2DDoubleArray(fillValue : Double): NullableDoubleArray2D =
    NullableDoubleArray2D(this.to2DDoubleArray(), fillValue)
fun ArrayDouble.toNullable3DDoubleArray(fillValue : Double): NullableDoubleArray3D =
    NullableDoubleArray3D(this.to3DDoubleArray(), fillValue)
fun ArrayDouble.toNullable4DDoubleArray(fillValue : Double): NullableDoubleArray4D =
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
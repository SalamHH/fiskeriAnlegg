package no.uio.ifi.team16.stim.util

typealias FloatArray1D = Array<Float>
typealias FloatArray2D = Array<FloatArray1D>
typealias FloatArray3D = Array<FloatArray2D>
typealias FloatArray4D = Array<FloatArray3D>

typealias DoubleArray1D = Array<Double?>
typealias DoubleArray2D = Array<DoubleArray1D>
typealias DoubleArray3D = Array<DoubleArray2D>
typealias DoubleArray4D = Array<DoubleArray3D>

typealias IntArray1D = Array<Int?>
typealias IntArray2D = Array<IntArray1D>
typealias IntArray3D = Array<IntArray2D>
typealias IntArray4D = Array<IntArray3D>

fun FloatArray2D.get(row: Int, column: Int): Float? = this[row][column]
fun FloatArray3D.get(depth: Int, row: Int, column: Int): Float? = this[depth][row][column]
fun FloatArray4D.get(depth: Int, time: Int, row: Int, column: Int): Float? =
    this[depth][time][row][column]

fun DoubleArray2D.get(row: Int, column: Int): Double? = this[row][column]
fun DoubleArray3D.get(depth: Int, row: Int, column: Int): Double? = this[depth][row][column]
fun DoubleArray4D.get(depth: Int, time: Int, row: Int, column: Int): Double? =
    this[depth][time][row][column]

fun IntArray2D.get(row: Int, column: Int): Int? = this[row][column]
fun IntArray3D.get(depth: Int, row: Int, column: Int): Int? = this[depth][row][column]
fun IntArray4D.get(depth: Int, time: Int, row: Int, column: Int): Int? =
    this[depth][time][row][column]

//"pretty" prints
fun FloatArray1D.prettyPrint(): String = fold("[") { acc, arr ->
    "$acc, " +
            (if (arr == null) "N/A  " else "${"%5.2f".format(arr)}")
} + "]"

fun FloatArray2D.prettyPrint(): String =
    foldIndexed("") { row, acc, arr -> "$acc[$row]${arr.prettyPrint()}" }

fun FloatArray3D.prettyPrint(): String =
    foldIndexed("[") { i, acc, arr -> "$acc\n${arr.prettyPrint()}" } + "],"

fun FloatArray4D.prettyPrint(): String =
    foldIndexed("[") { i, acc, arr -> "$acc\n${arr.prettyPrint()}" } + "]"

fun DoubleArray1D.prettyPrint(): String = fold("[") { acc, arr ->
    "$acc, " +
            (if (arr == null) "N/A  " else "${"%5.2f".format(arr)}")
} + "]"

fun DoubleArray2D.prettyPrint(): String =
    foldIndexed("") { row, acc, arr -> "$acc[$row]${arr.prettyPrint()}\n" }

fun DoubleArray3D.prettyPrint(): String =
    foldIndexed("[") { i, acc, arr -> "$acc\n${arr.prettyPrint()}" } + "],\n"

fun DoubleArray4D.prettyPrint(): String =
    foldIndexed("[") { i, acc, arr -> "$acc\n${arr.prettyPrint()}" } + "]"
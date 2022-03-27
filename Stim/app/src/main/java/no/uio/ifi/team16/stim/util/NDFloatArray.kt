package no.uio.ifi.team16.stim.util

typealias FloatArray2D = Array<FloatArray>
typealias FloatArray3D = Array<FloatArray2D>
typealias FloatArray4D = Array<FloatArray3D>
typealias DoubleArray2D = Array<DoubleArray>
typealias DoubleArray3D = Array<DoubleArray2D>
typealias DoubleArray4D = Array<DoubleArray3D>
typealias IntArray2D = Array<IntArray>
typealias IntArray3D = Array<IntArray2D>
typealias IntArray4D = Array<IntArray3D>

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
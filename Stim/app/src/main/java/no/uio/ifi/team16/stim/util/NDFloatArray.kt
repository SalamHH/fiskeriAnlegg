package no.uio.ifi.team16.stim.util

typealias FloatArray2D = Array<FloatArray>
typealias FloatArray3D = Array<FloatArray2D>

fun FloatArray2D.get(row: Int, column: Int): Float = this[row][column]
fun FloatArray3D.get(depth: Int, row: Int, column: Int): Float = this[depth][row][column]
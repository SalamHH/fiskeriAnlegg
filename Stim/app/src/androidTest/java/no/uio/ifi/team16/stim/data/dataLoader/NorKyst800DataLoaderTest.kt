package no.uio.ifi.team16.stim.data.dataLoader

import kotlinx.coroutines.runBlocking
import no.uio.ifi.team16.stim.util.NullableFloatArray4D
import org.junit.Assert.assertNotNull
import org.junit.Test
import kotlin.ranges.IntProgression.Companion.fromClosedRange

fun NullableFloatArray4D.scaleAndOffset(factor: Float, offset: Float): NullableFloatArray4D =
    map { arr3d ->
        arr3d.map { arr2d ->
            arr2d.map { arr1d ->
                arr1d.map { element ->
                    if (element == null) null else element * factor + offset
                }.toTypedArray()
            }.toTypedArray()
        }.toTypedArray()
    }.toTypedArray()

class NorKyst800DataLoaderTest {
    private val fakeResponse = """
        Dataset {
            Float64 depth[depth = 3];
            Grid {
             ARRAY:
                Float64 lat[Y = 2][X = 3];
             MAPS:
                Float64 Y[Y = 2];
                Float64 X[X = 3];
            } lat;
            Grid {
             ARRAY:
                Float64 lon[Y = 2][X = 3];
             MAPS:
                Float64 Y[Y = 2];
                Float64 X[X = 3];
            } lon;
            Grid {
             ARRAY:
                Int16 salinity[time = 3][depth = 2][Y = 2][X = 3];
             MAPS:
                Float64 time[time = 3];
                Float64 depth[depth = 2];
                Float64 Y[Y = 2];
                Float64 X[X = 3];
            } salinity;
            Grid {
             ARRAY:
                Int16 temperature[time = 3][depth = 2][Y = 2][X = 3];
             MAPS:
                Float64 time[time = 3];
                Float64 depth[depth = 2];
                Float64 Y[Y = 2];
                Float64 X[X = 3];
            } temperature;
            Float64 time[time = 2];
            Grid {
             ARRAY:
                Int16 u[time = 3][depth = 2][Y = 2][X = 3];
             MAPS:
                Float64 time[time = 3];
                Float64 depth[depth = 2];
                Float64 Y[Y = 2];
                Float64 X[X = 3];
            } u;
            Grid {
             ARRAY:
                Int16 v[time = 3][depth = 2][Y = 2][X = 3];
             MAPS:
                Float64 time[time = 3];
                Float64 depth[depth = 2];
                Float64 Y[Y = 2];
                Float64 X[X = 3];
            } v;
            Grid {
             ARRAY:
                Float32 w[time = 3][depth = 2][Y = 2][X = 3];
             MAPS:
                Float64 time[time = 3];
                Float64 depth[depth = 2];
                Float64 Y[Y = 2];
                Float64 X[X = 3];
            } w;
        } fou-hi/norkyst800m-1h/NorKyst-800m_ZDEPTHS_his.fc.2022042500.nc;
        ---------------------------------------------
        depth[3]
        0.0, 100.0, 2000.0

        lat.lat[2][3]
        [0], 55.908369929915125, 62.49262602518113, 68.2975145963228
        [1], 58.01408691998771, 65.26772230447494, 72.06046238531032

        lat.Y[2]
        0.0, 560000.0

        lat.X[3]
        0.0, 880000.0, 1760000.0


        lon.lon[2][3]
        [0], 9.194590109686652, 17.645135616058933, 31.23069355646127
        [1], 0.9749537179037423, 7.889075121859453, 20.506316233016715

        lon.Y[2]
        0.0, 560000.0

        lon.X[3]
        0.0, 880000.0, 1760000.0


        salinity.salinity[3][2][2][3]
        [0][0][0], -32767, -32767, -32767
        [0][0][1], 5085, 4591, 4695
        [0][1][0], -32767, -32767, -32767
        [0][1][1], -32767, 4716, 4727
        [1][0][0], -32767, -32767, -32767
        [1][0][1], 5081, 4589, 4698
        [1][1][0], -32767, -32767, -32767
        [1][1][1], -32767, 4721, 4737
        [2][0][0], -32767, -32767, -32767
        [2][0][1], 5086, 4591, 4700
        [2][1][0], -32767, -32767, -32767
        [2][1][1], -32767, 4724, 4733

        salinity.time[3]
        1.6509312E9, 1.6509564E9, 1.6509816E9

        salinity.depth[2]
        0.0, 200.0

        salinity.Y[2]
        0.0, 560000.0

        salinity.X[3]
        0.0, 880000.0, 1760000.0


        temperature.temperature[3][2][2][3]
        [0][0][0], -32767, -32767, -32767
        [0][0][1], 885, 685, 474
        [0][1][0], -32767, -32767, -32767
        [0][1][1], -32767, 679, 456
        [1][0][0], -32767, -32767, -32767
        [1][0][1], 861, 682, 470
        [1][1][0], -32767, -32767, -32767
        [1][1][1], -32767, 680, 464
        [2][0][0], -32767, -32767, -32767
        [2][0][1], 858, 685, 468
        [2][1][0], -32767, -32767, -32767
        [2][1][1], -32767, 679, 456

        temperature.time[3]
        1.6509312E9, 1.6509564E9, 1.6509816E9

        temperature.depth[2]
        0.0, 200.0

        temperature.Y[2]
        0.0, 560000.0

        temperature.X[3]
        0.0, 880000.0, 1760000.0


        time[2]
        1.6509312E9, 1.6509636E9

        u.u[3][2][2][3]
        [0][0][0], -32767, -32767, -32767
        [0][0][1], -32767, -96, -61
        [0][1][0], -32767, -32767, -32767
        [0][1][1], -32767, 24, -77
        [1][0][0], -32767, -32767, -32767
        [1][0][1], -32767, -37, -206
        [1][1][0], -32767, -32767, -32767
        [1][1][1], -32767, 12, 4
        [2][0][0], -32767, -32767, -32767
        [2][0][1], -32767, -57, -229
        [2][1][0], -32767, -32767, -32767
        [2][1][1], -32767, -11, -179

        u.time[3]
        1.6509312E9, 1.6509564E9, 1.6509816E9

        u.depth[2]
        0.0, 200.0

        u.Y[2]
        0.0, 560000.0

        u.X[3]
        0.0, 880000.0, 1760000.0


        v.v[3][2][2][3]
        [0][0][0], -32767, -32767, -32767
        [0][0][1], 19, -25, -134
        [0][1][0], -32767, -32767, -32767
        [0][1][1], -32767, 14, -163
        [1][0][0], -32767, -32767, -32767
        [1][0][1], 6, -4, -129
        [1][1][0], -32767, -32767, -32767
        [1][1][1], -32767, -25, -4
        [2][0][0], -32767, -32767, -32767
        [2][0][1], 129, 25, -38
        [2][1][0], -32767, -32767, -32767
        [2][1][1], -32767, 37, -14

        v.time[3]
        1.6509312E9, 1.6509564E9, 1.6509816E9

        v.depth[2]
        0.0, 200.0

        v.Y[2]
        0.0, 560000.0

        v.X[3]
        0.0, 880000.0, 1760000.0


        w.w[3][2][2][3]
        [0][0][0], 1.0E37, 1.0E37, 1.0E37
        [0][0][1], -6.440923E-5, -2.6763373E-5, -6.496307E-5
        [0][1][0], 1.0E37, 1.0E37, 1.0E37
        [0][1][1], 1.0E37, -5.989377E-4, 4.8309087E-4
        [1][0][0], 1.0E37, 1.0E37, 1.0E37
        [1][0][1], 6.8041554E-5, -1.4682717E-5, 5.5459644E-5
        [1][1][0], 1.0E37, 1.0E37, 1.0E37
        [1][1][1], 1.0E37, 1.4523527E-4, 1.492668E-4
        [2][0][0], 1.0E37, 1.0E37, 1.0E37
        [2][0][1], -4.5285957E-5, -2.0274536E-5, -1.02906655E-4
        [2][1][0], 1.0E37, 1.0E37, 1.0E37
        [2][1][1], 1.0E37, 5.2956515E-4, -0.0013055078

        w.time[3]
        1.6509312E9, 1.6509564E9, 1.6509816E9

        w.depth[2]
        0.0, 200.0

        w.Y[2]
        0.0, 560000.0

        w.X[3]
        0.0, 880000.0, 1760000.0
    """.trimIndent()
    val TAG = "NorKyst800DataLoaderTest"
    private val dataLoader = NorKyst800DataLoader()
    //val testUrl = "https://thredds.met.no/thredds/dodsC/fou-hi/norkyst800m-1h/NorKyst-800m_ZDEPTHS_his.fc.2022042500.nc.ascii?depth[0:7:14],lat[0:700:700][0:1100:2200],lon[0:700:700][0:1100:2200],salinity[0:7:14][0:9:9][0:700:700][0:1100:2200],temperature[0:7:14][0:9:9][0:700:700][0:1100:2200],time[0:9:9],u[0:7:14][0:9:9][0:700:700][0:1100:2200],v[0:7:14][0:9:9][0:700:700][0:1100:2200],w[0:7:14][0:9:9][0:700:700][0:1100:2200]"

    /**
     * Test wether the dataloader is able to load a non-null object
     */
    @Test
    fun testAbleToLoad() {
        val data = runBlocking {
            dataLoader.load(
                fromClosedRange(200, 210, 4),
                fromClosedRange(200, 212, 7),
                fromClosedRange(0, 10, 2)
            )
        }
        assertNotNull(data)
    }
}
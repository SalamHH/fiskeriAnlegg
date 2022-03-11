package no.uio.ifi.team16.stim

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Speed Dial Test
        initSpeedDialTest()
    }

    private fun initSpeedDialTest() {
        setContentView(R.layout.speed_dial_test)
        val sdTest = SpeedDialTest(findViewById(R.id.speedDial))
        sdTest.initSpeedDial(this)
    }

}
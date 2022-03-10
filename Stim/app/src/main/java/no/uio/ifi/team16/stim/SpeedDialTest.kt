package no.uio.ifi.team16.stim

import android.content.Context
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView

class SpeedDialTest(speedDialView : SpeedDialView) {

    private val speedDial = speedDialView

    fun initSpeedDial(context : Context) {

        speedDial.addActionItem(
            SpeedDialActionItem.Builder(R.id.test_fish, R.drawable.fish)
                .setFabBackgroundColor(ContextCompat.getColor(context, R.color.green_medium))
                .setFabImageTintColor(ContextCompat.getColor(context, R.color.white))
                .create())
        speedDial.addActionItem(
            SpeedDialActionItem.Builder(R.id.test_wave, R.drawable.ic_baseline_waves_24)
                .setFabBackgroundColor(ContextCompat.getColor(context, R.color.green_light))
                .setFabImageTintColor(ContextCompat.getColor(context, R.color.white))
                .create())

        speedDial.setOnActionSelectedListener(SpeedDialView.OnActionSelectedListener { actionItem ->
            when (actionItem.id) {
                R.id.test_fish -> {
                    Toast.makeText(context, "Speed Dial 'fish' klikket!", Toast.LENGTH_LONG).show()
                    speedDial.close() // To close the Speed Dial with animation
                    return@OnActionSelectedListener true // false will close it without animation
                }R.id.test_wave -> {
                    Toast.makeText(context, "Speed Dial 'wave' klikket!", Toast.LENGTH_LONG).show()
                    speedDial.close() // To close the Speed Dial with animation
                    return@OnActionSelectedListener true // false will close it without animation
                }
            }
            false
        })
    }

}
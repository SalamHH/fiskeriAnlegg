package no.uio.ifi.team16.stim

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import no.uio.ifi.team16.stim.util.Options
import java.util.*

class OpeningScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.opening_screen)

        val prefrences = getSharedPreferences(Options.SHARED_PREFERENCES_KEY, MODE_PRIVATE)
        val editor = prefrences.edit()

        Timer().schedule(object : TimerTask() {
            override fun run() {
                if (prefrences.getBoolean("NotFirst", false)) {
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                } else {
                    editor.apply() {
                        putBoolean("NotFirst", true)
                        apply()
                    }
                    startActivity(Intent(applicationContext, TutorialFragment::class.java))
                }


            }
        }, 3000)

        /**
        val layout = findViewById<ConstraintLayout>(R.id.openingLayout)
        layout.setOnTouchListener { view, event ->
        if (event.action == MotionEvent.ACTION_DOWN) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        true
        } else false
        }
         **/
    }
}
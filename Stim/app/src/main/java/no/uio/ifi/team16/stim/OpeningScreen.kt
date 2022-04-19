package no.uio.ifi.team16.stim

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import java.util.*

class OpeningScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.opening_screen)

        Timer().schedule(object : TimerTask() {
            override fun run() {
                startActivity(Intent(applicationContext, MainActivity::class.java))
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
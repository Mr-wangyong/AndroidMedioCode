package com.mrwang.androidmediocode

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.animation.OvershootInterpolator
import android.widget.Button
import com.mrwang.androidmediocode.Studio4.RippleView2
import org.jetbrains.anko.find

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rippleView2 = this.findViewById<RippleView2>(R.id.ripple);
        rippleView2.setPointColor(Color.WHITE);
        rippleView2.setRatio(400L)



        val start = this.find<Button>(R.id.start)
        val stop = this.find<Button>(R.id.stop)

        start.setOnClickListener {
            rippleView2.start()
        }
        start.scaleX=0.0f
        start.scaleY=0.0f
        start.post {
            start.animate()
                    .setInterpolator(OvershootInterpolator())
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(500L)
                    .start()
        }


        stop.setOnClickListener {
            rippleView2.stop()
        }
    }
}

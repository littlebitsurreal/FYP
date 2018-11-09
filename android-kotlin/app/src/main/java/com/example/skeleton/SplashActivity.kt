package com.example.skeleton

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import com.example.skeleton.helper.LP
import com.example.skeleton.helper.ResourceHelper
import com.example.skeleton.helper.ResourceHelper.dp

class SplashActivity : AppCompatActivity() {
    // region Life cycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setup()
    }

    private fun setup() {
        val layout = FrameLayout(this)
        val img = ImageView(this)

        layout.setBackgroundColor(ResourceHelper.color(R.color.primary))
        img.setImageResource(R.drawable.elephant)

        layout.addView(img, LP.frame(dp(120), dp(120), Gravity.CENTER).build())
        setContentView(layout)
    }

    override fun onResume() {
        super.onResume()
        Handler().postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, AppConfig.SPLASH_DURATION)
    }
    //endregion
}

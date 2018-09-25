package com.example.skeleton

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.FrameLayout

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = FrameLayout(this)
        layout.setBackgroundColor(Color.LTGRAY)
        setContentView(layout)

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

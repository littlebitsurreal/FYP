package com.example.skeleton

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.LinearLayout
import com.example.skeleton.helper.CsvHelper
import com.example.skeleton.helper.PermissionHelper
import java.io.File

class SplashActivity : AppCompatActivity() {
    // region Life cycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        layout.setBackgroundColor(Color.LTGRAY)
        setContentView(layout)

        val file = File(this.filesDir.path + "/testing2.csv")

        val readBtn = Button(this).apply {
            text = "read"
            setOnClickListener { CsvHelper.read(file) }
        }
        val writeBtn = Button(this).apply {
            text = "write"
            setOnClickListener { CsvHelper.write(file, listOf(CsvHelper.UsageRecord("testing app", 123, 456))) }
        }

        layout.addView(readBtn)
        layout.addView(writeBtn)

        if (!PermissionHelper.hasAppUsagePermission(this)) {
            PermissionHelper.getAppUsagePermission(this)
        }

        test()

//        val intent = Intent(this, MainActivity::class.java)
//        startActivity(intent)
//        finish()
    }
    //endregion

    fun test() {
        val intent = Intent(this, MyService::class.java)
        startService(intent)
    }
}

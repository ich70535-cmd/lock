package com.detox.lock

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnAccessibility = findViewById<Button>(R.id.btnAccessibility)
        val btnUsageStats = findViewById<Button>(R.id.btnUsageStats)

        btnAccessibility.setOnClickListener {
            Toast.makeText(this, "설치된 앱/디톡스락을 찾아 켜주세요", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        btnUsageStats.setOnClickListener {
            Toast.makeText(this, "디톡스락의 사용 추적 권한을 켜주세요", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
    }
}

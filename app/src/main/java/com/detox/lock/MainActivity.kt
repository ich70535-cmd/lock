package com.detox.lock

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvCurrentLimit: TextView
    private lateinit var tvCooldownStatus: TextView
    private lateinit var btnToggleStudyMode: Button
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvCurrentLimit = findViewById(R.id.tvCurrentLimit)
        tvCooldownStatus = findViewById(R.id.tvCooldownStatus)
        btnToggleStudyMode = findViewById(R.id.btnToggleStudyMode)

        val prefs = getSharedPreferences("DetoxPrefs", Context.MODE_PRIVATE)

        // 권한 버튼 연결
        findViewById<Button>(R.id.btnUsageAccess).setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
        findViewById<Button>(R.id.btnOverlayAccess).setOnClickListener {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        }

        // 제한 시간 변경 버튼 (30분 쿨다운 적용)
        findViewById<Button>(R.id.btnLimit15).setOnClickListener { requestLimitChange(15) }
        findViewById<Button>(R.id.btnLimit30).setOnClickListener { requestLimitChange(30) }
        findViewById<Button>(R.id.btnLimit60).setOnClickListener { requestLimitChange(60) }

        // 공부 모드 토글
        btnToggleStudyMode.setOnClickListener {
            val current = prefs.getBoolean("study_mode", false)
            prefs.edit().putBoolean("study_mode", !current).apply()
            updateUI()
        }

        updateUI()
    }

    private fun requestLimitChange(minutes: Int) {
        val prefs = getSharedPreferences("DetoxPrefs", Context.MODE_PRIVATE)
        val cooldownMillis = 30 * 60 * 1000L // 30분
        val targetTime = System.currentTimeMillis() + cooldownMillis

        prefs.edit()
            .putInt("pending_limit", minutes)
            .putLong("pending_apply_at", targetTime)
            .apply()

        Toast.makeText(this, "설정 변경 예약: 30분 뒤에 적용됩니다.", Toast.LENGTH_SHORT).show()
        updateUI()
    }

    private fun updateUI() {
        val prefs = getSharedPreferences("DetoxPrefs", Context.MODE_PRIVATE)
        val currentLimit = prefs.getInt("youtube_limit_minutes", 30)
        tvCurrentLimit.text = "현재 적용된 제한: ${currentLimit}분"

        val isStudyMode = prefs.getBoolean("study_mode", false)
        btnToggleStudyMode.text = if (isStudyMode) "집중 모드 해제" else "집중 모드 켜기"

        val pendingLimit = prefs.getInt("pending_limit", -1)
        val applyAt = prefs.getLong("pending_apply_at", 0L)
        val remain = applyAt - System.currentTimeMillis()

        timer?.cancel()
        if (pendingLimit != -1 && remain > 0) {
            timer = object : CountDownTimer(remain, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val min = millisUntilFinished / 1000 / 60
                    val sec = (millisUntilFinished / 1000) % 60
                    tvCooldownStatus.text = "⏳ [30분 쿨다운] ${pendingLimit}분으로 변경까지 남은 시간: ${min}분 ${sec}초"
                }
                override fun onFinish() {
                    prefs.edit()
                        .putInt("youtube_limit_minutes", pendingLimit)
                        .remove("pending_limit")
                        .remove("pending_apply_at")
                        .apply()
                    updateUI()
                }
            }.start()
        } else {
            tvCooldownStatus.text = "대기 중인 변경 요청 없음 (즉시 변경 불가 규칙 적용)"
        }
    }
}

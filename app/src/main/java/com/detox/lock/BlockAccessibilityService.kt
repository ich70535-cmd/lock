package com.detox.lock

import android.accessibilityservice.AccessibilityService
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import java.util.Calendar

class BlockAccessibilityService : AccessibilityService() {

    private val targetApp = "com.google.android.youtube" // 테스트 대상: 유튜브
    private var lastToastTime = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.packageName == null) return
        val currentPackage = event.packageName.toString()

        if (currentPackage == targetApp) {
            val prefs = getSharedPreferences("DetoxPrefs", Context.MODE_PRIVATE)

            // 1. 30분 쿨다운 완료 여부 반영
            checkPendingSettings(prefs)

            val isStudyMode = prefs.getBoolean("study_mode", false)
            val limitMinutes = prefs.getInt("youtube_limit_minutes", 30)
            val usedMinutes = getTodayUsageMinutes(targetApp)

            // 차단 조건: 공부 모드 활성화 상태이거나, 사용 제한 시간을 넘겼을 때
            if (isStudyMode || usedMinutes >= limitMinutes) {
                // 홈 화면으로 강제 이동
                performGlobalAction(GLOBAL_ACTION_HOME)

                // 2초 간격 팝업 메시지 노출 (UI 스레드 안전 처리)
                val now = System.currentTimeMillis()
                if (now - lastToastTime > 2000) {
                    lastToastTime = now
                    Handler(Looper.getMainLooper()).post {
                        val reason = if (isStudyMode) "📚 집중 공부 모드 활성화 중!" else "⏱️ 오늘 허용 시간(${limitMinutes}분) 초과! (사용: ${usedMinutes}분)"
                        Toast.makeText(applicationContext, "⚠️ 실행 차단: $reason", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    // 오늘 하루 누적 사용 시간 계산 (분 단위)
    private fun getTodayUsageMinutes(pkg: String): Int {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return 0
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, cal.timeInMillis, System.currentTimeMillis())
        val appStat = stats.find { it.packageName == pkg } ?: return 0
        return (appStat.totalTimeInForeground / 1000 / 60).toInt()
    }

    // 30분 지연 적용 로직
    private fun checkPendingSettings(prefs: android.content.SharedPreferences) {
        val pendingLimit = prefs.getInt("pending_limit", -1)
        val applyAt = prefs.getLong("pending_apply_at", 0L)

        if (pendingLimit != -1 && System.currentTimeMillis() >= applyAt) {
            prefs.edit()
                .putInt("youtube_limit_minutes", pendingLimit)
                .remove("pending_limit")
                .remove("pending_apply_at")
                .apply()
        }
    }

    override fun onInterrupt() {}
}

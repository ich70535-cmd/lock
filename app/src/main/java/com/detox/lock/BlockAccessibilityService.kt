package com.detox.lock

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class BlockAccessibilityService : AccessibilityService() {

    // 차단할 대상 패키지 목록 (추후 앱 설정에서 변경 가능)
    private val blockedPackages = setOf(
        "com.google.android.youtube", // 유튜브
        "com.instagram.android",      // 인스타그램
        "com.zhiliaoapp.musically"    // 틱톡
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.packageName == null) return

        val currentPackage = event.packageName.toString()

        // 차단 대상 앱 실행 감지 시
        if (blockedPackages.contains(currentPackage)) {
            // 1. 즉시 홈 화면으로 강제 이동
            performGlobalAction(GLOBAL_ACTION_HOME)

            // 2. 알림 메시지 출력
            Toast.makeText(
                applicationContext,
                "⚠️ 디톡스 모드 활성화 중: 실행이 차단되었습니다!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onInterrupt() {}
}

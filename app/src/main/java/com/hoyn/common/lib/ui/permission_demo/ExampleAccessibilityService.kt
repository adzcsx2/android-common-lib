package com.hoyn.common.lib.ui.permission_demo

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * 无障碍服务 Stub，仅用于演示无障碍服务权限请求
 */
class ExampleAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}

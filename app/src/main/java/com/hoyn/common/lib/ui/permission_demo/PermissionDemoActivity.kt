package com.hoyn.common.lib.ui.permission_demo

import android.os.Bundle
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.hjq.permissions.permission.base.IPermission
import com.hoyn.common.base.BaseActivity
import com.hoyn.common.base.NoViewModel
import com.hoyn.common.lib.R
import com.hoyn.common.lib.databinding.ActivityPermissionDemoBinding
import com.hoyn.common.ui.ext.click
import com.hoyn.common.ui.permission.isPermissionGranted
import com.hoyn.common.ui.permission.requestManageStoragePermission
import com.hoyn.common.ui.permission.requestPermission
import com.hoyn.common.ui.permission.requestStoragePermission
import com.hoyn.common.utils.Logger

/**
 * 权限请求演示页
 *
 * 覆盖 XXPermissions 官方示例的全部权限请求场景：
 * - 危险权限：相机、录音、位置(粗略+精确+后台)、联系人、日历、电话、多权限、活动识别、蓝牙、Wi-Fi、健康数据
 * - 存储权限：自动适配分区存储、所有文件访问、媒体位置信息、媒体读取(完整版)
 * - 特殊权限：通知、通知服务、通知监听、悬浮窗、安装应用、修改系统设置、使用统计、闹钟、勿扰、电池优化、画中画、管理媒体、已安装应用、全屏通知
 * - 高级特殊权限：VPN、设备管理器、无障碍服务
 * - 混合请求：危险权限+特殊权限同时申请
 * - 工具方法：权限状态检查、跳转设置页、打开应用详情页
 */
class PermissionDemoActivity : BaseActivity<ActivityPermissionDemoBinding, NoViewModel>() {

    companion object {
        private const val MAX_LOG_LINES = 50
    }

    override fun initView(savedInstanceState: Bundle?) {
        setupViews()
    }

    override fun initData() {}

    // ==================== 危险权限 ====================

    private fun setupDangerousPermissions() {
        // 相机权限
        // Manifest: <uses-permission android:name="android.permission.CAMERA" />
        binding.btnCamera.click {
            request("相机") { arrayOf(PermissionLists.getCameraPermission()) }
        }

        // 录音权限
        // Manifest: <uses-permission android:name="android.permission.RECORD_AUDIO" />
        binding.btnRecordAudio.click {
            request("录音") { arrayOf(PermissionLists.getRecordAudioPermission()) }
        }

        // 位置权限 - 粗略+精确定位
        // Manifest:
        //   <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
        //   <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
        binding.btnLocation.click {
            request("位置") {
                arrayOf(
                    PermissionLists.getAccessCoarseLocationPermission(),
                    PermissionLists.getAccessFineLocationPermission(),
                )
            }
        }

        // 后台定位权限
        // Manifest: <uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
        binding.btnBackgroundLocation.click {
            request("后台定位") { arrayOf(PermissionLists.getAccessBackgroundLocationPermission()) }
        }

        // 联系人权限
        // Manifest:
        //   <uses-permission android:name="android.permission.READ_CONTACTS" />
        //   <uses-permission android:name="android.permission.WRITE_CONTACTS" />
        binding.btnContacts.click {
            request("联系人") {
                arrayOf(
                    PermissionLists.getReadContactsPermission(),
                    PermissionLists.getWriteContactsPermission(),
                )
            }
        }

        // 日历权限
        // Manifest:
        //   <uses-permission android:name="android.permission.READ_CALENDAR" />
        //   <uses-permission android:name="android.permission.WRITE_CALENDAR" />
        binding.btnCalendar.click {
            request("日历") {
                arrayOf(
                    PermissionLists.getReadCalendarPermission(),
                    PermissionLists.getWriteCalendarPermission(),
                )
            }
        }

        // 电话状态权限
        // Manifest: <uses-permission android:name="android.permission.READ_PHONE_STATE" />
        binding.btnPhone.click {
            request("电话状态") { arrayOf(PermissionLists.getReadPhoneStatePermission()) }
        }

        // 多个危险权限同时申请 - 录音+日历读写
        // Manifest:
        //   <uses-permission android:name="android.permission.RECORD_AUDIO" />
        //   <uses-permission android:name="android.permission.READ_CALENDAR" />
        //   <uses-permission android:name="android.permission.WRITE_CALENDAR" />
        binding.btnMultiple.click {
            request("多个危险权限（录音+日历）") {
                arrayOf(
                    PermissionLists.getRecordAudioPermission(),
                    PermissionLists.getReadCalendarPermission(),
                    PermissionLists.getWriteCalendarPermission(),
                )
            }
        }

        // 活动识别权限（Android 10+）
        // Manifest: <uses-permission android:name="android.permission.ACTIVITY_RECOGNITION" />
        binding.btnActivityRecognition.click {
            request("活动识别") { arrayOf(PermissionLists.getActivityRecognitionPermission()) }
        }

        // 蓝牙权限（Android 12+）- 扫描+连接+广播
        // Manifest:
        //   <uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
        //   <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
        //   <uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />
        binding.btnBluetooth.click {
            request("蓝牙") {
                arrayOf(
                    PermissionLists.getBluetoothScanPermission(),
                    PermissionLists.getBluetoothConnectPermission(),
                    PermissionLists.getBluetoothAdvertisePermission(),
                )
            }
        }

        // Wi-Fi 设备权限（Android 13+）
        // Manifest: <uses-permission android:name="android.permission.NEARBY_WIFI_DEVICES" />
        binding.btnWifi.click {
            request("Wi-Fi") { arrayOf(PermissionLists.getNearbyWifiDevicesPermission()) }
        }

        // 健康数据权限（Android 14+）
        // Manifest:
        //   <uses-permission android:name="android.permission.health.READ_SLEEP" />
        //   <uses-permission android:name="android.permission.health.READ_ACTIVE_CALORIES_BURNED" />
        //   <uses-permission android:name="android.permission.health.READ_EXERCISE" />
        //   <uses-permission android:name="android.permission.health.READ_HEART_RATE" />
        //   <uses-permission android:name="android.permission.health.WRITE_HEART_RATE" />
        //   <application> 内需添加:
        //     <meta-data android:name="ScopedStorage" android:value="true" />
        //   当前 Activity 需添加 intent-filter:
        //     <intent-filter>
        //       <action android:name="android.intent.action.VIEW_PERMISSION_USAGE" />
        //       <category android:name="android.intent.category.HEALTH_PERMISSIONS" />
        //     </intent-filter>
        binding.btnHealth.click {
            request("健康数据") {
                arrayOf(
                    PermissionLists.getReadSleepPermission(),
                    PermissionLists.getReadActiveCaloriesBurnedPermission(),
                    PermissionLists.getReadExercisePermission(),
                    PermissionLists.getReadHeartRatePermission(),
                    PermissionLists.getWriteHeartRatePermission(),
                )
            }
        }
    }

    // ==================== 存储权限 ====================

    private fun setupStoragePermissions() {
        // 存储权限 - 自动适配 Android 版本
        // Manifest (按 targetSdk 版本自动选择):
        //   Android 12 及以下:
        //     <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
        //     <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
        //   Android 13+:
        //     <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
        //     <uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
        //     <uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
        //   Android 14+ 额外:
        //     <uses-permission android:name="android.permission.READ_MEDIA_VISUAL_USER_SELECTED" />
        //   <application> 内需添加:
        //     <meta-data android:name="ScopedStorage" android:value="true" />
        binding.btnStorage.click {
            requestStoragePermission(
                onGranted = { log("存储权限已授予") },
                onDenied = { log("存储权限被拒绝") }
            )
        }

        // 所有文件访问权限（Android 11+）
        // Manifest: <uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />
        binding.btnManageStorage.click {
            requestManageStoragePermission(
                onGranted = { log("所有文件访问权限已授予") },
                onDenied = { log("所有文件访问权限被拒绝") }
            )
        }

        // 媒体位置信息权限
        // Manifest:
        //   <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
        //   <uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
        //   <uses-permission android:name="android.permission.ACCESS_MEDIA_LOCATION" />
        binding.btnMediaLocation.click {
            request("媒体位置信息") {
                arrayOf(
                    PermissionLists.getReadMediaImagesPermission(),
                    PermissionLists.getReadMediaVideoPermission(),
                    PermissionLists.getAccessMediaLocationPermission(),
                )
            }
        }

        // 媒体读取权限（完整版）
        // Manifest:
        //   <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
        //   <uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
        //   <uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
        //   <uses-permission android:name="android.permission.READ_MEDIA_VISUAL_USER_SELECTED" />
        //   <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
        //   <application> 内需添加:
        //     <meta-data android:name="ScopedStorage" android:value="true" />
        binding.btnReadMedia.click {
            request("媒体读取（完整版）") {
                arrayOf(
                    PermissionLists.getReadMediaImagesPermission(),
                    PermissionLists.getReadMediaVideoPermission(),
                    PermissionLists.getReadMediaAudioPermission(),
                    PermissionLists.getReadMediaVisualUserSelectedPermission(),
                    PermissionLists.getWriteExternalStoragePermission(),
                )
            }
        }
    }

    // ==================== 特殊权限 ====================

    private fun setupSpecialPermissions() {
        // 通知权限（Android 13+）
        // Manifest: <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
        binding.btnNotification.click {
            request("通知") { arrayOf(PermissionLists.getPostNotificationsPermission()) }
        }

        // 通知服务权限（带通知渠道）
        // Manifest: <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
        binding.btnNotificationService.click {
            request("通知服务") {
                arrayOf(PermissionLists.getNotificationServicePermission("permission_demo_channel"))
            }
        }

        // 通知监听权限
        // Manifest:
        //   <service android:name=".ExampleNotificationListenerService"
        //       android:exported="false"
        //       android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE">
        //     <intent-filter>
        //       <action android:name="android.service.notification.NotificationListenerService" />
        //     </intent-filter>
        //   </service>
        binding.btnNotificationListener.click {
            request("通知监听") {
                arrayOf(
                    PermissionLists.getBindNotificationListenerServicePermission(
                        ExampleNotificationListenerService::class.java
                    )
                )
            }
        }

        // 悬浮窗权限
        // Manifest: <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
        binding.btnOverlay.click {
            request("悬浮窗") { arrayOf(PermissionLists.getSystemAlertWindowPermission()) }
        }

        // 安装未知来源应用权限
        // Manifest: <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
        binding.btnInstall.click {
            request("安装应用") { arrayOf(PermissionLists.getRequestInstallPackagesPermission()) }
        }

        // 修改系统设置权限
        // Manifest: <uses-permission android:name="android.permission.WRITE_SETTINGS" />
        binding.btnWriteSettings.click {
            request("修改系统设置") { arrayOf(PermissionLists.getWriteSettingsPermission()) }
        }

        // 使用情况访问权限
        // Manifest: <uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" />
        binding.btnUsageStats.click {
            request("使用情况访问") { arrayOf(PermissionLists.getPackageUsageStatsPermission()) }
        }

        // 闹钟和提醒权限
        // Manifest: <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
        binding.btnAlarm.click {
            request("闹钟和提醒") { arrayOf(PermissionLists.getScheduleExactAlarmPermission()) }
        }

        // 勿扰模式访问权限
        // Manifest: <uses-permission android:name="android.permission.ACCESS_NOTIFICATION_POLICY" />
        binding.btnDnd.click {
            request("勿扰模式") { arrayOf(PermissionLists.getAccessNotificationPolicyPermission()) }
        }

        // 忽略电池优化权限
        // Manifest: <uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
        binding.btnBattery.click {
            request("忽略电池优化") {
                arrayOf(PermissionLists.getRequestIgnoreBatteryOptimizationsPermission())
            }
        }

        // 画中画权限
        // Manifest: 无需声明权限，自动支持
        binding.btnPip.click {
            request("画中画") { arrayOf(PermissionLists.getPictureInPicturePermission()) }
        }

        // 管理媒体权限（Android 13+）
        // Manifest: <uses-permission android:name="android.permission.MANAGE_MEDIA" />
        binding.btnManageMedia.click {
            request("管理媒体") { arrayOf(PermissionLists.getManageMediaPermission()) }
        }

        // 获取已安装应用权限（Android 11+）
        // Manifest: <uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />
        binding.btnInstalledApps.click {
            request("获取已安装应用") { arrayOf(PermissionLists.getGetInstalledAppsPermission()) }
        }

        // 全屏通知权限（Android 14+）
        // Manifest:
        //   <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
        //   <uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />
        binding.btnFullscreenNotification.click {
            request("全屏通知") {
                arrayOf(
                    PermissionLists.getPostNotificationsPermission(),
                    PermissionLists.getUseFullScreenIntentPermission(),
                )
            }
        }
    }

    // ==================== 高级特殊权限 ====================

    private fun setupAdvancedPermissions() {
        // VPN 权限
        // Manifest:
        //   <service android:name=".ExampleVpnService"
        //       android:exported="false"
        //       android:permission="android.permission.BIND_VPN_SERVICE">
        //     <intent-filter>
        //       <action android:name="android.net.VpnService" />
        //     </intent-filter>
        //   </service>
        binding.btnVpn.click {
            request("VPN") { arrayOf(PermissionLists.getBindVpnServicePermission()) }
        }

        // 设备管理器权限
        // Manifest:
        //   <receiver android:name=".ExampleDeviceAdminReceiver"
        //       android:exported="true"
        //       android:permission="android.permission.BIND_DEVICE_ADMIN">
        //     <meta-data android:name="android.app.device_admin"
        //         android:resource="@xml/device_admin_policies" />
        //     <intent-filter>
        //       <action android:name="android.app.action.DEVICE_ADMIN_ENABLED" />
        //     </intent-filter>
        //   </receiver>
        binding.btnDeviceAdmin.click {
            request("设备管理器") {
                arrayOf(
                    PermissionLists.getBindDeviceAdminPermission(
                        ExampleDeviceAdminReceiver::class.java,
                        getString(R.string.permission_demo_device_admin)
                    )
                )
            }
        }

        // 无障碍服务权限
        // Manifest:
        //   <service android:name=".ExampleAccessibilityService"
        //       android:exported="false"
        //       android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
        //     <meta-data android:name="android.accessibilityservice"
        //         android:resource="@xml/accessibility_service_config" />
        //     <intent-filter>
        //       <action android:name="android.accessibilityservice.AccessibilityService" />
        //     </intent-filter>
        //   </service>
        binding.btnAccessibility.click {
            request("无障碍服务") {
                arrayOf(
                    PermissionLists.getBindAccessibilityServicePermission(
                        ExampleAccessibilityService::class.java
                    )
                )
            }
        }
    }

    // ==================== 混合请求 ====================

    private fun setupMixedRequest() {
        // 混合权限 - 通话记录（危险）+ 悬浮窗（特殊）
        // Manifest:
        //   <uses-permission android:name="android.permission.READ_CALL_LOG" />
        //   <uses-permission android:name="android.permission.WRITE_CALL_LOG" />
        //   <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
        binding.btnMixed.click {
            request("混合权限（通话记录+悬浮窗）") {
                arrayOf(
                    PermissionLists.getReadCallLogPermission(),
                    PermissionLists.getWriteCallLogPermission(),
                    PermissionLists.getSystemAlertWindowPermission(),
                )
            }
        }
    }

    // ==================== 工具方法 ====================

    private fun setupTools() {
        // 检查权限状态
        binding.btnCheckPermission.click {
            val granted = isPermissionGranted(PermissionLists.getCameraPermission())
            log("相机权限状态: ${if (granted) "已授予" else "未授予"}")
        }

        // 跳转权限设置页
        binding.btnOpenSettings.click {
            XXPermissions.startPermissionActivity(this)
            log("已跳转到权限设置页")
        }

        // 打开应用详情页
        binding.btnAppDetails.click {
            XXPermissions.startPermissionActivity(
                this, PermissionLists.getCameraPermission()
            )
            log("已跳转到应用详情页")
        }

        // 清空日志
        binding.btnClearLog.click {
            binding.tvLog.text = ""
        }
    }

    // ==================== 辅助方法 ====================

    private fun setupViews() {
        binding.btnBack.click { finish() }
        setupDangerousPermissions()
        setupStoragePermissions()
        setupSpecialPermissions()
        setupAdvancedPermissions()
        setupMixedRequest()
        setupTools()
    }

    /**
     * 通用的权限请求辅助方法，统一处理授予/拒绝回调
     */
    private fun request(label: String, permissions: () -> Array<IPermission>) {
        requestPermission(
            permissions = permissions(),
            onGranted = { log("${label}权限已授予") },
            onDenied = { log("${label}权限被拒绝") }
        )
    }

    /**
     * 输出日志到控制台和界面 TextView，保留最近 MAX_LOG_LINES 条
     */
    private fun log(message: String) {
        Logger.e(message)
        val current = binding.tvLog.text?.toString()?.takeIf { it.isNotBlank() } ?: ""
        val allLines = (current + "\n" + message).lines()
        binding.tvLog.text = allLines.takeLast(MAX_LOG_LINES).joinToString("\n")
    }
}

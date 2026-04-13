package com.hoyn.common.lib.ui.permission_demo

import android.os.Build
import android.os.Bundle
import com.blankj.utilcode.util.LogUtils
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.hoyn.common.base.BaseActivity
import com.hoyn.common.base.NoViewModel
import com.hoyn.common.lib.databinding.ActivityPermissionDemoBinding
import com.hoyn.common.ui.ext.click
import com.hoyn.common.ui.permission.isPermissionGranted
import com.hoyn.common.ui.permission.requestManageStoragePermission
import com.hoyn.common.ui.permission.requestPermission
import com.hoyn.common.ui.permission.requestStoragePermission

/**
 * 权限请求演示页
 *
 * 基于 XXPermissions 封装的 PermissionUtils，展示各类权限请求场景：
 * - 危险权限：相机、录音、位置、联系人、日历、电话
 * - 存储权限：自动适配 Android 10+ 分区存储策略
 * - 特殊权限：通知、悬浮窗、安装应用
 * - 工具方法：权限状态检查、跳转系统设置页
 *
 * 所有请求结果通过 LogUtils 输出，方便调试观察。
 */
class PermissionDemoActivity : BaseActivity<ActivityPermissionDemoBinding, NoViewModel>() {

    override fun initView(savedInstanceState: Bundle?) {
        setupViews()
    }

    override fun initData() {}

    /**
     * 初始化所有按钮点击事件
     *
     * 按功能分为四个区域：危险权限、存储权限、特殊权限、工具方法
     */
    private fun setupViews() {
        binding.btnBack.click { finish() }

        // ==================== 危险权限 ====================

        // 相机权限 - 单个权限请求的最简示例 无拒绝回调
        binding.btnCamera.click {
            requestPermission(
                permissions = arrayOf(PermissionLists.getCameraPermission())
            ) {
                log("相机权限已授予")
            }
        }

        // 录音权限 单个权限请求的最简示例 有拒绝回调
        binding.btnRecordAudio.click {
            requestPermission(
                permissions = arrayOf(PermissionLists.getRecordAudioPermission()),
                onGranted = { log("录音权限已授予") },
                onDenied = { log("录音权限被拒绝") }
            )
        }

        // 位置权限 - 同时申请粗略定位和精确定位
        binding.btnLocation.click {
            requestPermission(
                permissions = arrayOf(
                    PermissionLists.getAccessCoarseLocationPermission(),
                    PermissionLists.getAccessFineLocationPermission(),
                ),
                onGranted = { log("位置权限已授予") },
                onDenied = { log("位置权限被拒绝") }
            )
        }

        // 联系人权限 - 同时申请读和写
        binding.btnContacts.click {
            requestPermission(
                permissions = arrayOf(
                    PermissionLists.getReadContactsPermission(),
                    PermissionLists.getWriteContactsPermission(),
                ),
                onGranted = { log("联系人权限已授予") },
                onDenied = { log("联系人权限被拒绝") }
            )
        }

        // 日历权限 - 同时申请读和写
        binding.btnCalendar.click {
            requestPermission(
                permissions = arrayOf(
                    PermissionLists.getReadCalendarPermission(),
                    PermissionLists.getWriteCalendarPermission(),
                ),
                onGranted = { log("日历权限已授予") },
                onDenied = { log("日历权限被拒绝") }
            )
        }

        // 电话状态权限
        binding.btnPhone.click {
            requestPermission(
                permissions = arrayOf(PermissionLists.getReadPhoneStatePermission()),
                onGranted = { log("电话状态权限已授予") },
                onDenied = { log("电话状态权限被拒绝") }
            )
        }

        // ==================== 存储权限（分区存储适配） ====================

        // 存储权限 - 自动适配当前 Android 版本所需的存储权限
        // Android 14+: READ_MEDIA_IMAGES + READ_MEDIA_VIDEO + READ_MEDIA_AUDIO + READ_MEDIA_VISUAL_USER_SELECTED
        // Android 13:  READ_MEDIA_IMAGES + READ_MEDIA_VIDEO + READ_MEDIA_AUDIO
        // Android 12-: READ_EXTERNAL_STORAGE
        binding.btnStorage.click {
            requestStoragePermission(
                onGranted = { log("存储权限已授予") },
                onDenied = { log("存储权限被拒绝") }
            )
        }

        // 所有文件访问权限 - 仅 Android 11+ 可用
        binding.btnManageStorage.click {
            if (Build.VERSION.SDK_INT < 30) {
                log("所有文件访问权限需要 Android 11 及以上版本")
                return@click
            }
            requestManageStoragePermission(
                onGranted = { log("所有文件访问权限已授予") },
                onDenied = { log("所有文件访问权限被拒绝") }
            )
        }

        // ==================== 特殊权限 ====================

        // 通知权限 - Android 13+ 新增的运行时权限
        binding.btnNotification.click {
            if (Build.VERSION.SDK_INT < 33) {
                log("通知权限需要 Android 13 及以上版本")
                return@click
            }
            requestPermission(
                permissions = arrayOf(PermissionLists.getPostNotificationsPermission()),
                onGranted = { log("通知权限已授予") },
                onDenied = { log("通知权限被拒绝") }
            )
        }

        // 悬浮窗权限 - 通过系统设置页授权的特殊权限
        binding.btnOverlay.click {
            requestPermission(
                permissions = arrayOf(PermissionLists.getSystemAlertWindowPermission()),
                onGranted = { log("悬浮窗权限已授予") },
                onDenied = { log("悬浮窗权限被拒绝") }
            )
        }

        // 安装未知来源应用权限 - 通过系统设置页授权的特殊权限
        binding.btnInstall.click {
            requestPermission(
                permissions = arrayOf(PermissionLists.getRequestInstallPackagesPermission()),
                onGranted = { log("安装应用权限已授予") },
                onDenied = { log("安装应用权限被拒绝") }
            )
        }

        // ==================== 工具方法 ====================

        // 检查权限状态 - 不发起请求，仅查询当前授权状态
        binding.btnCheckPermission.click {
            val granted = isPermissionGranted(PermissionLists.getCameraPermission())
            log("相机权限状态: ${if (granted) "已授予" else "未授予"}")
        }

        // 跳转系统权限设置页
        binding.btnOpenSettings.click {
            XXPermissions.startPermissionActivity(this)
            log("已跳转到权限设置页")
        }
    }

    /**
     * 输出日志
     *
     * @param message 日志内容
     */
    private fun log(message: String) {
        LogUtils.e(message)
    }
}

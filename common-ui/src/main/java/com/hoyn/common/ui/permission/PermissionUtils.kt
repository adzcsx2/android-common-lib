@file:Suppress("unused")

package com.hoyn.common.ui.permission

import android.app.Activity
import android.content.Context
import android.os.Build
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.hjq.permissions.permission.base.IPermission
import com.hoyn.common.ui.R

/**
 * 权限请求工具类，基于 XXPermissions 封装。
 *
 * 提供两种使用方式：
 *
 * 方式一 - 直接调用：
 * ```kotlin
 * PermissionUtils.request(
 *     activity = this,
 *     permissions = arrayOf(PermissionLists.getCameraPermission()),
 *     onGranted = { /* 权限已授予 */ }
 * )
 * ```
 *
 * 方式二 - 扩展函数（推荐）：
 * ```kotlin
 * requestPermission(
 *     permissions = arrayOf(PermissionLists.getCameraPermission()),
 *     onGranted = { /* 权限已授予 */ }
 * )
 *
 * requestStoragePermission(
 *     onGranted = { /* 存储权限已授予 */ }
 * )
 * ```
 */
private object PermissionUtils {

    /**
     * 检查所有给定权限是否已授予
     */
    fun isGranted(context: Context, vararg permissions: IPermission): Boolean {
        return XXPermissions.isGrantedPermissions(context, permissions.toList())
    }

    /**
     * 请求权限，自动处理以下场景：
     * - 已授权 → 直接回调 onGranted，不弹系统对话框
     * - 拒绝但可再问 → 回调 onDenied
     * - "不再询问" → 自动弹 Dialog 引导跳转设置页
     *
     * @param activity    宿主 Activity
     * @param permissions 要请求的权限（使用 [PermissionLists] 工厂方法获取）
     * @param onGranted   全部权限授予时回调
     * @param onDenied    权限被拒绝时回调（可选）
     */
    fun request(
        activity: Activity,
        permissions: Array<IPermission>,
        onGranted: () -> Unit,
        onDenied: (() -> Unit)? = null
    ) {
        if (isGranted(activity, *permissions)) {
            onGranted()
            return
        }

        XXPermissions.with(activity)
            .permissions(permissions.toList())
            .request(object : OnPermissionCallback {
                override fun onResult(
                    grantedList: MutableList<IPermission>,
                    deniedList: MutableList<IPermission>
                ) {
                    if (deniedList.isEmpty()) {
                        onGranted()
                        return
                    }

                    val hasDoNotAskAgain = XXPermissions.isDoNotAskAgainPermissions(
                        activity, deniedList
                    )

                    if (hasDoNotAskAgain) {
                        showPermissionDeniedDialog(activity, deniedList, onGranted, onDenied)
                    } else {
                        onDenied?.invoke()
                    }
                }
            })
    }

    /**
     * 请求存储权限，自动适配 Android 版本：
     * - Android 14+: READ_MEDIA_IMAGES + READ_MEDIA_VIDEO + READ_MEDIA_AUDIO + READ_MEDIA_VISUAL_USER_SELECTED
     * - Android 13:  READ_MEDIA_IMAGES + READ_MEDIA_VIDEO + READ_MEDIA_AUDIO
     * - Android 12-: READ_EXTERNAL_STORAGE
     */
    fun requestStorage(
        activity: Activity,
        onGranted: () -> Unit,
        onDenied: (() -> Unit)? = null
    ) {
        request(activity, getStoragePermissions(), onGranted, onDenied)
    }

    /**
     * 获取当前 Android 版本对应的存储权限列表
     */
    fun getStoragePermissions(): Array<IPermission> {
        return when {
            Build.VERSION.SDK_INT >= 34 -> arrayOf(
                PermissionLists.getReadMediaImagesPermission(),
                PermissionLists.getReadMediaVideoPermission(),
                PermissionLists.getReadMediaAudioPermission(),
                PermissionLists.getReadMediaVisualUserSelectedPermission(),
            )

            Build.VERSION.SDK_INT >= 33 -> arrayOf(
                PermissionLists.getReadMediaImagesPermission(),
                PermissionLists.getReadMediaVideoPermission(),
                PermissionLists.getReadMediaAudioPermission(),
            )

            else -> arrayOf(
                PermissionLists.getReadExternalStoragePermission(),
            )
        }
    }

    /**
     * 获取"所有文件访问"权限对象（Android 11+）
     */
    fun getManageStoragePermission(): IPermission {
        return PermissionLists.getManageExternalStoragePermission()
    }

    /**
     * 请求"所有文件访问"权限（Android 11+）
     */
    fun requestManageStorage(
        activity: Activity,
        onGranted: () -> Unit,
        onDenied: (() -> Unit)? = null
    ) {
        request(activity, arrayOf(getManageStoragePermission()), onGranted, onDenied)
    }

    /**
     * 跳转系统权限设置页，用户返回后自动重新检查权限并回调
     */
    fun startPermissionActivity(
        activity: Activity,
        permissions: Array<IPermission>,
        onGranted: (() -> Unit)? = null,
        onDenied: (() -> Unit)? = null
    ) {
        XXPermissions.startPermissionActivity(
            activity, permissions.toList(),
            object : OnPermissionCallback {
                override fun onResult(
                    grantedList: MutableList<IPermission>,
                    deniedList: MutableList<IPermission>
                ) {
                    if (deniedList.isEmpty()) {
                        onGranted?.invoke()
                    } else {
                        onDenied?.invoke()
                    }
                }
            })
    }

    /**
     * 用户勾选"不再询问"时弹出引导 Dialog，点击"设置"跳转权限设置页
     */
    private fun showPermissionDeniedDialog(
        activity: Activity,
        deniedPermissions: List<IPermission>,
        onGranted: () -> Unit,
        onDenied: (() -> Unit)?
    ) {
        if (activity.isFinishing || activity.isDestroyed) return

        val dialog = android.app.AlertDialog.Builder(activity)
            .setTitle(R.string.permission_denied_title)
            .setMessage(activity.getString(R.string.permission_denied_message))
            .setPositiveButton(R.string.permission_denied_settings) { _, _ ->
                startPermissionActivity(
                    activity,
                    deniedPermissions.toTypedArray(),
                    onGranted = onGranted,
                    onDenied = onDenied
                )
            }
            .setNegativeButton(R.string.permission_denied_cancel) { dialog, _ ->
                dialog.dismiss()
                onDenied?.invoke()
            }
            .setCancelable(false)
            .create()

        dialog.show()
    }
}


// ==================== Activity 扩展函数 ====================

/**
 * 请求权限
 *
 * @param permissions 要请求的权限数组
 * @param onGranted   全部权限授予时回调
 * @param onDenied    权限被拒绝时回调（可选）
 */
fun Activity.requestPermission(
    permissions: Array<IPermission>,
    onGranted: () -> Unit,
    onDenied: (() -> Unit)? = null
) {
    PermissionUtils.request(this, permissions, onGranted, onDenied)
}

fun Activity.requestPermission(
    permissions: Array<IPermission>,
    onGranted: () -> Unit
) {
    PermissionUtils.request(this, permissions, onGranted)
}

/**
 * 请求存储权限（自动适配 Android 10+ 分区存储）
 *
 * @param onGranted 存储权限授予时回调
 * @param onDenied  存储权限被拒绝时回调（可选）
 */
fun Activity.requestStoragePermission(
    onGranted: () -> Unit,
    onDenied: (() -> Unit)? = null
) {
    PermissionUtils.requestStorage(this, onGranted, onDenied)
}

fun Activity.requestStoragePermission(
    onGranted: () -> Unit
) {
    PermissionUtils.requestStorage(this, onGranted)
}

/**
 * 请求"所有文件访问"权限（Android 11+）
 *
 * @param onGranted 权限授予时回调
 * @param onDenied  权限被拒绝时回调（可选）
 */
fun Activity.requestManageStoragePermission(
    onGranted: () -> Unit,
    onDenied: (() -> Unit)? = null
) {
    PermissionUtils.requestManageStorage(this, onGranted, onDenied)
}

fun Activity.requestManageStoragePermission(
    onGranted: () -> Unit
) {
    PermissionUtils.requestManageStorage(this, onGranted)
}

/**
 * 检查权限是否已授予（不发起请求）
 *
 * @param permissions 要检查的权限
 * @return 全部已授予返回 true
 */
fun Activity.isPermissionGranted(vararg permissions: IPermission): Boolean {
    return PermissionUtils.isGranted(this, *permissions)
}

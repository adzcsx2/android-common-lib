package com.hoyn.common.ui.permission

import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData

/**
 * 权限请求 Fragment
 *
 * @author GaoPC
 * @date 2019-10-25
 */
internal class LiveFragment : Fragment() {

    /** 权限请求结果 LiveData，用于通知调用方权限状态变化 */
    lateinit var liveData: MutableLiveData<PermissionResult>

    /** 权限请求码 */
    private val PERMISSIONS_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 保留 Fragment 实例，避免配置变更时重建
        retainInstance = true
    }

    /**
     * 请求权限
     *
     * 先过滤掉已授权的权限，仅对未授权的权限发起系统请求
     *
     * @param permissions 要请求的权限数组
     */
    @TargetApi(Build.VERSION_CODES.M)
    fun requestPermissions(permissions: Array<out String>) {
        liveData = MutableLiveData()
        // 过滤出未授权的权限
        val tempPermission = ArrayList<String>()
        permissions.forEach {
            if (activity?.checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED) {
                tempPermission.add(it)
            }
        }
        // 所有权限已授权，直接返回 Grant
        if (tempPermission.isEmpty()) {
            liveData.value = PermissionResult.Grant
        } else {
            requestPermissions(tempPermission.toTypedArray(), PERMISSIONS_REQUEST_CODE)
        }
    }

    /**
     * 处理权限请求结果
     *
     * 根据授权状态将权限分为已拒绝（可再次请求）和已拒绝（不再询问）两类
     *
     * @param requestCode 请求码
     * @param permissions 请求的权限数组
     * @param grantResults 授权结果数组
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            // 被拒绝且勾选了"不再询问"的权限
            val denyPermission = ArrayList<String>()
            // 被拒绝但未勾选"不再询问"的权限，可再次请求
            val rationalePermission = ArrayList<String>()
            for ((index, value) in grantResults.withIndex()) {
                if (value == PackageManager.PERMISSION_DENIED) {
                    if (shouldShowRequestPermissionRationale(permissions[index])) {
                        rationalePermission.add(permissions[index])
                    } else {
                        denyPermission.add(permissions[index])
                    }
                }
            }
            // 所有权限都已授权
            if (denyPermission.isEmpty() && rationalePermission.isEmpty()) {
                liveData.value = PermissionResult.Grant
            } else {
                // 优先返回 Rationale（可再次请求的拒绝）
                if (rationalePermission.isNotEmpty()) {
                    liveData.value = PermissionResult.Rationale(rationalePermission.toTypedArray())
                } else if (denyPermission.isNotEmpty()) {
                    liveData.value = PermissionResult.Deny(denyPermission.toTypedArray())
                }
            }
        }
    }
}

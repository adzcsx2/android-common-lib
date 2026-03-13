package com.hoyn.common.ui.permission

/**
 * 权限请求结果
 *
 * @author GaoPC
 * @date 2019-10-28
 */
sealed class PermissionResult {
    /**
     * 权限已授予
     */
    object Grant : PermissionResult()

    /**
     * 权限被拒绝（勾选了"不再询问"）
     */
    class Deny(val permissions: Array<String>) : PermissionResult()

    /**
     * 权限被拒绝（未勾选"不再询问"，可再次请求）
     */
    class Rationale(val permissions: Array<String>) : PermissionResult()
}

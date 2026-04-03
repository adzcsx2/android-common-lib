package com.hoyn.common.lib.ui.camera

import android.Manifest
import android.os.Build

internal data class CameraStartupPermissionRequirement(
    val runtimePermissions: List<String>,
    val promptsManageExternalStorage: Boolean
)

internal object CameraStartupPermissionPolicy {

    fun resolve(sdkInt: Int): CameraStartupPermissionRequirement {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
        if (sdkInt <= Build.VERSION_CODES.P) {
            permissions += Manifest.permission.WRITE_EXTERNAL_STORAGE
        }
        return CameraStartupPermissionRequirement(
            runtimePermissions = permissions,
            promptsManageExternalStorage = sdkInt >= Build.VERSION_CODES.R
        )
    }

    fun hasAllRuntimePermissions(
        requirement: CameraStartupPermissionRequirement,
        grantedPermissions: Set<String>
    ): Boolean {
        return requirement.runtimePermissions.all { permission -> permission in grantedPermissions }
    }

    fun canProceed(
        requirement: CameraStartupPermissionRequirement,
        grantedPermissions: Set<String>
    ): Boolean {
        return hasAllRuntimePermissions(requirement, grantedPermissions)
    }
}
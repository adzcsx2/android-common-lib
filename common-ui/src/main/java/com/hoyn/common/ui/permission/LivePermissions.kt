package com.hoyn.common.ui.permission

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData

/**
 * 基于 LiveData 的权限请求工具
 *
 * @author GaoPC
 * @date 2019-10-25
 */
class LivePermissions {

    companion object {
        /** 权限 Fragment 的 Tag */
        const val TAG = "permissions"
    }

    /**
     * 构造函数，基于 AppCompatActivity
     *
     * @param activity 宿主 Activity
     */
    constructor(activity: AppCompatActivity) {
        liveFragment = getInstance(activity.supportFragmentManager)
    }

    /**
     * 构造函数，基于 Fragment
     *
     * @param fragment 宿主 Fragment
     */
    constructor(fragment: Fragment) {
        liveFragment = getInstance(fragment.childFragmentManager)
    }

    /** 权限请求 Fragment 实例，使用 volatile 保证多线程可见性 */
    @Volatile
    private var liveFragment: LiveFragment? = null

    /**
     * 获取或创建 LiveFragment 单例
     *
     * 使用双重检查锁定确保线程安全，通过 Fragment Tag 避免重复创建
     *
     * @param fragmentManager FragmentManager 实例
     * @return LiveFragment 实例
     */
    private fun getInstance(fragmentManager: FragmentManager) = liveFragment ?: synchronized(this) {
        liveFragment ?: if (fragmentManager.findFragmentByTag(TAG) == null) LiveFragment().run {
            fragmentManager.beginTransaction().add(this, TAG).commitNow()
            this
        } else fragmentManager.findFragmentByTag(TAG) as LiveFragment
    }

    /**
     * 请求权限
     *
     * @param permissions 要请求的权限数组
     * @return MutableLiveData，可观察权限请求结果
     */
    fun request(vararg permissions: String): MutableLiveData<PermissionResult> {
        return this.requestArray(permissions)
    }

    /**
     * 请求权限（数组形式）
     *
     * @param permissions 要请求的权限数组
     * @return MutableLiveData，可观察权限请求结果
     */
    fun requestArray(permissions: Array<out String>): MutableLiveData<PermissionResult> {
        liveFragment!!.requestPermissions(permissions)
        return liveFragment!!.liveData
    }
}

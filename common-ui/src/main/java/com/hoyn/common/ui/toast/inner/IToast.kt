package com.hoyn.common.ui.toast.inner

import android.view.View
import com.hoyn.common.ui.toast.ToastDuration

/**
 * Toast 接口
 *
 * 定义 Toast 的基本操作接口，包括显示、隐藏、设置属性等
 */
interface IToast {

    /**
     * 显示 Toast
     */
    fun show()

    /**
     * 取消 Toast
     */
    fun cancel()

    /**
     * 设置 Toast 的视图
     *
     * @param mView 要设置的视图
     * @return 当前 Toast 实例，支持链式调用
     */
    fun setView(mView: View): IToast

    /**
     * 获取 Toast 的视图
     *
     * @return Toast 的视图对象
     */
    fun getView(): View

    /**
     * 设置 Toast 显示时长
     *
     * @param duration 显示时长，使用 ToastDuration 注解标记
     * @return 当前 Toast 实例，支持链式调用
     */
    fun setDuration(@ToastDuration duration: Int): IToast

    /**
     * 设置 Toast 显示位置（默认偏移）
     *
     * @param gravity 显示位置，使用 Gravity 常量
     * @return 当前 Toast 实例，支持链式调用
     */
    fun setGravity(gravity: Int): IToast

    /**
     * 设置 Toast 显示位置（带偏移）
     *
     * @param gravity 显示位置，使用 Gravity 常量
     * @param xOffset X 轴偏移量
     * @param yOffset Y 轴偏移量
     * @return 当前 Toast 实例，支持链式调用
     */
    fun setGravity(gravity: Int, xOffset: Int, yOffset: Int): IToast

    /**
     * 设置 Toast 显示动画
     *
     * @param animation 动画资源 ID
     * @return 当前 Toast 实例，支持链式调用
     */
    fun setAnimation(animation: Int): IToast

    /**
     * 设置 Toast 优先级
     *
     * @param mPriority 优先级值
     * @return 当前 Toast 实例，支持链式调用
     */
    fun setPriority(mPriority: Int): IToast
}

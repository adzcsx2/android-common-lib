package com.hoyn.common.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

/**
 * ViewBinding 类解析器
 *
 * 通过反射获取 BaseActivity/BaseFragment 中声明的 ViewBinding 泛型类型，
 * 并调用对应的 inflate 方法创建绑定实例
 */
internal object ViewBindingClassResolver {

    /**
     * 为 Activity 创建 ViewBinding 实例
     *
     * 通过反射解析 ViewBinding 泛型类型，并调用其 inflate 方法
     *
     * @param owner 拥有 ViewBinding 泛型的对象（如 BaseActivity）
     * @param baseClass 基类的 Class 对象
     * @return ViewBinding 实例
     */
    fun <VB : ViewBinding> inflateActivityBinding(owner: Any, baseClass: Class<*>): VB {
        val bindingClass = ViewModelClassResolver.resolveGenericClass<VB>(
            owner = owner,
            baseClass = baseClass,
            typeArgumentIndex = 0,
            expectedSuperClass = ViewBinding::class.java
        )
        val inflateMethod = bindingClass.getMethod("inflate", LayoutInflater::class.java)
        @Suppress("UNCHECKED_CAST")
        return inflateMethod.invoke(null, (owner as android.app.Activity).layoutInflater) as VB
    }

    /**
     * 为 Fragment 创建 ViewBinding 实例
     *
     * 通过反射解析 ViewBinding 泛型类型，并调用其 inflate 方法（带容器参数）
     *
     * @param owner 拥有 ViewBinding 泛型的对象（如 BaseFragment）
     * @param baseClass 基类的 Class 对象
     * @param inflater 布局填充器
     * @param container 父容器视图
     * @return ViewBinding 实例
     */
    fun <VB : ViewBinding> inflateFragmentBinding(
        owner: Any,
        baseClass: Class<*>,
        inflater: LayoutInflater,
        container: ViewGroup?
    ): VB {
        val bindingClass = ViewModelClassResolver.resolveGenericClass<VB>(
            owner = owner,
            baseClass = baseClass,
            typeArgumentIndex = 0,
            expectedSuperClass = ViewBinding::class.java
        )
        val inflateMethod = bindingClass.getMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.javaPrimitiveType
        )
        @Suppress("UNCHECKED_CAST")
        return inflateMethod.invoke(null, inflater, container, false) as VB
    }
}
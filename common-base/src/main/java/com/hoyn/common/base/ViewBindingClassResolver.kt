package com.hoyn.common.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

internal object ViewBindingClassResolver {

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
package com.hoyn.common.base

import androidx.lifecycle.ViewModel
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable

internal object ViewModelClassResolver {

    fun <VM : ViewModel> resolve(owner: Any, baseClass: Class<*>): Class<VM> {
        return resolveGenericClass(
            owner = owner,
            baseClass = baseClass,
            typeArgumentIndex = 1,
            expectedSuperClass = ViewModel::class.java
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> resolveGenericClass(
        owner: Any,
        baseClass: Class<*>,
        typeArgumentIndex: Int,
        expectedSuperClass: Class<*>
    ): Class<T> {
        var currentType: Type? = owner.javaClass.genericSuperclass
        val resolvedTypes = mutableMapOf<TypeVariable<*>, Type>()
        while (currentType != null) {
            when (currentType) {
                is ParameterizedType -> {
                    val rawType = currentType.rawType
                    val rawClass = rawType as? Class<*>
                    if (rawClass != null) {
                        rawClass.typeParameters.forEachIndexed { index, typeVariable ->
                            resolvedTypes[typeVariable] = currentType.actualTypeArguments[index].resolveType(resolvedTypes)
                        }
                    }
                    if (rawType == baseClass) {
                        return currentType.actualTypeArguments[typeArgumentIndex]
                            .resolveType(resolvedTypes)
                            .toTypedClass(expectedSuperClass)
                    }
                    currentType = rawClass?.genericSuperclass
                }

                is Class<*> -> {
                    currentType = currentType.genericSuperclass
                }

                else -> currentType = null
            }
        }
        throw IllegalStateException("Unable to resolve ViewModel class for ${owner.javaClass.name}")
    }

    private fun Type.resolveType(resolvedTypes: Map<TypeVariable<*>, Type>): Type {
        var current = this
        while (current is TypeVariable<*>) {
            current = resolvedTypes[current] ?: break
        }
        return current
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> Type.toTypedClass(expectedSuperClass: Class<*>): Class<T> {
        val clazz = when (this) {
            is Class<*> -> this
            is ParameterizedType -> rawType as? Class<*>
            else -> null
        }
        requireNotNull(clazz) { "Unsupported ViewModel type: $this" }
        require(expectedSuperClass.isAssignableFrom(clazz)) {
            "Resolved type ${clazz.name} is not assignable to ${expectedSuperClass.name}"
        }
        return clazz as Class<T>
    }
}
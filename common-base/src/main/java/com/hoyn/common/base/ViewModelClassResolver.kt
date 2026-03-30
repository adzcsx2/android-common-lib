package com.hoyn.common.base

import androidx.lifecycle.ViewModel
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable

/**
 * ViewModel 类解析器
 *
 * 通过反射获取 BaseActivity/BaseFragment 中声明的 ViewModel 泛型类型
 */
object ViewModelClassResolver {

    /**
     * 解析 ViewModel 的 Class 对象
     *
     * 从 owner 的泛型超类中提取 ViewModel 的类型参数
     *
     * @param owner 拥有 ViewModel 泛型的对象（如 Activity、Fragment）
     * @param baseClass 基类的 Class 对象（如 BaseActivity::class.java）
     * @return ViewModel 的 Class 对象
     */
    fun <VM : ViewModel> resolve(owner: Any, baseClass: Class<*>): Class<VM> {
        return resolveGenericClass(
            owner = owner,
            baseClass = baseClass,
            typeArgumentIndex = 1,
            expectedSuperClass = ViewModel::class.java
        )
    }

    /**
     * 解析泛型类的 Class 对象
     *
     * 通过遍历继承链，找到指定的基类，并提取对应索引位置的类型参数
     *
     * @param owner 拥有泛型的对象
     * @param baseClass 要查找的基类
     * @param typeArgumentIndex 泛型参数的索引位置
     * @param expectedSuperClass 期望的父类，用于类型验证
     * @return 解析到的泛型类型的 Class 对象
     * @throws IllegalStateException 如果无法解析泛型类型
     */
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

    /**
     * 解析类型的扩展函数
     *
     * 递归解析类型变量，直到找到具体的类型
     *
     * @param resolvedTypes 已解析的类型变量映射
     * @return 解析后的类型
     */
    private fun Type.resolveType(resolvedTypes: Map<TypeVariable<*>, Type>): Type {
        var current = this
        while (current is TypeVariable<*>) {
            current = resolvedTypes[current] ?: break
        }
        return current
    }

    /**
     * 将 Type 转换为 Class 对象
     *
     * @param expectedSuperClass 期望的父类，用于类型验证
     * @return 转换后的 Class 对象
     * @throws IllegalArgumentException 如果类型不支持或不满足父类要求
     */
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
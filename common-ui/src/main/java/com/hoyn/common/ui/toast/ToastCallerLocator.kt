package com.hoyn.common.ui.toast

import com.hjq.toast.Toaster
import com.hjq.toast.config.IToastInterceptor
import java.lang.reflect.Modifier

/**
 * Toast 调用栈定位器。
 *
 * 从当前线程堆栈中过滤掉 Toast 框架内部类（Toaster、ToastUtils 及其匿名内部类），
 * 以及接口/抽象类，找到业务代码中真正触发 Toast 显示的调用位置。
 */
internal object ToastCallerLocator {

    /** ToastUtils 的完整类名，用于堆栈过滤 */
    private const val TOAST_UTILS_CLASS_NAME = "com.hoyn.common.ui.toast.ToastUtils"

    /**
     * 从堆栈中查找真实调用者的 StackTraceElement
     * @param stackTraces 当前线程的堆栈数组
     * @param stackSkips 在已过滤的调用者列表中额外跳过的层数，默认 0
     * @param classResolver 类解析器，用于根据类名加载 Class 对象，可替换用于测试
     * @return 真实调用者的堆栈元素，未找到时返回 null
     */
    internal fun findCallerStackTrace(
        stackTraces: Array<StackTraceElement>,
        stackSkips: Int = 0,
        classResolver: (String) -> Class<*>? = { className -> resolveStackClass(className) }
    ): StackTraceElement? {
        val callerFrames = collectCallerFrames(stackTraces, classResolver)
        return callerFrames.getOrNull(stackSkips)
    }

    /**
     * 将堆栈元素转换为可展示的 ToastDisplayLocation
     * @param stackTrace 调用者堆栈元素
     * @return 包含文件名和行号的展示位置信息
     */
    internal fun resolveDisplayLocation(stackTrace: StackTraceElement): ToastDisplayLocation {
        return ToastDisplayLocation(
            fileName = stackTrace.fileName ?: stackTrace.className.substringAfterLast('.'),
            lineNumber = stackTrace.lineNumber.takeIf { it > 0 }
        )
    }

    /**
     * 从堆栈中收集业务调用者的帧，过滤掉框架内部和接口/抽象类
     * @param stackTraces 堆栈数组
     * @param classResolver 类解析器
     * @return 过滤后的业务调用者堆栈列表
     */
    private fun collectCallerFrames(
        stackTraces: Array<StackTraceElement>,
        classResolver: (String) -> Class<*>?
    ): List<StackTraceElement> {
        return stackTraces.filter { stackTrace ->
            if (stackTrace.lineNumber <= 0 || shouldFilterStackClassName(stackTrace.className)) {
                return@filter false
            }
            val clazz = classResolver(stackTrace.className) ?: return@filter false
            !shouldFilterStackClass(clazz)
        }
    }

    /** 根据类名加载 Class 对象，加载失败返回 null */
    private fun resolveStackClass(className: String): Class<*>? {
        return runCatching { Class.forName(className) }.getOrNull()
    }

    /** 根据类名判断是否为 Toast 框架内部类，需要过滤 */
    private fun shouldFilterStackClassName(className: String): Boolean {
        return className == Toaster::class.java.name ||
            className == TOAST_UTILS_CLASS_NAME ||
            className.startsWith("$TOAST_UTILS_CLASS_NAME$")
    }

    /** 根据类信息判断是否需要过滤：拦截器接口、接口类型、抽象类 */
    private fun shouldFilterStackClass(clazz: Class<*>): Boolean {
        return IToastInterceptor::class.java.isAssignableFrom(clazz) ||
            clazz.isInterface ||
            Modifier.isAbstract(clazz.modifiers)
    }
}

/**
 * Toast 调用位置展示信息
 * @param fileName 源文件名
 * @param lineNumber 行号，不可解析时为 null
 */
internal data class ToastDisplayLocation(
    val fileName: String,
    val lineNumber: Int?
) {

    /** 格式化为日志前缀，如 "(FileName.kt:42)" 或 "(FileName.kt)" */
    fun asLogPrefix(): String {
        return when {
            lineNumber != null -> "($fileName:$lineNumber)"
            else -> "($fileName)"
        }
    }
}

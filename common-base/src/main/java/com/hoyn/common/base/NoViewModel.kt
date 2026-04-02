package com.hoyn.common.base

/**
 * 空 ViewModel 标记类
 *
 * 用于不需要 ViewModel 的页面，作为 BaseActivity/BaseFragment 的泛型占位
 * 继承 BaseViewModel 但不持有任何 Repository 和业务逻辑
 */
class NoViewModel : BaseViewModel<Nothing?>() {
    /** 不持有任何 Repository，始终返回 null */
    override val repository: Nothing? = null
}
package com.hoyn.common.lib.ui.dialog_demo

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

/**
 * Parcelable 传参示例对象
 *
 * 用于演示 BaseDialogFragment 的 argumentParcelable() 参数传递
 *
 * @property orderId 订单 ID
 * @property amount 订单金额
 * @property remark 备注信息
 */
@Parcelize
data class DemoParcelablePayload(
    val orderId: String,
    val amount: Int,
    val remark: String
) : Parcelable

/**
 * Serializable 传参示例对象
 *
 * 用于演示 BaseDialogFragment 的 argumentSerializable() 参数传递
 *
 * @property owner 所有者名称
 * @property enabled 是否启用
 * @property scene 使用场景
 */
data class DemoSerializablePayload(
    val owner: String,
    val enabled: Boolean,
    val scene: String
) : Serializable
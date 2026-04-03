package com.hoyn.common.lib.ui.camera

/**
 * 相机分辨率数据类
 *
 * 用于替代原有的 Resolution 枚举，支持动态检测设备支持的所有慢动作分辨率。
 *
 * @property width 宽度（像素）
 * @property height 高度（像素）
 * @property displayName 显示名称（如 "720p", "1080p", "480p"）
 */
data class CameraResolution(
    val width: Int,
    val height: Int,
    val displayName: String
) {
    companion object {
        /**
         * 标准分辨率常量，用于向后兼容和默认值
         */
        val HD_720P = CameraResolution(1280, 720, "720p")
        val HD_1080P = CameraResolution(1920, 1080, "1080p")

        /**
         * 从旧的 ordinal 值恢复分辨率（向后兼容）
         *
         * @param ordinal 旧的 Resolution 枚举 ordinal 值
         * @return 对应的 CameraResolution，如果 ordinal 无效则返回 1080p
         */
        fun fromOrdinal(ordinal: Int): CameraResolution {
            return when (ordinal) {
                0 -> HD_720P
                1 -> HD_1080P
                else -> HD_1080P
            }
        }

        /**
         * 从宽高创建 CameraResolution
         *
         * @param width 宽度
         * @param height 高度
         * @return CameraResolution 实例
         */
        fun fromSize(width: Int, height: Int): CameraResolution {
            val displayName = when {
                width >= 3840 -> "4K"
                width >= 2560 -> "2.5K"
                width >= 1920 -> "1080p"
                width >= 1280 -> "720p"
                width >= 854 -> "480p"
                width >= 720 -> "480p"
                width >= 640 -> "360p"
                else -> "${width}x${height}"
            }
            return CameraResolution(width, height, displayName)
        }
    }

    /**
     * 获取用于存储的 ordinal 值（向后兼容）
     *
     * @return ordinal 值，0 表示 720p，1 表示 1080p，其他分辨率返回 1
     */
    fun toStorageOrdinal(): Int {
        return when (this) {
            HD_720P -> 0
            HD_1080P -> 1
            else -> 1 // 其他分辨率默认映射到 1080p
        }
    }

    /**
     * 检查是否为标准分辨率
     */
    fun isStandardResolution(): Boolean {
        return this == HD_720P || this == HD_1080P
    }

    /**
     * 获取宽高比
     */
    val aspectRatio: Float
        get() = width.toFloat() / height.toFloat()
}

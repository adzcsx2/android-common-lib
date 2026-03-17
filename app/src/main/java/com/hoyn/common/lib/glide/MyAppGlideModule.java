package com.hoyn.common.lib.glide;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.ImageHeaderParser;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.ArrayPool;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.ExternalPreferredCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.load.resource.bitmap.DefaultImageHeaderParser;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Glide 图片加载框架配置模块
 *
 * <p>本类通过 @GlideModule 注解被 Glide 自动识别，用于配置 Glide 的全局选项。
 * 主要功能包括：
 * <ul>
 *   <li>禁用 EXIF 处理以避免 PNG 图片加载时的警告</li>
 *   <li>配置内存缓存和磁盘缓存策略</li>
 *   <li>优化图片解码格式以减少内存占用</li>
 *   <li>禁用 manifest 解析以提高性能</li>
 * </ul>
 *
 * @see AppGlideModule
 */
@GlideModule
public class MyAppGlideModule extends AppGlideModule {
    /** Glide 磁盘缓存目录名称 */
    private static final String DISK_CACHE_DIR = "glide-theme-cache";

    /** Glide 磁盘缓存大小：500MB */
    private static final int DISK_CACHE_SIZE_BYTES = 500 * 1024 * 1024;

    /**
     * 应用 Glide 的全局配置选项
     *
     * <p>本方法在 Glide 初始化时被调用，用于配置以下内容：
     * <ul>
     *   <li>默认请求选项：图片解码格式、缓存策略等</li>
     *   <li>日志级别：设置为 ERROR 以过滤警告日志</li>
     *   <li>内存缓存：使用 LRU 策略，缓存 2 个屏幕的图片</li>
     *   <li>位图池：使用 LRU 策略，缓存 3 个屏幕的位图</li>
     *   <li>磁盘缓存：使用外部存储的首选缓存工厂，缓存大小 500MB</li>
     * </ul>
     *
     * @param context 应用程序上下文，用于获取系统资源
     * @param builder Glide 构建器，用于设置各种配置选项
     */
    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        // 设置默认的请求选项
        // 使用 ARGB_8888 格式以保证图片质量
        // 禁用硬件位图配置以避免兼容性问题
        RequestOptions defaultOptions = new RequestOptions()
                .format(DecodeFormat.PREFER_ARGB_8888)
                .disallowHardwareConfig()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .skipMemoryCache(false);

        builder.setDefaultRequestOptions(defaultOptions);

        // 设置日志级别为 ERROR，过滤掉 WARNING 级别的日志
        // 这样就不会显示 ExifInterface 的警告了
        builder.setLogLevel(android.util.Log.ERROR);

        // 配置内存缓存
        // 内存缓存大小：2 个屏幕
        // 位图池大小：3 个屏幕
        MemorySizeCalculator calculator = new MemorySizeCalculator.Builder(context)
                .setMemoryCacheScreens(2)
                .setBitmapPoolScreens(3)
                .build();

        builder.setMemoryCache(new LruResourceCache(calculator.getMemoryCacheSize()));
        builder.setBitmapPool(new LruBitmapPool(calculator.getBitmapPoolSize()));
        builder.setDiskCache(new ExternalPreferredCacheDiskCacheFactory(
                context,
                DISK_CACHE_DIR,
                DISK_CACHE_SIZE_BYTES
        ));
    }

    /**
     * 注册自定义组件以修改 Glide 的默认行为
     *
     * <p>本方法通过禁用 EXIF 方向解析来避免加载 PNG 图片时的警告。
     * EXIF 信息主要用于 JPEG 图片的自动旋转，对于 PNG 图片通常不需要。
     *
     * @param context 应用程序上下文
     * @param glide Glide 实例
     * @param registry 组件注册表，用于注册自定义组件
     */
    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        // 禁用 EXIF 解析，避免 PNG 图片加载时的 ExifInterface 警告
        List<ImageHeaderParser> parsers = registry.getImageHeaderParsers();
        try {
            // 尝试修改解析器列表，移除会触发 EXIF 解析的解析器
            ((List<ImageHeaderParser>) parsers).clear();
            ((List<ImageHeaderParser>) parsers).add(new DefaultImageHeaderParser());
        } catch (Exception e) {
            // 如果列表不可变，注册自定义解析器
            registry.register(new SkipExifOrientationParser());
        }
    }

    /**
     * 指定是否解析 AndroidManifest.xml 中的 Glide 模块
     *
     * <p>返回 false 禁用 manifest 解析，可以：
     * <ul>
     *   <li>提高应用启动性能（减少 XML 解析开销）</li>
     *   <li>避免重复加载已知的 Glide 模块</li>
     * </ul>
     *
     * @return false 表示不解析 manifest 文件中的 Glide 模块
     */
    @Override
    public boolean isManifestParsingEnabled() {
        // 禁用 manifest 解析以提高性能
        return false;
    }

    /**
     * 跳过 EXIF 方向解析的自定义图片头部解析器
     *
     * <p>本类实现 ImageHeaderParser 接口，用于拦截图片加载过程中的 EXIF 方向解析。
     * 对于不需要自动旋转的图片（如 PNG），直接返回正常方向可以避免触发 ExifInterface，
     * 从而消除相关的警告日志。
     *
     * <p>EXIF 方向值含义：
     * <ul>
     *   <li>1：正常方向，不旋转</li>
     *   <li>2-8：各种旋转和翻转方向</li>
     * </ul>
     */
    private static class SkipExifOrientationParser implements ImageHeaderParser {
        /**
         * 从输入流中获取图片类型
         *
         * @param is 图片输入流
         * @return 始终返回 UNKNOWN，表示不解析具体类型
         */
        @Override
        public ImageType getType(InputStream is) {
            return ImageType.UNKNOWN;
        }

        /**
         * 从字节缓冲区中获取图片类型
         *
         * @param byteBuffer 包含图片数据的字节缓冲区
         * @return 始终返回 UNKNOWN，表示不解析具体类型
         */
        @Override
        public ImageType getType(ByteBuffer byteBuffer) {
            return ImageType.UNKNOWN;
        }

        /**
         * 获取图片的 EXIF 方向信息
         *
         * <p>直接返回 1（正常方向），跳过实际的 EXIF 解析过程。
         *
         * @param is 图片输入流
         * @param byteArrayPool 字节数组池，用于临时存储
         * @return 始终返回 1，表示图片方向正常
         */
        @Override
        public int getOrientation(InputStream is, ArrayPool byteArrayPool) {
            return 1;
        }

        /**
         * 获取图片的 EXIF 方向信息（ByteBuffer 版本）
         *
         * <p>直接返回 1（正常方向），跳过实际的 EXIF 解析过程。
         *
         * @param byteBuffer 包含图片数据的字节缓冲区
         * @param byteArrayPool 字节数组池，用于临时存储
         * @return 始终返回 1，表示图片方向正常
         */
        @Override
        public int getOrientation(ByteBuffer byteBuffer, ArrayPool byteArrayPool) {
            return 1;
        }
    }
}

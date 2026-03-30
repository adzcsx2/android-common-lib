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
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.ExternalPreferredCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.load.resource.bitmap.ExifInterfaceImageHeaderParser;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;

import java.util.Iterator;
import java.util.List;

/**
 * Glide 图片加载框架配置模块
 *
 * <p>本类通过 @GlideModule 注解被 Glide 自动识别，用于配置 Glide 的全局选项。
 * 主要功能包括：
 * <ul>
 *   <li>配置内存缓存和磁盘缓存策略</li>
 *   <li>优化图片解码格式以减少内存占用</li>
 *   <li>禁用 manifest 解析以提高性能</li>
 * </ul>
 *
 * @see AppGlideModule
 */
@GlideModule
public class MyAppGlideModule extends AppGlideModule implements com.bumptech.glide.module.GlideModule {
    /**
     * Glide 磁盘缓存目录名称
     */
    private static final String DISK_CACHE_DIR = "glide-theme-cache";

    /**
     * Glide 磁盘缓存大小：500MB
     */
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

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        // 移除 ExifInterfaceImageHeaderParser，它对 PNG/GIF/WebP 等非 JPEG 格式
        // 也会调用 ExifInterface 导致 "Invalid byte order" 警告日志。
        // Glide 的 DefaultImageHeaderParser 已经能正确处理所有格式的方向检测。
        List<ImageHeaderParser> parsers = registry.getImageHeaderParsers();
        Iterator<ImageHeaderParser> iterator = parsers.iterator();
        while (iterator.hasNext()) {
            if (iterator.next() instanceof ExifInterfaceImageHeaderParser) {
                iterator.remove();
            }
        }
    }
}

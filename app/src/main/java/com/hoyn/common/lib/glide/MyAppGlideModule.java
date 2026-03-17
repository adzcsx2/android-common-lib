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
 * Glide 配置模块
 * 用于配置 Glide 的全局选项，包括禁用 EXIF 处理以避免 PNG 图片的警告
 */
@GlideModule
public class MyAppGlideModule extends AppGlideModule {
    private static final String DISK_CACHE_DIR = "glide-theme-cache";
    private static final int DISK_CACHE_SIZE_BYTES = 500 * 1024 * 1024;

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        // 设置默认的请求选项
        // 使用 PREFER_RGB_565 格式可以减少内存占用
        // 对于 PNG 图片，禁用不必要的转换以避免 EXIF 警告
        RequestOptions defaultOptions = new RequestOptions()
                .format(DecodeFormat.PREFER_ARGB_8888)
                .disallowHardwareConfig()  // 禁用硬件位图配置，避免某些兼容性问题
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .skipMemoryCache(false);

        builder.setDefaultRequestOptions(defaultOptions);

        // 可选：设置日志级别为 ERROR，过滤掉 WARNING 级别的日志
        // 这样就不会显示 ExifInterface 的警告了
        builder.setLogLevel(android.util.Log.ERROR);

        // 配置内存缓存
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

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        // 禁用 EXIF 解析，避免 PNG 图片加载时的 ExifInterface 警告
        List<ImageHeaderParser> parsers = registry.getImageHeaderParsers();
        try {
            // 尝试修改解析器列表，移除会触发 EXIF 解析的解析器
            ((List<ImageHeaderParser>) parsers).clear();
            ((List<ImageHeaderParser>) parsers).add(new DefaultImageHeaderParser());
        } catch (Exception e) {
            // 如果列表不可变，注册兜底解析器
            registry.register(new SkipExifOrientationParser());
        }
    }

    @Override
    public boolean isManifestParsingEnabled() {
        // 禁用 manifest 解析以提高性能
        return false;
    }

    /**
     * 跳过 EXIF 方向解析的内部类
     * 直接返回正常方向，避免触发 ExifInterface 解析
     */
    private static class SkipExifOrientationParser implements ImageHeaderParser {
        @Override
        public ImageType getType(InputStream is) {
            return ImageType.UNKNOWN;
        }

        @Override
        public ImageType getType(ByteBuffer byteBuffer) {
            return ImageType.UNKNOWN;
        }

        @Override
        public int getOrientation(InputStream is, ArrayPool byteArrayPool) {
            return 1; // 直接返回正常方向
        }

        @Override
        public int getOrientation(ByteBuffer byteBuffer, ArrayPool byteArrayPool) {
            return 1; // 直接返回正常方向
        }
    }
}

package com.netease.nim.uikit.support.glide;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.module.LibraryGlideModule;

/**
 * Created by huangjun on 2017/4/1.
 */
//@GlideModule //FIXME: make this work
public class NIMGlideModule extends LibraryGlideModule {
    private static final String TAG = "NIMGlideModule";

    private static final int M = 1024 * 1024;
    private static final int MAX_DISK_CACHE_SIZE = 256 * M;

    /**
     * ************************ Memory Cache ************************
     */

    static void clearMemoryCache(Context context) {
        Glide.get(context).clearMemory();
    }

    /**
     * ************************ GlideModule override ************************
     */
    /* FIXME: 由老的GlideModule迁移到LibraryGlideModule时此处应该如何处理？
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        // sdcard/Android/data/com.netease.nim.demo/glide
        final String cachedDirName = "glide";
        builder.setDiskCache(new ExternalCacheDiskCacheFactory(context, cachedDirName, MAX_DISK_CACHE_SIZE));
        LogUtil.i(TAG, "NIMGlideModule apply options, disk cached path=" + context.getExternalCacheDir() + File.pathSeparator + cachedDirName);
    }
    */

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {

    }
}

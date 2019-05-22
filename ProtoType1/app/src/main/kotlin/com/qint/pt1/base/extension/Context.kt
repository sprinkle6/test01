/**
 * Copyright (C) 2018 Fernando Cejas Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qint.pt1.base.extension

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Environment
import android.util.TypedValue
import androidx.core.content.ContextCompat
import org.jetbrains.anko.displayMetrics
import java.io.IOException

val Context.networkInfo: NetworkInfo? get() =
    (this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo

fun Context.screenWidthDp(): Int {
    //The current width of the available screen space, in dp units, corresponding to screen width resource qualifier.
    val screenWidthDp = resources.configuration.screenWidthDp

    //The smallest screen size an application will see in normal operation, corresponding to smallest screen width resource qualifier.
    //val smallestScreenWidthDp = configuration.smallestScreenWidthDp

    return screenWidthDp
}

fun Context.dp2px(value: Int) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), displayMetrics)

fun Context.px2dp(value: Int) = TypedValue.complexToDimensionPixelSize(value, displayMetrics)

fun Context.sp2px(value: Int) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value.toFloat(), displayMetrics)

fun Context.getAppCacheDir(): String? {
    var storageRootPath: String? = null
    try {
        // SD卡应用扩展存储区(APP卸载后，该目录下被清除，用户也可以在设置界面中手动清除)，请根据APP对数据缓存的重要性及生命周期来决定是否采用此缓存目录.
        // 该存储区在API 19以上不需要写权限，即可配置 <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="18"/>
        if (externalCacheDir != null) {
            storageRootPath = externalCacheDir.canonicalPath
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    if (storageRootPath.isNullOrBlank()) {
        // SD卡应用公共存储区(APP卸载后，该目录不会被清除，下载安装APP后，缓存数据依然可以被加载。SDK默认使用此目录)，该存储区域需要写权限!
        storageRootPath =
            Environment.getExternalStorageDirectory().path + "/" + packageName
    }

    return storageRootPath
}

fun Context.getColor(id: Int) = ContextCompat.getColor(this, id)

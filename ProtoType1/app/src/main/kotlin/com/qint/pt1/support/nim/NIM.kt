/*
 * Author: Matthew Zhang
 * Created on: 4/25/19 9:00 AM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.support.nim

import android.content.Context
import android.util.Log
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.SDKOptions
import com.qint.pt1.base.extension.getAppCacheDir
import com.qint.pt1.base.platform.RequestCallback
import com.qint.pt1.base.sdk.ThirdPartySDK
import com.qint.pt1.features.login.LoginFailure
import com.qint.pt1.features.login.LoginInfo
import com.qint.pt1.util.LOG_TAG
import com.qint.pt1.util.getScreenWidthInPixel
import javax.inject.Inject
import javax.inject.Singleton

typealias NIMRequestCallback<T> = com.netease.nimlib.sdk.RequestCallback<T>
typealias NIMLoginInfo = com.netease.nimlib.sdk.auth.LoginInfo

@Singleton
class NIM
@Inject constructor() : ThirdPartySDK {
    private var nimLoginInfo: NIMLoginInfo? = null

    override fun config(context: Context) {
        NIMClient.config(context, nimLoginInfo, options(context))
    }

    override fun init(context: Context) {
        NIMClient.initSDK()
        NimUIKit.init(context)
    }

    fun logout(){
        NimUIKit.logout()
    }

    fun login(loginInfo: NIMLoginInfo, callback: RequestCallback<LoginInfo>) {
        NimUIKit.login(loginInfo, object :
            NIMRequestCallback<NIMLoginInfo> {
            override fun onSuccess(loginInfo: NIMLoginInfo) {
                nimLoginInfo = loginInfo
                Log.d(LOG_TAG, "NIMLogin.onLoginSuccess(${loginInfo.account})")
                saveNIMLoginInfo(loginInfo)
                callback.onSuccess(loginInfo.toLoginInfo())
            }

            override fun onFailed(code: Int) {
                nimLoginInfo = null
                Log.e(LOG_TAG, "NIMLogin.onLoginFailed(${code})")
                callback.onFailure(LoginFailure.LoginFailed)
            }

            override fun onException(exception: Throwable?) {
                nimLoginInfo = null
                Log.e(LOG_TAG, "NIMLogin.onLoginException(${exception?.message})")
                exception?.printStackTrace()
                callback.onFailure(LoginFailure.LoginFailed)
            }

        })
    }

    // 如果返回值为 null，则全部使用默认参数。
    private fun options(context: Context): SDKOptions {
        val options = SDKOptions()

        with(options) {
            //用户登录IM时才启用IM push进程，UI进程退出后push消息进程也退出。不能使用自动登录，如果使用自动登录则该项自动失效。
            reducedIM = false
            asyncInitSDK = false
            checkManifestConfig = true //调试阶段打开，调试通过后关闭

            // 配置保存图片，文件，log 等数据的目录
            // 如果 options 中没有设置这个值，SDK 会使用采用默认路径作为 SDK 的数据目录。
            // 该目录目前包含 log, file, image, audio, video, thumb 这6个目录。
            val sdkPath: String = context.getAppCacheDir() + "/nim" // 可以不设置，那么将采用默认路径
            // 如果第三方 APP 需要缓存清理功能， 清理这个目录下面个子目录的内容即可。
            sdkStorageRootPath = sdkPath;

            // 配置是否需要预下载附件缩略图，默认为 true
            preloadAttach = true;

            // 配置附件缩略图的尺寸大小。表示向服务器请求缩略图文件的大小
            // 该值一般应根据屏幕尺寸来确定， 默认值为 Screen.width / 2
            thumbnailSize = getScreenWidthInPixel() / 2
        }
        return options;
    }

    private fun saveNIMLoginInfo(loginInfo: NIMLoginInfo?) {
        //TODO()
    }

}

fun NIMLoginInfo.toLoginInfo() = LoginInfo(account, token)
/*
 * Author: Matthew Zhang
 * Created on: 4/25/19 8:57 AM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.support.aliyun

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import com.alibaba.sdk.android.oss.ClientException
import com.alibaba.sdk.android.oss.OSSClient
import com.alibaba.sdk.android.oss.ServiceException
import com.alibaba.sdk.android.oss.common.OSSLog
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider
import com.alibaba.sdk.android.oss.model.PutObjectRequest
import com.qint.pt1.base.di.ApplicationModule
import com.qint.pt1.base.extension.getMD5Hex
import com.qint.pt1.domain.UserDataCategory
import com.qint.pt1.util.LOG_TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OSSHelper(val userId: String, val context: Context, private val ossToken: OSSToken) {
    companion object {
        const val END_POINT = "https://oss-cn-beijing.aliyuncs.com"
        const val BUCKET = "huayu-app"
    }

    enum class FileType(val tag: String, val mimeTypeFilter: String, val defaultPostFix: String) {
        IMAGE("img", "image/*", "jpg"),
        AUDIO("audio", "audio/*", "wav"),
        VIDEO("video", "video/*", "mp4");

        override fun toString(): String = tag
    }

    private val CALLBACK_URL = "${ApplicationModule.API_BASE_URL}oss_upload_notify"
    private val contentResolver = context.contentResolver

    private fun getOSSClient(): OSSClient {
        val credentialProvider = OSSStsTokenCredentialProvider(
            ossToken.accessKeyId,
            ossToken.accessKeySecret,
            ossToken.securityToken
        )
        return OSSClient(context,
            END_POINT, credentialProvider)
    }

    fun upload(category: UserDataCategory, fileType: FileType, uri: Uri){
        //TODO: 改为UseCase，然后返回上传成功失败状态并通知用户
        CoroutineScope(Dispatchers.IO).launch {
            val ossClient = getOSSClient()
            OSSLog.enableLog() //FIXME: disable this after debug
            try {
                val objectName =
                    generateOSSObjectName(category, fileType, uri)
                val inputStream = contentResolver.openInputStream(uri)
                val put = PutObjectRequest(BUCKET, objectName, inputStream.readBytes())

                put.callbackParam = mapOf(
                    "callbackUrl" to CALLBACK_URL,
                    "callbackBody" to generateOSSCallbackBody(objectName, fileType)
                )

                Log.d(LOG_TAG, "OSSHelper: uploading ${put.callbackParam}")
                val putResult = ossClient.putObject(put)
                Log.d(LOG_TAG, "OSSHelper: uploaded ${put.callbackParam}")
            } catch (e: ClientException) {
                //本地异常
                e.printStackTrace()
                Log.e(LOG_TAG, "OSSHelper upload exception: ${e.message}", e)
            } catch (e: ServiceException) {
                //OSS服务异常
                Log.e(LOG_TAG, "RequestId: ${e.requestId}")
                Log.e(LOG_TAG, "ErrorCode: ${e.errorCode}")
                Log.e(LOG_TAG, "HostId: ${e.hostId}")
                Log.e(LOG_TAG, "RawMessage: ${e.rawMessage}")
            } catch (e: Exception) {
                Log.e(LOG_TAG, "OSSHelper upload exception: ${e.message}", e)
            }
        }
    }

    private fun generateOSSObjectName(category: UserDataCategory, fileType: FileType, uri: Uri): String {
        //FIXME: 小米手机无法正确识别文件类型
        val extension =
            contentResolver.getContentFileExtension(uri) ?: fileType.defaultPostFix
        val md5 = getContentMD5Hex(uri)
        return "${userId}/${category.categoryPath}/${md5}.${extension}"
    }

    private fun generateOSSCallbackBody(objectName: String, fileType: FileType, scope: String = "user") =
        "filename=${objectName}&filetype=${fileType.tag}&uid=${userId}&scope=${scope}"

    private fun getContentMD5Hex(uri: Uri): String {
        return contentResolver.openInputStream(uri).getMD5Hex()
    }
}

data class OSSToken(
    val accessKeyId: String,
    val accessKeySecret: String,
    val securityToken: String,
    val expiration: Int
)

fun ContentResolver.getContentFileExtension(uri: Uri): String? {
    val mimeTypeMap = MimeTypeMap.getSingleton()
    if(uri.scheme == ContentResolver.SCHEME_CONTENT){
        val mimeType = this.getType(uri)
        if(mimeType != null) return mimeTypeMap.getExtensionFromMimeType(mimeType).toLowerCase()
    }

    return MimeTypeMap.getFileExtensionFromUrl(uri.toString()).toLowerCase()
}

/*
 * Copyright (c) 2019. QINT.TV
 * Author: Matthew Zhang
 * Created on: 3/28/19 3:45 PM
 */

package com.qint.pt1.base.extension

import android.content.ContentResolver
import android.net.Uri
import android.webkit.MimeTypeMap

const val MIME_TYPE_FILTER_DEFAULT = "*/*"

fun ContentResolver.getContentType(uri: Uri, mimeTypeFilter: String = MIME_TYPE_FILTER_DEFAULT): String? {
    //FIXME: 未测试
    var contentType: String? = null
    try {
        contentType = getType(uri)
    } catch (_: Exception) {
    }
    if (contentType.isNullOrBlank()) {
        try {
            contentType = getStreamTypes(uri, mimeTypeFilter).firstOrNull()
        } catch (_: Exception) {
        }
    }
    if (contentType.isNullOrBlank()) {
        try {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            contentType =
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase())
        } catch (_: Exception) {
        }
    }
    return contentType
}

fun ContentResolver.getImageContentType(uri: Uri) = getContentType(uri, "image/*")

fun ContentResolver.getAudioContentType(uri: Uri) = getContentType(uri, "audio/*")

fun ContentResolver.getVideoContentType(uri: Uri) = getContentType(uri, "video/*")


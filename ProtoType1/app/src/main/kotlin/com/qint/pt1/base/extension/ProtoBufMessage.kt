/*
 * Author: Matthew Zhang
 * Created on: 5/11/19 11:46 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.base.extension

import android.util.Base64
import android.util.Base64OutputStream
import android.util.Log
import com.google.protobuf.Message
import com.qint.pt1.util.LOG_TAG
import java.io.ByteArrayOutputStream

fun Message.encode(): String {
    return try {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val base64OutputStream = Base64OutputStream(byteArrayOutputStream, Base64.NO_WRAP)
        //val writeMethod = T::class.java.getMethod("writeTo", OutputStream::class.java)
        //writeMethod.invoke(value, base64OutputStream)
        this.writeTo(base64OutputStream)
        base64OutputStream.close()
        byteArrayOutputStream.toString()
    } catch (e: Throwable) {
        e.printStackTrace()
        Log.e(LOG_TAG, "encode message failed: $e")
        ""
    }
}

inline fun <reified T: Message> decodeProtoBufMessage(encoded: String): T{
    val parseMethod = T::class.java.getMethod("parseFrom", ByteArray::class.java)
    val protobufMessage = parseMethod.invoke(null, encoded.base64Decode()) as T
    return protobufMessage
}

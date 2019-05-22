/*
 * Copyright (c) 2019. QINT.TV
 * Author: Matthew Zhang
 * Created on: 3/28/19 3:41 PM
 */

package com.qint.pt1.base.extension

import java.io.InputStream
import java.security.DigestInputStream
import java.security.MessageDigest

fun InputStream.getMD5Hex(): String {
    val md = MessageDigest.getInstance("MD5")
    val dis = DigestInputStream(this, md)
    while (dis.read() != -1) {
    }
    dis.close()
    return md.digest().map { String.format("%02x", it) }.joinToString("")
}
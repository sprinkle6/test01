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

import android.util.Base64
import android.util.Base64InputStream
import java.io.ByteArrayInputStream

fun String.Companion.empty() = ""

fun String.base64Decode(): ByteArray {
    val byteArrayInputStream = ByteArrayInputStream(this.toByteArray())
    val base64InputStream = Base64InputStream(byteArrayInputStream, Base64.DEFAULT)
    val bytes = base64InputStream.readBytes()
    base64InputStream.close()
    return bytes
}

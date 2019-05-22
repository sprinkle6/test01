/*
 * Copyright (c) 2019. QINT.TV
 * Author: Zhang Gong
 */

package com.qint.pt1.base.sdk

import android.content.Context

interface ThirdPartySDK {
    fun config(context: Context)
    fun init(context: Context)
}
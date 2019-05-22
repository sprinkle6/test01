/*
 * Copyright (c) 2019. QINT.TV
 * Author: Zhang Gong
 */

package com.qint.pt1.features.im

import android.content.Context
import com.qint.pt1.base.platform.RequestCallback
import com.qint.pt1.features.login.LoginInfo
import com.qint.pt1.support.nim.NIM
import com.qint.pt1.support.nim.NIMLoginInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IM @Inject constructor(private val nim: NIM){
    private lateinit var context: Context
    private var initialized = false

    fun config(context: Context) {
        this.context = context
        nim.config(context)
    }

    fun init() {
        if(!initialized) {
            nim.init(context)
            initialized = true
        }
    }

    fun login(loginInfo: LoginInfo, callback: RequestCallback<LoginInfo>){
        init()
        nim.login(loginInfo.toNIMLoginInfo(), callback)
    }

    fun logout() = nim.logout()
}

private fun LoginInfo.toNIMLoginInfo() = NIMLoginInfo(account, authToken)
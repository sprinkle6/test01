/*
 * Copyright (c) 2019. QINT.TV
 * Author: Matthew Zhang
 * Created on: 3/25/19 5:39 PM
 */

package com.qint.pt1.features.account

import androidx.lifecycle.MutableLiveData
import com.qint.pt1.base.platform.BaseViewModel
import com.qint.pt1.domain.ImageUrl
import com.qint.pt1.features.login.Login
import javax.inject.Inject

class AccountViewModel
@Inject constructor(private val login: Login): BaseViewModel(){
    val avatarLiveData: MutableLiveData<ImageUrl> = MutableLiveData()
    val nickNameLiveData: MutableLiveData<String> = MutableLiveData()

    fun init() {
        val userProfile = login.user?.profile ?: return
        avatarLiveData.value = userProfile.avatar.url
        nickNameLiveData.value = userProfile.nickName
    }
}
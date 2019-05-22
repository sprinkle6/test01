/*
 * Copyright (c) 2019. QINT.TV
 * Author: Matthew Zhang
 * Created on: 3/21/19 10:12 PM
 */

package com.qint.pt1.features.main

import androidx.lifecycle.MutableLiveData
import com.qint.pt1.base.platform.BaseViewModel
import com.qint.pt1.domain.ImageUrl
import com.qint.pt1.features.login.Login
import javax.inject.Inject

class MainViewModel
@Inject constructor(private val login: Login) : BaseViewModel() {
    val avatar: MutableLiveData<ImageUrl> = MutableLiveData()

    fun init(){
        val userProfile = login.user?.profile ?: return
        avatar.value = userProfile.avatar.url
    }
}
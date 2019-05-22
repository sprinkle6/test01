/*
 * Copyright (c) 2019. QINT.TV
 * Author: Matthew Zhang
 * Created on: 3/26/19 12:51 PM
 */

package com.qint.pt1.features.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.qint.pt1.base.platform.BaseViewModel
import com.qint.pt1.domain.ImageUrl
import com.qint.pt1.domain.User
import com.qint.pt1.domain.UserDataCategory
import com.qint.pt1.domain.UserId
import com.qint.pt1.features.login.Login
import com.qint.pt1.features.users.UsersRepository
import com.qint.pt1.support.aliyun.OSSHelper
import com.qint.pt1.util.LOG_TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserProfileViewModel
@Inject constructor(private val login: Login): BaseViewModel(){
    val userLiveData: MutableLiveData<User> = MutableLiveData()

    @Inject
    lateinit var usersRepository: UsersRepository

    fun loadUserProfile(userId: UserId) {
        assert(userId.isNotBlank())
        CoroutineScope(Dispatchers.IO).launch {
            val resp = usersRepository.userDetails(userId, login.userId)
            launch(Dispatchers.Main) {
                resp.either(
                    { failure -> trigerFailure(failure)},
                    { user -> update(user)}
                )
            }
        }
    }

    private fun update(user: User){
        userLiveData.value = user
    }

    fun uploadProfileFile(context: Context, uri: Uri, category: UserDataCategory, fileType: OSSHelper.FileType) {
        //FIXME: 上传成功/失败通知用户
        Log.d(LOG_TAG, "UserProfileViewModel uploading profile uri: ${uri}")
        CoroutineScope(Dispatchers.IO).launch {
            val ossTokenResp = usersRepository.applyOSSToken()
            ossTokenResp.either(
                { failure -> Log.e(LOG_TAG, "get OSS Token failed: ${failure}") },
                { ossToken ->
                    val ossHelper = OSSHelper(
                        login.account!!.userId,
                        context,
                        ossToken
                    )
                    ossHelper.upload(category, fileType, uri)
                }
            )
        }
    }

}

//try best to get a profile photo
//FIXME: demo only, remove this in product version
fun User.getProfilePhoto(): ImageUrl =
    if (profile.profilePhotos.isNotEmpty())
        profile.profilePhotos[0]
    else if (profile.avatar.isNotEmpty())
        profile.avatar.url
    else ""

/*
 * Copyright (c) 2019. QINT.TV
 * Author: Matthew Zhang
 * Created on: 3/25/19 4:51 PM
 */

package com.qint.pt1.features.account

import android.os.Bundle
import android.view.View
import com.qint.pt1.R
import com.qint.pt1.base.extension.loadFromUrl
import com.qint.pt1.base.extension.observe
import com.qint.pt1.base.extension.viewModel
import com.qint.pt1.base.navigation.Navigator
import com.qint.pt1.base.platform.BaseFragment
import com.qint.pt1.domain.ImageUrl
import com.qint.pt1.features.login.Login
import kotlinx.android.synthetic.main.account_fragment.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 底部导航->我的
 */
@Singleton
class AccountFragment
@Inject constructor() : BaseFragment() {
    override fun layoutId() = R.layout.account_fragment

    private lateinit var accountViewModel: AccountViewModel

    @Inject lateinit var navigator: Navigator
    @Inject lateinit var login: Login

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)

        accountViewModel = viewModel(viewModelFactory) {
            observe(avatarLiveData, ::renderAvatar)
            observe(nickNameLiveData, ::renderNickName)
            observe(failureLiveData, ::handleFailure)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        avatar.setOnClickListener {
            navigator.showUserProfile(activity!!, login.account!!.userId)
        }
        accountViewModel.init()
        render()
    }

    private fun render() {
        with(accountViewModel) {
            renderAvatar(avatarLiveData.value)
            renderNickName(nickNameLiveData.value)
        }
    }

    private fun renderAvatar(_avatar: ImageUrl?) {
        if (_avatar.isNullOrBlank()) return //TODO: 将头像置空
        avatar.loadFromUrl(_avatar)
    }

    private fun renderNickName(_nickName: String?){
        nickName.text = _nickName
    }

}
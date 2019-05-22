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
package com.qint.pt1.features.login

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import com.qint.pt1.R
import com.qint.pt1.base.exception.Failure
import com.qint.pt1.base.navigation.Navigator
import com.qint.pt1.base.platform.BaseFragment
import com.qint.pt1.base.platform.RequestCallback
import com.qint.pt1.features.users.UsersRepository
import com.qint.pt1.util.LocationHelper
import kotlinx.android.synthetic.main.login_fragment.*
import javax.inject.Inject

class LoginFragment : BaseFragment() {
    @Inject internal lateinit var navigator: Navigator
    @Inject internal lateinit var usersRepository: UsersRepository
    @Inject internal lateinit var login: Login
    @Inject internal lateinit var locationHelper: LocationHelper

    //FIXME: 转到settings中统一封装
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mobile: String
    private lateinit var password: String

    private val KEY_MOBILE = "Login.KEY_MOBILE"
    private val KEY_PASSWORD = "Login.KEY_PASSWORD"

    override fun layoutId() = R.layout.login_fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
        locationHelper.init(baseActivity)
        loadSavedAccountInfo()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun loadSavedAccountInfo(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        mobile = sharedPreferences.getString(KEY_MOBILE, "")
        password = sharedPreferences.getString(KEY_PASSWORD, "")
    }

    private fun initView() {
        mobileInput.setText(mobile)
        verifyCodeInput.setText(password)

        loginButton.setOnClickListener {
            val mobile = mobileInput.text.toString()
            val verifyCode = verifyCodeInput.text.toString()

            //保存上次输入的用户名口令以省去再次输入
            with(sharedPreferences.edit()){
                putString(KEY_MOBILE, mobile)
                putString(KEY_PASSWORD, verifyCode)
                apply()
            }

            if (mobile.isNotBlank()) {
                login.login(mobile, verifyCode, object : RequestCallback<LoginInfo> {
                    override fun onSuccess(loginInfo: LoginInfo?) {
                        login.reportLocation(locationHelper)
                        navigator.showMain(activity!!) //TODO: 登录成功后回到之前触发登录的目标页面
                    }

                    override fun onFailure(failure: Failure) = handleFailure(failure)
                })
            }
        }
    }

}

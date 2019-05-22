/*
 * Copyright (c) 2019. QINT.TV
 * Author: Matthew Zhang
 * Created on: 3/21/19 10:11 PM
 */

package com.qint.pt1.features.main

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.qint.pt1.R
import com.qint.pt1.base.navigation.Navigator
import com.qint.pt1.base.platform.BaseActivity
import com.qint.pt1.features.account.AccountFragment
import com.qint.pt1.features.chatrooms.ChatRoomsFragment
import com.qint.pt1.features.login.Login
import com.qint.pt1.features.messages.MessagesFragment
import com.qint.pt1.features.users.UsersFragment
import com.qint.pt1.util.LocationHelper
import kotlinx.android.synthetic.main.main_activity.*
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask
import org.jetbrains.anko.singleTop
import javax.inject.Inject

class MainActivity : BaseActivity() {

    override fun layoutId() = R.layout.main_activity

    @Inject internal lateinit var usersFragment: UsersFragment
    @Inject internal lateinit var chatroomsFragment: ChatRoomsFragment
    @Inject internal lateinit var messagesFragment: MessagesFragment
    @Inject internal lateinit var accountFragment: AccountFragment

    @Inject internal lateinit var navigator: Navigator
    @Inject internal lateinit var login: Login
    @Inject internal lateinit var locationHelper: LocationHelper

    private val fragmentManager: FragmentManager = supportFragmentManager

    companion object {
        //FIXME: 加clearTop和newTask标志以防止按返回键时显示登录界面，但看起来好像不生效。
        fun callingIntent(context: Context) =
            context.intentFor<MainActivity>().singleTop().clearTop().newTask()
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_recommend_users -> {
                switchFragment(usersFragment, "推荐")
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_chatrooms -> {
                switchFragment(chatroomsFragment, "聊天室")
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_messages -> {
                if(login.isLogined) {
                    switchFragment(messagesFragment, "消息")
                    return@OnNavigationItemSelectedListener true
                }else{
                    navigator.showLogin(this)
                    return@OnNavigationItemSelectedListener true
                }
            }
            R.id.navigation_profile -> {
                if(login.isLogined) {
                    switchFragment(accountFragment, "我的")
                    return@OnNavigationItemSelectedListener true
                }else{
                    navigator.showLogin(this)
                    return@OnNavigationItemSelectedListener true
                }
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)

        initView()
        initLocation()
    }

    private fun initView(){
        setUpFragments()
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    private fun initLocation() {
        locationHelper.init(this)
        locationHelper.requestAndReportLocation()
    }

    private fun setUpFragments() {
        switchFragment(usersFragment, "推荐")
    }

    private fun switchFragment(fragment: Fragment, title: String) {
        fragmentManager.beginTransaction().replace(R.id.mainContentContainer, fragment).commit()
    }

}

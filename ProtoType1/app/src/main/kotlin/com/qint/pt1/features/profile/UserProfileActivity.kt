/*
 * Copyright (c) 2019. QINT.TV
 * Author: Matthew Zhang
 * Created on: 3/26/19 11:40 AM
 */

package com.qint.pt1.features.profile

import android.content.Context
import android.os.Bundle
import com.qint.pt1.base.platform.BaseFragmentActivity
import com.qint.pt1.domain.UserId
import kotlinx.android.synthetic.main.profile_fragment.*
import org.jetbrains.anko.intentFor

class UserProfileActivity : BaseFragmentActivity() {
    override fun fragment() = UserProfileFragment()

    companion object {
        fun callingIntent(context: Context, userId: UserId) =
            context.intentFor<UserProfileActivity>("userId" to userId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initAppBar()
    }

    private fun initAppBar(){
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) //在appbar中显示左侧的向上按钮
    }
}
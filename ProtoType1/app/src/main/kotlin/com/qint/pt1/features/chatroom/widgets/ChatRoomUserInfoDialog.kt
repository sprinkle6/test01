/*
 * Author: Matthew Zhang
 * Created on: 5/19/19 11:26 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.features.chatroom.widgets

import android.content.Context
import com.qint.pt1.R
import com.qint.pt1.api.sys.MetaData
import com.qint.pt1.base.extension.invisible
import com.qint.pt1.base.extension.loadFromUrl
import com.qint.pt1.base.extension.visible
import com.qint.pt1.base.navigation.Navigator
import com.qint.pt1.base.platform.BaseDialog
import com.qint.pt1.domain.ChatRoomUserInfo
import com.qint.pt1.domain.Gender
import com.qint.pt1.features.chatroom.ChatRoomViewModel
import com.qint.pt1.features.chatroom.UserTagDisplayHelper
import com.qint.pt1.features.login.Login
import kotlinx.android.synthetic.main.chatroom_userinfo_panel.*
import kotlinx.android.synthetic.main.common_gender_age_tag.*
import org.jetbrains.anko.toast
import javax.inject.Inject

//TODO: implement this
class ChatRoomUserInfoDialog(context: Context,
                             val userInfo: ChatRoomUserInfo,
                             val chatRoomViewModel: ChatRoomViewModel,
                             val seatIdx: Int): BaseDialog(context, R.layout.chatroom_userinfo_panel ) {
    init{
        init()
    }

    @Inject internal lateinit var navigator: Navigator
    @Inject internal lateinit var login: Login
    @Inject internal lateinit var metadata: MetaData
    @Inject internal lateinit var userTagDisplayHelper: UserTagDisplayHelper

    private fun init(){
        if (userInfo.avatar.isNotEmpty()) {
            guestInfoAvatar.loadFromUrl(userInfo.avatar)
        }
        guestInfoAvatar.setOnClickListener {
            navigator.showUserProfile(context, userInfo.userId)
        }
        guestInfoNickName.text = userInfo.nickName
        guestInfoUserId.text = "ID:${userInfo.userId}"

        //TODO: 年龄
        val genderAge = "${userInfo.gender.toIconString()}"
        when (userInfo.gender) { //FIXME：控件不支持直接设置颜色，先用两个控件切换的方式临时hack一下
            Gender.MALE -> {
                genderAgeMale.text = genderAge
                genderAgeMale.visible()
                genderAgeFamale.invisible()
            }
            Gender.FAMALE -> {
                genderAgeFamale.text = genderAge
                genderAgeFamale.visible()
                genderAgeMale.invisible()
            }
            Gender.UNKNOWN -> {
                guestInfoGenderAge.invisible()
            }
        }

        userInfo.nobleLevel = metadata.getNobleLevel(userInfo.nobleLevel.level)
        val tag = userTagDisplayHelper.getTag(userInfo, false, false)
        guestInfoLevelTag.text = tag.append("  ")

        guestInfoHomePageButton.setOnClickListener {
            navigator.showUserProfile(context, userInfo.userId)
        }

        //左侧红色按钮，如果是显示的当前登录用户自己的信息，则显示"续费"，否则显示"打赏"
        if (login.isMe(userInfo.userId)) {
            guestInfoRedButton.text = "续费"
            guestInfoRedButton.setOnClickListener {
                context.toast("续费")
            }
        } else {
            guestInfoRedButton.text = "打赏"
            guestInfoRedButton.setOnClickListener {
                if (!login.isLogined) navigator.showLogin(context)
                //FIXME: 需要调整结构，将显示打赏面板的操作从外部注入
//                showDonatePanel(listOf(userInfo))
                dismiss()
            }
        }

        //右侧蓝色按钮，如果是显示的当前登录用户自己的信息，则显示"下麦"，否则显示"关注"
        if (login.isMe(userInfo.userId)) {
            if (chatRoomViewModel.isOnMic) {
                guestInfoBlueButton.text = "下麦"
                guestInfoBlueButton.setOnClickListener {
                    chatRoomViewModel.releaseSeat()
                    dismiss()
                }
            } else {
                guestInfoBlueButton.invisible()
            }
        } else {
            guestInfoBlueButton.text = "关注"
            guestInfoBlueButton.setOnClickListener {
                if (!login.isLogined) navigator.showLogin(context)
                context.toast("关注") //FIXME: to implement
            }
        }

        if (chatRoomViewModel.hostIsMe) {
            if (login.isMe(userInfo.userId)) {
                guestInfoAdminReleaseSeatButton.invisible()
            } else {
                guestInfoAdminReleaseSeatButton.visible()
                guestInfoAdminReleaseSeatButton.setOnClickListener {
                    chatRoomViewModel.releaseSeat(seatIdx)
                    dismiss()
                }
            }

            guestInfoAdminMicControlButton.visible()
            guestInfoAdminMicControlButton.setOnClickListener {
                context.toast("关麦/开麦") //FIXME: to implement
            }
        } else {
            guestInfoAdminReleaseSeatButton.invisible()
            guestInfoAdminMicControlButton.invisible()
        }

    }
}
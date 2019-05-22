/*
 * Author: Matthew Zhang
 * Created on: 5/15/19 9:47 AM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.features.chatroom.widgets

import android.content.Context
import android.os.Handler
import android.os.Message
import android.text.Html
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import androidx.constraintlayout.widget.ConstraintLayout
import com.qint.pt1.R
import com.qint.pt1.base.extension.invisible
import com.qint.pt1.base.extension.loadFromUrl
import com.qint.pt1.base.extension.visible
import com.qint.pt1.features.chatroom.DonateNotification
import com.qint.pt1.features.chatroom.UserTagDisplayHelper
import kotlinx.android.synthetic.main.chatroom_donate_item.view.*

interface DonateItemLayoutListener{
    fun onAnimationEnd(item: DonateNotification)
}

class DonateItemLayout(context: Context): ConstraintLayout(context), Animation.AnimationListener{

    companion object{
        const val SHOW_TIME = 3000L
        const val MESSAGE_CODE = 0
    }

    enum class DisplayState{
        Default, Show
    }

    var state: DisplayState = DisplayState.Default

    private lateinit var notification: DonateNotification
    private lateinit var numAnim: Animation
    private lateinit var notificationInAnim: Animation
    private lateinit var notificationOutAnim: Animation
    lateinit var listener: DonateItemLayoutListener

    private val handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                MESSAGE_CODE -> {
                    this.removeCallbacksAndMessages(null)
                    state = DisplayState.Default
                    startNotificationOutAnimation()
                }
            }
        }
    }

    init{
        init(getContext())
    }

    private fun init(context: Context){
        View.inflate(context, R.layout.chatroom_donate_item, this)
        initNumAnim()
        initNotificationInAnim()
        initNotificationOutAnim()
    }

    private fun initNumAnim(){
        numAnim = ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        numAnim.setDuration(200);
        numAnim.setAnimationListener(this);
    }

    private fun initNotificationInAnim(){
        notificationInAnim = AnimationUtils.loadAnimation(context, R.anim.chatroom_donate_notification_in)
        notificationInAnim.fillAfter = true
        notificationInAnim.setAnimationListener(this)
    }

    private fun initNotificationOutAnim(){
        notificationOutAnim = AnimationUtils.loadAnimation(context, R.anim.chatroom_donate_notification_out)
        notificationOutAnim.fillAfter = true
        notificationOutAnim.setAnimationListener(this)
    }

    fun setData(notification: DonateNotification){
        this.notification = notification
        val donate = notification.donate
        val fromUserInfo = notification.from

        if(fromUserInfo.avatar.isNotEmpty()) {
            avatar.loadFromUrl(fromUserInfo.avatar)
            avatar.visible()
        }else{
            avatar.invisible() //FIXME: 显示默认头像
        }

        message.text = Html.fromHtml("${fromUserInfo.nickName}<font color='${UserTagDisplayHelper.MESSAGE_HIGHLIGHT_COLOR}'>打赏</font>${donate.toUserName}")

        val gift = donate.gift
        if(gift != null && gift.icon.isNotBlank()){
            giftImg.loadFromUrl(gift.icon)
            giftImg.visible()
        }else{
            giftImg.invisible()
        }

        giftCount.text = donate.count.toString()
    }

    fun updateCount(){
        handler.removeMessages(MESSAGE_CODE)
        val donate = notification.donate
        giftCount.text = donate.count.toString()
        giftCount.startAnimation(numAnim)
    }

    fun startNotificationInAnimation(){
        state = DisplayState.Show
        startAnimation(notificationInAnim)
    }

    fun startNotificationOutAnimation(){
        startAnimation(notificationOutAnim)
    }

    override fun onAnimationEnd(animation: Animation?) {
        when(animation){
            notificationInAnim -> {
                giftCount.startAnimation(numAnim)
            }
            numAnim -> {
                handler.sendEmptyMessageDelayed(MESSAGE_CODE, SHOW_TIME)
            }
            notificationOutAnim -> {
                if(::listener.isInitialized) {
                    listener.onAnimationEnd(notification)
                }
            }
        }
    }

    override fun onAnimationStart(animation: Animation?) { }

    override fun onAnimationRepeat(animation: Animation?) { }

}
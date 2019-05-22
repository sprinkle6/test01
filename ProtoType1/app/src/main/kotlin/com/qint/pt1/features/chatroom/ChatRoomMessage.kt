/*
 * Author: Matthew Zhang
 * Created on: 4/24/19 11:59 AM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.features.chatroom

import com.qint.pt1.domain.ChatRoomUserInfo
import com.qint.pt1.domain.Donate
import com.qint.pt1.domain.Seat
import com.qint.pt1.domain.Sticker

sealed class ChatRoomMessage{
    var successSend = false

    object UnSupportedMessage: ChatRoomMessage()
    data class InvaildMessage(val info: String): ChatRoomMessage()
}

/*
 * 聊天室中用户发送的消息
 */
sealed class ChatRoomUserMessage(open val sender: ChatRoomUserInfo) : ChatRoomMessage(){
    var isSendFromChatRoomHost: Boolean = false
}

/*
 * 普通聊天消息
 */
data class ChatMessage(val text: String, override val sender: ChatRoomUserInfo) : ChatRoomUserMessage(sender)

/*
 * 表情消息
 */
data class StickerMessage(val sticker: Sticker, override val sender: ChatRoomUserInfo) : ChatRoomUserMessage(sender)

/*
 * 由系统发送的聊天室通知
 */
sealed class ChatRoomNotificationMessage : ChatRoomMessage()

/*
 * 系统通知信息
 */
data class InformationNotification(val info: String) : ChatRoomNotificationMessage()

/*
 * 新用户进入聊天室显示的欢迎消息
 */
data class WelcomeNotification(val newChatRoomMember: ChatRoomUserInfo) : ChatRoomNotificationMessage(){
    override fun toString() = "${newChatRoomMember.nickName}来了"
}

object LeaveNotification : ChatRoomNotificationMessage()

/*
 * 聊天室座位变更通知
 */
data class SeatChangeNotification(val seat: Seat) : ChatRoomNotificationMessage(){
    var original: Seat? = null //有变化的座位在变化之前的状态
}

/*
 * 聊天室排麦通知
 */
class WaitQueueNotification(val waitQueue: List<SeatQueueItem>) : ChatRoomNotificationMessage()

/*
 * 赠送礼物/打赏通知: 1:M
 */
data class DonatesNotification(val from: ChatRoomUserInfo, val donates: List<Donate>) : ChatRoomNotificationMessage()

/*
 * 赠送礼物/打赏通知：1:1
 */
data class DonateNotification(val from: ChatRoomUserInfo, val donate: Donate): ChatRoomNotificationMessage(), Cloneable{
    fun canMerge(other: DonateNotification?) = if(other == null) false else donate.canMerge(other.donate)

    fun merge(other: DonateNotification) = donate.merge(other.donate)

    override fun clone(): Any {
        //FIXME: clone实现可能有问题，无参数调用copy时似乎还是浅拷贝，donate没有复制一份新的，需要调用copy(donate = origin.donate.copy())才行。
        //主要影响是聊天室打赏通知消息显示时，如果连续打赏并合并礼物计数，则聊天室消息列表中显示的打赏消息的计数也会一并变化
        return DonateNotification(from, donate.copy())
    }
}

/*
 * 开箱子通知
 */
data class OpenBoxNotification(val info: String) : ChatRoomNotificationMessage() //FIXME: implement this

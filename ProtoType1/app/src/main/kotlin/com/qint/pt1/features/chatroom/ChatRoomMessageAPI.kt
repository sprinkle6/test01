/*
 * Author: Matthew Zhang
 * Created on: 4/24/19 1:30 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.features.chatroom

import com.netease.nimlib.sdk.StatusCode
import com.qint.pt1.base.platform.RequestCallback
import com.qint.pt1.base.sdk.ThirdPartySDK
import com.qint.pt1.domain.ChatRoomId
import com.qint.pt1.domain.ImageUrl
import com.qint.pt1.domain.Name

interface ChatRoomMessageAPI: ThirdPartySDK {
    fun setMessageHandler(handler: (List<ChatRoomMessage>) -> Unit)
    fun joinChatRoom(messagePlatformChatRoomId: ChatRoomId, appPlatformChatRoomId: ChatRoomId, callback: RequestCallback<ChatRoomInfo>)
    fun leaveChatRoom()
    fun sendMessage(message: ChatRoomUserMessage, callback: RequestCallback<Unit>)

    //FIXME: 座位信息不一定由聊天室系统管理，目前只是在使用云信聊天室时这样实现，之后还是应考虑分离到不同接口
    fun getSeatInfo(callback: RequestCallback<Map<String, String>>)

    fun observeOnlineStatus(handler: (statusCode: StatusCode) -> Unit) //FIXME: 不应引入网易的数据类型依赖
}

data class ChatRoomInfo(val name: Name, val userCount: Int, val announcement: String, val background: ImageUrl? = null)
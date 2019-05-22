/*
 * Author: Matthew Zhang
 * Created on: 5/16/19 5:59 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.support.agora

import android.content.Context
import android.util.Log
import com.netease.nimlib.sdk.StatusCode
import com.qint.pt1.base.platform.RequestCallback
import com.qint.pt1.domain.ChatRoomId
import com.qint.pt1.features.chatroom.*
import com.qint.pt1.features.chatrooms.ChatRoomFailure
import com.qint.pt1.features.login.Login
import com.qint.pt1.util.LOG_TAG
import io.agora.AgoraAPIOnlySignal
import io.agora.NativeAgoraAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import proto_def.Message.MessagePackage
import javax.inject.Inject

@Deprecated("不再使用，改用MessageAPI接口，通过依赖注入获取MessageAPI的实例")
class AgoraMessageApi
@Inject constructor(private val login: Login): ChatRoomMessageAPI {
    //TODO: 分离接口，并使具体实现只对需要的ViewModel可见
    //FIXME: 与ChatRoomViewModel耦合过紧，需要解耦以适用于其它地方
    override fun observeOnlineStatus(handler: (statusCode: StatusCode) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setMessageHandler(handler: (List<ChatRoomMessage>) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun joinChatRoom(messagePlatformChatRoomId: ChatRoomId, appPlatformChatRoomId: ChatRoomId, callback: RequestCallback<ChatRoomInfo>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    override fun leaveChatRoom() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sendMessage(message: ChatRoomUserMessage, callback: RequestCallback<Unit>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun config(context: Context) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun init(context: Context) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSeatInfo(callback: RequestCallback<Map<String, String>>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private lateinit var chatRoomViewModel: ChatRoomViewModel
    private lateinit var agoraApi: AgoraAPIOnlySignal
    private var isLogined = false
    private var isJoined = false

    private val account get() = login.account?.userId ?: "anonymoususer"

    private val appId: String
        get() = chatRoomViewModel.agoraAppId

    private val roomId: ChatRoomId
        get() = chatRoomViewModel.roomId

    fun initMessageApiAndLogin(chatRoomViewModel: ChatRoomViewModel, messageApi: AgoraAPIOnlySignal) {
        this.chatRoomViewModel = chatRoomViewModel
        this.agoraApi = messageApi

        val timeOutInSeconds = 10
        val retryTime = 1

        //FIXME: 使用ss时登录失败
        //TODO: 改为使用token登录
        agoraApi.login2(appId, account, "_no_need_token", 0, null, timeOutInSeconds, retryTime)
        agoraApi.callbackSet(agoraApiEventHandler)
    }

    fun destroy() {
        if (isLogined) {
            leaveRoom()
            agoraApi.logout()
        }
        agoraApi.destroy()
    }

    fun leaveRoom() {
        if (isLogined && isJoined) {
            agoraApi.channelLeave(roomId)
        }
    }

    private fun sendMessageInChannel(contentString: String, messageId: String = "") {
        //TODO: 处理发送失败的情况
        if (!isLogined || !isJoined) return

        if (!isMessageVaild(contentString, messageId)) return

        CoroutineScope(Dispatchers.IO).launch {
            agoraApi.messageChannelSend(roomId, contentString, messageId)
        }
    }

    fun sendMessageInChannel(msgPack: MessagePackage, messageId: String = "") {
        val dataString = msgPack.encodeToBase64String()
        sendMessageInChannel(dataString, messageId)
    }

    private fun isMessageVaild(content: String, messageId: String = ""): Boolean {
        if (content.isNullOrBlank()) return false
        //TODO: 继续检查消息长度及messageId等有无超出限制
        return true
    }

    private val agoraApiEventHandler = object : NativeAgoraAPI.CallBack() {
        override fun onLoginSuccess(uid: Int, fd: Int) {
            isLogined = true
            CoroutineScope(Dispatchers.IO).launch {
                agoraApi.channelJoin(roomId)
            }
        }

        override fun onLoginFailed(ecode: Int) {
            Log.e(LOG_TAG, "agroaAPI: onLoginFailed(${ecode})")
            CoroutineScope(Dispatchers.Main).launch {
                chatRoomViewModel.unsetLoadingStatus(ChatRoomViewModel.LOADING_STATUS_MESSAGE_API)
                chatRoomViewModel.trigerFailure(ChatRoomFailure.SignalLoginFailed)
            }
        }

        override fun onLogout(ecode: Int) {
            isLogined = false
        }

        override fun onChannelJoined(channelID: String?) {
            isJoined = true
            chatRoomViewModel.joinedMessageChannel()
            agoraApi.channelQueryUserNum(roomId)
        }

        override fun onMessageChannelReceive(channelID: String?, account: String?, uid: Int, encodedMessage: String?) {
            if (encodedMessage.isNullOrBlank()) return
            if (account == account) return //本地发出的消息，忽略显示。如果允许一个账号多客户端登录，则该账号的其它已登录客户端会收不到该条消息。
            chatRoomViewModel.getMessage(encodedMessage)
            /*
            val msgPack: MessagePackage? = encodedMessage.decodeFromBase64StringToMessagePackage()
            if (msgPack == null) { //解析失败，当做纯文本处理，兼容一下纯文本的消息。也许不应该留这个后门？
                chatRoomViewModel.getMessage(encodedMessage)
            } else {
                chatRoomViewModel.getMessage(msgPack)
            }
            */
        }

        override fun onChannelJoinFailed(channelID: String?, ecode: Int) {
            Log.e(LOG_TAG, "agoraAPI: onChannelJoinFailed(${channelID}, ${ecode})")
            CoroutineScope(Dispatchers.Main).launch {
                chatRoomViewModel.unsetLoadingStatus(ChatRoomViewModel.LOADING_STATUS_MESSAGE_API)
                chatRoomViewModel.trigerFailure(ChatRoomFailure.SignalChannelJoinFailed)
            }
        }

        override fun onChannelLeaved(channelID: String?, ecode: Int) {
            isJoined = false
        }

        override fun onChannelUserJoined(account: String?, uid: Int) {
            if (account != null && account.isNotEmpty()) chatRoomViewModel.userJoined(account)
        }

        override fun onChannelUserLeaved(account: String?, uid: Int) {
            if (account != null && account.isNotEmpty()) chatRoomViewModel.userLeaved(account)
        }

        override fun onChannelUserList(accounts: Array<out String>?, uids: IntArray?) {
            //通知chatRoomViewModel
        }

        override fun onChannelQueryUserNumResult(channelID: String?, ecode: Int, num: Int) {
            CoroutineScope(Dispatchers.Main).launch {
                if (ecode == 0) {
                    chatRoomViewModel.userNumInChannelLiveData.value = num
                } else {
                    Log.e(LOG_TAG, "agroaAPI: onChannelQueryUserNumResule(${channelID}, ${ecode}, ${num})")
                }
            }
        }

        override fun onError(name: String?, ecode: Int, desc: String?) {
            Log.e(LOG_TAG, "agoraAPI: onError(${name}, ${ecode}, ${desc})")
            //TODO：根据错误码细分处理
            CoroutineScope(Dispatchers.Main).launch {
                chatRoomViewModel.trigerFailure(ChatRoomFailure.SignalApiError)
            }
        }
    }
}
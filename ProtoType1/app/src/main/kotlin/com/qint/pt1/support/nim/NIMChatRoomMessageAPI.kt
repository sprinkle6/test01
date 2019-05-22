/*
 * Author: Matthew Zhang
 * Created on: 4/25/19 9:02 AM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.support.nim

import android.content.Context
import android.util.Log
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.StatusCode
import com.netease.nimlib.sdk.chatroom.ChatRoomMessageBuilder
import com.netease.nimlib.sdk.chatroom.ChatRoomService
import com.netease.nimlib.sdk.chatroom.ChatRoomServiceObserver
import com.netease.nimlib.sdk.chatroom.model.ChatRoomNotificationAttachment
import com.netease.nimlib.sdk.chatroom.model.ChatRoomQueueChangeAttachment
import com.netease.nimlib.sdk.chatroom.model.EnterChatRoomData
import com.netease.nimlib.sdk.chatroom.model.EnterChatRoomResultData
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.ChatRoomQueueChangeType
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum
import com.netease.nimlib.sdk.msg.constant.NotificationType
import com.netease.nimlib.sdk.util.Entry
import com.qint.pt1.api.sys.MetaData
import com.qint.pt1.base.exception.Failure
import com.qint.pt1.base.extension.base64Decode
import com.qint.pt1.base.platform.RequestCallback
import com.qint.pt1.domain.*
import com.qint.pt1.features.chatroom.*
import com.qint.pt1.features.chatrooms.ChatRoomFailure
import com.qint.pt1.features.login.Login
import com.qint.pt1.util.LOG_TAG
import proto_def.RoomMessage
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

typealias NIMChatRoomMessage = com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage
typealias NIMChatRoomInfo = com.netease.nimlib.sdk.chatroom.model.ChatRoomInfo

@Singleton
class NIMChatRoomMessageAPI
@Inject constructor(private val login: Login,
                    private val meataData: MetaData,
                    private val getNIMChatRoomServAddrUseCase: GetNIMChatRoomServAddrUseCase): ChatRoomMessageAPI {

    private val chatRoomService: ChatRoomService by lazy{
        NIMClient.getService(ChatRoomService::class.java)
    }

    private val chatRoomServiceObserver: ChatRoomServiceObserver by lazy{
        NIMClient.getService(ChatRoomServiceObserver::class.java)
    }

    private lateinit var messageHandler: (List<ChatRoomMessage>) -> Unit
    private var chatRoomId: ChatRoomId? = null //云信系统的聊天室ID
    val isInChatRoom get() = chatRoomId != null

    private val MAX_RETRY = 3
    private val EXTENSION_KEY_USERINFO = "USERINFO"
    private val EXTENSION_KEY_ROOM_BACKGROUND = "CHATROOM_BACKGROUND"

    private val userExtensionInfo: Map<String, String> get() =
        if(login.isLogined)
            mapOf(EXTENSION_KEY_USERINFO to login.user!!.toMessageUserInfo().encode())
        else
            emptyMap()

    override fun config(context: Context) {}

    override fun init(context: Context) {
        NIMClient.getService(MsgService::class.java).registerCustomAttachmentParser(CustomAttachParser(context))
    }

    override fun setMessageHandler(handler: (List<ChatRoomMessage>) -> Unit) {
        this.messageHandler = handler

        chatRoomServiceObserver.observeReceiveMessage({ messages ->
            handler(messages.map{it.toChatRoomMessage()})
        }, true)
    }

    override fun joinChatRoom(messagePlatformChatRoomId: ChatRoomId,
                              appPlatformChatRoomId: ChatRoomId,
                              callback: RequestCallback<ChatRoomInfo>) {
        //FIXME: 处理已经进入聊天室后断线自动重连失败的情况
        //FIXME: 耗时操作，改为成功后再显示聊天室界面
        val enterParams = EnterChatRoomData(messagePlatformChatRoomId)
        val userInfo = login.user?.toMessageUserInfo() ?: ChatRoomUserInfo.AnonymousMessageUserInfo
        enterParams.nick = userInfo.nickName
        enterParams.avatar = userInfo.avatar.url

        if(login.isLogined) { //已登录，非独立模式进入聊天室
            enterParams.extension = userExtensionInfo
            enterParams.notifyExtension = userExtensionInfo
            enterChatRoom(messagePlatformChatRoomId, callback, enterParams)
        }else{ //未登录，使用独立模式匿名登录进入聊天室
            //TODO: 缓存聊天室服务器列表
            getNIMChatRoomServAddrUseCase(appPlatformChatRoomId){
               it.either(
                   {_failure ->
                       callback.onFailure(Failure.ServerError(501, "无法获取聊天室服务器列表: ${_failure.message}"))
                   },
                   {_addresses ->
                       if(_addresses.isNullOrEmpty()){
                           callback.onFailure(Failure.ServerError(502, "聊天室服务器列表为空"))
                       }else{
                           enterParams.setIndependentMode({roomId, _ ->
                               return@setIndependentMode _addresses
                           }, null, null)
                           Log.d(LOG_TAG, "get anonymous chatroom serv: ${_addresses}")
                           enterChatRoom(messagePlatformChatRoomId, callback, enterParams)
                       }
                   }
               )
            }
        }

    }

    private fun enterChatRoom(roomId: ChatRoomId, callback: RequestCallback<ChatRoomInfo>, enterParams: EnterChatRoomData){
        chatRoomService.enterChatRoomEx(enterParams, MAX_RETRY)
            .setCallback(object: NIMRequestCallback<EnterChatRoomResultData> {
                override fun onSuccess(result: EnterChatRoomResultData) {
                    chatRoomId = roomId
                    val nimChatRoomInfo: NIMChatRoomInfo = result.roomInfo
                    logChatRoomInfo(nimChatRoomInfo)
                    val nimRoomExtension: Map<String, Any>? = nimChatRoomInfo.extension
                    val background: ImageUrl? = nimRoomExtension?.get(EXTENSION_KEY_ROOM_BACKGROUND) as ImageUrl?
                    val chatRoomInfo = ChatRoomInfo(nimChatRoomInfo.name, nimChatRoomInfo.onlineUserCount, nimChatRoomInfo.announcement ?: "", background)
                    callback.onSuccess(chatRoomInfo)
                    Log.d( LOG_TAG, "NIMChatRoomMessageAPI enter chatroom $roomId success, get chatRoomInfo: ${chatRoomInfo}" )
                }

                override fun onFailed(code: Int) {
                    chatRoomId = null
                    Log.e( LOG_TAG, "NIMChatRoomMessageAPI enter chatroom $roomId failed: $code" )
                    onEnterFailed(code, callback)
                }

                override fun onException(exception: Throwable) {
                    chatRoomId = null
                    Log.e( LOG_TAG, "NIMChatRoomMessageAPI enter chatroom $roomId exception: $exception" )
                    callback.onFailure(ChatRoomFailure("加入聊天室失败：$exception"))
                }

            }
        )
    }

    private fun onEnterFailed(code: Int, callback: RequestCallback<ChatRoomInfo>){
        callback.onFailure(when(code){
            414 -> Failure.ParameterInvaildFailure("进入聊天室参数错误")
            404 -> ChatRoomFailure.RoomNotExist
            403 -> ChatRoomFailure.NoRoomPermission
            500 -> Failure.ServerError(500, "聊天室服务器异常")
            13001 -> ChatRoomFailure("聊天室主连接状态异常，请重试")
            13002 -> ChatRoomFailure.RoomInvaild
            13003 -> ChatRoomFailure.UserInRoomBlackList
            else -> ChatRoomFailure("加入聊天室失败：$code")
        })
    }

    private fun logChatRoomInfo(chatroomInfo: NIMChatRoomInfo){
        Log.d(LOG_TAG, """
            ChatRoomInfo:
                        name: ${chatroomInfo.name}
                        creator: ${chatroomInfo.creator}
                        extension: ${chatroomInfo.extension}
                        announcement: ${chatroomInfo.announcement}
                        broadcasturl: ${chatroomInfo.broadcastUrl}
                        onlineUserCount: ${chatroomInfo.onlineUserCount}
                        queueLevel: ${chatroomInfo.queueLevel}
                        isMute: ${chatroomInfo.isMute}
                        isVaild: ${chatroomInfo.isValid}
                    """.trimIndent())
        val ext = chatroomInfo.extension ?: return
        for((k,v) in ext){
            Log.d(LOG_TAG, "ChatRoomInfo.extension: ${k} -> ${v}, ${v.javaClass}")
        }
    }

    override fun leaveChatRoom() {
        chatRoomService.exitChatRoom(chatRoomId)
        chatRoomId = null
    }

    override fun sendMessage(message: ChatRoomUserMessage, callback: RequestCallback<Unit>) {
        if(!isInChatRoom){
            callback.onFailure(ChatRoomFailure.SendMessageFailure("必须进入聊天室才能发送消息"))
            return
        }

        if(!login.isLogined){
            callback.onFailure(ChatRoomFailure.SendMessageFailure("必须先登录才能发送消息"))
            return
        }

        var nimMessage: NIMChatRoomMessage? = message.toNIMChatRoomMessage(chatRoomId!!)

        if(nimMessage == null){
            Log.e(LOG_TAG, "try to send unsupported ChatRoomMessage: $message")
            callback.onFailure(ChatRoomFailure.SendMessageFailure("无法发送不支持的消息类型"))
            return
        }

        //将用户的等级、头衔等信息通过扩展属性附加到消息中
        //nimMessage.remoteExtension = createMessageExt(message) //由于进入房间时已设置了扩展信息，此处不再需要

        val resendOnFail = false
        chatRoomService.sendMessage(nimMessage, resendOnFail)
            .setCallback(object: NIMRequestCallback<Void> {
                override fun onSuccess(param: Void?) {
                    Log.d( LOG_TAG, "NIMChatRoomMessageAPI send message success: $message" )
                    callback.onSuccess(Unit)
                }

                override fun onFailed(code: Int) {
                    Log.e( LOG_TAG, "NIMChatRoomMessageAPI send message failed($code): $message" )
                    callback.onFailure(when(code){
                        13004 -> ChatRoomFailure.SendMessageFailure( "您当前被禁言" )
                        13006 -> ChatRoomFailure.SendMessageFailure( "聊天室整体被禁言，当前只有管理员能发言" )
                        else -> ChatRoomFailure.SendMessageFailure( "发送消息失败：$code" )
                    })
                }

                override fun onException(exception: Throwable) {
                    Log.e( LOG_TAG, "NIMChatRoomMessageAPI send message $message exception: $exception" )
                    callback.onFailure(
                        ChatRoomFailure.SendMessageFailure( "发送消息失败：$exception" )
                    )
                }
            })
    }

    private fun logQueue(queue: List<Entry<String, String>>?){
        queue?.forEach {
            Log.d(LOG_TAG, "ChatRoomQueueItem: (${it.key} -> ${it.value})")
        }
    }

    override fun getSeatInfo(callback: RequestCallback<Map<String, String>>) = fetchQueue(callback)

    private fun fetchQueue(callback: RequestCallback<Map<String, String>>){
        Log.d(LOG_TAG, "fetching ChatRoom Queue:")
        chatRoomService.fetchQueue(chatRoomId).setCallback(object: NIMRequestCallback<List<Entry<String, String>>>{
            override fun onSuccess(result: List<Entry<String, String>>?) {
                Log.d(LOG_TAG, "fetch ChatRoom Queue success")
                logQueue(result)
                val map = mutableMapOf<String, String>()
                result?.forEach { map[it.key] = it.value }
                callback.onSuccess(map)
            }

            override fun onFailed(code: Int) {
                Log.e(LOG_TAG, "fetch ChatRoom Queue failed: $code")
                callback.onFailure(ChatRoomFailure("获取聊天室队列信息失败：$code"))
            }

            override fun onException(exception: Throwable?) {
                Log.e(LOG_TAG, "fetch ChatRoom Queue exception: $exception")
                callback.onFailure(ChatRoomFailure("获取聊天室队列信息异常：$exception"))
            }
        })
    }

    private fun pollQueue(key: String, callback: RequestCallback<String>){
        Log.d(LOG_TAG, "polling ChatRoom Queue of key: $key")
        chatRoomService.pollQueue(chatRoomId, key).setCallback(object: NIMRequestCallback<Entry<String, String>>{
            override fun onSuccess(result: Entry<String, String>?) {
                callback.onSuccess(result?.value)
            }

            override fun onFailed(code: Int) {
                Log.e(LOG_TAG, "poll ChatRoom Queue of key $key failed: $code")
                callback.onFailure(ChatRoomFailure("获取聊天室队列${key}失败：$code"))
            }

            override fun onException(exception: Throwable?) {
                Log.e(LOG_TAG, "poll ChatRoom Queue of key $key exception: $exception")
                callback.onFailure(ChatRoomFailure("获取聊天室队列${key}异常：$exception"))
            }

        })
    }

    fun setRoomBackgroundImage(imageUrl: ImageUrl){
//        chatRoomService.updateRoomInfo()
    }

    private fun logMessageInfo(nimMessage: NIMChatRoomMessage){
        Log.d(LOG_TAG, """
                NIMMessage:
                    content: ${nimMessage.content}
                    fromAccount: ${nimMessage.fromAccount}
                    fromClientType: ${nimMessage.fromClientType}
                    fromNick: ${nimMessage.fromNick}
                NIMMessage.remoteExtension:
                    ${nimMessage.remoteExtension}
                chatRoomMesssageExtension:
                    senderNick: ${nimMessage.chatRoomMessageExtension.senderNick}
                    senderAvatar: ${nimMessage.chatRoomMessageExtension.senderAvatar}
                    senderExtension: ${nimMessage.chatRoomMessageExtension.senderExtension}
                attachment:
                    ${nimMessage.attachment}
                attachment json:
                    ${nimMessage.attachment?.toJson(false)}
            """.trimIndent())
    }

    private fun logNotification(nimMessage: NIMChatRoomMessage){
        if(nimMessage.msgType != MsgTypeEnum.notification) return
        val attachment = nimMessage.attachment as ChatRoomNotificationAttachment
        Log.d(LOG_TAG, """
                            chatroom notification:
                                type: ${attachment.type}
                                operator: ${attachment.operator}
                                operatorNick: ${attachment.operatorNick}
                                targets: ${attachment.targets}
                                targetNicks: ${attachment.targetNicks}
                                extension: ${attachment.extension}
                                json: ${attachment.toJson(false)}
                        """.trimIndent())
    }

    private fun NIMChatRoomMessage.getSender(): ChatRoomUserInfo = when(msgType) {
        MsgTypeEnum.text -> try {
            var userExtensionStr =
                chatRoomMessageExtension.senderExtension[EXTENSION_KEY_USERINFO] as String? //正常来说从这里取得用户信息，以下除了decode部分外都是容错处理
            if (userExtensionStr.isNullOrBlank()) userExtensionStr =
                remoteExtension[EXTENSION_KEY_USERINFO] as String?
            var userinfo: ChatRoomUserInfo? = null
            if (!userExtensionStr.isNullOrBlank()) {
                userinfo = ChatRoomUserInfo.parseFromBase64Protobuf(userExtensionStr)
            }
            if (userinfo != null) userinfo
            else { //try best to get the nickName
                var nickName: String? = fromNick
                if (nickName.isNullOrBlank()) nickName = chatRoomMessageExtension.senderNick
                if (!nickName.isNullOrBlank())
                    ChatRoomUserInfo(nickName)
                else ChatRoomUserInfo.NullMessageUserInfo
            }
        } catch (exception: Throwable) {
            Log.e(LOG_TAG, "unsupported message extension type: $exception")
            ChatRoomUserInfo.NullMessageUserInfo
        }

        MsgTypeEnum.notification -> try{
            val attachment = this.attachment as ChatRoomNotificationAttachment
            val userExtensionStr = attachment.extension?.get(EXTENSION_KEY_USERINFO) as String? //正常来说从这里取得用户信息，以下为容错处理
            var userInfo: ChatRoomUserInfo? = null
            if(!userExtensionStr.isNullOrBlank()) userInfo = ChatRoomUserInfo.parseFromBase64Protobuf(userExtensionStr)
            if(userInfo != null) {
                Log.d(LOG_TAG, "decoded notification userinfo: $userInfo")
                userInfo
            } else {
                Log.d(LOG_TAG, "failed to decode userinfo of notification")
                ChatRoomUserInfo.NullMessageUserInfo
            }
        }catch (exception: Exception) {
            Log.e(LOG_TAG, "unsupported message extension type: $exception")
            ChatRoomUserInfo.NullMessageUserInfo
        }

        else -> ChatRoomUserInfo.NullMessageUserInfo
    }

    private fun ChatRoomMessage.toNIMChatRoomMessage(chatRoomId: ID) = when(this){
        is ChatMessage ->
            ChatRoomMessageBuilder.createChatRoomTextMessage(
                chatRoomId,
                text
            )
        is StickerMessage ->
            ChatRoomMessageBuilder.createChatRoomTextMessage(
                chatRoomId,
                sticker.tag
            )
        else -> null
    }

    private fun logchatRoomQueueChange(qinfo: ChatRoomQueueChangeAttachment){
        Log.d(LOG_TAG, """
                            ChatRoomQueueChange:
                            queueChangeType: ${qinfo.chatRoomQueueChangeType}
                            content: ${qinfo.content}
                            contentMap: ${qinfo.contentMap}
                            key: ${qinfo.key}
                        """.trimIndent())
    }

    private fun NIMChatRoomMessage.toChatRoomMessage(): ChatRoomMessage = when(msgType){
        MsgTypeEnum.text -> {
            logMessageInfo(this)
            val sender = this.getSender()
            if(Sticker.isVaildStickerTag(content)){
                val tag = content
                val sticker = meataData.getSticker(tag)
                if(sticker != null) {
                    StickerMessage(sticker, sender)
                }else{
                    ChatMessage(content, sender)
                }
            }else {
                ChatMessage(content, sender)
            }
        }

        MsgTypeEnum.notification -> {
            logNotification(this)
            try {
                val attachment = this.attachment as ChatRoomNotificationAttachment
                when (attachment.type) {
                    NotificationType.ChatRoomMemberIn -> {
                        WelcomeNotification(this.getSender())
                    }
                    NotificationType.ChatRoomMemberExit -> LeaveNotification
                    NotificationType.undefined -> InformationNotification(attachment.toJson(false))
                    NotificationType.InviteMember -> InformationNotification(attachment.toJson(false))
                    NotificationType.KickMember -> InformationNotification(attachment.toJson(false))
                    NotificationType.TransferOwner -> InformationNotification(attachment.toJson(false) )
                    NotificationType.ChatRoomMemberBlackAdd -> InformationNotification(attachment.toJson(false))
                    NotificationType.ChatRoomMemberBlackRemove -> InformationNotification(attachment.toJson(false))
                    NotificationType.ChatRoomMemberMuteAdd -> InformationNotification(attachment.toJson(false))
                    NotificationType.ChatRoomMemberMuteRemove -> InformationNotification(attachment.toJson(false))
                    NotificationType.ChatRoomManagerAdd -> InformationNotification(attachment.toJson(false))
                    NotificationType.ChatRoomManagerRemove -> InformationNotification(attachment.toJson(false))
                    NotificationType.ChatRoomCommonAdd -> InformationNotification(attachment.toJson(false))
                    NotificationType.ChatRoomCommonRemove -> InformationNotification(attachment.toJson(false))
                    NotificationType.ChatRoomClose -> InformationNotification(attachment.toJson(false))
                    NotificationType.ChatRoomInfoUpdated -> InformationNotification(attachment.toJson(false))
                    NotificationType.ChatRoomMemberKicked -> InformationNotification(attachment.toJson(false))
                    NotificationType.ChatRoomMemberTempMuteAdd -> InformationNotification(attachment.toJson(false))
                    NotificationType.ChatRoomMemberTempMuteRemove -> InformationNotification(attachment.toJson(false))
                    NotificationType.ChatRoomMyRoomRoleUpdated -> InformationNotification(attachment.toJson(false))
                    NotificationType.ChatRoomQueueChange -> { //座位或者排麦队列有变化
                        val qinfo = attachment as ChatRoomQueueChangeAttachment
                        logchatRoomQueueChange(qinfo)
                        when(qinfo.chatRoomQueueChangeType) {
                            ChatRoomQueueChangeType.OFFER -> when (qinfo.key) {
                                in Seat.SEAT_KEYS -> {
                                    val seat =
                                        qinfo.content.decodeFromBase64ToSeatQueueItem()?.toSeat()
                                    Log.d(LOG_TAG, "decoded seat ${qinfo.key} change notification: $seat from ${qinfo.content}")
                                    if(seat != null){
                                        val nobleLevel = seat.userInfo.nobleLevel
                                        seat.userInfo.nobleLevel = meataData.getNobleLevel(nobleLevel.level) //FIXME: 有时还是会取不到爵位名称，在清除所有座位时有时出现
                                        SeatChangeNotification(seat)
                                    }else {
                                        ChatRoomMessage.InvaildMessage("failed to decode seat information from queue key ${qinfo.key} and content ${qinfo.content}")
                                    }
                                }
                                Seat.WAIT_QUEUE_KEY -> {
                                    WaitQueueNotification(SeatQueueItem.parseQueueFromBase64Protobuf(qinfo.content))
                                }
                                else -> {
                                    Log.e(LOG_TAG, "Unsupported ChatRoomQueueChange notification: key ${qinfo.key} unsupported")
                                    ChatRoomMessage.UnSupportedMessage
                                }
                            }
                            else -> {
                                Log.e(LOG_TAG, "Unsupported ChatRoomQueueChange notification: type ${qinfo.chatRoomQueueChangeType} unsupported")
                                ChatRoomMessage.UnSupportedMessage
                            }
                        }
                    }
                    NotificationType.ChatRoomRoomMuted -> InformationNotification(attachment.toJson(false))
                    NotificationType.ChatRoomRoomDeMuted -> InformationNotification(attachment.toJson(false))
                    NotificationType.ChatRoomQueueBatchChange -> InformationNotification(attachment.toJson(false))
                    else -> InformationNotification(attachment.toJson(false))
                }
            }catch(exception: Exception){
                InformationNotification(this.toString())
            }

        } //FIXME

        MsgTypeEnum.custom -> {
            logMessageInfo(this)
            val attachment: CustomAttachment? = this.attachment as CustomAttachment?
            when(attachment){
                null -> ChatRoomMessage.InvaildMessage("CustomMessage attachment is null")
                is InvaildAttachment -> ChatRoomMessage.InvaildMessage("failed to parse CustomMessage attachment: ${attachment.originalJson}")
                is ExpiredAttachment -> ChatRoomMessage.InvaildMessage("CustomMessage attachment expired: ${attachment.originalJson}")
                is UnSupportedCustomAttachment -> ChatRoomMessage.InvaildMessage("Unsupported CustomMessage: ${attachment.originalJson}")
                is OpenBoxAttachment -> OpenBoxNotification("NOT Implemented")
                is DonateAttachment -> {
                    val nobleLevel = attachment.from.nobleLevel
                    attachment.from.nobleLevel = meataData.getNobleLevel(nobleLevel.level)
                    attachment.donates.forEach { it.gift = meataData.getGift(it.giftId) }
                    DonatesNotification(attachment.from, attachment.donates)
                }
            }
        }
        MsgTypeEnum.undef -> ChatRoomMessage.UnSupportedMessage
        MsgTypeEnum.image -> ChatRoomMessage.UnSupportedMessage
        MsgTypeEnum.audio -> ChatRoomMessage.UnSupportedMessage
        MsgTypeEnum.video -> ChatRoomMessage.UnSupportedMessage
        MsgTypeEnum.location -> ChatRoomMessage.UnSupportedMessage
        MsgTypeEnum.file -> ChatRoomMessage.UnSupportedMessage
        MsgTypeEnum.avchat -> ChatRoomMessage.UnSupportedMessage
        MsgTypeEnum.tip -> ChatRoomMessage.UnSupportedMessage
        MsgTypeEnum.robot -> ChatRoomMessage.UnSupportedMessage
    }

    override fun observeOnlineStatus(handler: (statusCode: StatusCode) -> Unit){ //FIXME: 对这里的异常在UI上做提示或相应界面跳转
        chatRoomServiceObserver.observeOnlineStatus({statusChangeData -> handler(statusChangeData.status) }, true)
        chatRoomServiceObserver.observeOnlineStatus({statusChangeData ->
            val statusCode = statusChangeData.status
            val roomId = statusChangeData.roomId
            when(statusCode){
                StatusCode.UNLOGIN -> { //断网重连，自动登录失败
                    val code = chatRoomService.getEnterErrorCode(roomId) //获取登录失败原因
                    Log.e( LOG_TAG, "NIMChatRoomMessageAPI reconnect chatroom ${roomId} failed: $code" )
                    chatRoomId = null //用于标记当前已不在聊天室中
                    onEnterFailed(code, object: RequestCallback<ChatRoomInfo>{
                        override fun onSuccess(result: ChatRoomInfo?) { }
                        override fun onFailure(failure: Failure) {
                            Log.e(LOG_TAG, "ChatRoom UNLOGIN: $failure")
                        }
                    })
                    handler(statusCode)
                }
                StatusCode.INVALID -> logOnlineStatueChange("未定义")
                StatusCode.NET_BROKEN -> logOnlineStatueChange("网络连接已断开")
                StatusCode.CONNECTING -> logOnlineStatueChange("正在连接服务器")
                StatusCode.LOGINING -> logOnlineStatueChange("正在登录中")
                StatusCode.SYNCING -> logOnlineStatueChange("正在同步数据")
                StatusCode.LOGINED -> logOnlineStatueChange("已成功登录")
                StatusCode.KICKOUT -> {
                    logOnlineStatueChange("被其它端的登录踢掉")
                    handler(statusCode)
                }
                StatusCode.KICK_BY_OTHER_CLIENT -> {
                    logOnlineStatueChange("被同时在线的其它端主动踢掉")
                    handler(statusCode)
                }
                StatusCode.FORBIDDEN -> logOnlineStatueChange("被服务器禁止登录")
                StatusCode.VER_ERROR -> logOnlineStatueChange("客户端版本错误")
                StatusCode.PWD_ERROR -> logOnlineStatueChange("用户名或密码错误")
            }
        }, true)
    }

    private fun logOnlineStatueChange(change: String){
        Log.d(LOG_TAG, "NIMChatRoomMessageAPI Online Status Change: ${change}")
    }

}

private fun RoomMessage.SeatQueueItem.toSeat(): Seat{
    val seat = Seat(
        seatIdx,
        seatState.toSeatState(),
        "",
        uid,
        userName,
        Avatar(userAvatar),
        micState == RoomMessage.ControlState.ENABLE,
        false
        )
    seat.userInfo.nobleLevel = NobleLevel(userNoble, "")
    seat.userInfo.vipLevel = VIPLevel(userVip, "")
    return seat
}

private fun RoomMessage.SeatQueueItem.SeatState.toSeatState(): SeatState = when(this){
    RoomMessage.SeatQueueItem.SeatState.UNK -> SeatState.DISABLED
    RoomMessage.SeatQueueItem.SeatState.LOCKED -> SeatState.DISABLED
    RoomMessage.SeatQueueItem.SeatState.OPEN -> SeatState.OPEN
    RoomMessage.SeatQueueItem.SeatState.OCCUPIED -> SeatState.OCCUPIED
    RoomMessage.SeatQueueItem.SeatState.UNRECOGNIZED -> SeatState.DISABLED
}

fun String.decodeFromBase64ToSeatQueueItem(): RoomMessage.SeatQueueItem? =
    try{
        val bytes = this.base64Decode()
        RoomMessage.SeatQueueItem.parseFrom(bytes)
    }catch(e: IOException){
        Log.e(LOG_TAG, "failed to decode $this as base64 string to SeatQueueItem")
        null
    }

fun String.decodeFromBase64toSeat(): Seat? = this.decodeFromBase64ToSeatQueueItem()?.toSeat()


package com.qint.pt1.features.chatrooms

import com.qint.pt1.base.exception.Failure

//TODO: maybe make a general failureLiveData class?
open class ChatRoomFailure(message: String) : Failure.FeatureFailure(message) {
    object ListNotAvailable : ChatRoomFailure("取不到聊天室列表")
    object RoomNotExist : ChatRoomFailure("聊天室不存在")
    object PasswordRequired : ChatRoomFailure("需要口令")
    object RoomInvaild : ChatRoomFailure("聊天室不可用")
    object RoomFull : ChatRoomFailure("聊天室已满")
    object RoomPasswordInvaild : ChatRoomFailure("口令不正确")
    object NoRoomPermission : ChatRoomFailure("无进入聊天室权限")
    object UserInRoomBlackList : ChatRoomFailure("黑名单用户禁止进入聊天室")
    object SendGiftFailed: ChatRoomFailure("发送礼物失败")

    data class VoiceEngineError(val code: Int) : ChatRoomFailure("语音引擎异常, 错误码: ${code}")
    object NoPermissionRecordAudio : ChatRoomFailure("请授予录音权限，否则无法上麦。")
    data class VoiceEngineInitialazationFailure(val exception: Exception) : ChatRoomFailure("语音引擎初始化失败: $exception")

    object SignalApiError : ChatRoomFailure("信令异常")
    object SignalLoginFailed : ChatRoomFailure("信令登录失败，请检查网络连接是否正常。如果使用了VPN请关闭VPN。")
    object SignalChannelJoinFailed : ChatRoomFailure("聊天室异常")

    open class SendMessageFailure(message: String) : ChatRoomFailure(message)
    object SendMessageFailed : SendMessageFailure("发送消息失败")
}
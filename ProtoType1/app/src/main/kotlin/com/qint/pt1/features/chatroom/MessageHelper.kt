package com.qint.pt1.features.chatroom

import android.util.Base64
import android.util.Base64InputStream
import android.util.Base64OutputStream
import android.util.Log
import com.google.protobuf.ByteString
import com.google.protobuf.GeneratedMessageV3
import com.qint.pt1.domain.*
import com.qint.pt1.domain.StyledMessage.Companion.buildPlainTextSystemMessage
import com.qint.pt1.util.LOG_TAG
import proto_def.Message
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException

//FIXME: 兼容其它客户端发送的纯文本格式的消息还有问题，显示无法解析。

//将一个MessagePackage编码为Base64 String
fun Message.MessagePackage.encodeToBase64String(): String {
    return try {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val base64OutputStream = Base64OutputStream(byteArrayOutputStream, Base64.NO_WRAP)
        this.writeTo(base64OutputStream)
        base64OutputStream.close()
        byteArrayOutputStream.toString()
    } catch (e: IOException) {
        e.printStackTrace()
        Log.e(LOG_TAG, "encode message package error")
        "encode message error: ${e.message}"
    }
}

//将Base64编码的String解码为MessagePackage
fun String.decodeFromBase64StringToMessagePackage(): Message.MessagePackage? {
    try {
        val byteArrayInputStream = ByteArrayInputStream(this.toByteArray())
        val base64InputStream = Base64InputStream(byteArrayInputStream, Base64.DEFAULT)
        val bytes = base64InputStream.readBytes()
        base64InputStream.close()
        return Message.MessagePackage.parseFrom(bytes)
    } catch (e: IOException) {
        e.printStackTrace()
        Log.e(LOG_TAG, "decode message package error: ${this}")
        return this.toMessagePackage()
    }
}

//TODO: 优化代码，改为配置方式，简化双向转换
fun Message.MessagePackage.toTargetMessage(): Any {
    try {
        return when (type) {
            Message.MessagePackage.MessageType.PLAIN_TEXT -> {
                Message.PlainTextMessage.parseFrom(data)
            }
            Message.MessagePackage.MessageType.SIMPLE_FORMATTED_TEXT -> {
                Message.SimpleFormattedText.parseFrom(data)
            }
            Message.MessagePackage.MessageType.ICON -> {
                Message.Icon.parseFrom(data)
            }
            Message.MessagePackage.MessageType.ICONED_SIMPLE_FORMATTED_TEXT -> {
                Message.IconedSimpleFormattedText.parseFrom(data)
            }
            Message.MessagePackage.MessageType.VISUAL_EFFECT -> {
                Message.VisualEffect.parseFrom(data)
            }
            Message.MessagePackage.MessageType.ROOM_SYSTEM_MESSAGE -> {
                Message.RoomSystemMessage.parseFrom(data)
            }
            Message.MessagePackage.MessageType.ROOM_USER_MESSAGE -> {
                Message.RoomUserMessage.parseFrom(data)
            }
            Message.MessagePackage.MessageType.ROOM_USER_ACTION_MESSAGE -> {
                Message.RoomUserActionMessage.parseFrom(data)
            }
            Message.MessagePackage.MessageType.ROOM_STATUS_CHANGE_REQ -> {
                Message.RoomStatusChangeReq.parseFrom(data)
            }
            Message.MessagePackage.MessageType.ROOM_STATUS_CHANGED_MESSAGE -> {
                Message.RoomStatusChangedMessage.parseFrom(data)
            }
            Message.MessagePackage.MessageType.UNRECOGNIZED -> { //FIXME: 改为定义专门的错误类型消息
                InvaildStyledMessage("无法识别的MessagePackage消息类型:{${String(data.toByteArray())}}")
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return InvaildStyledMessage("无法解析的MessagePackage消息: ${e.message}")
    }
}

fun toMessagePackage(message: Any): Message.MessagePackage? {
    return when (message) {
        is Message.PlainTextMessage -> message.toMessagePackage()
        is Message.SimpleFormattedText -> message.toMessagePackage()
        is Message.Icon -> message.toMessagePackage()
        is Message.IconedSimpleFormattedText -> message.toMessagePackage()
        is Message.VisualEffect -> message.toMessagePackage()
        is Message.RoomSystemMessage -> message.toMessagePackage()
        is Message.RoomUserMessage -> message.toMessagePackage()
        is Message.RoomUserActionMessage -> message.toMessagePackage()
        is Message.RoomStatusChangeReq -> message.toMessagePackage()
        is Message.RoomStatusChangedMessage -> message.toMessagePackage()
        is Message.MessagePackage -> message
        else -> null
    }
}

/* ---- convert protobuf object to domain object ---- */
fun Message.MessagePackage.toDomain(): com.qint.pt1.domain.StyledMessage {
    return when (val protoBufMessage = this.toTargetMessage()) {
        is Message.PlainTextMessage -> PlainTextStyledMessage(protoBufMessage.body)
        is Message.SimpleFormattedText -> com.qint.pt1.domain.StyledMessage.buildBaseMessage(protoBufMessage.toDomain())
        is Message.Icon -> com.qint.pt1.domain.StyledMessage.buildBaseMessage(protoBufMessage.toDomain())
        is Message.IconedSimpleFormattedText -> com.qint.pt1.domain.StyledMessage.buildBaseMessage(protoBufMessage.toDomain())
        is Message.VisualEffect -> com.qint.pt1.domain.StyledMessage.buildBaseMessage(protoBufMessage.toDomain())
        is Message.RoomSystemMessage -> RoomSystemStyledMessage(protoBufMessage.body.toDomain())
        is Message.RoomUserMessage -> RoomUserStyledMessage(protoBufMessage.userId, protoBufMessage.body.toDomain(), protoBufMessage.targetUserId)
        is Message.RoomUserActionMessage -> RoomUserStyledMessage(protoBufMessage.userId, protoBufMessage.body.toDomain(), protoBufMessage.targetUserId)
        is Message.RoomStatusChangeReq -> TODO()
        is Message.RoomStatusChangedMessage -> buildPlainTextSystemMessage(protoBufMessage.toString()) //TODO: implement this
        is InvaildStyledMessage -> protoBufMessage
        else -> InvaildStyledMessage("未知的消息: ${protoBufMessage}") //TODO: 抛出错误
    }
}

private fun Message.Icon.toDomain() = Icon(id, iconImg, code)
private fun Message.SimpleFormattedText.toDomain() = SimpleFormattedText(text, textColor, backgroundColor)
private fun Message.IconedSimpleFormattedText.toDomain() = IconedSimpleFormattedText(icon.toDomain(), sftText.toDomain())

private fun Message.VisualEffect.Type.toDomain() = when (this) {
    Message.VisualEffect.Type.TOP_SCROLL_RIGHT_TO_LEFT -> VisualEffectType.TOP_SCROLL_RIGHT_TO_LEFT
    Message.VisualEffect.Type.FULL_SCREEN_ANIMATION -> VisualEffectType.FULL_SCREEN_ANIMATION
    Message.VisualEffect.Type.UNRECOGNIZED -> VisualEffectType.UNRECOGNIZED
}

private fun Message.VisualEffect.toDomain() = VisualEffect(type.toDomain(), animationResource)

private fun Message.SimpleVisualEffectText.toDomain(): SimpleVisualEffectText {
    val elements: MutableList<IconedSimpleFormattedText> = mutableListOf()
    elements.addAll(elementsList.map { it.toDomain() })
    val visualEffects: MutableList<VisualEffect> = mutableListOf()
    visualEffects.addAll(visualEffectsList.map { it.toDomain() })
    return SimpleVisualEffectText(elements, visualEffects)
}

/* ---- convert domain object to protobuf object ---- */
fun com.qint.pt1.domain.StyledMessage.toProtoBuf(): Any {
    return when (this) {
        is PlainTextStyledMessage -> Message.PlainTextMessage.newBuilder().setBody(text).build()
        is BaseStyledMessage -> Message.RoomSystemMessage.newBuilder().setBody(mBody.toProtoBuf()).build()
        is RoomSystemStyledMessage -> Message.RoomSystemMessage.newBuilder().setBody(mBody.toProtoBuf()).build()
        is RoomUserStyledMessage -> {
            val builder = Message.RoomUserMessage.newBuilder().setUserId(userId).setBody(mBody.toProtoBuf())
            if (targetUserId != null) builder.targetUserId = targetUserId
            return builder.build()
        }
        else -> TODO() //上下麦等改用RoomSystemMessage通知？
    }
}

private fun Icon.toProtoBuf() = Message.Icon.newBuilder().setId(id).setCode(code).setIconImg(image).build()

private fun SimpleFormattedText.toProtoBuf(): Message.SimpleFormattedText {
    val builder = Message.SimpleFormattedText.newBuilder().setText(text)
    if (textColor != null) builder.textColor = textColor
    if (backgroundColor != null) builder.backgroundColor = backgroundColor
    return builder.build()
}

private fun IconedSimpleFormattedText.toProtoBuf(): Message.IconedSimpleFormattedText {
    val builder = Message.IconedSimpleFormattedText.newBuilder()
    if(icon != null) builder.icon = icon.toProtoBuf()
    if(sftText != null) builder.sftText = sftText.toProtoBuf()
    return builder.build()
}

private fun VisualEffectType.toProtoBuf() = when (this) {
    VisualEffectType.TOP_SCROLL_RIGHT_TO_LEFT -> Message.VisualEffect.Type.TOP_SCROLL_RIGHT_TO_LEFT
    VisualEffectType.FULL_SCREEN_ANIMATION -> Message.VisualEffect.Type.FULL_SCREEN_ANIMATION
    VisualEffectType.UNRECOGNIZED -> Message.VisualEffect.Type.UNRECOGNIZED
}

private fun VisualEffect.toProtoBuf() = Message.VisualEffect.newBuilder().setType(type.toProtoBuf()).setAnimationResource(animationResource).build()

private fun SimpleVisualEffectText.toProtoBuf(): Message.SimpleVisualEffectText {
    val builder = Message.SimpleVisualEffectText.newBuilder()
    elements.forEach { builder.addElements(it.toProtoBuf()) }
    visualEffects.forEach { builder.addVisualEffects(it.toProtoBuf()) }
    return builder.build()
}

/* ---- convert protobuf message to plain string ---- */
fun toPlainString(obj: Any): String {
    var message = obj
    if (obj is Message.MessagePackage) message = obj.toTargetMessage()
    return when (message) {
        is String -> message
        is Message.PlainTextMessage -> message.body
        is Message.RoomSystemMessage -> message.body.toPlainString()
        is Message.RoomUserMessage -> message.body.toPlainString()
        is Message.RoomUserActionMessage -> message.body.toPlainString()
        is Message.RoomStatusChangeReq -> "RoomStatusChangeReq: " + message.type.toString()
        is Message.RoomStatusChangedMessage -> "RoomStatusChanged: " + message.type.toString()
        else -> message.toString()
    }
}

fun Message.SimpleFormattedText.toPlainString(): String {
    return text
}

fun Message.IconedSimpleFormattedText.toPlainString(): String {
    var ret = ""
    if (icon.code.isNotEmpty()) ret = "[${icon.code}]"
    ret += sftText.toPlainString()
    return ret
}

fun Message.SimpleVisualEffectText.toPlainString(): String {
    return elementsList.joinToString("") { it.toPlainString() }
}

/* -- convert protobuf actual message object to MessagePackage object ---- */
fun String.toMessagePackage(): Message.MessagePackage {
    return Message.MessagePackage.newBuilder()
            .setType(Message.MessagePackage.MessageType.PLAIN_TEXT)
            .setData(ByteString.copyFrom(this.toByteArray()))
            .build()
}

private fun GeneratedMessageV3.toMessagePackage(type: Message.MessagePackage.MessageType): Message.MessagePackage {
    return Message.MessagePackage.newBuilder()
            .setType(type)
            .setData(this.toByteString())
            .build()
}

fun Message.PlainTextMessage.toMessagePackage(): Message.MessagePackage = toMessagePackage(Message.MessagePackage.MessageType.PLAIN_TEXT)

fun Message.SimpleFormattedText.toMessagePackage(): Message.MessagePackage = toMessagePackage(Message.MessagePackage.MessageType.SIMPLE_FORMATTED_TEXT)

fun Message.Icon.toMessagePackage(): Message.MessagePackage = toMessagePackage(Message.MessagePackage.MessageType.ICON)

fun Message.IconedSimpleFormattedText.toMessagePackage(): Message.MessagePackage = toMessagePackage(Message.MessagePackage.MessageType.ICONED_SIMPLE_FORMATTED_TEXT)

fun Message.VisualEffect.toMessagePackage(): Message.MessagePackage = toMessagePackage(Message.MessagePackage.MessageType.VISUAL_EFFECT)

fun Message.RoomSystemMessage.toMessagePackage(): Message.MessagePackage = toMessagePackage(Message.MessagePackage.MessageType.ROOM_SYSTEM_MESSAGE)

fun Message.RoomUserMessage.toMessagePackage(): Message.MessagePackage = toMessagePackage(Message.MessagePackage.MessageType.ROOM_USER_MESSAGE)

fun Message.RoomUserActionMessage.toMessagePackage(): Message.MessagePackage = toMessagePackage(Message.MessagePackage.MessageType.ROOM_USER_ACTION_MESSAGE)

fun Message.RoomStatusChangeReq.toMessagePackage(): Message.MessagePackage = toMessagePackage(Message.MessagePackage.MessageType.ROOM_STATUS_CHANGE_REQ)

fun Message.RoomStatusChangedMessage.toMessagePackage(): Message.MessagePackage = toMessagePackage(Message.MessagePackage.MessageType.ROOM_STATUS_CHANGED_MESSAGE)

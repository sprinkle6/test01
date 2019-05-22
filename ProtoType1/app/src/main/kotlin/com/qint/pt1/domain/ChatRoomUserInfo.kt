/*
 * Author: Matthew Zhang
 * Created on: 5/13/19 11:48 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.domain

import android.util.Base64
import android.util.Base64OutputStream
import android.util.Log
import com.qint.pt1.base.extension.base64Decode
import com.qint.pt1.base.extension.empty
import com.qint.pt1.util.LOG_TAG
import java.io.ByteArrayOutputStream
import java.io.IOException

data class ChatRoomUserInfo(
    val userId: UserId,
    val nickName: NickName,
    val avatar: Avatar,
    var vipLevel: VIPLevel,
    var nobleLevel: NobleLevel,
    val gender: Gender
) {
    constructor(userId: UserId, nickName: NickName): this(userId, nickName, Avatar.empty(), VIPLevel.EMPTY_LEVEL, NobleLevel.EMPTY_LEVEL, Gender.UNKNOWN)
    constructor(nickName: NickName): this(ID.empty(), nickName)

    companion object {
        val NullMessageUserInfo = ChatRoomUserInfo(
            UserId.empty(),
            NickName.empty(),
            Avatar.empty(),
            Level.EMPTY_LEVEL,
            Level.EMPTY_LEVEL,
            Gender.UNKNOWN
        )

        val AnonymousMessageUserInfo = ChatRoomUserInfo(
            UserId.empty(),
            "anonymoususer",
            Avatar.Anonymous,
            Level.EMPTY_LEVEL,
            Level.EMPTY_LEVEL,
            Gender.UNKNOWN
        )

        fun parseFromBase64Protobuf(encodedProtobufString: String): ChatRoomUserInfo? =
            try{
                val bytes = encodedProtobufString.base64Decode()
                val protoMessage = proto_def.PeerMessage.PeerUserInfo.parseFrom(bytes)
                ChatRoomUserInfo(
                    protoMessage.userId,
                    protoMessage.nickName,
                    Avatar(protoMessage.avatar),
                    protoMessage.vip.toDomainLevel(),
                    protoMessage.noble.toDomainLevel(),
                    Gender.fromString(protoMessage.gender)
                )
            }catch (e: IOException){
                e.printStackTrace()
                Log.e(LOG_TAG, "failed to decode base64 string $encodedProtobufString to ChatRoomUserInfo")
                null
            }
    }

    fun isNullOrAnonymous(): Boolean{
        return this == NullMessageUserInfo
                || this == AnonymousMessageUserInfo
                || nickName.isNullOrBlank()
    }

    private fun toProtoBuf(): proto_def.PeerMessage.PeerUserInfo =
        proto_def.PeerMessage.PeerUserInfo.newBuilder()
            .setUserId(userId)
            .setNickName(nickName)
            .setAvatar(avatar.url)
            .setVip(vipLevel.toProtobuf())
            .setNoble(nobleLevel.toProtobuf())
            .setGender(gender.toMessageString())
            .build()

    fun encode(): String = toProtoBuf().encode()
}

private fun proto_def.PeerMessage.Level.toDomainLevel() = Level(level, title)

private fun Level.toProtobuf() = proto_def.PeerMessage.Level.newBuilder()
    .setLevel(level).setTitle(description).build()

//将用户扩展信息的protobuf转为base64，考虑到反射的性能开销，不使用ProtoBufMessage.kt中定义的encode扩展方法
private fun proto_def.PeerMessage.PeerUserInfo.encode(): String {
    return try {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val base64OutputStream = Base64OutputStream(byteArrayOutputStream, Base64.NO_WRAP)
        this.writeTo(base64OutputStream)
        base64OutputStream.close()
        byteArrayOutputStream.toString()
    } catch (e: IOException) {
        e.printStackTrace()
        Log.e(LOG_TAG, "encode message failed: $e")
        ""
    }
}

fun User.toMessageUserInfo() = ChatRoomUserInfo(
    id,
    profile.nickName,
    profile.avatar,
    info.vipLevel,
    info.nobleLevel,
    profile.gender
)

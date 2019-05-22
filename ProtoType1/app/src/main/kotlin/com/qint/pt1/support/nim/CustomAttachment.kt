/*
 * Author: Matthew Zhang
 * Created on: 5/7/19 2:27 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.support.nim

import android.content.Context
import android.util.Base64
import android.util.Log
import com.alibaba.fastjson.JSON
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment
import com.netease.nimlib.sdk.msg.attachment.MsgAttachmentParser
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jwt.SignedJWT
import com.qint.pt1.base.extension.base64Decode
import com.qint.pt1.domain.*
import com.qint.pt1.util.LOG_TAG
import proto_def.RoomMessage
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.*

enum class CustomMessageType{
    SendGift, OpenBox
}

abstract sealed class CustomAttachment: MsgAttachment {
    override fun toJson(send: Boolean): String {
        return JSON.toJSONString(this)
    }
}

data class DonateAttachment(val from: ChatRoomUserInfo, val donates: List<Donate>): CustomAttachment()

class OpenBoxAttachment: CustomAttachment()

data class UnSupportedCustomAttachment(val originalJson: String): CustomAttachment()

data class ExpiredAttachment(val originalJson: String): CustomAttachment()

data class InvaildAttachment(val originalJson: String): CustomAttachment()

// 需要将解析器注册到SDK
// NIMClient.getService(MsgService.class).registerCustomAttachmentParser(new CustomAttachParser(context));
// 监听的注册，必须在主进程中。
class CustomAttachParser(val context: Context,
                         val publicKeyFilePath: String = "key/public.pem",
                         val ignoreExpiredMessage: Boolean = false): MsgAttachmentParser {

    private val keyBytes = context.assets.open(publicKeyFilePath).readBytes()
    private val publicKey = getPublicKey(keyBytes)
    private val decoder = getDecoder(publicKey)

    override fun parse(json: String?): MsgAttachment? {
        if(json.isNullOrBlank()) return null
        val attachmentWrapper= JSON.parseObject(json, AttachmentWrapper::class.java)

        try {
            val jwsString = attachmentWrapper.data
            val result = decoder.decode(jwsString, "probuf")

            if(ignoreExpiredMessage && result.state == JWTDecodeState.EXPIRED) return ExpiredAttachment(json)

            val protobufString = result.decoded
            return when(attachmentWrapper.type){
                RoomMessage.NotificationType.EVENT_VALUE -> {
                    val message = RoomMessage.RoomEventNotification.parseFrom(protobufString.base64Decode())
                    when(message.type) {
                        RoomMessage.RoomEventReq.EventType.SEND_GIFT -> {
                            val from = message.user.toChatRoomUserInfo()
                            val donates = message.giftsList.map {
                                Donate(
                                    it.giftId.toString(),
                                    it.toUid,
                                    it.toName,
                                    from.userId,
                                    from.nickName,
                                    it.count
                                )
                            }
                            DonateAttachment(from, donates)
                        }
                        RoomMessage.RoomEventReq.EventType.OPEN_BOX -> {
                            OpenBoxAttachment() //FIXME: implement this
                        }
                        else -> {
                            UnSupportedCustomAttachment(json)
                        }
                    }
                }
                RoomMessage.NotificationType.ROOM_CONTROL_VALUE -> {
                    UnSupportedCustomAttachment(json)
                }
                RoomMessage.NotificationType.SEAT_CONTROL_VALUE -> {
                    UnSupportedCustomAttachment(json)
                }
                RoomMessage.NotificationType.MEMBER_CONTROL_VALUE -> {
                    UnSupportedCustomAttachment(json)
                }
                else -> {
                    Log.e(LOG_TAG, "Unsupported Custom Message Type: ${attachmentWrapper.type}, attachment: ${json}")
                    UnSupportedCustomAttachment(json)
                }
            }
        }catch(e: Exception){
            return InvaildAttachment(json)
        }
    }

    private fun getDecoder(publicKey: RSAPublicKey) = JoseJWTDecoder(publicKey)
}

internal class AttachmentWrapper(){
    var type: Int = 0
    var from: String = ""
    var data: String = ""
}

internal fun RoomMessage.RoomEventNotification.User.toChatRoomUserInfo() =
    ChatRoomUserInfo(
        uid,
        name,
        Avatar(avatar),
        VIPLevel(vip, ""),
        NobleLevel(noble, ""),
        Gender.UNKNOWN
    )

internal abstract class JWTDecoder(val publicKey: RSAPublicKey){
    abstract fun decode(jwsString: String, claim: String): JWTDecodeResult
}

internal data class JWTDecodeResult(val decoded: String, val state: JWTDecodeState)

internal enum class JWTDecodeState{
    VERIFIED, VERIFIED_FAILED, EXPIRED
}

internal class JoseJWTDecoder(publicKey: RSAPublicKey): JWTDecoder(publicKey){
    override fun decode(jwsString: String, claim: String): JWTDecodeResult {
        val signedJWT = SignedJWT.parse(jwsString)
        val verifier = RSASSAVerifier(publicKey)
        val verified = signedJWT.verify(verifier)
        if(!verified) return JWTDecodeResult("", JWTDecodeState.VERIFIED_FAILED)

        val decoded = signedJWT.jwtClaimsSet.getClaim(claim) as String
        if(Date().after(signedJWT.jwtClaimsSet.expirationTime)) return JWTDecodeResult(decoded, JWTDecodeState.EXPIRED)
        return JWTDecodeResult(decoded, JWTDecodeState.VERIFIED)
    }
}

private fun getPublicKey(keyBytes: ByteArray): RSAPublicKey {
    val beginTag = "-----BEGIN PUBLIC KEY-----"
    val endTag = "-----END PUBLIC KEY-----"
    val returnTag = "\\n"
    val publicKeyContent = String(keyBytes)
        .replace(returnTag, "")
        .replace(beginTag, "")
        .replace(endTag, "")
    val kf = KeyFactory.getInstance("RSA")
    val keySpecX509 = X509EncodedKeySpec(Base64.decode(publicKeyContent, Base64.DEFAULT))
    return kf.generatePublic(keySpecX509) as RSAPublicKey
}
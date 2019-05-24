/*
 * Author: Matthew Zhang
 * Created on: 4/30/19 4:15 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.api.shop

import android.util.Log
import com.qint.pt1.api.sys.MetaData
import com.qint.pt1.base.exception.Failure
import com.qint.pt1.base.functional.Either
import com.qint.pt1.base.platform.APIHandler
import com.qint.pt1.base.platform.ResourceHandler
import com.qint.pt1.domain.*
import com.qint.pt1.util.LOG_TAG
import proto_def.ShopMessage
import proto_def.SysMessage
import javax.inject.Inject

class UmbrellaAPI
@Inject constructor(
    private val apiHandler: APIHandler,
    private val resourceHandler: ResourceHandler,
    private val service: UmbrellaServiceImpl
) {
    fun sendGift(roomId: ChatRoomId, account: Account, donate: Donate) =
        sendGift(roomId, account, listOf(donate))

    fun sendGift(roomId: ChatRoomId, account: Account, donates: List<Donate>): Either<Failure, Boolean> {
        val reqBuilder = ShopMessage.SendGiftReq.newBuilder()
            .setUid(account.userId)
            .setToken(account.getToken())
            .setRoomId(roomId)

        donates.forEach {
            reqBuilder.addGifts(
                ShopMessage.SendGiftReq.Gift.newBuilder()
                    .setGiftId(it.giftId.toInt()) //FIXME: if toInt failed abort
                    .setCount(it.count)
                    .setToUid(it.toUserId)
                    .setToName(it.toUserName)
            )
        }

        Log.d(
            LOG_TAG, """calling UmbrellaAPI.sendGift:
            |uid: ${account.userId}
            |token: ${account.getToken()}
            |roomId: $roomId
            |donates: $donates
        """.trimMargin())

        return apiHandler.request(
            service.sendGift(reqBuilder.build()),
            { resp -> resp.status == ShopMessage.SendGiftResp.STATUS.OK }
        )
    }

    fun getNobleList(): Either<Failure, List<NobleLevel>> =
        resourceHandler.request(
            service.getNobleList(),
            { resp -> resp.noblesList.map { it.toNobleLevel() } },
            MetaData.NOBLELEVELS_CACHE_KEY
        )

    fun getGiftList(): Either<Failure, List<Gift>>{
        val req = ShopMessage.ShopProductsReq.newBuilder()
            .setCategory(ShopMessage.CATEGORY.GIFT).build()
        return resourceHandler.request(
            service.getProductList(req),
            { resp -> resp.productsList.map { it.toGift() } },
            MetaData.CHATROOM_GIFTS_CACHE_KEY
        )
    }
}

private fun ShopMessage.Product.toGift(): Gift = Gift(pid.toString(), title, icon, price)

private fun SysMessage.NobleListResp.Noble.toNobleLevel(): NobleLevel = NobleLevel(nid, title)

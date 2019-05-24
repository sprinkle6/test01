/*
 * Author: Matthew Zhang
 * Created on: 5/22/19 10:17 AM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.api.user

import android.util.Log
import com.qint.pt1.base.exception.Failure
import com.qint.pt1.base.functional.Either
import com.qint.pt1.base.platform.APIHandler
import com.qint.pt1.domain.*
import com.qint.pt1.util.LOG_TAG
import proto_def.UserMessage
import javax.inject.Inject

class BeesAPI @Inject constructor(private val apiHandler: APIHandler,
                                  private val service: BeesServiceImpl){

    fun getUserBackpack(account: Account): Either<Failure, Backpack>{
        val req = UserMessage.BagReq.newBuilder()
            .setUid(account.userId)
            .setToken(account.getToken())
            .build()
        return apiHandler.request(
            service.getUserBackpack(req),
            {resp ->
                Log.d(LOG_TAG, "BeesAPI.getUserBackpack: ${resp}")
                resp.toBackpack()
            }
        )
    }
}

private fun UserMessage.BagResp.toBackpack(): Backpack{
    val backpack = Backpack()
    backpack.items.addAll(this.productsList.map { it.toBackpackItem() })
    return backpack
}

private fun UserMessage.BagResp.Product.toBackpackItem(): ProductPack = ProductPack(this.toProduct(), count, expire)

private fun UserMessage.BagResp.Product.toProduct(): Product {
    val period = if(permant) Period.FOREVER else Period(0)
    val price = 0
    val priority = 0
    return when(category){
        UserMessage.BagResp.CATEGORY.GIFT -> Product.Gift(pid, title, icon, price, period, priority, grade.toProductGrade())
        UserMessage.BagResp.CATEGORY.BOX -> Product.Box(pid, title, icon, price, period, priority, grade.toProductGrade())
        UserMessage.BagResp.CATEGORY.KEY -> Product.Key(pid, title, icon, price, period, priority, grade.toProductGrade())
        //FIXME: support coupon
        else -> Product.General(pid, title, icon, price, period, priority, grade.toProductGrade())
    }
}

private fun UserMessage.BagResp.PRODUCT_GRADE.toProductGrade(): ProductGrade = when(this){
    UserMessage.BagResp.PRODUCT_GRADE.NONE -> ProductGrade.NONE
    UserMessage.BagResp.PRODUCT_GRADE.BROZEN -> ProductGrade.BROZEN
    UserMessage.BagResp.PRODUCT_GRADE.SILVER -> ProductGrade.SILVER
    UserMessage.BagResp.PRODUCT_GRADE.GOLDEN -> ProductGrade.GOLDEN
    UserMessage.BagResp.PRODUCT_GRADE.UNRECOGNIZED -> ProductGrade.NONE
}

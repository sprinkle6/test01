/*
 * Author: Matthew Zhang
 * Created on: 5/22/19 10:17 AM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.api.user

import proto_def.UserMessage
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST
import javax.inject.Inject
import javax.inject.Singleton

interface BeesService {
    companion object{
        private const val module = "bees"
        private const val USER_BAG = "${module}/user_bag"
        private const val USER_OVERVIEW = "${module}/user_overview"
        private const val USER_FOLLOW = "${module}/user_follow"
        private const val ORDER_LIST = "${module}/order_list"
        private const val PLACE_ORDER = "${module}/place_order"
        private const val CHANGE_ORDER = "${module}/change_order"
    }

    @POST(USER_BAG)
    fun getUserBackpack(@Body req: UserMessage.BagReq): Call<UserMessage.BagResp>

    @POST(USER_FOLLOW)
    fun follow(@Body req: UserMessage.FollowReq): Call<UserMessage.FollowResp>

    @POST(USER_OVERVIEW)
    fun getUserOverview(@Body req: UserMessage.FollowReq): Call<UserMessage.FollowResp>

    @POST(ORDER_LIST)
    fun getOrderList(@Body req: UserMessage.OrderListReq): Call<UserMessage.OrderListResp>

    @POST(PLACE_ORDER)
    fun makeOrder(@Body req: UserMessage.MakeOrderReq): Call<UserMessage.MakeOrderResp>

    @POST(CHANGE_ORDER)
    fun updateOrder(@Body req: UserMessage.ChangeOrderStateReq): Call<UserMessage.ChangeOrderStateResp>
}

@Singleton
class BeesServiceImpl
@Inject constructor(retrofit: Retrofit): BeesService{
    private val api: BeesService by lazy { retrofit.create(BeesService::class.java) }

    override fun getUserBackpack(req: UserMessage.BagReq): Call<UserMessage.BagResp> = api.getUserBackpack(req)

    override fun follow(req: UserMessage.FollowReq): Call<UserMessage.FollowResp> = api.follow(req)

    override fun getUserOverview(req: UserMessage.FollowReq): Call<UserMessage.FollowResp> = api.getUserOverview(req)

    override fun getOrderList(req: UserMessage.OrderListReq): Call<UserMessage.OrderListResp> = api.getOrderList(req)

    override fun makeOrder(req: UserMessage.MakeOrderReq): Call<UserMessage.MakeOrderResp> = api.makeOrder(req)

    override fun updateOrder(req: UserMessage.ChangeOrderStateReq): Call<UserMessage.ChangeOrderStateResp> = api.updateOrder(req)

}
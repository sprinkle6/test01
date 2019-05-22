/*
 * Author: Matthew Zhang
 * Created on: 4/30/19 4:11 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.api.shop

import proto_def.ShopMessage
import proto_def.SysMessage
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import javax.inject.Inject
import javax.inject.Singleton

interface UmbrellaService{
    companion object{
        private const val module = "umbrella"
        private const val SEND_GIFT = "${module}/send_gift"
        private const val OPEN_BOX = "${module}/open_box"
        private const val MUSIC_SEARCH = "${module}/music_search"
        private const val PRODUCT_LIST = "${module}/product_list"
        private const val NOBLE_LIST = "${module}/noble_list"
    }

    @POST(SEND_GIFT)
    fun sendGift(@Body req: ShopMessage.SendGiftReq): Call<ShopMessage.SendGiftResp>

    @GET(NOBLE_LIST)
    fun getNobleList(): Call<SysMessage.NobleListResp>

    @POST(PRODUCT_LIST)
    fun getProductList(@Body req: ShopMessage.ShopProductsReq): Call<ShopMessage.ShopProductsResp>
    fun getAllProducts(): Call<ShopMessage.ShopProductsResp>
}

@Singleton
class UmbrellaServiceImpl
@Inject constructor(retrofit: Retrofit): UmbrellaService{
    private val api: UmbrellaService by lazy { retrofit.create(UmbrellaService::class.java) }

    override fun sendGift(req: ShopMessage.SendGiftReq) = api.sendGift(req)

    override fun getNobleList(): Call<SysMessage.NobleListResp> = api.getNobleList()

    override fun getProductList(req: ShopMessage.ShopProductsReq): Call<ShopMessage.ShopProductsResp> = api.getProductList(req)

    override fun getAllProducts(): Call<ShopMessage.ShopProductsResp> {
        val req = ShopMessage.ShopProductsReq.newBuilder()
            .setCategory(ShopMessage.CATEGORY.ALL).build()
        return api.getProductList(req)
    }
}
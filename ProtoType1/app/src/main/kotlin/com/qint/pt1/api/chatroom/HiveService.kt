/*
 * Author: Matthew Zhang
 * Created on: 4/28/19 5:12 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.api.chatroom

import proto_def.RoomMessage
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST
import javax.inject.Inject
import javax.inject.Singleton

interface HiveService{
    companion object{
        private const val module = "hive"
        private const val SEAT_MODIFY = "${module}/seat_modify"
        private const val YUNXIN_ADDR = "${module}/yunxin_addr"
        private const val SUBSCRIBE_ROOM = "${module}/room_follow"
    }

    @POST(SEAT_MODIFY)
    fun modifySeat(@Body req: RoomMessage.SeatControlReq): Call<RoomMessage.SeatControlResp>

    @POST(YUNXIN_ADDR)
    fun getNIMChatRoomServAddr(@Body req: RoomMessage.YunxinAddressReq): Call<RoomMessage.YunxinAddressResp>

    @POST(SUBSCRIBE_ROOM)
    fun subscribeRoom(@Body req: RoomMessage.RoomSubscribeReq): Call<RoomMessage.RoomSubscribeResp>
}

@Singleton
class HiveServiceImpl
@Inject constructor(retrofit: Retrofit): HiveService{
    private val api: HiveService by lazy { retrofit.create(HiveService::class.java) }

    override fun modifySeat(req: RoomMessage.SeatControlReq) = api.modifySeat(req)

    override fun getNIMChatRoomServAddr(req: RoomMessage.YunxinAddressReq) = api.getNIMChatRoomServAddr(req)

    override fun subscribeRoom(req: RoomMessage.RoomSubscribeReq) = api.subscribeRoom(req)
}
package com.qint.pt1.features.chatrooms

import proto_def.RoomMessage
import proto_def.ShopMessage
import proto_def.SysMessage
import retrofit2.Call
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRoomsService
@Inject constructor(retrofit: Retrofit) : ChatRoomsApi {
    private val chatRoomsApi: ChatRoomsApi by lazy { retrofit.create(ChatRoomsApi::class.java) }

    override fun chatRoomCategoryList(req: SysMessage.RoomCategoryListReq): Call<SysMessage.RoomCategoryListResp> =
        chatRoomsApi.chatRoomCategoryList(req)

    override fun chatRooms(req: RoomMessage.RoomListReq): Call<RoomMessage.RoomListResp> = chatRoomsApi.chatRooms(req)

    override fun chatRoomDetails(req: RoomMessage.RoomDetailReq): Call<RoomMessage.RoomDetailResp> = chatRoomsApi.chatRoomDetails(req)

    override fun stickers(): Call<ShopMessage.StickersResp> = chatRoomsApi.stickers()

    override fun gifts(): Call<ShopMessage.ShopGiftsResp> = chatRoomsApi.gifts()

}
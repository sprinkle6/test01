package com.qint.pt1.features.chatrooms

import proto_def.RoomMessage
import proto_def.ShopMessage
import proto_def.SysMessage
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

//TODO: 也许应将所有API和Service合并为一以便管理和使用
internal interface ChatRoomsApi {
    companion object {
        private const val moduleHive = "hive"
        private const val moduleDemo = "demo"
        private const val module = moduleDemo

        private const val CHATROOM_CATEGORY_LIST = "${module}/room_category_list"
        private const val CHATROOMS = "${module}/chatroom_list"
        private const val PARAM_CHATROOM_ID = "${module}/chatroom_id"
        private const val CHATROOM_DETAILS = "${module}/chatroom_detail"
        private const val STICKERS = "${module}/sticker_list"
        private const val GIFTS = "${module}/gift_list"
    }

    @POST(CHATROOM_CATEGORY_LIST)
    fun chatRoomCategoryList(@Body req: SysMessage.RoomCategoryListReq): Call<SysMessage.RoomCategoryListResp>

    @POST(CHATROOMS)
    fun chatRooms(@Body req: RoomMessage.RoomListReq): Call<RoomMessage.RoomListResp>

    @POST(CHATROOM_DETAILS)
    fun chatRoomDetails(@Body req: RoomMessage.RoomDetailReq): Call<RoomMessage.RoomDetailResp>

    @GET(STICKERS)
    fun stickers(): Call<ShopMessage.StickersResp>

    @GET(GIFTS)
    fun gifts(): Call<ShopMessage.ShopGiftsResp>

}

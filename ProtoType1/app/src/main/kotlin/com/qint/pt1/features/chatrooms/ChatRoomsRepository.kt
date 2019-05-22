package com.qint.pt1.features.chatrooms

import com.qint.pt1.api.sys.MetaData
import com.qint.pt1.base.exception.Failure
import com.qint.pt1.base.functional.Either
import com.qint.pt1.base.platform.APIHandler
import com.qint.pt1.base.platform.ResourceHandler
import com.qint.pt1.domain.*
import com.qint.pt1.features.chatroom.ChatRoomDetails
import com.qint.pt1.util.DEFAULT_ITEM_NUM_PER_PAGE
import proto_def.RoomMessage
import proto_def.ShopMessage
import proto_def.SysMessage
import javax.inject.Inject

interface ChatRoomsRepository {
    fun chatRoomCategories(account: Account = Account.NullAccount): Either<Failure, List<ChatRoomCategory>>
    fun chatRooms(categoryId: ID, page: Int = 0, numPerPage: Int = DEFAULT_ITEM_NUM_PER_PAGE): Either<Failure, List<ChatRoom>>
    fun chatRoomDetails(chatRoomId: ChatRoomId, account: Account = Account.NullAccount, password: String=""): Either<Failure, ChatRoomDetails>
    fun stickers(): Either<Failure, List<Sticker>>
    fun gifts(): Either<Failure, List<Gift>>

    class Network
    @Inject constructor(
        private val resourceHandler: ResourceHandler,
        private val apiHandler: APIHandler,
        private val service: ChatRoomsService) : ChatRoomsRepository {

        override fun chatRoomCategories(account: Account): Either<Failure, List<ChatRoomCategory>> {
            val req = SysMessage.RoomCategoryListReq.newBuilder()
                .setUid(account.userId)
                .setToken(account.getToken())
                .build()
            return resourceHandler.request(
                service.chatRoomCategoryList(req),
                {resp -> resp.categoriesList.map { ChatRoomCategory(it.cateId.toString(), it.title) }},
                MetaData.CHATROOM_CATEGORIES_CACHE_KEY
            )
        }

        override fun chatRooms(categoryId: ID, page: Int, numPerPage: Int): Either<Failure, List<ChatRoom>> {
            val req = RoomMessage.RoomListReq.newBuilder()
                .setCategory(categoryId)
                .setPageNumber(page)
                .setNumPerPage(numPerPage)
                .build()
            return apiHandler.request(
                service.chatRooms(req),
                { it.roomsList.map { it.toChatRoom() } }
            )
        }

        override fun chatRoomDetails(chatRoomId: ChatRoomId, account: Account, password: String): Either<Failure, ChatRoomDetails> {
            val req = RoomMessage.RoomDetailReq.newBuilder()
                .setChatroomId(chatRoomId)
                .setUid(account.userId)
                .setPassword(password)
                .build()
            val result = apiHandler.request(service.chatRoomDetails(req))
            return result.either(
                {failure -> Either.Left(failure) },
                {resp -> when(resp.code){
                    RoomMessage.RoomDetailResp.STATUS.OK -> {
                        Either.Right(resp.toChatRoomDetails())
                    }
                    RoomMessage.RoomDetailResp.STATUS.PASSWORD_INVALID ->
                        Either.Left(ChatRoomFailure.RoomPasswordInvaild)
                    RoomMessage.RoomDetailResp.STATUS.ROOM_IS_FULL ->
                        Either.Left(ChatRoomFailure.RoomFull)
                    else ->
                        Either.Left(ChatRoomFailure.RoomInvaild)
                }}
            ) as Either<Failure, ChatRoomDetails>
        }

        override fun stickers(): Either<Failure, List<Sticker>> = resourceHandler.request(
            service.stickers(),
            { it.stickersList.map { it.toSticker() } },
            MetaData.CHATROOM_STICKERS_CACHE_KEY
        )

        override fun gifts(): Either<Failure, List<Gift>> = resourceHandler.request(
            service.gifts(),
            { it.giftsList.map { it.toGift() } },
            MetaData.CHATROOM_GIFTS_CACHE_KEY
        )


    }
}

fun RoomMessage.Room.toChatRoom(): ChatRoom {
    val chatRoom = ChatRoom(id, title, category, layout.toRoomLayoutType())
    chatRoom.creator = User(ownerId, ownerName, Avatar(ownerAvatar))
    chatRoom.memberNum = memberNum
    chatRoom.backgroundPicture = roomImg
    chatRoom.needPassword = needPassword
    chatRoom.externalRoomId = externRoomId
    return chatRoom
}

fun RoomMessage.RoomDetailResp.toChatRoomDetails(): ChatRoomDetails { //这里的seatsList信息不再使用了
    return ChatRoomDetails(room.toChatRoom(), roomFollowed,
            seatsList.map { Seat(it.idx, it.state.toSeatState(), it.description, it.userId, it.userName, Avatar(it.userAvatar)) })
}

fun RoomMessage.Room.LAYOUT.toRoomLayoutType() = when (this) {
    RoomMessage.Room.LAYOUT.SEATS_4 -> RoomLayoutType.SEAT_4
    RoomMessage.Room.LAYOUT.SEATS_8 -> RoomLayoutType.SEAT_8
    else -> RoomLayoutType.SEAT_8
}

fun RoomMessage.RoomDetailResp.SEAT_STATE.toSeatState() = when (this) {
    RoomMessage.RoomDetailResp.SEAT_STATE.DISABLED -> SeatState.DISABLED
    RoomMessage.RoomDetailResp.SEAT_STATE.OPEN -> SeatState.OPEN
    RoomMessage.RoomDetailResp.SEAT_STATE.OCCUPIED -> SeatState.OCCUPIED
    else -> SeatState.DISABLED
}

fun ShopMessage.Sticker.toSticker(): Sticker {
    return Sticker(id, title, icon)
}

fun ShopMessage.Gift.toGift(): Gift {
    return Gift(id, title, icon, price)
}
package com.qint.pt1.domain

import android.os.Parcelable
import com.qint.pt1.base.extension.empty
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

typealias ChatRoomId = String
typealias ChatRoomTitle = String

@Parcelize
data class ChatRoomCategory(val id: ID, val title: Title): Parcelable{
    companion object{
        val CATEGORY_HOT = ChatRoomCategory("", "热门") //默认展示分类
        val DefaultCategory = CATEGORY_HOT
    }
}

//TODO: domain entities should not be depend on UI requirements such as Parcelable
@Parcelize
data class ChatRoom(
    val id: ChatRoomId,
    var title: ChatRoomTitle,
    val category: Tag,
    val layoutType: RoomLayoutType
) : Parcelable {
    @IgnoredOnParcel var creator: User = User.NullUser
    @IgnoredOnParcel val owner = creator.profile.nickName
    @IgnoredOnParcel val ownerAvatar = creator.profile.avatar
    @IgnoredOnParcel var backgroundPicture: ImageUrl = ImageUrl.empty()
    @IgnoredOnParcel var memberNum: Int = 0
    @IgnoredOnParcel var needPassword: Boolean = false
    @IgnoredOnParcel var externalRoomId: ID = ID.empty()
    @IgnoredOnParcel
    val seats = arrayOfNulls<Seat>(layoutType.guestSeatNum() + 1)
    @IgnoredOnParcel val guestSeatNum = layoutType.guestSeatNum()

    init {
        for (idx in seats.indices) {
            seats[idx] =
                Seat(idx, SeatState.OPEN, "", UserId.empty(), UserName.empty(), Avatar.Empty)
        }
    }
}

enum class RoomLayoutType {
    SEAT_4 {
        override fun guestSeatNum() = 4
    },
    SEAT_8 {
        override fun guestSeatNum() = 8
    };

    abstract fun guestSeatNum(): Int

    companion object {
        fun default() = SEAT_8
    }
}

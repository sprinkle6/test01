package com.qint.pt1.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Seat(val idx: Int,
                var state: SeatState,
                var description: String,
                val _userId: UserId,
                var _userName: UserName,
                var _userAvatar: Avatar,
                var micEnabled: Boolean = false,
                var micMuted: Boolean = true
) : Parcelable{ //TODO: 味道不好，需要考虑重构
    val userInfo: ChatRoomUserInfo = ChatRoomUserInfo(
        _userId,
        _userName,
        _userAvatar,
        VIPLevel.EMPTY_LEVEL,
        NobleLevel.EMPTY_LEVEL,
        Gender.UNKNOWN
    )
    val userId get() = _userId
    val userName get() = _userName
    val userAvatar get() = _userAvatar
    val isHost get() = idx == 0 //是否主持人座位
    val seatName get() = if(isHost) "主播麦" else "${idx}号麦"

    companion object{
        const val SEAT0_KEY = "SEAT0"
        const val SEAT1_KEY = "SEAT1"
        const val SEAT2_KEY = "SEAT2"
        const val SEAT3_KEY = "SEAT3"
        const val SEAT4_KEY = "SEAT4"
        const val SEAT5_KEY = "SEAT5"
        const val SEAT6_KEY = "SEAT6"
        const val SEAT7_KEY = "SEAT7"
        const val SEAT8_KEY = "SEAT8"

        const val WAIT_QUEUE_KEY = "WAIT_Q"

        val SEAT_KEYS = setOf(SEAT0_KEY, SEAT1_KEY, SEAT2_KEY, SEAT3_KEY, SEAT4_KEY, SEAT5_KEY, SEAT6_KEY, SEAT7_KEY, SEAT8_KEY)
    }
}

enum class SeatState {
    UNKNOWN, DISABLED, OPEN, OCCUPIED
}
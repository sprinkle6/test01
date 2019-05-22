package com.qint.pt1.features.chatroom

import android.os.Parcelable
import com.qint.pt1.domain.ChatRoom
import com.qint.pt1.domain.Seat
import kotlinx.android.parcel.Parcelize

@Parcelize
class ChatRoomDetails(val room: ChatRoom, val isFollowed: Boolean, var seats: List<Seat>) : Parcelable
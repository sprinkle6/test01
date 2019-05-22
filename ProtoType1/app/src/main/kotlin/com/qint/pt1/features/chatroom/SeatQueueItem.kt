/*
 * Author: Matthew Zhang
 * Created on: 4/30/19 12:59 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.features.chatroom

import android.util.Log
import com.qint.pt1.base.extension.base64Decode
import com.qint.pt1.domain.*
import com.qint.pt1.util.LOG_TAG
import proto_def.RoomMessage
import java.io.IOException

data class SeatQueueItem(val userInfo: ChatRoomUserInfo,
                         val seatIdx: Int,
                         val timestamp: Int){
    companion object{
        fun parseQueueFromBase64Protobuf(base64EncodedString: String): List<SeatQueueItem> =
            try {
                val bytes = base64EncodedString.base64Decode()
                val wq = RoomMessage.WaitQueue.parseFrom(bytes)
                val waitQueue = mutableListOf<SeatQueueItem>()
                wq.waitUsersMap.forEach{
                    waitQueue.add(it.value.toSeatQueueItem())
                }
                waitQueue.sortBy { it.timestamp }
                waitQueue
            }catch(e: IOException){
                Log.e(LOG_TAG, "failed to decode $this as base64 string to WaitQueue")
                emptyList()
            }
    }

}

private fun RoomMessage.WaitQueue.User.toSeatQueueItem(): SeatQueueItem =
    SeatQueueItem(
        ChatRoomUserInfo(
            uid, name, Avatar(avatar),
            VIPLevel(vip, ""),
            NobleLevel(noble, ""),
            Gender.fromString(gender)
        ),
        seatIdx, timestamp
    )
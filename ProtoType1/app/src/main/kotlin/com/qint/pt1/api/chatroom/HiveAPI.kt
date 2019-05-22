/*
 * Author: Matthew Zhang
 * Created on: 4/28/19 5:09 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.api.chatroom

import android.util.Log
import com.qint.pt1.base.exception.Failure
import com.qint.pt1.base.functional.Either
import com.qint.pt1.base.platform.APIHandler
import com.qint.pt1.domain.Account
import com.qint.pt1.domain.ChatRoomId
import com.qint.pt1.domain.UserId
import com.qint.pt1.util.LOG_TAG
import proto_def.RoomMessage
import javax.inject.Inject

class HiveAPI
@Inject constructor(
    private val apiHandler: APIHandler,
    private val service: HiveServiceImpl ) {

    //结果反映在队列变更通知的WAIT_Q key上，结果为SeatQueueItem结构
    fun applySeat(roomId: ChatRoomId, targetUid: UserId, seatIdx: Int, account: Account): Either<Failure, Boolean> =
        seatControl(RoomMessage.SeatControlReq.ControlType.APPLY, roomId, targetUid, seatIdx, account)

    //结果反映在队列变更通知的WAIT_Q key上
    fun cancelApply(roomId: ChatRoomId, targetUid: UserId, seatIdx: Int, account: Account): Either<Failure, Boolean> =
        seatControl(RoomMessage.SeatControlReq.ControlType.CANCEL, roomId, targetUid, seatIdx, account)

    //以下结果都反映在队列变更通知的SEATnkey上，n从0~8，结果为WaitQueue结构
    fun takenSeat(roomId: ChatRoomId, targetUid: UserId, seatIdx: Int, account: Account): Either<Failure, Boolean> =
        seatControl(RoomMessage.SeatControlReq.ControlType.TAKEN, roomId, targetUid, seatIdx, account)

    fun releaseSeat(roomId: ChatRoomId, targetUid: UserId, seatIdx: Int, account: Account): Either<Failure, Boolean> =
        seatControl(RoomMessage.SeatControlReq.ControlType.RELEASE, roomId, targetUid, seatIdx, account)

    fun lockSeat(roomId: ChatRoomId, targetUid: UserId, seatIdx: Int, account: Account): Either<Failure, Boolean> =
        seatControl(RoomMessage.SeatControlReq.ControlType.LOCK, roomId, targetUid, seatIdx, account)

    fun unlockSeat(roomId: ChatRoomId, targetUid: UserId, seatIdx: Int, account: Account): Either<Failure, Boolean> =
        seatControl(RoomMessage.SeatControlReq.ControlType.UNLOCK, roomId, targetUid, seatIdx, account)

    fun openMic(roomId: ChatRoomId, targetUid: UserId, seatIdx: Int, account: Account): Either<Failure, Boolean> =
        seatControl(RoomMessage.SeatControlReq.ControlType.OPEN_MIC, roomId, targetUid, seatIdx, account)

    fun closeMic(roomId: ChatRoomId, targetUid: UserId, seatIdx: Int, account: Account): Either<Failure, Boolean> =
        seatControl(RoomMessage.SeatControlReq.ControlType.CLOSE_MIC, roomId, targetUid, seatIdx, account)

    private fun seatControl(controlType: RoomMessage.SeatControlReq.ControlType,
                            roomId: ChatRoomId,
                            targetUid: UserId,
                            seatIdx: Int,
                            account: Account): Either<Failure, Boolean>{
        val req = RoomMessage.SeatControlReq.newBuilder()
            .setType(controlType)
            .setUid(account.userId)
            .setToken(account.getToken())
            .setRoomId(roomId)
            .setTgtUid(targetUid)
            .setSeatIdx(seatIdx)
            .build()
        Log.d(LOG_TAG, """calling HiveAPI.seatControl:
            |controlType: ${controlType}
            |uid: ${account.userId}
            |token: ${account.getToken()}
            |roomId: $roomId
            |TgtUid: $targetUid
            |seatIdx: $seatIdx
        """.trimMargin())
        return apiHandler.request(
            service.modifySeat(req),
            { resp -> resp.status == RoomMessage.SeatControlResp.STATUS.OK }
        )
    }

    fun getNIMChatRoomServAddr(roomId: ChatRoomId): Either<Failure, List<String>>{
        val req = RoomMessage.YunxinAddressReq.newBuilder()
            .setRoomId(roomId)
            .build()
        return apiHandler.request(
            service.getNIMChatRoomServAddr(req),
            {resp -> resp.addrListList}
        )
    }

    fun subscribeRoom(roomId: ChatRoomId, follow: Boolean, account: Account): Either<Failure, Boolean>{
        val req = RoomMessage.RoomSubscribeReq.newBuilder()
            .setRoomId(roomId)
            .setFollowed(follow)
            .setUid(account.userId)
            .setToken(account.getToken())
            .build()
        return apiHandler.request(
            service.subscribeRoom(req),
            {resp -> resp.followed}
        )
    }
}


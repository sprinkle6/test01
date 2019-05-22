/*
 * Author: Matthew Zhang
 * Created on: 4/28/19 10:34 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.features.chatroom

import com.qint.pt1.api.chatroom.HiveAPI
import com.qint.pt1.base.exception.Failure
import com.qint.pt1.base.functional.Either
import com.qint.pt1.base.interactor.UseCase
import javax.inject.Inject

class GetNIMChatRoomServAddrUseCase
@Inject constructor(private val api: HiveAPI) : UseCase<List<String>, String>() {
    override suspend fun run(roomId: String): Either<Failure, List<String>> = api.getNIMChatRoomServAddr(roomId)
}
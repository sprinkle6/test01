package com.qint.pt1.features.chatrooms

import com.qint.pt1.base.interactor.UseCase
import com.qint.pt1.domain.ChatRoom
import com.qint.pt1.domain.ID
import javax.inject.Inject

class GetChatRoomsUseCase
@Inject constructor(private val chatRoomsRepository: ChatRoomsRepository) : UseCase<List<ChatRoom>, ID>() {
    override suspend fun run(categoryId: ID) = chatRoomsRepository.chatRooms(categoryId)
}
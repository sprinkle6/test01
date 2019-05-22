/*
 * Author: Matthew Zhang
 * Created on: 4/19/19 3:42 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.features.chatrooms

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.qint.pt1.base.interactor.UseCase
import com.qint.pt1.base.platform.BaseViewModel
import com.qint.pt1.domain.ChatRoomCategory
import javax.inject.Inject

class ChatRoomsViewModel
@Inject constructor(private val chatRoomsRepository: ChatRoomsRepository) : BaseViewModel() {
    private val _chatRoomCategoriesLiveData: MutableLiveData<List<ChatRoomCategory>> = MutableLiveData()
    val chatRoomCategoriesLiveData: LiveData<List<ChatRoomCategory>> = _chatRoomCategoriesLiveData

    init{
        _chatRoomCategoriesLiveData.value = mutableListOf(ChatRoomCategory.CATEGORY_HOT)
    }

    fun loadChatRoomCategories(){
        val usecase = GetChatRoomCategoriesUseCase(chatRoomsRepository)
        usecase{it.either(::trigerFailure, ::handleChatRoomCategories)}
    }

    private fun handleChatRoomCategories(categories: List<ChatRoomCategory>) {
        val chatRoomCategories = mutableListOf<ChatRoomCategory>()
        chatRoomCategories.addAll(categories)
        _chatRoomCategoriesLiveData.value = chatRoomCategories
    }
}

class GetChatRoomCategoriesUseCase
@Inject constructor(private val chatRoomsRepository: ChatRoomsRepository) : UseCase<List<ChatRoomCategory>, UseCase.None>() {
    override suspend fun run(param: None) = chatRoomsRepository.chatRoomCategories()
}
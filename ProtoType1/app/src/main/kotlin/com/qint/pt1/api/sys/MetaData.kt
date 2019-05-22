/*
 * Author: Matthew Zhang
 * Created on: 5/13/19 9:20 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.api.sys

import com.qint.pt1.api.chatroom.HiveAPI
import com.qint.pt1.api.shop.UmbrellaAPI
import com.qint.pt1.base.exception.Failure
import com.qint.pt1.base.functional.Either
import com.qint.pt1.base.interactor.UseCase
import com.qint.pt1.domain.*
import com.qint.pt1.features.chatrooms.ChatRoomsRepository
import com.qint.pt1.features.users.UsersRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetaData @Inject constructor(
    private val umbrellaAPI: UmbrellaAPI,
    private val hiveAPI: HiveAPI,
    private val usersRepository: UsersRepository,
    private val chatRoomsRepository: ChatRoomsRepository
) {
    companion object {
        const val NOBLELEVELS_CACHE_KEY = "MetaData.NobleLevels"
        const val CHATROOM_CATEGORIES_CACHE_KEY = "MetaData.ChatRoomCategories"
        const val CHATROOM_STICKERS_CACHE_KEY = "OperationData.ChatRoomStickers"
        const val CHATROOM_GIFTS_CACHE_KEY = "OperationData.ChatRoomGifts"
        const val SKILLROADS_CACHE_KEY = "MetaData.SkillRoads"
    }

    init {
        //FIXME: 目前尚不能保证init后所有数据都能及时成功加载，对于加载失败的情况，仍然需要一个补救机制
        init()
    }

    fun init() {
        initNobleLevels()
        initStickers()
        initGifts()
        initChatRoomCategories()
        initSkillRoads()
    }

    private var _nobleLevels: List<NobleLevel> = emptyList()
    val nobleLevels: List<NobleLevel>
        get() = _nobleLevels

    private fun initNobleLevels() {
        val nobleCase = GetNobleLevelsUseCase(umbrellaAPI)
        nobleCase() { result ->
            result.doIfRight {
                _nobleLevels = it
            }
        }
    }

    fun getNobleLevel(levelValue: Int): NobleLevel =
        nobleLevels.find { it.level == levelValue } ?: NobleLevel(levelValue, "")


    private var _chatroomCategories: List<ChatRoomCategory> = emptyList()
    val chatroomCategories: List<ChatRoomCategory>
        get() = _chatroomCategories

    private fun initChatRoomCategories(){
        val chatRoomCategoryCase = GetChatRoomCategoriesUseCase(chatRoomsRepository)
        chatRoomCategoryCase() {result ->
            result.doIfRight {
                _chatroomCategories = it
            }
        }
    }

    private var _stickers: List<Sticker> = emptyList()
    private var _stickerMap: MutableMap<Tag, Sticker> = mutableMapOf()
    val stickers: List<Sticker>
        get() = _stickers

    private fun initStickers() {
        val stickerCase = GetStickersUseCase(chatRoomsRepository)
        stickerCase() { result ->
            result.doIfRight {
                _stickers = it
                _stickers.forEach { _stickerMap[it.tag] = it }
            }
        }
    }

    fun getSticker(tag: Tag): Sticker? = _stickerMap.get(tag)

    private var _gifts: List<Gift> = emptyList()
    private var _giftMap: MutableMap<ID, Gift> = mutableMapOf()
    val gifts: List<Gift>
        get() = _gifts

    private fun initGifts() {
        val giftCase = GetGiftsUseCase(umbrellaAPI)
        giftCase() { result ->
            result.doIfRight {
                _gifts = it
                _gifts.forEach { _giftMap[it.id] = it }
            }
        }
    }

    fun getGift(id: ID): Gift? = _giftMap.get(id)

    private var _skillRoads: List<SkillRoad> = emptyList()
    private var _skillRoadMap: MutableMap<SkillId, SkillRoad> = mutableMapOf()
    val skillRoads: List<SkillRoad>
        get() = _skillRoads

    private fun initSkillRoads(){
        val skillRoadsCase = GetSkillRoadsUseCase(usersRepository)
        skillRoadsCase() {result ->
            result.doIfRight {
                _skillRoads = it
                _skillRoads.forEach { _skillRoadMap[it.skill.id] = it }
            }
        }
    }

    val donateCountCandidates = listOf<Int>(1, 10, 60, 99, 520, 666, 999) //FIXME: 使用红后返回的列表

}

class GetNobleLevelsUseCase @Inject constructor(private val umbrellaAPI: UmbrellaAPI) :
    UseCase<List<NobleLevel>, UseCase.None>() {
    override suspend fun run(params: None): Either<Failure, List<NobleLevel>> =
        umbrellaAPI.getNobleList()
}

class GetStickersUseCase @Inject constructor(private val chatRoomsRepository: ChatRoomsRepository) :
    UseCase<List<Sticker>, UseCase.None>() {
    override suspend fun run(params: None): Either<Failure, List<Sticker>> =
        chatRoomsRepository.stickers()
}

class GetGiftsUseCase @Inject constructor(private val umbrellaAPI: UmbrellaAPI) :
    UseCase<List<Gift>, UseCase.None>() {
    override suspend fun run(params: None): Either<Failure, List<Gift>> =
        umbrellaAPI.getGiftList()
}

class GetChatRoomCategoriesUseCase @Inject constructor(private val chatRoomsRepository: ChatRoomsRepository) :
    UseCase<List<ChatRoomCategory>, Account>() {
    override suspend fun run(account: Account): Either<Failure, List<ChatRoomCategory>> =
        chatRoomsRepository.chatRoomCategories(account)
}

class GetSkillRoadsUseCase @Inject constructor(private val usersRepository: UsersRepository) :
    UseCase<List<SkillRoad>, UseCase.None>() {
    override suspend fun run(params: None): Either<Failure, List<SkillRoad>> =
        usersRepository.skillRoads()
}
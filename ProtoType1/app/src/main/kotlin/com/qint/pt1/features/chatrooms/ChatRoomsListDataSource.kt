/*
 * Author: Matthew Zhang
 * Created on: 4/22/19 10:25 AM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.features.chatrooms

import android.util.Log
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import com.qint.pt1.base.exception.Failure
import com.qint.pt1.base.exception.FailureHandler
import com.qint.pt1.base.extension.empty
import com.qint.pt1.domain.ID
import com.qint.pt1.util.LOG_TAG
import javax.inject.Inject

/*
 * 分页列表所需的DataSource
 * TODO: 抽象DataSource以便复用
 */
class ChatRoomsListDataSource
@Inject constructor(private val categoryId: ID,
                    private val repository: ChatRoomsRepository,
                    private val failureHandler: FailureHandler?) :
    PageKeyedDataSource<Int, ChatRoomsListViewItem>() {

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, ChatRoomsListViewItem>
    ) {
        repository.chatRooms(categoryId, 0, params.requestedLoadSize).either(
            { failure ->
                Log.e(LOG_TAG, "ChatRoomsListDataSource load initial failed: ${failure}")
                handleFailure(failure)
            },
            { chatRooms ->
                Log.d(LOG_TAG, "ChatRoomsListDataSource load initial get ${chatRooms.size} chatRooms with param ${params.requestedLoadSize}")
                callback.onResult(chatRooms.map { it.toChatRoomsListViewItem() }, null, 1)
            }
        )
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, ChatRoomsListViewItem>) {
        repository.chatRooms(categoryId, params.key, params.requestedLoadSize).either(
            { failure ->
                Log.e(LOG_TAG, "ChatRoomsListDataSource load after failed: ${failure}. ${params}")
                handleFailure(failure)
            },
            {chatRooms ->
                Log.d(LOG_TAG, "ChatRoomsListDataSource load after get ${chatRooms.size} chatRooms with param (${params.key}, ${params.requestedLoadSize})")
                callback.onResult(chatRooms.map { it.toChatRoomsListViewItem() }, params.key + 1)
            }
        )
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, ChatRoomsListViewItem>) {}

    private fun handleFailure(failure: Failure) {
        failureHandler?.handleFailure(failure)
    }
}

class ChatRoomsListDataSourceFactory
@Inject constructor(private val repository: ChatRoomsRepository) :
    DataSource.Factory<Int, ChatRoomsListViewItem>() {

    var categoryId: ID = ID.empty()
    var failureHandler: FailureHandler? = null

    override fun create(): DataSource<Int, ChatRoomsListViewItem> =
        ChatRoomsListDataSource(categoryId, repository, failureHandler)
}
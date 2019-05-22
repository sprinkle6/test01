/*
 * Author: Matthew Zhang
 * Created on: 4/16/19 3:38 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.features.search

import android.util.Log
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import com.qint.pt1.base.exception.Failure
import com.qint.pt1.base.exception.FailureHandler
import com.qint.pt1.domain.User
import com.qint.pt1.features.users.UsersRepository
import com.qint.pt1.util.LOG_TAG
import javax.inject.Inject

//分页列表所需的DataSource
class SearchResultDataSource
@Inject constructor(private val usersRepository: UsersRepository,
                    private val keyword: Keyword,
                    private val failureHandler: FailureHandler?) :
    PageKeyedDataSource<Int, SearchResultUserItem>() {

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, SearchResultUserItem>
    ) {
        usersRepository.search(keyword, 0, params.requestedLoadSize).either(
            { failure ->
                Log.e(LOG_TAG, "SearchResultDataSource load initial failed: ${failure}")
                handleFailure(failure)
            },
            { users ->
                Log.d(LOG_TAG, "SearchResultDataSource load initial get ${users.size} users with param ${params.requestedLoadSize}")
                callback.onResult(users.map { it.toSearchResultUserItem() }, null, 1)
            }
        )
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, SearchResultUserItem>) {
        usersRepository.search(keyword, params.key, params.requestedLoadSize).either(
            { failure ->
                Log.e(LOG_TAG, "SearchResultDataSource load after failed: ${failure}. ${params}")
                handleFailure(failure)
            },
            { users ->
                Log.d(LOG_TAG, "SearchResultDataSource load after get ${users.size} users with param (${params.key}, ${params.requestedLoadSize})")
                callback.onResult(users.map { it.toSearchResultUserItem() }, params.key + 1)
            }
        )
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, SearchResultUserItem>) {}

    private fun handleFailure(failure: Failure) {
        failureHandler?.handleFailure(failure)
    }
}

class SearchResultDataSourceFactory
@Inject constructor(private val usersRepository: UsersRepository) :
    DataSource.Factory<Int, SearchResultUserItem>() {

    var keyword: Keyword = ""
    var failureHandler: FailureHandler? = null

    override fun create(): DataSource<Int, SearchResultUserItem> =
        SearchResultDataSource(usersRepository, keyword, failureHandler)
}

private fun User.toSearchResultUserItem() = SearchResultUserItem.fromUser(this)

/*
 * Copyright (c) 2019. QINT.TV
 * Author: Matthew Zhang
 * Created on: 3/22/19 2:15 PM
 */

package com.qint.pt1.features.users

import android.util.Log
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import com.qint.pt1.base.exception.Failure
import com.qint.pt1.base.exception.FailureHandler
import com.qint.pt1.base.platform.Selector
import com.qint.pt1.util.LOG_TAG
import javax.inject.Inject

//分页列表所需的DataSource
class UsersListDataSource
@Inject constructor(private val usersRepository: UsersRepository,
                    private val selectors: Set<Selector<*,*>>,
                    private val failureHandler: FailureHandler?) :
    PageKeyedDataSource<Int, UsersListViewItem>() {

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, UsersListViewItem>
    ) {
        usersRepository.users(0, params.requestedLoadSize, selectors).either(
            { failure ->
                Log.e(LOG_TAG, "UsersListDataSource load initial failed: ${failure}")
                handleFailure(failure)
            },
            { users ->
                Log.d(LOG_TAG, "UsersListDataSource load initial get ${users.size} users with param ${params.requestedLoadSize}")
                callback.onResult(users.map { it.toUsersViewItem() }, null, 1)
            }
        )
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, UsersListViewItem>) {
        usersRepository.users(params.key, params.requestedLoadSize, selectors).either(
            { failure ->
                Log.e(LOG_TAG, "UsersListDataSource load after failed: ${failure}. ${params}")
                handleFailure(failure)
            },
            { users ->
                Log.d(LOG_TAG, "UsersListDataSource load after get ${users.size} users with param (${params.key}, ${params.requestedLoadSize})")
                callback.onResult(users.map { it.toUsersViewItem() }, params.key + 1)
            }
        )
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, UsersListViewItem>) {}

    private fun handleFailure(failure: Failure) {
        failureHandler?.handleFailure(failure)
    }
}

class UserListDataSourceFactory
@Inject constructor(private val usersRepository: UsersRepository) :
    DataSource.Factory<Int, UsersListViewItem>() {

    var selectors: Set<Selector<*,*>> = emptySet()
    var failureHandler: FailureHandler? = null

    override fun create(): DataSource<Int, UsersListViewItem> =
        UsersListDataSource(usersRepository, selectors, failureHandler)
}
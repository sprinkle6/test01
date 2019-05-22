/**
 * Copyright (C) 2018 Fernando Cejas Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qint.pt1.features.users

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.given
import com.qint.pt1.AndroidTest
import com.qint.pt1.base.functional.Either.Right
import com.qint.pt1.domain.User
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldEqualTo
import org.junit.Before
import org.junit.Test
import org.mockito.Mock

class UsersListViewModelTest : AndroidTest() {

    private lateinit var usersListViewModel: UsersListViewModel

    @Mock
    private lateinit var getUsers: GetUsers

    @Mock
    private lateinit var userListDataSourceFactory: UserListDataSourceFactory

    private val sampleUser1 = User("1")
    private val sampleUser2 = User("2")

    init{
        sampleUser1.profile.nickName = "IronMan"
        sampleUser1.profile.avatar = "AvatarIronMan"
        sampleUser2.profile.nickName = "BatMan"
        sampleUser2.profile.avatar = "AvatarBatMan"
    }

    @Before
    fun setUp() {
        usersListViewModel = UsersListViewModel(userListDataSourceFactory)
    }

    @Test
    fun `loading users should update live data`() {
        val usersList = listOf(sampleUser1, sampleUser2)
        given { runBlocking { getUsers.run(eq(any())) } }.willReturn(Right(usersList))

        usersListViewModel.usersLiveData.observeForever {
            it!!.size shouldEqualTo 2
            it[0]!!.id shouldBeEqualTo "1"
            it[0]!!.nickName shouldBeEqualTo sampleUser1.profile.nickName
            it[0]!!.avatar shouldBeEqualTo sampleUser1.profile.avatar
            it[1]!!.id shouldBeEqualTo "2"
            it[1]!!.nickName shouldBeEqualTo sampleUser2.profile.nickName
            it[1]!!.avatar shouldBeEqualTo sampleUser2.profile.avatar
        }

        //FIXME
        //runBlocking { usersListViewModel.loadUsers() }
    }
}
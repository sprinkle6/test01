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

import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.qint.pt1.UnitTest
import com.qint.pt1.base.functional.Either.Right
import com.qint.pt1.domain.User
import com.qint.pt1.features.user.GetUserDetails
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock

class GetUserDetailsTest : UnitTest() {

    private val USER_ID = "1"

    private lateinit var getUserDetails: GetUserDetails

    @Mock
    private lateinit var usersRepository: UsersRepository

    @Before fun setUp() {
        getUserDetails = GetUserDetails(usersRepository)
        given { usersRepository.userDetails(USER_ID) }.willReturn(Right(User.empty()))
    }

    @Test fun `should get data from repository`() {
        runBlocking { getUserDetails.run(GetUserDetails.Params(USER_ID)) }

        verify(usersRepository).userDetails(USER_ID)
        verifyNoMoreInteractions(usersRepository)
    }
}

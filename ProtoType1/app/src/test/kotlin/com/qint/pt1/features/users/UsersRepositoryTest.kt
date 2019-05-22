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
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.qint.pt1.AndroidTest
import com.qint.pt1.base.exception.Failure.NetworkConnectionError
import com.qint.pt1.base.exception.Failure.ServerError
import com.qint.pt1.base.functional.Either
import com.qint.pt1.base.functional.Either.Right
import com.qint.pt1.base.platform.NetworkHandler
import com.qint.pt1.domain.AudioResource
import com.qint.pt1.domain.Gender
import com.qint.pt1.domain.User
import com.qint.pt1.domain.UserStatus
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldEqual
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import proto_def.UserMessage
import retrofit2.Call
import retrofit2.Response

class UsersRepositoryTest : AndroidTest() {

    private lateinit var networkRepository: UsersRepository.Network

    @Mock
    private lateinit var networkHandler: NetworkHandler
    @Mock
    private lateinit var service: UsersService

    @Mock
    private lateinit var usersCall: Call<UserMessage.BanBanListResp>
    @Mock
    private lateinit var usersResponse: Response<UserMessage.BanBanListResp>
    @Mock
    private lateinit var userDetailsCall: Call<UserMessage.HomePageResp>
    @Mock
    private lateinit var userDetailsResponse: Response<UserMessage.HomePageResp>

    private val sampleUser = User("1")
    init {
        sampleUser.profile.nickName = "Tom"
        sampleUser.profile.age = 18
        sampleUser.profile.avatar = "Avatar"
        sampleUser.info.status = UserStatus.OFFLINE
        sampleUser.profile.location = "北京"
        sampleUser.profile.declaration = "Hello World"
        sampleUser.profile.gender = Gender.MALE
        sampleUser.profile.promotionAudio = AudioResource("AudioUrl")
        sampleUser.profile.primarySkillTag = "Running"
        sampleUser.info.lastVisit = "Yesterday"
        sampleUser.info.freshMan = true
        sampleUser.info.fansNumber = 0
    }

    private val sampleUserHomePageResp = UserMessage.HomePageResp.getDefaultInstance()

    private val usersReq = UserMessage.BanbanListReq.newBuilder()
        .setPageNumber(0)
        .setNumPerPage(20)
        .build()

    //FIXME
    private val homePageReq = UserMessage.HomePageReq.newBuilder().setUid(sampleUser.id).build()

    @Before
    fun setUp() {
        networkRepository = UsersRepository.Network(networkHandler, service)
    }

    @Test
    fun `should return empty list by default`() {
        given { networkHandler.isConnected }.willReturn(true)
        given { usersResponse.body() }.willReturn(null)
        given { usersResponse.isSuccessful }.willReturn(true)
        given { usersCall.execute() }.willReturn(usersResponse)
        given { service.recommendUsers(usersReq) }.willReturn(usersCall)

        val users = networkRepository.users()

        users shouldEqual Right(emptyList<User>())
        verify(service).recommendUsers(usersReq)
    }

    @Test
    fun `should get user list from service`() {
        given { networkHandler.isConnected }.willReturn(true)
        //TODO: make this work
        //given { usersResponse.body() }.willReturn(UserMessage.BanBanListResp("1", "avatar"))
        given { usersResponse.isSuccessful }.willReturn(true)
        given { usersCall.execute() }.willReturn(usersResponse)
        given { service.recommendUsers(usersReq) }.willReturn(usersCall)

        val users = networkRepository.users()

        users shouldEqual Right(listOf(sampleUser))
        verify(service).recommendUsers(usersReq)
    }

    @Test
    fun `users service should return network failure when no connection`() {
        given { networkHandler.isConnected }.willReturn(false)

        val users = networkRepository.users()

        users shouldBeInstanceOf Either::class.java
        users.isLeft shouldEqual true
        users.either({ failure -> failure shouldBeInstanceOf NetworkConnectionError::class.java }, {})
        verifyZeroInteractions(service)
    }

    @Test
    fun `users service should return network failure when undefined connection`() {
        given { networkHandler.isConnected }.willReturn(null)

        val users = networkRepository.users()

        users shouldBeInstanceOf Either::class.java
        users.isLeft shouldEqual true
        users.either({ failure -> failure shouldBeInstanceOf NetworkConnectionError::class.java }, {})
        verifyZeroInteractions(service)
    }

    @Test
    fun `users service should return server error if no successful response`() {
        given { networkHandler.isConnected }.willReturn(true)

        val users = networkRepository.users()

        users shouldBeInstanceOf Either::class.java
        users.isLeft shouldEqual true
        users.either({ failure -> failure shouldBeInstanceOf ServerError::class.java }, {})
    }

    @Test
    fun `users request should catch exceptions`() {
        given { networkHandler.isConnected }.willReturn(true)

        val users = networkRepository.users()

        users shouldBeInstanceOf Either::class.java
        users.isLeft shouldEqual true
        users.either({ failure -> failure shouldBeInstanceOf UnknownError::class.java }, {})
    }

    @Test
    fun `should return empty user details by default`() {
        given { networkHandler.isConnected }.willReturn(true)
        given { userDetailsResponse.body() }.willReturn(null)
        given { userDetailsResponse.isSuccessful }.willReturn(true)
        given { userDetailsCall.execute() }.willReturn(userDetailsResponse)
        given { service.userDetails(homePageReq) }.willReturn(userDetailsCall)

        val userDetails = networkRepository.userDetails("not_exist_id")

        userDetails shouldEqual Right(User.empty())
        verify(service).userDetails(homePageReq)
    }

    @Test
    fun `should get user details from service`() {
        given { networkHandler.isConnected }.willReturn(true)
        given { userDetailsResponse.body() }.willReturn(sampleUserHomePageResp)
        given { userDetailsResponse.isSuccessful }.willReturn(true)
        given { userDetailsCall.execute() }.willReturn(userDetailsResponse)
        given { service.userDetails(homePageReq) }.willReturn(userDetailsCall)

        val userDetails = networkRepository.userDetails("1")

        userDetails shouldEqual Right(sampleUser)
        verify(service).userDetails(homePageReq)
    }

    @Test
    fun `user details service should return network failure when no connection`() {
        given { networkHandler.isConnected }.willReturn(false)

        val userDetails = networkRepository.userDetails("1")

        userDetails shouldBeInstanceOf Either::class.java
        userDetails.isLeft shouldEqual true
        userDetails.either({ failure -> failure shouldBeInstanceOf NetworkConnectionError::class.java }, {})
        verifyZeroInteractions(service)
    }

    @Test
    fun `user details service should return network failure when undefined connection`() {
        given { networkHandler.isConnected }.willReturn(null)

        val userDetails = networkRepository.userDetails("1")

        userDetails shouldBeInstanceOf Either::class.java
        userDetails.isLeft shouldEqual true
        userDetails.either({ failure -> failure shouldBeInstanceOf NetworkConnectionError::class.java }, {})
        verifyZeroInteractions(service)
    }

    @Test
    fun `user details service should return server error if no successful response`() {
        given { networkHandler.isConnected }.willReturn(true)

        val userDetails = networkRepository.userDetails("1")

        userDetails shouldBeInstanceOf Either::class.java
        userDetails.isLeft shouldEqual true
        userDetails.either({ failure -> failure shouldBeInstanceOf ServerError::class.java }, {})
    }

    @Test
    fun `user details request should catch exceptions`() {
        given { networkHandler.isConnected }.willReturn(true)

        val userDetails = networkRepository.userDetails("1")

        userDetails shouldBeInstanceOf Either::class.java
        userDetails.isLeft shouldEqual true
        userDetails.either({ failure -> failure shouldBeInstanceOf ServerError::class.java }, {})
    }
}
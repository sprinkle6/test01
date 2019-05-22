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
import com.qint.pt1.domain.AudioResource
import com.qint.pt1.domain.Gender
import com.qint.pt1.domain.User
import com.qint.pt1.domain.UserStatus
import com.qint.pt1.features.profile.UserProfileViewModel
import com.qint.pt1.features.user.GetUserDetails
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldEqualTo
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import javax.inject.Inject

class UserProfileViewModelTest : AndroidTest() {

    @Inject
    lateinit var userProfileViewModel: UserProfileViewModel

    @Mock
    private lateinit var getUserDetails: GetUserDetails

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

    @Before
    fun setUp() {
        //userProfileViewModel = UserProfileViewModel(Login())
    }

    @Test
    fun `loading user details should update live data`() {
        given { runBlocking { getUserDetails.run(eq(any())) } }.willReturn(Right(sampleUser))

        userProfileViewModel.userLiveData.observeForever {
            with(it!!) {
                id shouldEqualTo "1"
                profile.nickName shouldEqualTo "IronMan"
                profile.avatar shouldEqualTo "avatar"
                profile.primarySkillTag shouldEqualTo "王者荣耀"
                profile.declaration shouldEqualTo "只接单不私聊"
                info.lastVisit shouldEqualTo "1小时前"
                profile.location shouldEqualTo "北京"
            }
        }

        runBlocking { userProfileViewModel.loadUserProfile("1") }
    }
}
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
package com.qint.pt1.base.navigation

import com.qint.pt1.AndroidTest
import com.qint.pt1.features.chatroom.ChatRoomActivity
import com.qint.pt1.features.chatrooms.ChatRoomsListViewItem
import com.qint.pt1.features.login.Login
import com.qint.pt1.features.login.LoginActivity
import com.qint.pt1.features.main.MainActivity
import com.qint.pt1.features.profile.UserProfileActivity
import com.qint.pt1.shouldNavigateTo
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock


class NavigatorTest : AndroidTest() {

    private lateinit var navigator: Navigator

    @Mock private lateinit var login: Login

    private val chatroom = ChatRoomsListViewItem("1", "title", "category", 100, "OPTom", "pic", "avatar")

    @Before fun setup() {
        navigator = Navigator(login)
    }

    @Test fun `joinChatRoom should forward user to login screen if not logined`() {
        When calling login.isLogined itReturns false

        navigator.joinChatRoom(activityContext(), chatroom)

        Verify on login that login.isLogined was called
        RouteActivity::class shouldNavigateTo LoginActivity::class
    }

    @Test fun `joinChatRoom should forward user to chatroom screen if logined`() {
        When calling login.isLogined itReturns true

        navigator.joinChatRoom(activityContext(), chatroom)

        Verify on login that login.isLogined was called
        RouteActivity::class shouldNavigateTo ChatRoomActivity::class
    }

    @Test
    fun `showMain should forward user to main screen`() {
        navigator.showMain(activityContext())

        VerifyNoInteractions on login
        RouteActivity::class shouldNavigateTo MainActivity::class
    }

    @Test
    fun `showUserProfile should forward user to profile screen`() {
        navigator.showUserProfile(activityContext(), "1")

        VerifyNoInteractions on login
        RouteActivity::class shouldNavigateTo UserProfileActivity::class
    }

}

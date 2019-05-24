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
package com.qint.pt1.base.di

import com.qint.pt1.FlowerLanguageApplication
import com.qint.pt1.base.di.viewmodel.ViewModelModule
import com.qint.pt1.base.navigation.RouteActivity
import com.qint.pt1.features.account.AccountFragment
import com.qint.pt1.features.chatroom.ChatRoomActivity
import com.qint.pt1.features.chatroom.ChatRoomFragment
import com.qint.pt1.features.chatroom.widgets.ChatRoomBackpackPanelFragment
import com.qint.pt1.features.chatroom.widgets.ChatRoomGiftPanelFragment
import com.qint.pt1.features.chatrooms.ChatRoomsFragment
import com.qint.pt1.features.chatrooms.ChatRoomsListFragment
import com.qint.pt1.features.login.LoginFragment
import com.qint.pt1.features.main.MainActivity
import com.qint.pt1.features.messages.MessagesFragment
import com.qint.pt1.features.profile.UserProfileActivity
import com.qint.pt1.features.profile.UserProfileEditFragment
import com.qint.pt1.features.profile.UserProfileFragment
import com.qint.pt1.features.search.SearchActivity
import com.qint.pt1.features.users.UsersFilterListFragment
import com.qint.pt1.features.users.UsersFragment
import com.qint.pt1.features.users.UsersListFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class, ViewModelModule::class])
interface ApplicationComponent {
    fun inject(application: FlowerLanguageApplication)
    fun inject(routeActivity: RouteActivity)

    fun inject(mainActivity: MainActivity)
    fun inject(loginFragment: LoginFragment)

    fun inject(usersFragment: UsersFragment)
    fun inject(usersListFragment: UsersListFragment)

    fun inject(chatRoomsFragment: ChatRoomsFragment)
    fun inject(chatRoomsListFragment: ChatRoomsListFragment)

    fun inject(chatRoomActivity: ChatRoomActivity)
    fun inject(chatRoomFragment: ChatRoomFragment)
    fun inject(chatRoomGiftPanelFragment: ChatRoomGiftPanelFragment)
    fun inject(chatRoomBackpackPanelFragment: ChatRoomBackpackPanelFragment)

    fun inject(messagesFragment: MessagesFragment)
    fun inject(accountFragment: AccountFragment)

    fun inject(userProfileActivity: UserProfileActivity)
    fun inject(userProfileFragment: UserProfileFragment)
    fun inject(userProfileEditFragment: UserProfileEditFragment)
    fun inject(usersFilterListFragment: UsersFilterListFragment)

    fun inject(searchActivity: SearchActivity)
}

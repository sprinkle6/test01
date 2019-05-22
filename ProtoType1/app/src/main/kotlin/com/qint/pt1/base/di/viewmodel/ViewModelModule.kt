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
package com.qint.pt1.base.di.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.qint.pt1.features.account.AccountViewModel
import com.qint.pt1.features.chatroom.ChatRoomViewModel
import com.qint.pt1.features.chatrooms.ChatRoomsListViewModel
import com.qint.pt1.features.chatrooms.ChatRoomsViewModel
import com.qint.pt1.features.main.MainViewModel
import com.qint.pt1.features.profile.UserProfileViewModel
import com.qint.pt1.features.search.SearchViewModel
import com.qint.pt1.features.users.UsersListViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {
    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindsMainViewModel(mainViewModel: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UsersListViewModel::class)
    abstract fun bindsUserListViewModel(usersListViewModel: UsersListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChatRoomsViewModel::class)
    abstract fun bindsChatRoomsViewModel(chatRoomsViewModel: ChatRoomsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChatRoomsListViewModel::class)
    abstract fun bindsChatRoomsListViewModel(chatRoomsListViewModel: ChatRoomsListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChatRoomViewModel::class)
    abstract fun bindChatRoomViewModel(chatRoomViewModel: ChatRoomViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AccountViewModel::class)
    abstract fun bindMineViewModel(accountViewModel: AccountViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UserProfileViewModel::class)
    abstract fun bindUserProfileViewModel(userProfileViewModel: UserProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SearchViewModel::class)
    abstract fun bindSearchViewModel(searchViewModel: SearchViewModel): ViewModel
}
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

import android.content.Context
import android.preference.PreferenceManager
import com.google.protobuf.ExtensionRegistryLite
import com.qint.pt1.BuildConfig
import com.qint.pt1.FlowerLanguageApplication
import com.qint.pt1.features.chatroom.ChatRoomMessageAPI
import com.qint.pt1.features.chatrooms.ChatRoomsRepository
import com.qint.pt1.features.users.UsersRepository
import com.qint.pt1.support.nim.NIMChatRoomMessageAPI
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.protobuf.ProtoConverterFactory
import javax.inject.Singleton

@Module
class ApplicationModule(private val application: FlowerLanguageApplication) {
    companion object {
        const val API_BASE_URL = "http://ml.qint.tv:8000/"
    }

    @Provides
    @Singleton
    fun provideApplicationContext(): Context = application

    @Provides
    @Singleton
    fun provideSharedPreferences(context: Context) = PreferenceManager.getDefaultSharedPreferences(context)

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        //ProtoConverterFactory must use createWithRegistry, if use create() the parser will throw null pointer exception
        return Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .client(createClient())
                .addConverterFactory(ProtoConverterFactory.createWithRegistry(ExtensionRegistryLite.newInstance()))
                .build()
    }

    private fun createClient(): OkHttpClient {
        val okHttpClientBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC)
            okHttpClientBuilder.addInterceptor(loggingInterceptor)
        }
        return okHttpClientBuilder.build()
    }

    @Provides
    @Singleton
    fun provideUsersRepository(dataSource: UsersRepository.Network): UsersRepository = dataSource

    @Provides
    @Singleton
    fun provideChatRoomsRepository(dataSource: ChatRoomsRepository.Network): ChatRoomsRepository = dataSource

    @Provides
    @Singleton
    fun provideChatRoomMessageAPI(messageAPI: NIMChatRoomMessageAPI): ChatRoomMessageAPI = messageAPI
}

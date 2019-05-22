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

import com.qint.pt1.AndroidTest
import com.qint.pt1.base.navigation.Navigator
import org.junit.Before
import org.junit.Test
import org.mockito.Mock

//TODO: change to voice play test
class PlayVoiceTest : AndroidTest() {

    private val VOICE_URL = "https://www.youtube.com/watch?v=fernando"

    //private lateinit var playVoice: PlayVoice

    private val context = context()

    @Mock private lateinit var navigator: Navigator

    @Before fun setUp() {
        //playVoice = PlayVoice(context, navigator)
    }

    @Test
    fun `should play voice trailer`() {
        /*
        val params = PlayVoice.Params(VOICE_URL)

        playVoice(params)

        verify(navigator).openVoice(context, VOICE_URL)
        */
    }
}

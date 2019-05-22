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
package com.qint.pt1.base.platform

import androidx.lifecycle.MutableLiveData
import com.qint.pt1.AndroidTest
import com.qint.pt1.base.exception.Failure
import com.qint.pt1.base.exception.Failure.NetworkConnectionError
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Test

class BaseViewModelTest : AndroidTest() {

    @Test fun `should handle failure by updating live data`() {
        val viewModel = MyViewModel()

        viewModel.handleError(NetworkConnectionError())

        val failure = viewModel.failureLiveData
        val error = viewModel.failureLiveData.value

        failure shouldBeInstanceOf MutableLiveData::class.java
        error shouldBeInstanceOf NetworkConnectionError::class.java
    }

    private class MyViewModel : BaseViewModel() {
        fun handleError(failure: Failure) = trigerFailure(failure)
    }
}
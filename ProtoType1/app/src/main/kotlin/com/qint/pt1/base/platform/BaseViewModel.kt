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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.qint.pt1.base.exception.Failure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Base ViewModel class with default Failure handling.
 * @see ViewModel
 * @see Failure
 */
abstract class BaseViewModel : ViewModel() {

    private val _failureLiveData: MutableLiveData<Failure> = MutableLiveData()
    val failureLiveData: LiveData<Failure> = _failureLiveData

    /*
     * 设置failureLiveData值，以便Activity/Fragment可以观察到发生了failure对用户进行提示
     */
    fun trigerFailure(failure: Failure) {
        CoroutineScope(Dispatchers.Main).launch { //调用者可能在非主线程中运行，转入主线程更新LiveData数据
            _failureLiveData.value = failure
        }
    }
}
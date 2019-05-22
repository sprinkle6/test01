/*
 * Author: Matthew Zhang
 * Created on: 4/24/19 2:58 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.base.platform

import com.qint.pt1.base.exception.Failure

interface RequestCallback<T> {
    //TODO: 最好能封装一个默认实现，在其中仅允许onSuccess或者onFailure执行一次，以防止处理逻辑较复杂的时候错误地多次执行回调引发混乱
    fun onSuccess(result: T?)
    fun onFailure(failure: Failure)
}
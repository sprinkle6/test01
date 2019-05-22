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
package com.qint.pt1.base.interactor

import com.qint.pt1.base.exception.Failure
import com.qint.pt1.base.functional.Either
import kotlinx.coroutines.*

/**
 * Abstract class for a Use Case (Interactor in terms of Clean Architecture).
 * This abstraction represents an execution unit for different use cases (this means than any use
 * case in the application should implement this contract).
 *
 * By convention each [UseCase] implementation will execute its job in a background thread
 * (kotlin coroutine) and will post the result in the UI thread.
 *
 * Matthew Zhang: 是一种封装，将同步调用改为异步，从主线程发起对UseCase的调用，在IO线程执行任务，结果通过回调函数在主线程中处理。
 */
abstract class UseCase<out Type, in Params> where Type : Any {

    /*
     * 注意事项：
     * 1. 不要直接调用run方法，使用usecase(param){onResult}的形式调用。
     * 2. 尽量避免调用同一个UseCase实例多次，最好为每次调用创建新的实例。
     */
    abstract suspend fun run(params: Params): Either<Failure, Type>

    private lateinit var job: Deferred<Either<Failure, Type>>

    val isCompleted: Boolean get() = ::job.isInitialized && job.isCompleted

    operator fun invoke(
        params: Params = None() as Params,
        onResult: (Either<Failure, Type>) -> Unit = {}
    ) {
        if (::job.isInitialized && !job.isCompleted) { //上次调用还在进行中
            throw Exception("Can not invoke an usecase again before its last invoking completed. Don't invoke an UseCase instance twice to avoid this problem.")
        }
        CoroutineScope(Dispatchers.Main).launch {
            job = async(Dispatchers.IO) {
                try {
                    run(params)
                } catch (e: Exception) {
                    Either.Left(Failure.UnknownError("Meet Exception in UseCase: ${e.message}"))
                }
            }
            onResult(job.await())
        }
    }

    class None
}

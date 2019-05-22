/*
 * Author: Matthew Zhang
 * Created on: 4/28/19 5:44 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.base.platform

import android.util.Log
import com.qint.pt1.base.exception.Failure
import com.qint.pt1.base.functional.Either
import com.qint.pt1.util.LOG_TAG
import retrofit2.Call
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class APIHandler
@Inject constructor(private val networkHandler: NetworkHandler){
    fun <T, R> request(call: Call<T>, transform: (T) -> R): Either<Failure, R> {
        return when (networkHandler.isConnected) {
            false, null -> Either.Left(Failure.NetworkConnectionError())
            true -> try {
                val response = call.execute()
                when (response.isSuccessful) {
                    true -> {
                        val body = response.body()
                        if(body != null) {
                            Either.Right(transform(body))
                        }else{
                            Either.Left(Failure.ServerError(0, "服务器异常：响应为空"))
                        }
                    }
                    false -> Either.Left(
                        Failure.ServerError(
                            response.code(),
                            "服务器异常：code ${response.code()}"
                        )
                    )
                }
            } catch (exception: Throwable) {
                val errorMessage = exception.message ?: exception.toString()
                Log.e(LOG_TAG, "request remote data error: ${errorMessage}")
                exception.printStackTrace()
                Either.Left(Failure.UnknownError(errorMessage))
            }
        }
    }

    /*
     * 需要取得原始调用返回结果再进行细分处理的使用此方法
     */
    fun <T> request(call: Call<T>): Either<Failure, T> = request(call, {it -> it})

}
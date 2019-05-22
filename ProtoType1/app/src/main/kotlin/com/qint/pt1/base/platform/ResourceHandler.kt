/*
 * Author: Matthew Zhang
 * Created on: 4/21/19 6:12 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.base.platform

import android.content.SharedPreferences
import android.util.Log
import com.google.protobuf.Message
import com.qint.pt1.base.exception.Failure
import com.qint.pt1.base.extension.decodeProtoBufMessage
import com.qint.pt1.base.extension.encode
import com.qint.pt1.base.functional.Either
import com.qint.pt1.util.LOG_TAG
import retrofit2.Call
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResourceHandler @Inject constructor(val sharedPreferences: SharedPreferences,
                                          val apiHandler: APIHandler,
                                          val memCache: MemCache
) {
    val CACHE_KEY_PREFIX = "ResourceCache."

    fun <T : Message, R> request(call: Call<T>, transform: (T) -> R): Either<Failure, R> =
        apiHandler.request(call, transform)

    inline fun <reified T: Message, R> request(
        call: Call<T>,
        crossinline transform: (T) -> R,
        key: String
    ): Either<Failure, R> {
        /*
         * 先查看内存有无domain对象的缓存
         */
        assert(key.isNotBlank())
        val cachedDomainValue = memCache.get(key)
        if (cachedDomainValue != null) {
            Log.d(LOG_TAG, "hit memCache $key")
            return Either.Right(cachedDomainValue as R)
        }
        Log.d(LOG_TAG, "miss memCache $key")

        return request(call, key).either(
            { failure -> Either.Left(failure) },
            { resp ->
                val domainObj = transform(resp)
                memCache.set(key, domainObj as Any)
                Log.d(LOG_TAG, "set memCache $key")
                Either.Right(domainObj)
            }
        ) as Either<Failure, R>

    }

    fun <T: Message> request(call: Call<T>): Either<Failure, T> = apiHandler.request(call)

    inline fun <reified T: Message> request(call: Call<T>, key: String): Either<Failure, T> {
        //FIXME: localstorage缓存需要增加老化过期能力
        assert(key.isNotBlank())
        val cacheKey = "${CACHE_KEY_PREFIX}${key}"
        try {
            val cachedProtobufValue = sharedPreferences.getString(cacheKey, "")
            if (!cachedProtobufValue.isNullOrBlank()) {
                Log.d(LOG_TAG, "hit localStorageCache ${key}")
                val protobufObj = decodeProtoBufMessage(cachedProtobufValue) as T
                return Either.Right(protobufObj)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            Log.w(LOG_TAG, "failed to get localStorageCache ${cacheKey}: $e")
        }
        Log.d(LOG_TAG, "miss localStorageCache ${key}")

        val result = apiHandler.request(call)
        try {
            result.either(
                { failure -> },
                { resp ->
                    val encoded = resp.encode()
                    if (!encoded.isNullOrBlank()) {
                        with(sharedPreferences.edit()) {
                            putString(cacheKey, encoded)
                            apply()
                        }
                        Log.d(LOG_TAG, "saved localStorageCache ${key}")
                    }
                }
            )
        } catch (e: Throwable) {
            Log.e(LOG_TAG, "failed to save localStorageCache ${key}: $e")
        }

        return result
    }
}

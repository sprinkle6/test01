/*
 * Author: Matthew Zhang
 * Created on: 4/21/19 5:31 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.base.platform

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemCache @Inject constructor() {
    private val store: ConcurrentHashMap<String, Any> = ConcurrentHashMap()

    fun set(key: String, value: Any) = store.set(key, value)
    fun get(key: String): Any? = store.get(key)
}
/*
 * Author: Matthew Zhang
 * Created on: 4/18/19 9:32 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.base.exception

import android.util.Log
import android.view.View
import com.qint.pt1.util.LOG_TAG

interface FailureHandler{
    fun handleFailure(failure: Failure?)

    fun handleFailure(failure: Failure, actionPrompt: String, action: (View) -> Unit)

    fun renderFailure(failure: Failure)

    fun renderFailure(failure: Failure, actionPrompt: String, action: (View) -> Unit)

    open class NoopFailureHandler: FailureHandler{
        override fun handleFailure(failure: Failure?) {
            renderFailure(failure ?: return)
        }

        override fun handleFailure(
            failure: Failure,
            actionPrompt: String,
            action: (View) -> Unit
        ) {
            renderFailure(failure, actionPrompt, action)
        }

        override fun renderFailure(failure: Failure) {
            Log.e(LOG_TAG, failure.toString())
        }

        override fun renderFailure(
            failure: Failure,
            actionPrompt: String,
            action: (View) -> Unit
        ) {
            Log.e(LOG_TAG, failure.toString())
        }

    }
}
/*
 * Author: Matthew Zhang
 * Created on: 4/16/19 10:16 AM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.base.platform

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.qint.pt1.FlowerLanguageApplication
import com.qint.pt1.base.di.ApplicationComponent
import com.qint.pt1.base.exception.Failure
import com.qint.pt1.base.exception.FailureHandler
import com.qint.pt1.util.LOG_TAG
import org.jetbrains.anko.contentView
import org.jetbrains.anko.design.indefiniteSnackbar
import org.jetbrains.anko.design.longSnackbar
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity(), FailureHandler {

    val appComponent: ApplicationComponent by lazy(mode = LazyThreadSafetyMode.NONE) {
        (application as FlowerLanguageApplication).appComponent
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    abstract fun layoutId(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(layoutId())
    }

    internal fun notify(@StringRes message: Int) = contentView?.longSnackbar(message)

    internal fun notify(message: String) {
        contentView?.longSnackbar(message)
    }

    internal fun notifyWithAction(message: String, actionPrompt: String, action: (View) -> Unit) {
        contentView?.indefiniteSnackbar(message, actionPrompt, action)
    }

    override fun handleFailure(failure: Failure?) {
        if(failure == null) return
        Log.e(LOG_TAG, failure.toString())
        renderFailure(failure)
    }

    override fun handleFailure(failure: Failure, actionPrompt: String, action: (View) -> Unit){
        Log.e(LOG_TAG, failure.toString())
        renderFailure(failure, actionPrompt, action)
    }

    override fun renderFailure(failure: Failure) = notify(failure.message)

    override fun renderFailure(failure: Failure, actionPrompt: String, action: (View) -> Unit) =
            notifyWithAction(failure.message, actionPrompt, action)

}

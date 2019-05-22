package com.qint.pt1.base.extension

import android.app.Activity
import android.content.pm.PackageManager
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders

inline fun <reified T : ViewModel> AppCompatActivity.viewModel(factory: ViewModelProvider.Factory, body: T.() -> Unit): T {
    val vm = ViewModelProviders.of(this, factory)[T::class.java]
    vm.body()
    return vm
}

fun Activity.screenWidthDp(): Int {
    //The current width of the available screen space, in dp units, corresponding to screen width resource qualifier.
    val screenWidthDp = resources.configuration.screenWidthDp

    //The smallest screen size an application will see in normal operation, corresponding to smallest screen width resource qualifier.
    //val smallestScreenWidthDp = configuration.smallestScreenWidthDp

    return screenWidthDp
}

fun Activity.requestPermission(permission: String, requestCode: Int = 0): Boolean{
    if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
        ActivityCompat.requestPermissions(this, arrayOf(permission), 0)
        return false
    }
    return true
}

/*
 * 隐藏系统导航按钮
 */
fun Activity.hideNavigationButtons() {
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
}

/*
 * 进入全屏沉浸模式
 */
fun Activity.immersive() {
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        .or(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        .or(View.SYSTEM_UI_FLAG_FULLSCREEN)
        .or(View.SYSTEM_UI_FLAG_IMMERSIVE)
}
/*
 * Author: Matthew Zhang
 * Created on: 5/19/19 11:17 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.base.platform

import android.app.Dialog
import android.content.Context
import android.view.MotionEvent
import android.view.WindowManager
import androidx.annotation.LayoutRes
import com.qint.pt1.R
import com.qint.pt1.base.extension.dp2px
import com.qint.pt1.base.extension.screenWidthDp

open class BaseDialog(context: Context, @LayoutRes layout: Int): Dialog(context) {
    init{
        setContentView(layout)

        val margin = 16 //dp
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window.attributes)
        lp.width = context.dp2px(context.screenWidthDp() - margin * 2).toInt()
        window.attributes = lp

        window.setBackgroundDrawableResource(R.color.transparent)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        this.dismiss()
        return super.onTouchEvent(event)
    }
}
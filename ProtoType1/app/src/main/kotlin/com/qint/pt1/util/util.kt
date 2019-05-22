package com.qint.pt1.util

import android.content.res.Resources

const val LOG_TAG = "flower-word"

const val DEFAULT_ITEM_NUM_PER_PAGE = 20

fun getScreenWidthInPixel() = Resources.getSystem().displayMetrics.widthPixels

fun getScreenHeightInPixel() = Resources.getSystem().displayMetrics.heightPixels

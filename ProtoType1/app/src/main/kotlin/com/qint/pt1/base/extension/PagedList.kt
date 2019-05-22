/*
 * Author: Matthew Zhang
 * Created on: 4/16/19 4:13 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.base.extension

import androidx.paging.PagedList

private val DEFAULT_ITEM_NUM_PER_PAGE = 20

fun getDefaultPagedListConfig() = PagedList.Config.Builder()
    .setEnablePlaceholders(false)
    .setInitialLoadSizeHint(DEFAULT_ITEM_NUM_PER_PAGE)
    .setPageSize(DEFAULT_ITEM_NUM_PER_PAGE).build()
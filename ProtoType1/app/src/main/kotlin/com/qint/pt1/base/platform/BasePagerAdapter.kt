/*
 * Copyright (c) 2019. QINT.TV
 * Author: Matthew Zhang
 * Created on: 3/27/19 10:40 AM
 */

package com.qint.pt1.base.platform

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

abstract class BasePagerAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {
    abstract fun pageTitles(): List<String>

    override fun getItem(position: Int): androidx.fragment.app.Fragment = EmptyFragment()

    override fun getPageTitle(position: Int): CharSequence? {
        if (position in 0..(count - 1)) return pageTitles()[position]
        return pageTitles()[0]
    }

    override fun getCount() = pageTitles().size
}
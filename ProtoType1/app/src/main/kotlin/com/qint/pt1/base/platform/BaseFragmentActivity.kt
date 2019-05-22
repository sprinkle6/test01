/*
 * Author: Matthew Zhang
 * Created on: 4/16/19 10:16 AM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.base.platform

import android.os.Bundle
import com.qint.pt1.R
import com.qint.pt1.base.extension.inTransaction
import com.qint.pt1.base.extension.invisible
import com.qint.pt1.base.extension.visible
import kotlinx.android.synthetic.main.progressbar.*

abstract class BaseFragmentActivity: BaseActivity() {
    override fun layoutId() = R.layout.base_activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addFragment(savedInstanceState)
    }

    override fun onBackPressed() {
        (supportFragmentManager.findFragmentById(
            R.id.fragmentContainer) as BaseFragment).onBackPressed()
        super.onBackPressed()
    }

    private fun addFragment(savedInstanceState: Bundle?) =
        savedInstanceState ?: supportFragmentManager.inTransaction { add(
            R.id.fragmentContainer, fragment()) }

    abstract fun fragment(): BaseFragment

    internal fun showProgress() = progress.visible()

    internal fun hideProgress() = progress.invisible()

}
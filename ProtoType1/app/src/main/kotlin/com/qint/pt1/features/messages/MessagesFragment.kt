package com.qint.pt1.features.messages

import android.os.Bundle
import android.view.View
import com.netease.nim.uikit.business.contact.ContactsFragment
import com.netease.nim.uikit.business.recent.RecentContactsFragment
import com.qint.pt1.R
import com.qint.pt1.base.platform.BaseFragment
import kotlinx.android.synthetic.main.messages_fragment.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 底部导航->消息
 */
@Singleton
class MessagesFragment
@Inject constructor() : BaseFragment() {
    override fun layoutId() = R.layout.messages_fragment

    private lateinit var pagerAdapter: MessagesPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
        //Fragment中嵌套Fragment必须要用childFragmentManager
        pagerAdapter = MessagesPagerAdapter(childFragmentManager)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
    }

    private fun initializeView() {
        messagesPager.adapter = pagerAdapter
        messagesTab.setupWithViewPager(messagesPager)
    }
}

class MessagesPagerAdapter(fm: androidx.fragment.app.FragmentManager) : androidx.fragment.app.FragmentPagerAdapter(fm) {
    override fun getCount() = 2 //"消息"及"羁绊"

    override fun getPageTitle(position: Int): CharSequence? = when (position) {
        0 -> "消息"
        1 -> "羁绊"
        else -> "消息"
    }

    override fun getItem(position: Int): androidx.fragment.app.Fragment = when (position) {
        0 -> RecentContactsFragment()
        1 -> ContactsFragment()
        else -> RecentContactsFragment()
    }
}
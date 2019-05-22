package com.qint.pt1.features.users

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.qint.pt1.R
import com.qint.pt1.base.navigation.Navigator
import com.qint.pt1.base.platform.BaseFragment
import kotlinx.android.synthetic.main.users_fragment.*
import org.jetbrains.anko.support.v4.toast
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 底部导航->首页
 */
@Singleton
class UsersFragment
@Inject constructor() : BaseFragment() {
    override fun layoutId() = R.layout.users_fragment

    private lateinit var usersPagerAdapter: UsersPagerAdapter

    @Inject internal lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
        //Fragment中嵌套Fragment必须要用childFragmentManager
        usersPagerAdapter = UsersPagerAdapter(childFragmentManager)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_search -> {
            navigator.goSearch(baseActivity)
            true
        }
        R.id.action_settings -> {
            toast("do settings")
            true
        }
        R.id.action_refresh -> {
            toast("do refresh")
            true
        }
        R.id.action_report -> {
            toast("do report")
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    private fun initializeView() {
        usersPager.adapter = usersPagerAdapter
        usersTab.setupWithViewPager(usersPager)
        initToolbar()
    }

    private fun initToolbar(){
        toolbar.inflateMenu(R.menu.users_toolbar)
//        toolbar.overflowIcon = resources.getDrawable(R.drawable.overflow, null)
        toolbar.setOnMenuItemClickListener { item -> onOptionsItemSelected(item) }
    }
}

class UsersPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    private val titles = arrayOf("推荐", "发现")

    override fun getCount() = titles.size

    override fun getPageTitle(position: Int) = titles[position]

    override fun getItem(position: Int): Fragment = when (position) {
        0 -> UsersListFragment()
        1 -> UsersFilterListFragment()
        else -> UsersListFragment()
    }

}
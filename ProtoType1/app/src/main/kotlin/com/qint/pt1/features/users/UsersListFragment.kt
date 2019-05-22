/*
 * Copyright (c) 2019. QINT.TV
 * Author: Matthew Zhang
 * Created on: 3/22/19 10:37 PM
 */

package com.qint.pt1.features.users

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.qint.pt1.R
import com.qint.pt1.base.exception.Failure
import com.qint.pt1.base.extension.*
import com.qint.pt1.base.navigation.Navigator
import com.qint.pt1.base.platform.BaseFragment
import kotlinx.android.synthetic.main.common_empty.*
import kotlinx.android.synthetic.main.users_list_fragment.*
import javax.inject.Inject

//FIXME: reuse UsersFilterList code for this
class UsersListFragment: BaseFragment() {
    override fun layoutId() = R.layout.users_list_fragment

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var usersListAdapter: UsersListAdapter

    private lateinit var usersListViewModel: UsersListViewModel

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)

        usersListViewModel = viewModel(viewModelFactory) {
            observe(usersLiveData, ::renderUsersList)
            failure(failureLiveData, ::handleFailure)
        }
    }

    private fun refresh() {
        usersListViewModel.refresh()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun onPause() {
        super.onPause()
        usersListAdapter.stop() //stop the voicePlayer
    }

    override fun onDestroyView() {
        super.onDestroyView()
        usersListAdapter.release() //To release the voice player
    }

    private fun initView() {
        usersList.visible()
        emptyView.invisible()

        usersList.layoutManager = LinearLayoutManager(context)
        usersList.adapter = usersListAdapter
        usersListAdapter.itemClickListener = { usersViewItem, navigationExtras ->
            navigator.showUserProfile(activity!!, usersViewItem.id)
        }

        //FIXME: 使用kotlinx扩展直接引用xml中swipeRefresh对象的方式运行时在这里总是报告对象为空，而在聊天室列表中无此问题，不知何故
        //暂时这里先使用传统方式
        //注意：下拉刷新后分页数据只重新取了前两页的，后面之前已加载过的内容没有重新取，不知道是否测试返回的数据都相同的缘故。
        //FIXME: 有时从其它页面切回首页还是会由于swipeRefresh为空导致崩溃，原因不明，先临时做个保护，还需查明原因彻底解决
        swipeRefresh = activity!!.findViewById(R.id.swipeRefresh)
        if(::swipeRefresh.isInitialized && swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener { refresh() }
        }
        progressBar = activity!!.findViewById(R.id.progressBar)

        showProgress()
    }

    private fun renderUsersList(pagedList: PagedList<UsersListViewItem>?) {
        usersListAdapter.submitList(pagedList)
        hideProgress()
    }

    override fun handleFailure(failure: Failure?) {
        renderFailure(failure ?: return, "获取数据失败，请重试", {view -> initView()})
    }

    override fun renderFailure(failure: Failure, actionPrompt: String, action: (View) -> Unit) {
        usersList.invisible()
        emptyView.visible()
        hideProgress()
        super.renderFailure(failure, actionPrompt, action)
    }

    override fun showProgress(){
        progressBar.visible()
    }

    override fun hideProgress() {
        progressBar.invisible()
        //FIXME: 有时从其它页面切回首页还是会由于swipeRefresh为空导致崩溃，原因不明，先临时做个保护，还需查明原因彻底解决
        if(::swipeRefresh.isInitialized && swipeRefresh != null) {
            swipeRefresh.isRefreshing = false
        }
    }

}
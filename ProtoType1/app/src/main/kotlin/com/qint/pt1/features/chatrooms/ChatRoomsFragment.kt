/*
 * Copyright (c) 2019. QINT.TV
 * Author: Matthew Zhang
 * Created on: 3/25/19 2:47 PM
 */

package com.qint.pt1.features.chatrooms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.qint.pt1.R
import com.qint.pt1.base.extension.failure
import com.qint.pt1.base.extension.observe
import com.qint.pt1.base.extension.viewModel
import com.qint.pt1.base.platform.BaseFragment
import com.qint.pt1.base.platform.BasePagerAdapter
import com.qint.pt1.domain.ChatRoomCategory
import kotlinx.android.synthetic.main.chatrooms_fragment.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 底部导航->聊天室
 */
@Singleton
class ChatRoomsFragment
@Inject constructor() : BaseFragment() {
    override fun layoutId() = R.layout.chatrooms_fragment

    private lateinit var pagerAdapter: ChatRoomsPagerAdapter
    private lateinit var chatRoomsViewModel: ChatRoomsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView(){
        pagerAdapter = ChatRoomsPagerAdapter(childFragmentManager)
        chatRoomsViewModel.loadChatRoomCategories()
    }

    private fun renderChatRoomCategories(categories: List<ChatRoomCategory>?) {
        if(categories.isNullOrEmpty()) return

        pagerAdapter.categories = categories
        chatroomsPager.adapter = pagerAdapter
        chatroomsTab.setupWithViewPager(chatroomsPager)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //NOTE: must init ViewModel in or after onCreateView and before DestroyView
        //      otherwise the lifecycleOwner is not ready to init ViewModel
        chatRoomsViewModel = viewModel(viewModelFactory) {
            observe(chatRoomCategoriesLiveData, ::renderChatRoomCategories)
            failure(failureLiveData, ::handleFailure)
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

}

class ChatRoomsPagerAdapter(fm: FragmentManager) : BasePagerAdapter(fm) {

    internal var categories: List<ChatRoomCategory>? = null

    override fun pageTitles() =
        categories?.map{it.title} ?: listOf(ChatRoomCategory.DefaultCategory.title)

    override fun getItem(position: Int) =
        ChatRoomsListFragment(categories?.get(position) ?: ChatRoomCategory.DefaultCategory)

}

package com.qint.pt1.features.chatrooms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.qint.pt1.R
import com.qint.pt1.base.exception.Failure
import com.qint.pt1.base.extension.*
import com.qint.pt1.base.navigation.Navigator
import com.qint.pt1.base.platform.BaseFragment
import com.qint.pt1.domain.ChatRoomCategory
import kotlinx.android.synthetic.main.chatrooms_list_fragment.*
import kotlinx.android.synthetic.main.common_empty.*
import javax.inject.Inject

class ChatRoomsListFragment(val category: ChatRoomCategory) : BaseFragment() {

    //Note: 为防止屏幕方向旋转后由于fragment没有默认构造函数崩溃，目前设定了屏幕禁止旋转后应该不需要了，但保险起见仍然保留
    constructor(): this(ChatRoomCategory.DefaultCategory)

    override fun layoutId() = R.layout.chatrooms_list_fragment

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var chatRoomsListAdapter: ChatRoomsListAdapter

    private lateinit var chatRoomsListViewModel: ChatRoomsListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        chatRoomsListViewModel = viewModel(viewModelFactory) {
            observe(chatRoomsLiveData, ::renderChatRoomsList)
            failure(failureLiveData, ::handleFailure)
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        loadChatRoomList()
    }

    private fun initializeView() {
        chatroomList.layoutManager = GridLayoutManager(context, 2)
        chatroomList.adapter = chatRoomsListAdapter
        chatRoomsListAdapter.clickListener = { chatRoom ->
            navigator.joinChatRoom(activity!!, chatRoom)
        }

        swiperefresh.setOnRefreshListener {
            loadChatRoomList()
        }

    }

    private fun loadChatRoomList() {
        emptyView.invisible()
        chatroomList.visible()
        chatRoomsListViewModel.loadChatRooms(category.id)
    }

    private fun renderChatRoomsList(chatRooms: List<ChatRoomsListViewItem>?) {
        chatRoomsListAdapter.collection = chatRooms.orEmpty()
        swiperefresh.isRefreshing = false
    }

    override fun renderFailure(failure: Failure) {
        chatroomList.invisible()
        emptyView.visible()
        super.renderFailure(failure)
    }
}

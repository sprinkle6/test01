package com.qint.pt1.features.chatrooms

import androidx.paging.LivePagedListBuilder
import com.qint.pt1.base.exception.Failure
import com.qint.pt1.base.exception.FailureHandler
import com.qint.pt1.base.extension.getDefaultPagedListConfig
import com.qint.pt1.base.platform.BaseViewModel
import com.qint.pt1.domain.ChatRoomCategory
import com.qint.pt1.domain.ID
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject

class ChatRoomsListViewModel
@Inject constructor(private val chatRoomsListDataSourceFactory: ChatRoomsListDataSourceFactory,
                    private val chatRoomsRepository: ChatRoomsRepository) : BaseViewModel() {
    private val executor: Executor = Executors.newFixedThreadPool(3)

    private val pageListConfig = getDefaultPagedListConfig()

    init{
        //必须在初始化LivePagedListBuilder之前设置DataSourceFactory的failureHandler
        chatRoomsListDataSourceFactory.failureHandler = ChatRoomsListFailureHandler()
    }

    val chatRoomsLiveData = LivePagedListBuilder(chatRoomsListDataSourceFactory, pageListConfig)
        .setFetchExecutor(executor).build()

    fun loadChatRooms(categoryId: ID = ChatRoomCategory.DefaultCategory.id) {
        chatRoomsListDataSourceFactory.categoryId = categoryId
        chatRoomsLiveData.value?.dataSource?.invalidate()
    }

    /*
     * 用于传入DataSource中执行Failure处理
     */
    inner class ChatRoomsListFailureHandler: FailureHandler.NoopFailureHandler(){
        override fun handleFailure(failure: Failure?) {
            trigerFailure(failure ?: return)
        }
    }
}
package com.qint.pt1.features.chatroom

import android.content.Context
import com.qint.pt1.base.platform.BaseFragment
import com.qint.pt1.base.platform.BaseFragmentActivity
import com.qint.pt1.features.chatrooms.ChatRoomsListViewItem
import org.jetbrains.anko.intentFor

//FIXME：长时间发呆掉线（如锁屏等），恢复界面后应能自动重连到信令服务器
class ChatRoomActivity : BaseFragmentActivity() {
    override fun fragment(): BaseFragment = ChatRoomFragment()

    companion object {
        //FIXME: 传入参数的类型由ChatRoomListViewItem改为ChatRoom，去耦合
        fun callingIntent(context: Context, chatRoom: ChatRoomsListViewItem) =
            context.intentFor<ChatRoomActivity>("chatRoom" to chatRoom)
    }

}

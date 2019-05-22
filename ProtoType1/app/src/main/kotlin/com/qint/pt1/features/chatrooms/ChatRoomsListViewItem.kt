package com.qint.pt1.features.chatrooms

import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import com.qint.pt1.domain.*
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChatRoomsListViewItem(val id: ChatRoomId,
                                 val title: ChatRoomTitle,
                                 val category: Tag,
                                 val memberNum: Int,
                                 val operator: UserName,
                                 val picture: String,
                                 val operatorAvatar: Avatar,
                                 val externalRoomId: ID) : Parcelable{
    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ChatRoomsListViewItem> = object:
            DiffUtil.ItemCallback<ChatRoomsListViewItem>() {
            override fun areItemsTheSame(oldItem: ChatRoomsListViewItem, newItem: ChatRoomsListViewItem): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ChatRoomsListViewItem, newItem: ChatRoomsListViewItem): Boolean =
                oldItem == newItem
        }
    }
}

fun ChatRoom.toChatRoomsListViewItem() =
    ChatRoomsListViewItem(id, title, category, memberNum, owner, backgroundPicture, ownerAvatar, externalRoomId)
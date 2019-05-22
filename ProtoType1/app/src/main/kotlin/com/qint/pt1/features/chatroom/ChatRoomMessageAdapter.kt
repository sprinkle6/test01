package com.qint.pt1.features.chatroom

import android.text.Html
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.qint.pt1.R
import com.qint.pt1.base.extension.gone
import com.qint.pt1.base.extension.inflate
import com.qint.pt1.base.extension.loadFromUrl
import com.qint.pt1.base.extension.visible
import com.qint.pt1.base.navigation.Navigator
import com.qint.pt1.domain.SeatState
import kotlinx.android.synthetic.main.chatroom_message_item.view.*
import kotlin.properties.Delegates

typealias MessageItem = ChatRoomMessage

class ChatRoomMessageAdapter : RecyclerView.Adapter<ChatRoomMessageAdapter.MessageViewHolder>() {

    internal var collection: List<MessageItem> by Delegates.observable(emptyList()) { _, _, _ ->
        notifyDataSetChanged()
    }

    private var itemClickListener: (MessageItem, Navigator.Extras) -> Unit = { _, _ -> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            MessageViewHolder(parent.inflate(R.layout.chatroom_message_item))

    override fun onBindViewHolder(viewHolder: MessageViewHolder, position: Int) =
            viewHolder.bind(collection[position], itemClickListener)

    override fun getItemCount() = collection.size

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val helper = UserTagDisplayHelper(itemView.context)

        fun bind(messageItem: MessageItem, itemClickListener: (MessageItem, Navigator.Extras) -> Unit) {
            when(messageItem){
                is ChatMessage -> {
                    //TODO: 实现昵称颜色半透明效果
                    itemView.icon.gone()
                    itemView.message.text = helper.getTag(messageItem).append(messageItem.text)
                }
                is StickerMessage -> {
                    val sticker = messageItem.sticker
                    if (sticker != null) {
                        itemView.message.text = helper.getTag(messageItem)
                        itemView.icon.visible()
                        itemView.icon.loadFromUrl(sticker.icon)
                    } else {
                        itemView.icon.gone()
                    }
                }
                is WelcomeNotification -> {
                    itemView.icon.gone()
                    val userInfo = messageItem.newChatRoomMember
                    val tag = helper.getTag(userInfo, false, false)
                    itemView.message.text =
                        tag.append(Html.fromHtml("<font color='${UserTagDisplayHelper.MESSAGE_HIGHLIGHT_COLOR}'>${userInfo.nickName}</font>来了"))
                }
                is InformationNotification -> {
                    itemView.icon.gone()
                    itemView.message.text = messageItem.info
                }
                is DonateNotification -> {
                    val from = messageItem.from
                    val donate = messageItem.donate
                    val tag = helper.getTag(from, false, false)
                    val gift = donate.gift
                    if(gift != null){
                        itemView.message.text =
                            tag.append(Html.fromHtml("${from.nickName}<font color='${UserTagDisplayHelper.MESSAGE_HIGHLIGHT_COLOR}'>打赏</font>${donate.toUserName} <font color='${UserTagDisplayHelper.MESSAGE_HIGHLIGHT_COLOR}'>X${donate.count}</font>"))
                        itemView.icon.loadFromUrl(gift.icon)
                        itemView.icon.visible()
                    }else {
                        itemView.icon.gone()
                        itemView.message.text =
                            tag.append(Html.fromHtml("${from.nickName}<font color='${UserTagDisplayHelper.MESSAGE_HIGHLIGHT_COLOR}'>打赏</font>${donate.toUserName} ${donate.giftId} X ${donate.count}"))
                    }
                }
                is SeatChangeNotification -> {
                    itemView.icon.gone()
                    val seat = messageItem.seat
                    when(seat.state){
                        SeatState.OCCUPIED ->{
                            val userInfo = messageItem.seat.userInfo
                            val tag = helper.getTag(userInfo, false, true, false)
                            itemView.message.text = tag.append(Html.fromHtml("<font color='${UserTagDisplayHelper.MESSAGE_HIGHLIGHT_COLOR}'>上了${seat.seatName}</font>"))
                        }
                        SeatState.OPEN -> {
                            val userInfo = messageItem.original?.userInfo ?: return
                            val tag = helper.getTag(userInfo, false, true, false)
                            itemView.message.text = tag.append(Html.fromHtml("<font color='${UserTagDisplayHelper.MESSAGE_HIGHLIGHT_COLOR}'>下了${seat.seatName}</font>"))
                        }
                    }
                }
                else -> itemView.message.text = "Debug: ${messageItem}" //FIXME: 属于不需要在这里显示的类型，不应添加到adapter的collection中
            }
        }

    }
}


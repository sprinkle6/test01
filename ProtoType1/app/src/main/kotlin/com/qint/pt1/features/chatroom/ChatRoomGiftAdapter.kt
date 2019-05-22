package com.qint.pt1.features.chatroom

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.qint.pt1.R
import com.qint.pt1.base.extension.inflate
import com.qint.pt1.base.extension.loadFromUrl
import kotlinx.android.synthetic.main.chatroom_gift_item.view.*
import kotlin.properties.Delegates

class ChatRoomGiftAdapter : RecyclerView.Adapter<ChatRoomGiftAdapter.GiftViewHolder>() {
    internal var collection: List<GiftItem> by Delegates.observable(emptyList()) { _, _, _ ->
        notifyDataSetChanged()
    }

    internal var itemClickListener: (GiftItem) -> Unit = { _ -> }

    var selectedPosition = -1
    var selectedView: View? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GiftViewHolder =
            GiftViewHolder(parent.inflate(R.layout.chatroom_gift_item))

    override fun getItemCount(): Int = collection.size

    override fun onBindViewHolder(viewHolder: GiftViewHolder, position: Int) =
            viewHolder.bind(collection[position], position, itemClickListener)

    fun getSelectedItem(): GiftItem? =
        if (selectedPosition == -1) null else collection.get(selectedPosition)

    inner class GiftViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(giftItem: GiftItem, position: Int, itemClickListener: (GiftItem) -> Unit) {
            itemView.title.text = giftItem.title
            itemView.price.setText(giftItem.priceInDiamond)
            itemView.icon.loadFromUrl(giftItem.icon)
            itemView.setOnClickListener {
                if(position == selectedPosition){ //对已选中的item，再次点击取消选中
                    selectedPosition = -1
                    selectedView = null
                    unCheckItem()
                }else{
                    val selectedItemView = selectedView
                    if(selectedItemView != null){ //清除之前选中的item的选择背景状态显示
                        unCheckItem(selectedItemView)
                    }
                    selectedPosition = position
                    checkItem()
                }
                itemClickListener(giftItem)
            }
            if(position == selectedPosition) checkItem() else unCheckItem()
        }

        private fun checkItem() {
            itemView.setBackgroundResource(R.drawable.rounded_rectangle_a20_blue_r14)
            selectedView = itemView
        }

        private fun unCheckItem() = unCheckItem(itemView)

        private fun unCheckItem(view: View) {
            view.setBackgroundColor(view.resources.getColor(R.color.transparent))
        }
    }

}

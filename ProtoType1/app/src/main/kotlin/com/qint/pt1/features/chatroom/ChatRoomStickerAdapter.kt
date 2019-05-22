package com.qint.pt1.features.chatroom

import android.view.View
import android.view.ViewGroup
import com.qint.pt1.R
import com.qint.pt1.base.extension.inflate
import com.qint.pt1.base.extension.loadFromUrl
import kotlinx.android.synthetic.main.chatroom_sticker_item.view.*
import kotlin.properties.Delegates

class ChatRoomStickerAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<ChatRoomStickerAdapter.StickerViewHolder>() {
    internal var collection: List<StickerItem> by Delegates.observable(emptyList()) { _, _, _ ->
        notifyDataSetChanged()
    }

    internal var itemClickListener: (StickerItem) -> Unit = { _ -> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            StickerViewHolder(parent.inflate(R.layout.chatroom_sticker_item))

    override fun onBindViewHolder(viewHolder: StickerViewHolder, position: Int) =
            viewHolder.bind(collection[position], itemClickListener)

    override fun getItemCount() = collection.size

    class StickerViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        //TODO: 实现表情选择面板的元素宽度、间距及面板边距自适应
        fun bind(stickerItem: StickerItem, itemClickListener: (StickerItem) -> Unit) {
            itemView.icon.loadFromUrl(stickerItem.icon)
            itemView.setOnClickListener { itemClickListener(stickerItem) }
        }
    }

}

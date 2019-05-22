package com.qint.pt1.features.chatrooms

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.qint.pt1.R
import com.qint.pt1.base.extension.dp2px
import com.qint.pt1.base.extension.inflate
import com.qint.pt1.base.extension.loadFromUrl
import com.qint.pt1.util.getScreenWidthInPixel
import kotlinx.android.synthetic.main.chatrooms_list_item.view.*
import javax.inject.Inject
import kotlin.properties.Delegates

class ChatRoomsListAdapter
@Inject constructor(private val context: Context) :
    PagedListAdapter<ChatRoomsListViewItem, ChatRoomsListAdapter.ViewHolder>(ChatRoomsListViewItem.DIFF_CALLBACK){

    internal var collection: List<ChatRoomsListViewItem> by Delegates.observable(emptyList()) { _, _, _ ->
        notifyDataSetChanged()
    }

    internal var clickListener: (ChatRoomsListViewItem) -> Unit = { _ -> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(parent.inflate(R.layout.chatrooms_list_item))

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(collection[position], clickListener)
    }

    override fun getItemCount(): Int = collection.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        init {
            setViewSize()
        }

        fun bind(viewItem: ChatRoomsListViewItem, clickListener: (ChatRoomsListViewItem) -> Unit) {
            mView.roomPicture.loadFromUrl(viewItem.picture)
            mView.chatRoomTitle.text = viewItem.title
            mView.roomType.text = viewItem.category
            mView.memberNum.text = viewItem.memberNum.toString()
            mView.setOnClickListener { clickListener(viewItem) }
        }

        /*
         * 根据屏幕宽度计算图片的显示宽度，并根据图片宽度和高宽比计算显示高度
         */
        private fun setViewSize(){
            val aspectRatio = 1.1
            val marginsInDP = 16 + 9 + 16 //FIXME: 左中右间距，如果布局文件中有调整需要同步调整，最好也能动态获取自动同步
            val width: Int = ((getScreenWidthInPixel() - context.dp2px(marginsInDP))/2).toInt()
            val height: Int = (width / aspectRatio).toInt()
            mView.roomPicture.layoutParams.width = width
            mView.roomPicture.layoutParams.height = height
        }
    }
}

/*
 * Author: Matthew Zhang
 * Created on: 5/14/19 3:32 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.features.chatroom

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.qint.pt1.R
import com.qint.pt1.features.chatroom.widgets.DonateItemLayout
import com.qint.pt1.features.chatroom.widgets.DonateItemLayoutListener

class ChatRoomDonatesAdapter(context: Context, val itemDisplayCount: Int = 3): ArrayAdapter<DonateNotification>(context, R.layout.chatroom_donate_item){ //FIXME: 无用的layout参数

    val itemViews: MutableList<DonateItemLayout> = mutableListOf()
    val itemQueue: MutableList<DonateNotification> = mutableListOf() //暂存所有待显示还未显示的item

    private fun createItemView(item: DonateNotification): DonateItemLayout{
        val itemView = DonateItemLayout(context)
        itemView.setData(item)
        itemView.listener = ItemListener()
        return itemView
    }

    private inner class ItemListener: DonateItemLayoutListener{
        override fun onAnimationEnd(item: DonateNotification) {
            remove(item)
        }
    }

    fun updateCount(position: Int){
        if(position >= count) return
        itemViews.get(position)?.updateCount()
    }

    override fun add(item: DonateNotification?) {
        item ?: return
        val itemCopy = item.copy(donate = item.donate.copy()) //拷贝一份防止合并计数时干扰消息列表中的显示
        var merged = false
        for(i in itemQueue.indices){ //检查是否可与已有的条目合并
            val oldItem = itemQueue[i]
            if(oldItem.canMerge(itemCopy)){
                oldItem.merge(itemCopy)
                merged = true
                break
            }
        }
        if(!merged) {
            itemQueue.add(itemCopy)
        }
        fillDisplay()
    }

    override fun addAll(collection: MutableCollection<out DonateNotification>) {
        if(collection.isNotEmpty()) collection.forEach { this.add(it) }
    }

    private fun fillDisplay() {
        //先检查待显示队列中有无可以与当前显示中条目合并的
        if (!isEmpty) {
            for (i in 0..count - 1) {
                val displayingItem = getItem(i)
                for(j in itemQueue.indices){
                    val queueItem = itemQueue[j]
                    if(displayingItem.canMerge(queueItem)){
                        displayingItem.merge(queueItem)
                        updateCount(i)
                        itemQueue.removeAt(j)
                        break
                    }
                }
            }
        }

        //从待显示队列中提取条目填充到当前显示条目
        while(count < this.itemDisplayCount && itemQueue.isNotEmpty()){ //填充待显示条目
            val item = itemQueue.get(0)
            super.add(item)
            itemQueue.removeAt(0)
            val itemView = createItemView(item)
            itemViews.add(itemView)
        }
    }

    override fun clear(){
        super.clear()
        itemViews.clear()
    }

    override fun remove(item: DonateNotification?) {
        for(i in 0..count-1){
            if(item == getItem(i)){
                itemViews.removeAt(i)
                break
            }
        }
        super.remove(item)
        fillDisplay()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = itemViews.get(position)
        if(itemView.state == DonateItemLayout.DisplayState.Default) {
            itemView.startNotificationInAnimation()
        }
        return itemView
    }

}

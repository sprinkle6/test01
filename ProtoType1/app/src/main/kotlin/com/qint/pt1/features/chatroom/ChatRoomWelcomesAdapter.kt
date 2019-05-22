/*
 * Author: Matthew Zhang
 * Created on: 5/10/19 5:11 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.features.chatroom

import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.qint.pt1.R
import com.qint.pt1.base.extension.gone
import com.qint.pt1.base.extension.loadFromUrl
import com.qint.pt1.base.extension.visible
import kotlinx.android.synthetic.main.chatroom_welcome_item.view.*

class ChatRoomWelcomesAdapter(context: Context): ArrayAdapter<WelcomeNotification>(context, R.layout.chatroom_welcome_item) { //FIXME: 去除无用的layout参数
    private val helper = UserTagDisplayHelper(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView = inflater.inflate(R.layout.chatroom_welcome_item, parent, false)
        val notification = getItem(position)
        val userInfo = notification.newChatRoomMember

        val tag = helper.getTag(userInfo, false, false)
        if(userInfo.nobleLevel.notEmpty) {
            itemView.avatar.loadFromUrl(userInfo.avatar)
            itemView.avatar.visible()
            tag.append(Html.fromHtml("<font color='#FFFFFF'>${userInfo.nickName}</font><font color='${UserTagDisplayHelper.MESSAGE_DIM_COLOR}'>来了</font>"))
        }else{
            itemView.avatar.gone()
            tag.append(Html.fromHtml("<font color='${UserTagDisplayHelper.MESSAGE_HIGHLIGHT_COLOR}'>${userInfo.nickName}</font><font color='${UserTagDisplayHelper.MESSAGE_DIM_COLOR}'>来了</font>"))
        }

        itemView.message.text = tag

        return itemView
    }
}
/*
 * Author: Matthew Zhang
 * Created on: 4/16/19 4:32 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.features.search

import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.qint.pt1.R
import com.qint.pt1.base.extension.inflate
import com.qint.pt1.base.extension.invisible
import com.qint.pt1.base.extension.loadFromUrl
import com.qint.pt1.base.extension.visible
import com.qint.pt1.domain.Gender
import kotlinx.android.synthetic.main.search_result_user_item.view.*
import javax.inject.Inject

class SearchResultAdapter
@Inject constructor() :
    PagedListAdapter<SearchResultUserItem, SearchResultAdapter.ViewHolder>(SearchResultUserItem.DIFF_CALLBACK){

    internal var itemClickListener: (SearchResultUserItem) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(parent.inflate(R.layout.search_result_user_item))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = getItem(position)
        if(user != null) {
            holder.bind(user, itemClickListener)
        }else{
            holder.clear()
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun bind(userItem: SearchResultUserItem, clickListener: (SearchResultUserItem) -> Unit){
            itemView.userAvatar.loadFromUrl(userItem.avatar)
            itemView.nickName.text = userItem.nickName

            //设置性别年龄
            //FIXME: 将性别年龄抽取控件
            val genderAge = "${userItem.gender.toIconString()}${userItem.age}"
            when (userItem.gender) {
                Gender.MALE -> {
                    itemView.genderAgeMale.text = genderAge
                    itemView.genderAgeMale.visible()
                    itemView.genderAgeFamale.invisible()
                }
                Gender.FAMALE -> {
                    itemView.genderAgeFamale.text = genderAge
                    itemView.genderAgeFamale.visible()
                    itemView.genderAgeMale.invisible()
                }
                Gender.UNKNOWN -> {
                    itemView.genderAgeMale.text = genderAge
                    itemView.genderAgeFamale.invisible()
                    itemView.genderAgeMale.visible()
                }
            }

            itemView.userIdLabel.text = "ID: ${userItem.id}"

            itemView.setOnClickListener {
                clickListener(userItem)
            }
        }

        fun clear(){

        }
    }

}
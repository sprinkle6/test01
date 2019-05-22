/*
 * Author: Matthew Zhang
 * Created on: 4/16/19 3:32 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.features.search

import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import com.qint.pt1.domain.*
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SearchResultUserItem(
    val id: UserId,
    val nickName: NickName,
    val gender: Gender,
    val age: Age,
    val avatar: Avatar): Parcelable{

    companion object{
        fun fromUser(user: User): SearchResultUserItem = with(user){
            SearchResultUserItem(id, profile.nickName, profile.gender, profile.age, profile.avatar)
        }

        val DIFF_CALLBACK: DiffUtil.ItemCallback<SearchResultUserItem> = object:
            DiffUtil.ItemCallback<SearchResultUserItem>() {
            override fun areItemsTheSame(oldItem: SearchResultUserItem, newItem: SearchResultUserItem): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: SearchResultUserItem, newItem: SearchResultUserItem): Boolean =
                oldItem == newItem
        }

    }
}
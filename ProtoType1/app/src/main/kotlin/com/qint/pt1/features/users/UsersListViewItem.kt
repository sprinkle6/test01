package com.qint.pt1.features.users

import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import com.qint.pt1.domain.*
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UsersListViewItem(val id: UserId,
                             val nickName: NickName,
                             val avatar: Avatar,
                             val status: UserStatus,
                             val location: String,
                             val declaration: String,
                             val gender: Gender,
                             val age: Age,
                             val nobleLevel: NobleLevel,
                             var vipLevel: VIPLevel,
                             val audio: AudioResource,
                             val lastAlive: LastAlive,
                             val skillTag: String,
                             val skillCategory: SkillCategory
) : Parcelable {
    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<UsersListViewItem> = object:
            DiffUtil.ItemCallback<UsersListViewItem>() {
            override fun areItemsTheSame(oldItem: UsersListViewItem, newItem: UsersListViewItem): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: UsersListViewItem, newItem: UsersListViewItem): Boolean =
                oldItem == newItem
        }
    }
}

fun User.toUsersViewItem(): UsersListViewItem =
    UsersListViewItem(
        id,
        profile.nickName,
        profile.avatar,
        info.status,
        profile.location.toString(),
        profile.declaration,
        profile.gender,
        profile.age,
        info.nobleLevel,
        info.vipLevel,
        profile.promotionAudio,
        info.lastVisit,
        profile.primarySkillTag,
        profile.primarySkillCategory
    )
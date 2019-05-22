/*
 * Author: Matthew Zhang
 * Created on: 5/10/19 5:30 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.features.chatroom

import android.content.Context
import android.graphics.Paint
import android.text.SpannableStringBuilder
import android.text.Spanned
import com.qint.pt1.R
import com.qint.pt1.base.widgets.RoundedBackgroundSpan
import com.qint.pt1.domain.ChatRoomUserInfo
import com.qint.pt1.domain.NobleLevel
import javax.inject.Inject

class UserTagDisplayHelper @Inject constructor(val context: Context) {
    companion object{
        const val MESSAGE_HIGHLIGHT_COLOR = "#FDB42B"
        const val MESSAGE_DIM_COLOR = "#80AFBA" //FIXME: 实际应该是50%的alpha
    }

    fun getTag(messageItem: ChatRoomUserMessage) =
        getTag(messageItem.sender, messageItem.isSendFromChatRoomHost)

    fun getTag(
        userInfo: ChatRoomUserInfo,
        tagHost: Boolean = false,
        showName: Boolean = true,
        showNameSeperator: Boolean = true
    ): SpannableStringBuilder {
        var start = 0

        val noble = userInfo.nobleLevel
        val nobleTitle =
            if (noble.notEmpty) { //FIXME: 礼物消息里没有爵位的title，先特殊处理一下，应从系统预加载的爵位表中查询爵位title
                if (noble.description.isNullOrBlank()) "N${noble.level}" else noble.description
            } else ""

        val ssBuilder = SpannableStringBuilder()

        if (userInfo.nobleLevel.notEmpty) {
            val nobleTagText = "${nobleTitle}"
            ssBuilder.append(nobleTagText)
            val nobleSpan = RoundedBackgroundSpan(
                context, 7,
                context.resources.getColor(getColorByNobleLevel(noble)),
                context.resources.getColor(R.color.main_white_f4), 10,
                Paint.Style.FILL, R.drawable.crown_in_hexagon, 14
            )
            ssBuilder.setSpan(nobleSpan, 0, nobleTagText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            start += nobleTagText.length
        }

        if (userInfo.vipLevel.notEmpty) {
            val vipTagText = "V${userInfo.vipLevel.level}"
            val vipSpan = RoundedBackgroundSpan(
                context, 7,
                context.resources.getColor(R.color.main_yellow_f8),
                context.resources.getColor(R.color.main_yellow_f8), 10,
                Paint.Style.STROKE
            )
            ssBuilder.append(vipTagText)
            ssBuilder.setSpan(
                vipSpan,
                start,
                start + vipTagText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            start += vipTagText.length
        }

        if (tagHost) {
            val hostTagText = " 主播 "
            val hostSpan = RoundedBackgroundSpan(
                context, 7,
                context.resources.getColor(R.color.main_blue_c1),
                context.resources.getColor(R.color.main_blue_c1), 10,
                Paint.Style.STROKE
            )
            ssBuilder.append("${hostTagText}")
            ssBuilder.setSpan(
                hostSpan,
                start,
                start + hostTagText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            start += hostTagText.length
        }

        if(showName) {
            ssBuilder.append("${userInfo.nickName}")
            if (showNameSeperator) ssBuilder.append("：")
        }
        return ssBuilder
    }

    fun getColorByNobleLevel(nobleLevel: NobleLevel): Int = when (nobleLevel.level) {
        1, 2 -> R.color.main_blue_light
        3, 4, 5 -> R.color.main_blue_c1
        6, 7, 8 -> R.color.main_purple
        9, 10 -> R.color.main_yellow_f8
        else -> R.color.main_black_f1
    }
}
package com.qint.pt1.domain

data class Sticker(val id: StickerId, val title: String, val icon: ImageUrl){
    companion object{
        const val TAG_START = '['
        const val TAG_END = ']'

        fun isVaildStickerTag(text: String) = text.startsWith(TAG_START) && text.endsWith(TAG_END) && text.length > 2
    }
    val tag = "${TAG_START}${title}${TAG_END}"
}

fun Sticker.toIcon() = Icon(id, icon, title)

typealias StickerId = String
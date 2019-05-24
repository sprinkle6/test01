/*
 * Author: Matthew Zhang
 * Created on: 5/22/19 12:16 AM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.features.chatroom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.qint.pt1.R
import com.qint.pt1.base.navigation.Navigator
import com.qint.pt1.features.login.Login
import kotlinx.android.synthetic.main.chatroom_sticker_panel.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChatRoomStickerPanelFragment(val chatRoomViewModel: ChatRoomViewModel): BottomSheetDialogFragment() {
    private lateinit var stickerAdapter: ChatRoomStickerAdapter

    @Inject lateinit var login: Login
    @Inject lateinit var navigator: Navigator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.chatroom_sticker_panel, container, false)

        stickerPanel.layoutManager = GridLayoutManager(context, 5)
        stickerAdapter = ChatRoomStickerAdapter()
        stickerPanel.adapter = stickerAdapter
        stickerAdapter.itemClickListener = { stickerItem ->
            if(!login.isLogined) navigator.showLogin(context!!)
            clickedStickerItem(stickerItem)
        }

        return view
    }

    private fun clickedStickerItem(stickerItem: StickerItem) {
        dismiss()
        chatRoomViewModel.sendSticker(stickerItem)
    }

    fun renderStickerList(stickers: List<StickerItem>?) {
        if (stickers.isNullOrEmpty()) return
        CoroutineScope(Dispatchers.Main).launch {
            stickerAdapter.collection = stickers
        }
    }
}
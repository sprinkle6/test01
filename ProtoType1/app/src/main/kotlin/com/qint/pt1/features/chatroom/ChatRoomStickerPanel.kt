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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.qint.pt1.R

class ChatRoomStickerPanel: BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.chatroom_sticker_panel, container, false)
    }
}
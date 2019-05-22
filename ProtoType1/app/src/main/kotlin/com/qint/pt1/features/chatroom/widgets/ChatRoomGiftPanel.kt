/*
 * Author: Matthew Zhang
 * Created on: 5/17/19 12:00 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.features.chatroom.widgets

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.GridLayoutManager
import com.qint.pt1.R
import com.qint.pt1.api.sys.MetaData
import com.qint.pt1.base.extension.failure
import com.qint.pt1.base.extension.loadFromUrl
import com.qint.pt1.base.extension.observe
import com.qint.pt1.base.navigation.Navigator
import com.qint.pt1.base.platform.BaseFragment
import com.qint.pt1.domain.ChatRoomUserInfo
import com.qint.pt1.domain.Seat
import com.qint.pt1.domain.SeatState
import com.qint.pt1.features.chatroom.ChatRoomGiftAdapter
import com.qint.pt1.features.chatroom.ChatRoomViewModel
import com.qint.pt1.features.chatroom.GiftItem
import com.qint.pt1.features.login.Login
import kotlinx.android.synthetic.main.chatroom_donate_targetuser_item.view.*
import kotlinx.android.synthetic.main.chatroom_gift_panel.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import q.rorbin.badgeview.Badge
import q.rorbin.badgeview.QBadgeView
import javax.inject.Inject

class ChatRoomGiftPanelFragment: BaseFragment(){
    override fun layoutId() = R.layout.chatroom_gift_panel

    private lateinit var chatRoomViewModel: ChatRoomViewModel
    private lateinit var giftAdapter: ChatRoomGiftAdapter

    @Inject lateinit var login: Login
    @Inject lateinit var navigator: Navigator
    @Inject lateinit var metaData: MetaData

    private val candidateUserStatus: MutableList<CandidateUserStatus> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        chatRoomViewModel = viewModel {
            observe(giftsLiveData, ::renderGiftList)
            observe(seatsInfoLiveData, ::updateCandidates)
            observe(seatChangeLiveData, ::updateCandidate)
            observe(donateTargetUsersLiveData, ::renderTargetUsers)
            failure(failureLiveData, ::handleFailure)
        }

        initView()

        super.onViewCreated(view, savedInstanceState)
    }

    private fun initView(){
        initTargetUsers()
        initGiftList()
        initCount()
        initSendButton()
    }

    private fun initTargetUsers() {
    }

    private fun initGiftList() {
        giftPanel.layoutManager = GridLayoutManager(baseActivity, 4)
        giftAdapter = ChatRoomGiftAdapter()
        giftPanel.adapter = giftAdapter
        giftAdapter.itemClickListener = { giftItem ->
            if(!login.isLogined) navigator.showLogin(baseActivity)
        }
    }

    private fun initCount() {
        val countCandidates = metaData.donateCountCandidates
        val countsAdapter = ArrayAdapter<Int>(context, R.layout.spinner_item_selected, countCandidates)
        countsAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        donateCountSelection.adapter = countsAdapter
    }

    private fun initSendButton() {
        sendDonateButton.setOnClickListener {
            val giftItem = giftAdapter.getSelectedItem()
            val count = donateCountSelection.selectedItem as Int
            if(giftItem != null) {
                val targetUsers = candidateUserStatus.filter { it.selected }.map { it.userInfo }
                chatRoomViewModel.sendGift(giftItem, count, targetUsers)
                chatRoomViewModel.hideDonatePanel()
            }
        }
    }

    private fun updateCandidates(seats: MutableMap<Int, Seat>?) = redrawAllCandidates()

    private fun updateCandidate(seat: Seat?) = redrawAllCandidates()

    private fun redrawAllCandidates(){
        candidateUserList.removeAllViews()
        candidateUserStatus.clear()
        val seats = chatRoomViewModel.seatsInfoLiveData.value?.values?.toList() ?: return
        candidateUserStatus.addAll(seats.filter { it.state == SeatState.OCCUPIED }.map { CandidateUserStatus(it.userInfo, true) })
        val inflater = baseActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        candidateUserStatus.forEach {candidate ->
            val itemView = inflater.inflate(R.layout.chatroom_donate_targetuser_item, null, false)
            itemView.avatar.loadFromUrl(candidate.userInfo.avatar)
            candidateUserList.addView(itemView)
            val checkBadge = QBadgeView(baseActivity).bindTarget(itemView)
                .setBadgeBackground(resources.getDrawable(R.drawable.check, null))
                .setGravityOffset(0.5f, -0.5f, true)
                .setBadgeNumber(-1) //以便背景图标能够显示出来
            itemView.setOnClickListener {
                candidate.selected = !candidate.selected
                if (login.isMe(candidate.userInfo.userId)) candidate.selected = false //不能给自己送礼物
                displayCheck(checkBadge, candidate.selected)
            }
            candidate.selected = chatRoomViewModel.donateTargetUsersLiveData.value?.filter {
                it.userId == candidate.userInfo.userId && !login.isMe(candidate.userInfo.userId) //不能给自己送礼物
            }?.isNotEmpty() ?: false
            displayCheck(checkBadge, candidate.selected)
        }
    }

    private fun renderTargetUsers(targetUsers: List<ChatRoomUserInfo>?) = redrawAllCandidates()

    private fun displayCheck(badge: Badge, checked: Boolean) {
        if (checked) badge.setBadgeNumber(-1) else badge.hide(false)
    }

    private fun renderGiftList(gifts: List<GiftItem>?) {
        if (gifts.isNullOrEmpty()) return
        CoroutineScope(Dispatchers.Main).launch {
            giftAdapter.collection = gifts
        }
    }

}

private data class CandidateUserStatus(val userInfo: ChatRoomUserInfo, var selected: Boolean = false)
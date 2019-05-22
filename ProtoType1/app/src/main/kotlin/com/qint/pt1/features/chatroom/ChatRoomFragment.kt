/*
 * Author: Matthew Zhang
 * Created on: 4/30/19 9:31 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.features.chatroom

import android.Manifest
import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.gridlayout.widget.GridLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.april21dev.multipulseanimation.MultiPulseLayout
import com.qint.pt1.R
import com.qint.pt1.api.sys.MetaData
import com.qint.pt1.base.extension.*
import com.qint.pt1.base.navigation.Navigator
import com.qint.pt1.base.platform.BaseFragment
import com.qint.pt1.base.platform.BasePagerAdapter
import com.qint.pt1.base.widgets.RoundedBackgroundSpan
import com.qint.pt1.domain.*
import com.qint.pt1.features.chatroom.widgets.ChatRoomGiftPanelFragment
import com.qint.pt1.features.chatroom.widgets.ChatRoomPackagePanelFragment
import com.qint.pt1.features.chatrooms.ChatRoomFailure
import com.qint.pt1.features.chatrooms.ChatRoomsListViewItem
import com.qint.pt1.features.login.Login
import com.qint.pt1.support.agora.RtcVoiceEngine
import com.qint.pt1.util.LOG_TAG
import com.tbruyelle.rxpermissions2.RxPermissions
import io.agora.rtc.RtcEngine
import kotlinx.android.synthetic.main.chatroom_announce_panel.*
import kotlinx.android.synthetic.main.chatroom_donate_panel.*
import kotlinx.android.synthetic.main.chatroom_fragment.*
import kotlinx.android.synthetic.main.chatroom_fragment.donatePanel
import kotlinx.android.synthetic.main.chatroom_seat.view.*
import kotlinx.android.synthetic.main.chatroom_sticker_panel.*
import kotlinx.android.synthetic.main.chatroom_userinfo_panel.*
import kotlinx.android.synthetic.main.common_gender_age_tag.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.support.v4.longToast
import org.jetbrains.anko.support.v4.toast
import javax.inject.Inject

class ChatRoomFragment : BaseFragment() {
    //TODO: 拆分为几个组件模块，避免此文件体积过大
    override fun layoutId() = R.layout.chatroom_fragment

    private val ANIMATION_LAST_TIME_IN_MS: Long = 2000

    private lateinit var chatRoomViewModel: ChatRoomViewModel
    private lateinit var messageListAdapter: ChatRoomMessageAdapter
    private lateinit var stickerAdapter: ChatRoomStickerAdapter

    private lateinit var welcomeAdapter: ChatRoomWelcomesAdapter
    private lateinit var donateAdapter: ChatRoomDonatesAdapter
    private lateinit var donatePanelPagerAdapter: ChatRoomDonatePanelPagerAdapter

    @Inject internal lateinit var navigator: Navigator
    @Inject internal lateinit var login: Login
    @Inject internal lateinit var userTagDisplayHelper: UserTagDisplayHelper
    @Inject internal lateinit var metadata: MetaData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val chatRoom = baseActivity.intent.getParcelableExtra<ChatRoomsListViewItem>("chatRoom")

        chatRoomViewModel = viewModel {
            observe(chatRoomDetailsLiveData, ::renderShowRoom)
            observe(messagesLiveData, ::updateMessageList)
            observe(welcomesLiveData, ::updateWelcomeList)
            observe(donatesLiveData, ::updateDonateList)
            observe(loadingStatusLiveData, ::updateLoadingStatusBar)
            observe(userNumInChannelLiveData, ::updateUserNumDisplay)
            observe(roomTitleLiveData, ::updateRoomTitle)
            observe(roomBackgroundLiveData, ::updateRoomBackground)
            observe(stickersLiveData, ::renderStickerList)
            observe(visualEffectLiveData, ::showVisualEffect)
            observe(isOnMicLiveData, ::updateMicStatus)
            observe(seatsInfoLiveData, ::updateSeats)
            observe(seatQueueLiveData, ::updateQueue)
            observe(seatChangeLiveData, ::updateSeat)
            observe(mySeatLiveData, ::updateMySeat)
            observe(isSubscribedLiveData, ::updateBookmark)
            observe(showDonatePanelLiveData, ::showDonatePanel)
            failure(failureLiveData, ::handleFailure)
        }

        val agoraAppId = resources.getString(R.string.agora_private_app_id)
        chatRoomViewModel.init(chatRoom.id, chatRoom.externalRoomId, chatRoom.title, agoraAppId)

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun leaveRoom(){
        chatRoomViewModel.leaveRoom() //FIXME: 也许不在这里调用，留给viewmodel的生命周期方法处理更合适
        navigator.showMain(baseActivity)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId){
        R.id.actionSubscribeRoom -> {
            chatRoomViewModel.toggleSubscribeRoom()
            true
        }
        R.id.actionShareRoom -> {
            toast(item.title)
            true
        }
        R.id.actionComplainRoom -> {
            toast(item.title)
            true
        }
        R.id.actionLeaveRoom -> {
            leaveRoom()
            true
        }
        R.id.actionClearAllSeats -> {
            chatRoomViewModel.releaseAllSeats()
            true
        }
        R.id.actionSendTestMessages -> {
            chatRoomViewModel.sendTestMessages()
            true
        }
        R.id.actionTestWelcome -> {
            chatRoomViewModel.testWelcomeNotifications()
            true
        }
        else -> {
            toast(item.title)
            true
        }
    }

    private fun updateRoomBackground(background: ImageUrl?) {
        if(background.isNullOrBlank()) return
        chatRoomBackground.loadFromUrl(background)
    }

    private fun updateRoomTitle(title: String?) {
        if(title.isNullOrBlank()) return
        chatRoomTitle.text = title
    }

    private fun updateBookmark(isBookmarked: Boolean?) {
        when(isBookmarked){
            null, false -> {
                //显示为未收藏状态
                buttonSubscribe.setImageResource(R.drawable.star)
            }
            true -> {
                //显示为已收藏状态
                buttonSubscribe.setImageResource(R.drawable.bookmark)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        showProgress()
        initView()
        initEngines()
    }

    private fun initEngines() {
        // 动态申请权限，初始化声网语音引擎
        val rxPermission = RxPermissions(this)
        rxPermission.request(
            Manifest.permission.RECORD_AUDIO
        ).subscribe{granted ->
            if(granted){
                try {
                    initRTCEngine()
                }catch(e: Exception) {
                    e.printStackTrace()
                    //FIXME: view is not ready yet, the failureLiveData render may fail
                    chatRoomViewModel.trigerFailure(ChatRoomFailure.VoiceEngineInitialazationFailure(e))
                }
            } else {
                longToast("No permission for " + Manifest.permission.RECORD_AUDIO)
                chatRoomViewModel.trigerFailure(ChatRoomFailure.NoPermissionRecordAudio)
            }
        }
    }

    private fun initRTCEngine(){
        val rtcEngine: RtcEngine = RtcEngine.create(baseActivity, chatRoomViewModel.agoraAppId, RtcVoiceEngine.rtcEventHandler)
        chatRoomViewModel.initVoiceEngineAndJoinChannel(baseActivity, rtcEngine)
    }

    private fun initView() {
        initStatusBar()

        initToolbar()

        //FIXME: 目前只能点屏幕上半部才能消去表情/礼物面板
        showRoomArea.setOnClickListener { resetPanels() }

        initShowRoom()

        initMessageList()

        initBottomBar()

        initStickerPanel()

        initDonatePanel()
    }

    private fun initToolbar(){
        toolbar.setNavigationOnClickListener { view -> leaveRoom()  }
        toolbar.inflateMenu(R.menu.chatroom_toolbar)
        toolbar.overflowIcon = resources.getDrawable(R.drawable.overflow, null)
        toolbar.setOnMenuItemClickListener { item -> onOptionsItemSelected(item) }
        buttonSubscribe.setOnClickListener { view ->
            if(login.isLogined) {
                chatRoomViewModel.toggleSubscribeRoom()
            } else {
                navigator.showLogin(baseActivity)
            }
        }
    }

    private fun initStatusBar(){
//        baseActivity.immersive()

        val window = baseActivity.window

        // 改变StatusBar的颜色
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(resources.getColor(R.color.main_gradient_darkblue_start))

        // 隐藏导航条
//        baseActivity.hideNavigationButtons()
    }

    private fun initShowRoom() {
        chatRoomViewModel.loadRoomDetail() // renderShowRoom will be called after room detail data loaded
        chatRoomViewModel.loadStickers()
        chatRoomViewModel.loadShopGifts()
        initAnnouncement()
    }

    private fun initAnnouncement() {
        announcementTag.setOnClickListener {
            showAnnouncementDialog()
        }
    }

    fun showAnnouncementDialog(){
        val announcementDialog = makeDialog(R.layout.chatroom_announce_panel)
        announcementDialog.announcement.text = chatRoomViewModel.roomAnnouncementLiveData.value
        announcementDialog.announcement.setOnClickListener { announcementDialog.dismiss() }
        announcementDialog.announcement.movementMethod = ScrollingMovementMethod() //使得公告文字可滚动
        announcementDialog.show()
    }

    private fun initMessageList() {
        //false参数使得新消息在最下方加入
        val layoutManager = LinearLayoutManager(
            baseActivity, RecyclerView.VERTICAL, false
        )
        layoutManager.stackFromEnd = true //用于自动向下滚动
        messageListAdapter = ChatRoomMessageAdapter()

        messageList.layoutManager = layoutManager
        messageList.adapter = messageListAdapter

        welcomeList.dividerHeight= 0
        welcomeAdapter = ChatRoomWelcomesAdapter(baseActivity)
        welcomeList.adapter = welcomeAdapter

        donateList.dividerHeight = 0
        donateAdapter = ChatRoomDonatesAdapter(baseActivity)
        donateList.adapter = donateAdapter
    }


    private fun initBottomBar() {
        messageInputBar.invisible()
        interactionBar.visible()

        messageButton.setOnClickListener {
            if(!login.isLogined) navigator.showLogin(baseActivity)

            interactionBar.invisible()
            messageInputBar.visible()
            messageInputEdit.requestFocus()

            //打开输入法软键盘
            val imm = baseActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(messageInputEdit, InputMethodManager.RESULT_UNCHANGED_SHOWN)
        }

        sendMessageButton.setOnClickListener { actionSendMessage() }
        messageInputEdit.setOnEditorActionListener { v, actionId, event ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_SEND -> {
                    actionSendMessage()
                    true
                }
                else -> false
            }
        }

        micButton.setOnClickListener {
            if(!login.isLogined) navigator.showLogin(baseActivity)

            Log.d(LOG_TAG, "Mic button clicked, isOnMic: ${chatRoomViewModel.isOnMic}")
            when (chatRoomViewModel.isOnMic) {
                false -> { //上麦
                    toast("请点击座位选择要上的位子")
                }
                true -> { //下麦
                    chatRoomViewModel.releaseSeat()
                }
            }
        }

        stickerButton.setOnClickListener {
            chatRoomViewModel.loadStickers()
            stickerPanel.visible()
        }

        giftButton.setOnClickListener {
            chatRoomViewModel.loadShopGifts()
            //打开礼物面板时默认选中所有主持/嘉宾
            showDonatePanel(emptyList())
        }
    }

    private fun actionSendMessage(){
        val messageStr: String = messageInputEdit.text.toString()
        if (messageStr.isNotBlank()) {
            chatRoomViewModel.sendMessage(messageStr)
            messageInputEdit.text?.clear()
        }

        //关闭输入法软键盘
        val imm = baseActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0)

        messageInputBar.invisible()
        interactionBar.visible()
    }

    private fun resetPanels() {
        interactionBar.visible()
        messageInputBar.gone()
        stickerPanel.gone()
        donatePanel.gone()
    }

    //TODO: 实现更细粒度控制，只刷新有更新的数据
    private fun renderShowRoom(roomDetails: ChatRoomDetails?) {
        if (roomDetails == null) return

        val room = roomDetails.room //FIXME: 背景、标题、人数等改用拆分后的livedata

        with(room) {
            chatRoomBackground.invisible()
            chatRoomTitle.text = title
            guestNum.text = memberNum.toString()
        }

        initSeats(room)
    }

    private fun initSeats(room: ChatRoom){
        val guestSeatsNum = room.guestSeatNum
        seatsContainer.removeAllViews()

        var seatImg: ImageView
        var seatLabel: TextView
        val emptySeatAlpha = 0.3f
        val totalHorizontalMargin = 48 //dp, left && right margin of guest seats area
        val seatWidthInDp = baseActivity.screenWidthDp() - totalHorizontalMargin
        val seatWidth = (baseActivity.dp2px(seatWidthInDp) / 4).toInt()

        for (seatIdx in 0 .. guestSeatsNum) { //初始化主持人+嘉宾座位显示状态
            if (seatIdx == 0) { //主持人位，xml布局中已有，无需创建
                seatImg = seatHostImg
                seatLabel = seatHostLabel
            } else {
                //FIXME: 根据屏幕宽度动态调整尺寸，以适应较低分辨率屏幕
                val seatView =
                    LayoutInflater.from(baseActivity).inflate(R.layout.chatroom_seat, null)

                val lp = GridLayout.LayoutParams()
//                lp.rightMargin = baseActivity.dp2px(16).toInt()
                lp.width = seatWidth
                seatsContainer.addView(seatView, lp)
                seatImg = seatView.seatImg
                seatLabel = seatView.seatLabel

                when(room.layoutType){
                    RoomLayoutType.SEAT_4 -> {
                        when (seatIdx) { //设置座位边框
                            1 -> seatView.seatImgFrame.setBackgroundResource(R.drawable.avatar_frame_golden)
                            2 -> seatView.seatImgFrame.setBackgroundResource(R.drawable.avatar_frame_silver)
                            else -> seatView.seatImgFrame.setBackgroundResource(R.drawable.avatar_frame_copper)
                        }
                    }
                    RoomLayoutType.SEAT_8 -> seatView.seatImgFrame.invisible()
                }
            }

            //所有座位样式均初始化为Open状态
            seatImg.setImageResource(R.drawable.open_seat)
            seatImg.alpha = emptySeatAlpha
            seatLabel.invisible()
            seatImg.setOnClickListener {
                if(!login.isLogined) navigator.showLogin(baseActivity)
                chatRoomViewModel.takenSeat(seatIdx)
            }
        }
    }

    private fun renderSeats(seats: List<Seat>){
        var seatImg: ImageView
        var seatLabel: TextView
        var seatPulse: MultiPulseLayout
        val emptySeatAlpha = 0.3f
        for (seat in seats) {
            if(seat.idx == 0){ //主持人位
                seatImg = seatHostImg
                seatLabel = seatHostLabel
                seatPulse = seatHostPulse
            }else{
                val seatView = seatsContainer.getChildAt(seat.idx - 1)
                if(seatView == null){
                    Log.e(LOG_TAG, "cannot get seat view of seat ${seat.idx}, seatsContainer has ${seatsContainer.childCount} children")
                    continue
                }
                seatImg = seatView.seatImg
                seatLabel = seatView.seatLabel
                seatPulse = seatView.seatPulse

                when(seat.idx){ //设置座位边框
                    1 -> seatView.seatImgFrame.setBackgroundResource(R.drawable.avatar_frame_golden)
                    2 -> seatView.seatImgFrame.setBackgroundResource(R.drawable.avatar_frame_silver)
                    else -> seatView.seatImgFrame.setBackgroundResource(R.drawable.avatar_frame_copper)
                }
            }
            when (seat.state) {
                SeatState.OCCUPIED -> {
                    seatImg.loadFromUrl(seat.userAvatar) //FIXME: 【bug】有时上麦了后座位上不显示头像
                    seatImg.alpha = 1.0f
                    seatLabel.text = getSeatLabelText(seat)
                    seatLabel.visible()
                    seatPulse.start() //FIXME: 仅当麦克风有音量时才显示动效
                    seatImg.setOnClickListener { showUserInfo(seat.idx) }
                }
                SeatState.OPEN -> {
                    seatImg.setImageResource(R.drawable.open_seat)
                    seatImg.alpha = emptySeatAlpha
                    seatLabel.invisible()
                    seatPulse.clear()
                    seatImg.setOnClickListener {
                        if(!login.isLogined) navigator.showLogin(baseActivity)
                        if(chatRoomViewModel.hasSeat()){
                            toast("已经有座位了, 如果想换座位请先下麦")
                        }else {
                            chatRoomViewModel.takenSeat(seat.idx)
                        }
                    }
                }
                SeatState.DISABLED -> {
                    seatImg.setImageResource(R.drawable.locked_seat)
                    seatImg.alpha = emptySeatAlpha
                    seatLabel.invisible()
                    seatPulse.clear()
                    seatImg.setOnClickListener {  }
                }
            }

        }
    }

    private fun getSeatLabelText(seat: Seat): SpannableStringBuilder{
        val ssBuilder = SpannableStringBuilder()
        if(seat.isHost){
            val hostTagText = " 主播 "
            ssBuilder.append(hostTagText)
            //FIXME: 如果昵称长度超过显示区域长度，主播标签中的文字会显示为小方块
            val hostSpan = RoundedBackgroundSpan(context!!, 7,
                resources.getColor(R.color.main_blue_c1),
                resources.getColor(R.color.main_blue_c1), 10,
                Paint.Style.STROKE)
            ssBuilder.setSpan(hostSpan, 0, hostTagText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        ssBuilder.append(seat.userName)
        return ssBuilder
    }

    private fun updateMySeat(seatIdx: Int?) { }

    private fun updateQueue(list: List<SeatQueueItem>?) {
        if(list == null || list.isEmpty()) return
        val info = "seat queue updated: ${list?.joinToString { "(${it.seatIdx}: ${it.userInfo.nickName})" }}"
        longToast(info)
        Log.d(LOG_TAG, info)
    }

    private fun updateSeats(seats: Map<Int, Seat>?) {
        if(seats == null || seats.isEmpty()) return
        renderSeats(seats.values.toList())
    }

    private fun updateSeat(seat: Seat?) {
        if(seat == null) return
        renderSeats(listOf(seat))
    }

    private fun showUserInfo(seatIdx: Int){
        val userInfo = chatRoomViewModel.getSeatUserInfo(seatIdx) ?: return
        showUserInfo(userInfo, seatIdx)
    }

    private fun showUserInfo(userInfo: ChatRoomUserInfo, seatIdx: Int){
        val userInfoDialog = makeDialog(R.layout.chatroom_userinfo_panel)
        with(userInfoDialog) {
            if (userInfo.avatar.isNotEmpty()) {
                guestInfoAvatar.loadFromUrl(userInfo.avatar)
            }
            guestInfoAvatar.setOnClickListener {
                navigator.showUserProfile(baseActivity, userInfo.userId)
            }
            guestInfoNickName.text = userInfo.nickName
            guestInfoUserId.text = "ID:${userInfo.userId}"

            //TODO: 年龄
            val genderAge = "${userInfo.gender.toIconString()}"
            when (userInfo.gender) { //FIXME：控件不支持直接设置颜色，先用两个控件切换的方式临时hack一下
                Gender.MALE -> {
                    genderAgeMale.text = genderAge
                    genderAgeMale.visible()
                    genderAgeFamale.invisible()
                }
                Gender.FAMALE -> {
                    genderAgeFamale.text = genderAge
                    genderAgeFamale.visible()
                    genderAgeMale.invisible()
                }
                Gender.UNKNOWN -> {
                    guestInfoGenderAge.invisible()
                }
            }

            userInfo.nobleLevel = metadata.getNobleLevel(userInfo.nobleLevel.level)
            val tag = userTagDisplayHelper.getTag(userInfo, false, false)
            guestInfoLevelTag.text = tag.append("  ")

            guestInfoHomePageButton.setOnClickListener {
                navigator.showUserProfile(baseActivity, userInfo.userId)
            }

            //左侧红色按钮，如果是显示的当前登录用户自己的信息，则显示"续费"，否则显示"打赏"
            if (login.isMe(userInfo.userId)) {
                guestInfoRedButton.text = "续费"
                guestInfoRedButton.setOnClickListener {
                    toast("续费")
                }
            } else {
                guestInfoRedButton.text = "打赏"
                guestInfoRedButton.setOnClickListener {
                    if (!login.isLogined) navigator.showLogin(baseActivity)
                    showDonatePanel(listOf(userInfo))
                    dismiss()
                }
            }

            //右侧蓝色按钮，如果是显示的当前登录用户自己的信息，则显示"下麦"，否则显示"关注"
            if (login.isMe(userInfo.userId)) {
                if (chatRoomViewModel.isOnMic) {
                    guestInfoBlueButton.text = "下麦"
                    guestInfoBlueButton.setOnClickListener {
                        chatRoomViewModel.releaseSeat()
                        dismiss()
                    }
                } else {
                    guestInfoBlueButton.invisible()
                }
            } else {
                guestInfoBlueButton.text = "关注"
                guestInfoBlueButton.setOnClickListener {
                    if (!login.isLogined) navigator.showLogin(baseActivity)
                    toast("关注") //FIXME: to implement
                }
            }

            if (chatRoomViewModel.hostIsMe) {
                if (login.isMe(userInfo.userId)) {
                    guestInfoAdminReleaseSeatButton.invisible()
                } else {
                    guestInfoAdminReleaseSeatButton.visible()
                    guestInfoAdminReleaseSeatButton.setOnClickListener {
                        chatRoomViewModel.releaseSeat(seatIdx)
                        dismiss()
                    }
                }

                guestInfoAdminMicControlButton.visible()
                guestInfoAdminMicControlButton.setOnClickListener {
                    toast("关麦/开麦") //FIXME: to implement
                }
            } else {
                guestInfoAdminReleaseSeatButton.invisible()
                guestInfoAdminMicControlButton.invisible()
            }
        }
        userInfoDialog.show()
    }

    private fun updateWelcomeList(welcomes: MutableList<WelcomeNotification>?) {
        welcomeAdapter.clear()
        if(welcomes.isNullOrEmpty()) {
            welcomeList.gone()
            return
        }
        welcomeList.visible()
        welcomeAdapter.addAll(welcomes)
        welcomeAdapter.notifyDataSetChanged()
    }

    private fun updateDonateList(donates: MutableList<DonateNotification>?){
        if(donates.isNullOrEmpty()){
            donateAdapter.clear()
            donateList.gone()
            return
        }

        donateAdapter.addAll(donates)
        donateList.visible()
    }

    private fun updateMicStatus(isOnMic: Boolean?){
        when(isOnMic){
            null, false ->
                micButton.setImageResource(R.drawable.mic_closed)
            true ->
                micButton.setImageResource(R.drawable.mic)
        }
    }

    private fun updateMessageList(messages: List<MessageItem>?) {
        if (messages.isNullOrEmpty()) return //FIXME: should clear message list display
        CoroutineScope(Dispatchers.Main).launch {
            messageListAdapter.collection = messages
            messageList.smoothScrollToPosition(messageListAdapter.itemCount - 1) // 自动滚动，FIXME：用户手动滚动到上面时需要停止自动滚动，待回到最下后才恢复
        }
    }

    private fun initStickerPanel() {
        stickerPanel.gone()
        stickerPanel.layoutManager = GridLayoutManager(baseActivity, 5)
        stickerAdapter = ChatRoomStickerAdapter()
        stickerPanel.adapter = stickerAdapter
        stickerAdapter.itemClickListener = { stickerItem ->
            if(!login.isLogined) navigator.showLogin(baseActivity)
            clickedStickerItem(stickerItem)
        }
    }

    private fun clickedStickerItem(stickerItem: StickerItem) {
        stickerPanel.gone()
        chatRoomViewModel.sendSticker(stickerItem)
    }

    private fun renderStickerList(stickers: List<StickerItem>?) {
        if (stickers.isNullOrEmpty()) return
        CoroutineScope(Dispatchers.Main).launch {
            stickerAdapter.collection = stickers
        }
    }

    private fun initDonatePanel() {
        donatePanelPagerAdapter = ChatRoomDonatePanelPagerAdapter(childFragmentManager)
        chatroomDonatePanelPager.adapter = donatePanelPagerAdapter
        chatroomDonatePanelTab.setupWithViewPager(chatroomDonatePanelPager)
        donatePanel.gone()
    }

    private fun showDonatePanel(show: Boolean?) = when(show){
        false, null -> donatePanel.gone()
        true -> donatePanel.visible()
    }

    private fun showDonatePanel(targetUsers: List<ChatRoomUserInfo>){
        val targets = mutableListOf<ChatRoomUserInfo>()
        if(targetUsers.isEmpty()){
            if(chatRoomViewModel.donateTargetUsersLiveData.value.isNullOrEmpty()){ //如果之前没有选中任何用户，默认选中所有人
                targets.addAll(chatRoomViewModel.seatsInfoLiveData.value?.values?.map { it.userInfo } ?: emptyList())
                chatRoomViewModel.setDonateTargetUsers(targets)
            }
        }else{
            targets.addAll(targetUsers)
            chatRoomViewModel.setDonateTargetUsers(targets)
        }
        donatePanel.visible()
    }

    private fun showVisualEffect(visualEffect: VisualEffect?) {
        if (visualEffect == null || visualEffect.animationResource.isEmpty()) return
        visualEffectAnimation.visible()
        visualEffectAnimation.loadFromUrl(visualEffect.animationResource)
        CoroutineScope(Dispatchers.Main).launch {
            delay(ANIMATION_LAST_TIME_IN_MS)
            visualEffectAnimation.invisible()
        }
    }

    override fun showProgress() = progress.visible()

    override fun hideProgress() = progress.invisible()

    private fun updateLoadingStatusBar(loadingStatus: MutableList<Boolean>?) {
        if (loadingStatus!!.any { it }) {
            showProgress()
        } else {
            hideProgress()
        }
    }

    private fun updateUserNumDisplay(userNum: Int?) {
        guestNum.text = "在线 ${userNum}"
    }

}

class ChatRoomDonatePanelPagerAdapter(fm: FragmentManager) : BasePagerAdapter(fm) {

    override fun pageTitles() = listOf("礼物", "背包")

    override fun getItem(position: Int) = when(position){
        0 -> ChatRoomGiftPanelFragment()
        1 -> ChatRoomPackagePanelFragment()
        else -> ChatRoomGiftPanelFragment()
    }

}

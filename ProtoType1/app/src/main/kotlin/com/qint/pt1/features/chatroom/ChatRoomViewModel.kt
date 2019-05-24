package com.qint.pt1.features.chatroom

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.netease.nimlib.sdk.StatusCode
import com.qint.pt1.api.chatroom.HiveAPI
import com.qint.pt1.api.shop.UmbrellaAPI
import com.qint.pt1.api.user.BeesAPI
import com.qint.pt1.base.exception.Failure
import com.qint.pt1.base.platform.BaseViewModel
import com.qint.pt1.base.platform.RequestCallback
import com.qint.pt1.domain.*
import com.qint.pt1.features.chatrooms.ChatRoomFailure
import com.qint.pt1.features.chatrooms.ChatRoomsRepository
import com.qint.pt1.features.im.IM
import com.qint.pt1.features.login.Login
import com.qint.pt1.support.agora.RtcVoiceEngine
import com.qint.pt1.support.nim.decodeFromBase64toSeat
import com.qint.pt1.util.LOG_TAG
import io.agora.rtc.RtcEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


class ChatRoomViewModel
@Inject constructor(private val chatRoomsRepository: ChatRoomsRepository,
                    private val login: Login,
                    private val im: IM,
                    private val hiveAPI: HiveAPI,
                    private val messageAPI: ChatRoomMessageAPI,
                    private val umbrellaAPI: UmbrellaAPI,
                    private val beesAPI: BeesAPI) : BaseViewModel() {

    lateinit var roomId: ChatRoomId
        private set //keep this readonly to outside

    lateinit var externalRoomId: ChatRoomId
        private set

    lateinit var agoraAppId: String //appId used by agora sdk
        private set

    //目前仅使用其中的部分数据，不使用其中的座位信息数据，座位信息从云信聊天室队列取
    val chatRoomDetailsLiveData: MutableLiveData<ChatRoomDetails> = MutableLiveData()

    val messagesLiveData: MutableLiveData<MutableList<MessageItem>> = MutableLiveData()
    val welcomesLiveData: MutableLiveData<MutableList<WelcomeNotification>> = MutableLiveData()
    val donatesLiveData: MutableLiveData<MutableList<DonateNotification>> = MutableLiveData()
    val loadingStatusLiveData: MutableLiveData<MutableList<Boolean>> = MutableLiveData()
    val userNumInChannelLiveData: MutableLiveData<Int> = MutableLiveData()
    val roomAnnouncementLiveData: MutableLiveData<String> = MutableLiveData()
    val roomTitleLiveData: MutableLiveData<String> = MutableLiveData()
    val roomBackgroundLiveData: MutableLiveData<ImageUrl> = MutableLiveData()
    val stickersLiveData: MutableLiveData<List<StickerItem>> = MutableLiveData()
    val giftsLiveData: MutableLiveData<List<GiftItem>> = MutableLiveData()
    val backpackLiveData: MutableLiveData<Backpack> = MutableLiveData()
    val visualEffectLiveData: MutableLiveData<VisualEffect> = MutableLiveData()
    val isOnMicLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val seatsInfoLiveData: MutableLiveData<MutableMap<Int, Seat>> = MutableLiveData()
    val seatQueueLiveData: MutableLiveData<List<SeatQueueItem>> = MutableLiveData()
    val seatChangeLiveData: MutableLiveData<Seat> = MutableLiveData()
    val mySeatLiveData: MutableLiveData<Int> = MutableLiveData()
    val isSubscribedLiveData: MutableLiveData<Boolean> = MutableLiveData()

    //控制礼物/背包面板显示
    val showDonatePanelLiveData: MutableLiveData<Boolean> = MutableLiveData()

    //在ChatRoomFragment中设置要打赏的用户，然后通过以下的livedata传递到ChatRoomGiftPanel中
    private val _donateTargetUsersLiveData: MutableLiveData<List<ChatRoomUserInfo>> = MutableLiveData()
    val donateTargetUsersLiveData: LiveData<List<ChatRoomUserInfo>> = _donateTargetUsersLiveData

    var guestSeatsNum: Int = 8
    val isOnMic: Boolean get() = isOnMicLiveData.value ?: false
    val hostIsMe: Boolean get() = login.isMe(seatsInfoLiveData.value?.get(0)?.userId)

    val AUTO_SEND_TEST_MESSAGE_ON_JOIN = false
    val NO_SEAT = -1

    private var senderInfo: ChatRoomUserInfo

    init {
        //To ensure live data value not null
        messagesLiveData.value = mutableListOf()
        welcomesLiveData.value = mutableListOf()
        donatesLiveData.value = mutableListOf()
        loadingStatusLiveData.value = mutableListOf(true, true, true)
        userNumInChannelLiveData.value = 0
        roomTitleLiveData.value = ""
        stickersLiveData.value = emptyList()
        giftsLiveData.value = emptyList()
        isOnMicLiveData.value = false
        seatsInfoLiveData.value = mutableMapOf()
        seatQueueLiveData.value = mutableListOf()
        mySeatLiveData.value = NO_SEAT
        isSubscribedLiveData.value = false
        showDonatePanelLiveData.value = false
        _donateTargetUsersLiveData.value = emptyList()

        senderInfo = login.user?.toMessageUserInfo() ?: ChatRoomUserInfo.NullMessageUserInfo
    }

    fun init(roomId: ChatRoomId, externalRoomId: ChatRoomId, roomTitle: Title, appId: String) {
        this.roomId = roomId
        this.externalRoomId = externalRoomId
        this.agoraAppId = appId
        roomTitleLiveData.value = roomTitle
        im.init()
        messageAPI.setMessageHandler(::getMessages)
        messageAPI.observeOnlineStatus(::handleOnlineStatusChange)
    }

    fun initVoiceEngineAndJoinChannel(context: Context, voiceEngine: RtcEngine) {
        setLoadingStatus(LOADING_STATUS_VOICE_ENGINE)
        messageAPI.init(context)

        RtcVoiceEngine.initAgoraEngineAndJoinChannel(context, voiceEngine, roomId, object: RequestCallback<Unit>{
            override fun onSuccess(result: Unit?) {
                CoroutineScope(Dispatchers.Main).launch {
                    unsetLoadingStatus(LOADING_STATUS_VOICE_ENGINE)
                }
            }

            override fun onFailure(failure: Failure) {
                CoroutineScope(Dispatchers.Main).launch {
                    trigerFailure(failure)
                    unsetLoadingStatus(LOADING_STATUS_VOICE_ENGINE)
                }
            }

        })

        messageAPI.joinChatRoom(externalRoomId, roomId, object: RequestCallback<ChatRoomInfo>{
            override fun onSuccess(chatRoomInfo: ChatRoomInfo?) {
                loadSeatsInfo()
                if(login.isLogined && AUTO_SEND_TEST_MESSAGE_ON_JOIN) {
                    sendTestMessages()
                }
                if(chatRoomInfo == null) return
                userNumInChannelLiveData.value = chatRoomInfo.userCount
                roomAnnouncementLiveData.value = chatRoomInfo.announcement
                val background = chatRoomInfo.background
                if(!background.isNullOrBlank()) roomBackgroundLiveData.value = background

                //不用云信系统中的聊天室标题，使用红后返回的标题
                //roomTitleLiveData.value = chatRoomInfo.name
            }

            override fun onFailure(failure: Failure) {
                trigerFailure(failure)
            }
        })
    }

    override fun onCleared() {
        super.onCleared()
        CoroutineScope(Dispatchers.IO).launch {//耗时约500ms，不要阻塞主线程
            leaveRoom()
            RtcVoiceEngine.destroy()
        }
    }

    fun leaveRoom() {
        releaseSeat()
        RtcVoiceEngine.leaveChannel()
        messageAPI.leaveChatRoom()
    }

    //TODO: convert to UseCase
    fun loadRoomDetail() {
        setLoadingStatus(LOADING_STATUS_ROOM_DATA)
        CoroutineScope(Dispatchers.IO).launch {
            val resp = chatRoomsRepository.chatRoomDetails(roomId)
            launch(Dispatchers.Main) {
                resp.either(
                    { failure -> trigerFailure(failure) },
                    { chatRoomDetails -> updateRoomDeatilsData(chatRoomDetails) }
                )
            }
        }
    }

    private fun loadSeatsInfo(){
        messageAPI.getSeatInfo(object: RequestCallback<Map<String, String>>{
            override fun onSuccess(result: Map<String, String>?) {
                val seatsInfo = mutableMapOf<Int, Seat>()
                result?.forEach {
                    when(it.key){
                        in Seat.SEAT_KEYS -> {
                            val seat = it.value.decodeFromBase64toSeat()
                            if(seat != null) seatsInfo.set(seat.idx, seat)
                        }
                        Seat.WAIT_QUEUE_KEY -> {
                            seatQueueLiveData.value = SeatQueueItem.parseQueueFromBase64Protobuf(it.value)
                        }
                    }
                }
                seatsInfoLiveData.value = seatsInfo
            }

            override fun onFailure(failure: Failure) {
                trigerFailure(failure)
            }
        })
    }

    private fun updateRoomDeatilsData(chatRoomDetails: ChatRoomDetails) {
        unsetLoadingStatus(LOADING_STATUS_ROOM_DATA)
        this.chatRoomDetailsLiveData.value = chatRoomDetails

        //用户数改为使用云信登录时返回的数据
        //userNumInChannelLiveData.value = chatRoomDetails.room.memberNum
        roomTitleLiveData.value = chatRoomDetails.room.title
        roomBackgroundLiveData.value = chatRoomDetails.room.backgroundPicture
        isSubscribedLiveData.value = chatRoomDetails.isFollowed
        guestSeatsNum = chatRoomDetails.room.guestSeatNum
    }

    fun sendMessage(text: String) = sendMessage(ChatMessage(text, senderInfo))

    fun sendMessage(message: ChatRoomUserMessage){ //发送消息并将其添加到聊天室消息列表中
        messageAPI.sendMessage(message, object: RequestCallback<Unit>{
            override fun onSuccess(result: Unit?) {
                message.successSend = true
                getMessage(message)
            }

            override fun onFailure(failure: Failure) {
                trigerFailure(failure)
                message.successSend = false
                getMessage(message)
            }
        })
    }

    @Deprecated("仅为了兼容Agora信令消息接口的编译而提供，不应使用此方法")
    internal fun getMessage(text: String) = getNotification(InformationNotification(text))

    private fun getMessage(message: ChatRoomUserMessage) {
        getMessages(listOf(message))
    }

    //尽可能批量处理消息，以避免逐个处理大量消息导致界面频繁刷新
    private fun getMessages(messages: List<ChatRoomMessage>){
        val oldMessages: MutableList<MessageItem> = this.messagesLiveData.value!!
        for(message in messages){
            when(message){
                is ChatRoomUserMessage -> {
                    markHostTag(message)
                    oldMessages.add(message)
                }
                is ChatRoomNotificationMessage -> getNotification(message)
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            messagesLiveData.value = oldMessages
        }
    }

    private fun markHostTag(message: ChatRoomUserMessage){ //如果是当前房间主持人发出的消息设置标记
        //FIXME: 对收到的远程消息不能正确识别
        val hostUid = seatsInfoLiveData.value?.get(0)?.userId ?: return
        val sender = message.sender
        if(sender.userId == hostUid) message.isSendFromChatRoomHost = true
    }

    private fun getNotification(notification: ChatRoomNotificationMessage){
        Log.d(LOG_TAG, "get notification: $notification")
        when(notification){
            //TODO: 处理聊天室排麦变更信息
            //TODO: 处理聊天室禁言、拉黑、被踢等信息
            //TODO: 处理聊天室背景变更
            is WelcomeNotification -> handleWelcomeNotification(notification)
            is InformationNotification -> displayNotificationInMessageList(notification)
            is LeaveNotification -> userNumDec()
            is SeatChangeNotification -> handleSeatChangeNotification(notification)
            is WaitQueueNotification -> handleWaitQueueNotification(notification)
            is DonatesNotification -> handleDonatesNotification(notification)
            else -> displayNotificationInMessageList(notification) //FIXME: debug only
        }
    }

    private fun handleWelcomeNotification(notification: WelcomeNotification){
        userNumInc()
        if(!notification.newChatRoomMember.isNullOrAnonymous()) {
            displayNotificationInMessageList(notification)

            //更新欢迎通知显示
            val welcomes = welcomesLiveData.value!!
            welcomes.add(notification)
            CoroutineScope(Dispatchers.Main).launch {
                welcomesLiveData.value = welcomes
                delay(2000)
                welcomes.removeAt(0)
                welcomesLiveData.value = welcomes
            }
        }
    }

    private fun handleDonatesNotification(notification: DonatesNotification) {
        val from = notification.from
        val donates = notification.donates
        val subNotifications = donates.map { DonateNotification(from, it) }
        displayNotificationInMessageList(subNotifications)

        //更新礼物通知显示
        val liveDonates = donatesLiveData.value!!
        liveDonates.addAll(subNotifications)
        CoroutineScope(Dispatchers.Main).launch {
            donatesLiveData.value = liveDonates
            donatesLiveData.value!!.clear() //待显示数据全部送入相应的adapter自行处理，这里不再保留
        }
    }

    private fun handleSeatChangeNotification(notification: SeatChangeNotification){
        val seatsInfo = seatsInfoLiveData.value!!
        val seat = notification.seat
        notification.original = seatsInfo[seat.idx] //保存座位在变化之前的状态以便显示相关信息
        seatsInfo[seat.idx] = seat
        //seatsInfoLiveData.value = seatsInfo //更新所有座位显示状态
        seatChangeLiveData.value = seat //仅更新当前变化的座位

        //更新当前用户麦位状态
        if(login.isMe(seat.userInfo.userId)){ //如果通知消息的座位用户是当前用户，或者座位是当前用户使用的座位，需要更新当前用户的麦位状态
            when(seat.state){
                SeatState.OCCUPIED -> {
                    mySeatLiveData.value = seat.idx
                    switchToBroadCast()
                }
            }
        }
        if(seat.idx == mySeatLiveData.value && seat.state != SeatState.OCCUPIED){
            switchToAudience()
            //判断一下是否已经设置过livedata了，避免重复刷新
            if(mySeatLiveData.value != NO_SEAT) mySeatLiveData.value = NO_SEAT
        }

        if(seat.state == SeatState.OCCUPIED || !notification.original?.userInfo?.nickName.isNullOrBlank()) {
            displayNotificationInMessageList(notification)
        }
    }

    private fun handleWaitQueueNotification(notification: WaitQueueNotification){
        seatQueueLiveData.value = notification.waitQueue
    }

    private fun displayNotificationInMessageList(notification: ChatRoomNotificationMessage){
        displayNotificationInMessageList(listOf(notification))
    }

    private fun displayNotificationInMessageList(notifications: List<ChatRoomNotificationMessage>){
        val oldMessages: MutableList<MessageItem> = this.messagesLiveData.value!!
        oldMessages.addAll(notifications)
        CoroutineScope(Dispatchers.Main).launch {
            messagesLiveData.value = oldMessages
        }
    }

    private fun showVisualEffect(visualEffect: VisualEffect) {
        this.visualEffectLiveData.value = visualEffect
    }

    companion object {
        const val LOADING_STATUS_VOICE_ENGINE = 0
        const val LOADING_STATUS_MESSAGE_API = 1
        const val LOADING_STATUS_ROOM_DATA = 2
    }

    fun setLoadingStatus(component: Int) {
        updateLoadingStatus(component, true)
    }

    fun unsetLoadingStatus(component: Int) {
        updateLoadingStatus(component, false)
    }

    private fun updateLoadingStatus(component: Int, status: Boolean) {
        CoroutineScope(Dispatchers.Main).launch {
            val previousLoadingStatus = loadingStatusLiveData.value!!
            if (previousLoadingStatus[component] != status) {
                loadingStatusLiveData.value!![component] = status
                loadingStatusLiveData.value = loadingStatusLiveData.value //重置LiveData数据以触发观察者更新进度条状态
            }
        }
    }

    fun hasSeat() = mySeatLiveData.value != null && mySeatLiveData.value != NO_SEAT

    fun takenSeat(seatIdx: Int) {
        val account = login.account ?: return
        if(hasSeat()) return
        CoroutineScope(Dispatchers.IO).launch{
//            hiveAPI.applySeat(roomId, account.userId, seatIdx, account) //FIXME: 测试排麦队列用，触发队列变更，完成后去除
            val resp = hiveAPI.takenSeat(roomId, account.userId, seatIdx, account)
            launch(Dispatchers.Main) {
                resp.either(
                    {_failure -> trigerFailure(_failure)},
                    {_success ->
                        if(_success){
                            switchToBroadCast()
                            mySeatLiveData.value = seatIdx
                        }else{
                            Log.w(LOG_TAG, "HiveAPI.takenSeat denied")
                            Unit
                        }
                    }
                )
            }
        }
    }

    private fun switchToBroadCast(){
        RtcVoiceEngine.openMic()
        isOnMicLiveData.value = true
    }

    private fun switchToAudience(){
        RtcVoiceEngine.closeMic()
        CoroutineScope(Dispatchers.Main).launch {
            isOnMicLiveData.value = false
        }
    }

    fun openMic(seatIdx: Int){
        TODO()
    }

    fun closeMic(seatIdx: Int){
        TODO()
    }

    fun openMyMic(){
        RtcVoiceEngine.openMic()
    }

    fun closeMyMic(){
        RtcVoiceEngine.closeMic()
    }

    fun releaseSeat() {
        switchToAudience() //不等服务器确认，直接释放资源，也许有安全风险
        val seatIdx = mySeatLiveData.value ?: return
        if(seatIdx == NO_SEAT) return
        CoroutineScope(Dispatchers.Main).launch {
            mySeatLiveData.value = NO_SEAT
        }
        releaseSeat(seatIdx)
    }

    fun releaseSeat(seatIdx: Int){
        val account = login.account ?: return
        CoroutineScope(Dispatchers.IO).launch{
            hiveAPI.releaseSeat(roomId, account.userId, seatIdx, account)
        }
    }

    fun releaseAllSeats(){ //FIXME: debug only
        for(seatIdx in 0 .. 8) releaseSeat(seatIdx)
    }

    fun getSeatUserInfo(seatIdx: Int): ChatRoomUserInfo?{
        val seats = seatsInfoLiveData.value
        return seats?.get(seatIdx)?.userInfo
    }

    private fun userNumInc() {
        CoroutineScope(Dispatchers.Main).launch {
            val lastValue = userNumInChannelLiveData.value ?: 0
            userNumInChannelLiveData.value = lastValue + 1
        }
    }

    private fun userNumDec() {
        CoroutineScope(Dispatchers.Main).launch {
            val lastValue = userNumInChannelLiveData.value ?: 1
            userNumInChannelLiveData.value = lastValue - 1
        }
    }

    //TODO: convert to UseCase
    fun loadStickers() {
        CoroutineScope(Dispatchers.IO).launch {
            val resp = chatRoomsRepository.stickers()
            launch(Dispatchers.Main) {
                resp.either(
                        { _failure -> trigerFailure(_failure) },
                        { _stickers -> updateStickersData(_stickers) })
            }
        }
    }

    private fun updateStickersData(stickers: List<Sticker>) {
        this.stickersLiveData.value = stickers.map { StickerItem(it.id, it.title, it.icon) }
    }

    fun sendSticker(stickerItem: StickerItem) {
        val message = StickerMessage(stickerItem.toSticker(), senderInfo)
        sendMessage(message)
    }

    //TODO: convert to GetShopGiftsUseCase
    fun loadShopGifts() {
        CoroutineScope(Dispatchers.IO).launch {
            val resp = chatRoomsRepository.gifts()
            launch(Dispatchers.Main) {
                resp.either(
                    { _failure -> trigerFailure(_failure) },
                    { _gifts -> updateGiftsData(_gifts) }
                )
            }
        }
    }

    private fun updateGiftsData(gifts: List<Gift>) {
        this.giftsLiveData.value = gifts.map { GiftItem(it.id, it.title, it.icon, it.price) }
    }

    fun sendGift(giftItem: GiftItem, count: Int = 1, to: List<ChatRoomUserInfo>? = null) { //FIXME: 应增加赠送对象参数，如果没有指定赠送对象则给所有座位上的人全送
        val account = login.account ?: return
        val targetUsers: List<ChatRoomUserInfo>? = to
        if(targetUsers.isNullOrEmpty()) return
        val donates = targetUsers.map {
            Donate(
                giftItem.id,
                it.userId, it.nickName,
                account.userId, account.user.profile.nickName,
                count
            )
        }
        CoroutineScope(Dispatchers.IO).launch {
            val resp = umbrellaAPI.sendGift(roomId, account, donates)
            launch(Dispatchers.Main) {
                resp.either(
                    {_failure -> trigerFailure(_failure)},
                    {_success -> if(_success){
                        Log.d(LOG_TAG, "发送礼物成功")
                    }else{
                        trigerFailure(ChatRoomFailure.SendGiftFailed)
                        Log.w(LOG_TAG, "发送礼物被拒绝")
                    }}
                )
            }
        }
    }

    fun setDonateTargetUsers(users: List<ChatRoomUserInfo>){
        _donateTargetUsersLiveData.value = users
    }

    fun hideDonatePanel() {
        showDonatePanelLiveData.value = false
    }

    fun toggleSubscribeRoom(){
        val account = login.account ?: return

        val toSubscribe = !(isSubscribedLiveData.value ?: false)
        CoroutineScope(Dispatchers.IO).launch {
            val resp = hiveAPI.subscribeRoom(roomId, toSubscribe, account)
            launch(Dispatchers.Main) {
                resp.either(
                    {failure -> trigerFailure(failure) },
                    {isFollowed ->
                        isSubscribedLiveData.value = isFollowed
                        isFollowed //没什么用，就是为了满足返回Any的要求使得编译通过
                    }
                )
            }
        }
    }

    fun loadBackpack() {
        val account = login.account ?: return
        CoroutineScope(Dispatchers.IO).launch {
            val resp = beesAPI.getUserBackpack(account)
            launch(Dispatchers.Main) {
                resp.either(
                    { _failure -> trigerFailure(_failure) },
                    { _backpack -> updateBackpack(_backpack) }
                )
            }
        }
    }

    private fun updateBackpack(backpack: Backpack){
        backpackLiveData.value = backpack
    }

    private fun handleOnlineStatusChange(statusCode: StatusCode){ //FIXME: 触发handleFailure处理
        when(statusCode){
            StatusCode.UNLOGIN -> logOnlineStatueChange("断线")
            StatusCode.KICKOUT -> logOnlineStatueChange("被其它端的登录踢掉")
            StatusCode.KICK_BY_OTHER_CLIENT -> logOnlineStatueChange("被同时在线的其它端主动踢掉")
        }
    }

    private fun logOnlineStatueChange(change: String){
        Log.d(LOG_TAG, "NIMChatRoomMessageAPI Online Status Change: ${change}")
    }

    @Deprecated("仅在Agora信令接口中使用")
    fun userJoined(account: String) {
        getNotification(InformationNotification("${account} joined")) //仅为了兼容其它类型的客户端，可以去掉。
        userNumInc()
    }

    @Deprecated("仅在Agora信令接口中使用")
    fun userLeaved(account: String) {
        getNotification(InformationNotification("${account} leaved")) //仅为调试测试用，可以去掉。
        userNumDec()
    }

    @Deprecated("仅在Agora信令接口中使用")
    fun joinedMessageChannel() {
        CoroutineScope(Dispatchers.Main).launch {
            unsetLoadingStatus(ChatRoomViewModel.LOADING_STATUS_MESSAGE_API)
        }
        sendTestMessages()
    }

    fun sendTestMessages(){
        CoroutineScope(Dispatchers.IO).launch {
            //FIXME: test only
            repeat(10) {
                val message = ChatRoomMessageHelper.randomChatMessage()
                sendMessage(message)
                delay((200 .. 1500).random().toLong())
            }
        }

    }

    fun testWelcomeNotifications(){
        CoroutineScope(Dispatchers.Main).launch {
            repeat(30){
                val welcomeMessage = ChatRoomMessageHelper.randomWelcomeNotification()
                getNotification(welcomeMessage)
                delay((200 .. 1000).random().toLong())
            }
        }
    }

}

data class StickerItem(val id: StickerId, val title: String, val icon: ImageUrl)

fun StickerItem.toSticker(): Sticker = Sticker(id, title, icon)

data class GiftItem(val id: GiftId, val title: String, val icon: ImageUrl, val price: Price) {
    val priceInDiamond: String
        get() = "${price}钻"
}

fun GiftItem.toGift(): Gift = Gift(id, title, icon, price)

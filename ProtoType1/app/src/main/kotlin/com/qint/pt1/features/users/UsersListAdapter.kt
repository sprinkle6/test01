/*
 * Copyright (c) 2019. QINT.TV
 * Author: Matthew Zhang
 * Created on: 3/22/19 10:10 PM
 */

package com.qint.pt1.features.users

import android.graphics.drawable.Animatable
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.qint.pt1.R
import com.qint.pt1.base.extension.inflate
import com.qint.pt1.base.extension.invisible
import com.qint.pt1.base.extension.loadFromUrl
import com.qint.pt1.base.extension.visible
import com.qint.pt1.base.navigation.Navigator
import com.qint.pt1.domain.Gender
import com.qint.pt1.domain.SkillCategory
import com.qint.pt1.domain.UserStatus
import com.qint.pt1.util.LOG_TAG
import kotlinx.android.synthetic.main.common_gender_age_tag.view.*
import kotlinx.android.synthetic.main.users_list_item.view.*
import kotlinx.coroutines.*
import org.jetbrains.anko.textColor
import javax.inject.Inject

class UsersListAdapter
@Inject constructor() :
    PagedListAdapter<UsersListViewItem, UsersListAdapter.ViewHolder>(UsersListViewItem.DIFF_CALLBACK) {

    /*
     * TODO: 首页的推荐和发现两个标签各自使用单独的VoicePlayer，可能同时播放两段声音。
     */
    internal var itemClickListener: (UsersListViewItem, Navigator.Extras) -> Unit = { _, _ -> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent.inflate(R.layout.users_list_item))

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val user = getItem(position)
        if(user != null){
            viewHolder.bind(user, itemClickListener)
        }else {
            // Null defines a placeholder item - PagedListAdapter will automatically invalidate
            // this row when the actual object is loaded from the database
            viewHolder.clear()
        }
    }

    private val voicePlayer = VoicePlayer()

    private var activePlayingItemViewHolder: ViewHolder? = null

    //stop the player if the fragment pause or stop
    fun stop() {
        voicePlayer.stop()
        activePlayingItemViewHolder = null
    }

    //release resources such as player
    fun release() {
        stop()
        voicePlayer.release()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private lateinit var bindedUsersViewItem: UsersListViewItem
        private var voiceTimeCountDownJob: Job? = null

        fun clear(){
            voiceTrailerButtonShowStop()
        }

        fun bind(
            usersViewItem: UsersListViewItem,
            clickListener: (UsersListViewItem, Navigator.Extras) -> Unit
        ) {
            bindedUsersViewItem = usersViewItem

            //根据绑定的数据重置activePlayingItemViewHolder状态，避免由于itemViewHolder回收重利用导致状态错误
            if(voicePlayer.isPlaying(bindedUsersViewItem)){
                activePlayingItemViewHolder = this
            }else if(activePlayingItemViewHolder == this){
                activePlayingItemViewHolder = null
            }

            updateView()

            itemView.setOnClickListener {
                clickListener(
                    usersViewItem,
                    Navigator.Extras(itemView.userAvatar)
                )
            }

            setupPlayer()
        }

        private fun updateView() {
            val usersViewItem = bindedUsersViewItem
            //头像及昵称
            itemView.userAvatar.loadFromUrl(usersViewItem.avatar)
            itemView.nickName.text = usersViewItem.nickName

            //技能标签
            if(usersViewItem.skillTag.isNotBlank()){
                itemView.skillTag.text = usersViewItem.skillTag
                when(usersViewItem.skillCategory){
                    SkillCategory.GAME -> {
                        itemView.skillTag.setBackgroundResource(R.drawable.skill_tag_background_game)
                        itemView.skillTag.textColor =
                            itemView.resources.getColor(R.color.main_yellow_f5)
                    }
                    SkillCategory.VOICE -> {
                        itemView.skillTag.setBackgroundResource(R.drawable.skill_tag_background_voice)
                        itemView.skillTag.textColor =
                            itemView.resources.getColor(R.color.main_white_f4)
                    }
                }
                itemView.skillTag.visible()
            }else{
                itemView.skillTag.invisible()
            }

            //设置位置
            if (usersViewItem.location.isNullOrBlank()) {
                itemView.locationIcon.invisible()
                itemView.location.invisible()
                itemView.locationDivider.invisible()
            } else {
                itemView.locationIcon.visible()
                itemView.location.visible()
                itemView.locationDivider.visible()
                itemView.location.text = usersViewItem.location.trim()
            }

            //设置在线状态
            val status: String =
                if (usersViewItem.status == UserStatus.OFFLINE) usersViewItem.lastAlive
                else usersViewItem.status.toString()
            itemView.status.text = status

            //设置性别年龄
            val genderAge = "${usersViewItem.gender.toIconString()} ${usersViewItem.age}"
            when (usersViewItem.gender) { //FIXME：控件不支持直接设置颜色，先用两个控件切换的方式临时hack一下
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

            //声明/签名
            itemView.declaration.text = usersViewItem.declaration

            setupVoiceTrailerButtonStatus()
        }

        //设置音频播放按钮状态
        private fun setupVoiceTrailerButtonStatus() {
            val usersViewItem = bindedUsersViewItem
            if (usersViewItem.audio.isAvailable()) {
                itemView.voiceTrailerButton.isClickable = true
                setVoiceTrailerButtonTimeRemained(usersViewItem.audio.durationInSeconds)
                itemView.voiceTrailerButton.visible()
                //TODO: 更精细化的处理，避免不必要的按钮动画状态变更操作
                if (voicePlayer.isPlaying(usersViewItem) && !voicePlayer.pausing) {
                    itemView.voiceTrailerButton.isChecked = true
                    startVoiceButtonAnimation()
                    startVoiceTimeCountDown()
                } else {
                    voiceTrailerButtonShowStop()
                }
            } else {
                itemView.voiceTrailerButton.invisible()
            }
        }

        private fun startVoiceButtonAnimation() {
            itemView.voiceTrailerButton?.setBackgroundResource(R.drawable.voice_play_animation)
            val playAnimation = itemView.voiceTrailerButton?.background
            if (playAnimation is Animatable) {
                playAnimation.start()
            }
        }

        private fun stopVoiceButtonAnimation() {
            val playAnimation = itemView.voiceTrailerButton?.background
            if (playAnimation is Animatable) {
                playAnimation.stop()
            }
            itemView.voiceTrailerButton?.setBackgroundResource(R.drawable.voice_trailer_button_background_selector)
        }

        private fun startVoiceTimeCountDown() {
            voiceTimeCountDownJob = CoroutineScope(Dispatchers.Main).launch {
                //因为回调可能在播放器线程发起，需要在主线程中执行UI操作
                setVoiceTrailerButtonTimeRemained(voicePlayer.durationRemindInSeconds())
                while (voicePlayer.isPlaying(bindedUsersViewItem) && voicePlayer.durationRemindInSeconds() >= 0) {
                    setVoiceTrailerButtonTimeRemained(voicePlayer.durationRemindInSeconds())
                    delay(1000)
                }
                setVoiceTrailerButtonTimeRemained(bindedUsersViewItem.audio.durationInSeconds)
            }
        }

        private fun stopVoiceTimeCountDown() {
            voiceTimeCountDownJob?.cancel()
            setVoiceTrailerButtonTimeRemained(bindedUsersViewItem.audio.durationInSeconds)
        }

        private val onPreparedCallback = {
            bindedUsersViewItem.audio.durationInSeconds = voicePlayer.totalDurationInSeconds()
            CoroutineScope(Dispatchers.Main).launch {
                itemView.voiceTrailerButton.isChecked = true //防止由于列表滚动和界面切换造成的状态混乱，强行重置一下
                startVoiceButtonAnimation()
            }
            startVoiceTimeCountDown()
        }

        private val onCompletionCallback = {
            voiceTrailerButtonShowStop()
            activePlayingItemViewHolder = null
        }

        private val onErrorCallback = { errorMessage: String ->
            //FIXME: should tell user about the error
            //TODO: should report/collect/analyse these errors
            voiceTrailerButtonShowStop()
            activePlayingItemViewHolder = null
        }

        private fun setupPlayer() {
            if (voicePlayer.isPlaying(bindedUsersViewItem)) {
                voicePlayer.onPreparedCallback = onPreparedCallback
                voicePlayer.onCompletionCallback = onCompletionCallback
                voicePlayer.onErrorCallback = onErrorCallback
            }

            itemView.voiceTrailerButton.setOnClickListener { //TODO: 改用onCheckedChangeListener以简化状态判断
                if (!voicePlayer.voicePlaying) { //not playing any voice
                    play()
                    return@setOnClickListener
                }

                //本条目在播放中，暂停或从暂停恢复继续播放
                if (voicePlayer.isPlaying(bindedUsersViewItem)) {
                    if (!voicePlayer.voicePlayerPrepared) {//连续点击，播放器还未准备好
                        itemView.voiceTrailerButton.isChecked = false
                        return@setOnClickListener
                    }
                    if (!voicePlayer.pausing) {
                        itemView.voiceTrailerButton.isChecked = false
                        stopVoiceButtonAnimation()
                        voicePlayer.pause()
                    } else {
                        itemView.voiceTrailerButton.isChecked = true
                        startVoiceButtonAnimation()
                        voicePlayer.resume()
                    }
                    return@setOnClickListener
                }

                //当前有其他用户的声音在播放，停止之前播放的声音，播放本条目用户声音
                play()
            }
        }

        private fun setVoiceTrailerButtonTimeRemained(timeRemained: Int) {
            val timeRemainedText = if(timeRemained > 0) "${timeRemained}s" else ""
            itemView.voiceTrailerButton.text = timeRemainedText
            itemView.voiceTrailerButton.textOn = timeRemainedText
            itemView.voiceTrailerButton.textOff = timeRemainedText
        }

        private fun play() {
            val usersViewItem = bindedUsersViewItem
            if(!usersViewItem.audio.isAvailable()) {
                itemView.voiceTrailerButton.isChecked = false
                return
            }
            if (voicePlayer.voicePlaying && !voicePlayer.isPlaying(usersViewItem)) { //已经在播放其他用户的声音，重置其按钮状态
                activePlayingItemViewHolder?.voiceTrailerButtonShowStop()
            }
            activePlayingItemViewHolder = this

            //绑定播放器事件回调到当前itemView
            voicePlayer.onPreparedCallback = onPreparedCallback
            voicePlayer.onCompletionCallback = onCompletionCallback
            voicePlayer.onErrorCallback = onErrorCallback

            voicePlayer.play(usersViewItem)
        }

        private fun voiceTrailerButtonShowStop(){
            itemView.voiceTrailerButton.isChecked = false
            stopVoiceButtonAnimation()
            stopVoiceTimeCountDown()
        }
    }
}

class VoicePlayer {
    private val voicePlayer = MediaPlayer()

    //播放状态标志，用于辅助判断处理播放声音逻辑
    private var activePlayingItem: UsersListViewItem? = null

    var voicePlayerPrepared = false
        private set

    var pausing = false
        private set

    //FIXME: 这些回调引用是否会造成资源泄露？
    lateinit var onPreparedCallback: () -> Unit
    lateinit var onCompletionCallback: () -> Unit
    lateinit var onErrorCallback: (errorMessage: String) -> Unit

    init {
        initVoicePlayer()
    }

    private fun initVoicePlayer() {
        voicePlayer.setAudioAttributes(
            AudioAttributes.Builder()
                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .build()
        )

        //TODO: 如果播放完成后紧接着再次播放同一段语音，这时应该可以不需要重新准备播放器数据源，可以直接再次播放以节省用户流量和CDN开销
        voicePlayer.setOnPreparedListener {
            voicePlayerPrepared = true
            voicePlayer.start()
            if (::onPreparedCallback.isInitialized) onPreparedCallback()
        }

        //TODO: 如果播放完成后紧接着再次播放同一段语音，这时应该可以不需要重新准备播放器数据源，可以直接再次播放以节省用户流量和CDN开销
        voicePlayer.setOnCompletionListener {
            if (::onCompletionCallback.isInitialized) onCompletionCallback()
            voicePlayerPrepared = false
            activePlayingItem = null
        }

        voicePlayer.setOnErrorListener { _, what, extra ->
            if (::onErrorCallback.isInitialized) onErrorCallback("VoicePlayer error: $what $extra")
            Log.e(LOG_TAG, "voicePlayer error: $what $extra")
            //TODO: should report/collect/analyse these errors
            voicePlayerPrepared = false
            activePlayingItem = null
            voicePlayer.reset()
            return@setOnErrorListener true
        }
    }

    //注意：此时播放器可能还在准备中，并未真正开始播放
    val voicePlaying: Boolean get() = activePlayingItem != null

    //注意：此时播放器可能还在准备中，并未真正开始播放
    fun isPlaying(usersViewItem: UsersListViewItem) =
        activePlayingItem?.id.equals(usersViewItem.id)

    fun play(usersViewItem: UsersListViewItem) {
        if (!usersViewItem.audio.isAvailable()) return

        voicePlayerPrepared = false
        activePlayingItem = usersViewItem
        pausing = false

        try {
            voicePlayer.reset()
            voicePlayer.setDataSource(usersViewItem.audio.url)
            voicePlayer.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(LOG_TAG, "play voice error: ${e.message}", e)
        }
    }

    fun pause() {
        if (!voicePlaying) return
        pausing = true
        voicePlayer.pause()
    }

    fun resume() {
        if (!voicePlaying || !pausing) return
        pausing = false
        voicePlayer.start()
    }

    //stop the player if the fragment pause or stop
    fun stop() {
        if (activePlayingItem != null && voicePlayerPrepared) voicePlayer.stop()
        voicePlayerPrepared = false
        activePlayingItem = null
    }

    //release resources such as player
    fun release() {
        voicePlayerPrepared = false
        activePlayingItem = null
        voicePlayer.release()
    }

    fun durationRemindInSeconds() =
        if (voicePlayerPrepared && voicePlayer.duration >= 0)
            (voicePlayer.duration - voicePlayer.currentPosition) / 1000
        else
            -1

    fun totalDurationInSeconds() =
        if (voicePlayerPrepared && voicePlayer.duration >= 0)
            voicePlayer.duration / 1000
        else -1

}
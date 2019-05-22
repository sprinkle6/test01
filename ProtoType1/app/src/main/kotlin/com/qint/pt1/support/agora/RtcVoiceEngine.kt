/*
 * Author: Matthew Zhang
 * Created on: 5/16/19 5:59 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.support.agora

import android.content.Context
import android.util.Log
import com.qint.pt1.base.extension.getAppCacheDir
import com.qint.pt1.base.platform.RequestCallback
import com.qint.pt1.domain.ChatRoomId
import com.qint.pt1.features.chatrooms.ChatRoomFailure
import com.qint.pt1.util.LOG_TAG
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine

object RtcVoiceEngine {
    //注意：RtcEngine在全应用只有一个实例
    //TODO: clean debug output
    private lateinit var rtcEngine: RtcEngine
    private var isInitialized = false
    private var isInChannel = false

    private lateinit var callback: RequestCallback<Unit>

    fun initAgoraEngineAndJoinChannel(context: Context, engine: RtcEngine, roomId: ChatRoomId, callback: RequestCallback<Unit>) {
        RtcVoiceEngine.callback = callback

        try {
            // 初始化用于音频的RtcEngine对象
            rtcEngine = engine
            val logFile = "${context.getAppCacheDir()}/log/agorasdk.log"
            rtcEngine.setLogFile(logFile)
            rtcEngine.setLogFilter(Constants.LOG_FILTER_ERROR)
        } catch (e: Exception) {
            e.printStackTrace()
            //throw RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e))
            callback.onFailure(ChatRoomFailure.VoiceEngineInitialazationFailure(e))
            return
        }

        closeMic()
        rtcEngine.setAudioProfile(Constants.AUDIO_PROFILE_MUSIC_STANDARD, Constants.AUDIO_SCENARIO_CHATROOM_ENTERTAINMENT)

        // 设置直播模式
        rtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)

        // 启动音量监听
        // 如果不需要显示其他人说话状态可先屏蔽
        //rtcEngine.enableAudioVolumeIndication(1000, 3)

        isInitialized = true

        // 当 joinChannel api 中填入 0 时，agora 服务器会生成一个唯一的随机数，并在 onJoinChannelSuccess 回调中返回
        //rtcEngine.joinChannel(chatRoomId, chatRoomName, "", 0)
        //FIXME: 改用token机制
        rtcEngine.joinChannel(null, roomId, "", 0)

    }

    fun leaveChannel() {
        if (isInitialized && isInChannel) rtcEngine.leaveChannel() //注意，此为异步操作，实际退出channel后会触发onLeaveChannel回调
    }

    fun destroy() {
        leaveChannel()
        if (isInitialized)
            RtcEngine.destroy()
        isInitialized = false
    }

    /**
     * 上麦
     */
    fun openMic() {
        rtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
    }

    /**
     * 下麦
     */
    fun closeMic() {
        rtcEngine.setClientRole(Constants.CLIENT_ROLE_AUDIENCE)
    }

    /**
     * 声网频道内业务回调
     */
    val rtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            //TODO
            Log.d(LOG_TAG, "rtcEngine: remote user ${uid} joined")
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            //TODO
            Log.d(LOG_TAG, "rtcEngine: remote user ${uid} leaved for reason ${reason}")
        }

        override fun onUserMuteAudio(uid: Int, muted: Boolean) {
            //TODO
            Log.d(LOG_TAG, "rtcEngine: user ${uid} muted")
        }

        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            // onJoinChannelSuccess 回调中，uid 不会为0
            // 当 joinChannel api 中填入 0 时，agora 服务器会生成一个唯一的随机数，并在 onJoinChannelSuccess 回调中返回
            assert(uid != 0)
            Log.d(LOG_TAG, "rtcEngine: join channel $channel sucess as uid ${uid}")
            isInChannel = true
            callback.onSuccess(Unit)
        }

        override fun onAudioVolumeIndication(speakers: Array<IRtcEngineEventHandler.AudioVolumeInfo>?, totalVolume: Int) {
            /**
             * 根据uid判断是他人还是自己， uid 0 默认是自己，根据 uid = 0 的取本地音量值，和joinchannelsuccess 内
             * 本地的 LocalUid 对应
             *
             */
            if (speakers != null) {
                for (audioVolumeInfo in speakers) {
                    if (audioVolumeInfo.uid != 0) {
                        /*val index = getUserIndex(audioVolumeInfo.uid)
                        if (index >= 0) {
                            userList.get(index).setAudioVolum(audioVolumeInfo.volume)
                        }*/
                        Log.d(LOG_TAG, "rtcEngine: remote volume change")
                    } else {
                        /*val index = getUserIndex(mLocalUid)
                        if (index >= 0) {
                            userList.get(index).setAudioVolum(audioVolumeInfo.volume)
                        }*/
                        Log.d(LOG_TAG, "rtcEngine: local volume change")
                    }
                }

            }
        }

        //TODO: implement onLeaveChannel callback
        override fun onLeaveChannel(stats: RtcStats?) {
            isInChannel = false
        }

        override fun onAudioMixingFinished() {
            //TODO
            super.onAudioMixingFinished()
            /** 伴奏播放结束时，将button 置为未选中状态  */
            Log.d(LOG_TAG, "rtcEngine: audio mixing finished")
        }

        override fun onError(code: Int) {
            Log.e(LOG_TAG, "rtcEngine: onError(${code})")
            callback.onFailure(ChatRoomFailure.VoiceEngineError(code))
        }
    }
}

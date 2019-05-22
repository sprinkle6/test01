/*
 * Copyright (c) 2019. QINT.TV
 * Author: Matthew Zhang
 * Created on: 3/27/19 2:43 PM
 */

package com.qint.pt1.features.profile

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder
import cafe.adriel.androidaudiorecorder.model.AudioChannel
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate
import cafe.adriel.androidaudiorecorder.model.AudioSource
import com.qint.pt1.R
import com.qint.pt1.base.exception.Failure
import com.qint.pt1.base.extension.loadFromUrl
import com.qint.pt1.base.extension.observe
import com.qint.pt1.base.extension.requestPermission
import com.qint.pt1.base.extension.viewModel
import com.qint.pt1.base.platform.BaseFragment
import com.qint.pt1.domain.ImageUrl
import com.qint.pt1.domain.User
import com.qint.pt1.domain.UserDataCategory
import com.qint.pt1.domain.UserId
import com.qint.pt1.util.LOG_TAG
import com.qint.pt1.support.aliyun.OSSHelper
import kotlinx.android.synthetic.main.profile_edit_fragment.*
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.support.v4.selector
import org.jetbrains.anko.support.v4.toast
import java.io.File

class UserProfileEditFragment: BaseFragment() {
    //TODO: 申请录音和拍照权限改用rxPermission
    private val REQUEST_GET_PHOTO = 1
    private val REQUEST_GET_VOICE = 2
    private val REQUEST_RECORD_AUDIO = 3
    private val REQUEST_TAKE_PHOTO = 4

    private val PERMISSION_REQ_CODE_RECORD_AUDIO = 11 //TODO: 是否应该集中到一处管理所有申请权限的request code？如果有重复code是否会产生冲突？
    private val PERMISSION_REQ_CODE_WRITE_EXTERNAL_STORAGE = 12
    private val PERMISSION_REQ_CODE_TAKE_PHOTO = 13

    override fun layoutId() = R.layout.profile_edit_fragment

    lateinit var viewModel: UserProfileViewModel

    private lateinit var imageUri: Uri
    private lateinit var voiceUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)

        viewModel = viewModel(viewModelFactory) {
            observe(userLiveData, ::render)
            observe(failureLiveData, ::handleFailure)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
    }

    private fun initializeView(){
        val userId: UserId = activity!!.intent.extras["userId"] as UserId
        viewModel.loadUserProfile(userId)
        setupButtons()
    }

    private fun setupButtons() {
        profilePhotoAddButton.setOnClickListener {
            selector("添加方式", listOf("拍照", "选择上传文件")){
                dialogInterface, i ->
                when(i){
                    0 -> takePhoto(it)
                    1 -> selectLocalPhoto()
                }
            }
        }
        voiceProfileButton.setOnClickListener {
            selector("添加方式", listOf("录音", "选择上传文件")) { dialogInterface, i ->
                when (i) {
                    0 -> recordAudio(it)
                    1 -> selectLocalVoice()
                }
            }
        }
    }

    private fun render(user: User?) {
        if (user == null) return
        loadProfilePhoto(user)
        nickName.text = user.profile.nickName
        declaration.text = user.profile.declaration
    }

    private fun loadProfilePhoto(user: User){
        var profilePhoto: ImageUrl = ""
        if (user.profile.profilePhotos.isNotEmpty())
            profilePhoto = user.profile.profilePhotos[0] //TODO: 加载显示所有当前的profile photo
        if (profilePhoto.isNotBlank()) profilePhoto1.loadFromUrl(profilePhoto)
    }

    private fun selectLocalPhoto() {
        //TODO: 支持相机
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "选择照片"), REQUEST_GET_PHOTO)
    }

    private fun selectLocalVoice() {
        //TODO: 支持录音机
        toast("上传语音资料")
        val intent = Intent()
        intent.type = "voice/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, REQUEST_GET_VOICE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.e(LOG_TAG, "UserProfileEdit onActivityResult: requestCode ${requestCode}, resultCode ${resultCode}")
        if(resultCode != RESULT_OK){
            Log.e(LOG_TAG, "UserProfileEdit onActivityResult error: requestCode ${requestCode}, resultCode ${resultCode}")
            viewModel.trigerFailure(when(requestCode){
                REQUEST_GET_PHOTO -> GetPhotoFailure(resultCode)
                REQUEST_GET_VOICE -> GetVoiceFailure(requestCode)
                REQUEST_RECORD_AUDIO -> {
                    try{
                        deleteUri(context!!, voiceUri)
                    }catch (_:Exception){}
                    RecordFailure(resultCode, "TODO")
                }
                REQUEST_TAKE_PHOTO -> {
                    try {
                        deleteUri(context!!, imageUri)
                    }catch (_:Exception){}
                    TakePhotoFailure(resultCode)
                }
                else -> Failure.UnknownError(
                    "UserProfileEdit onActivityResult get an unknown error result: requestCode ${requestCode}, resultCode ${resultCode}")
            })
            return
        }

        when (requestCode) {
            REQUEST_GET_PHOTO -> {
                val uri = data?.data ?: return
                Log.d(LOG_TAG, "UserProfileEdit onActivityResult photo uri: ${uri}")
                uploadProfilePhoto(uri)
            }
            REQUEST_GET_VOICE -> {
                val uri = data?.data ?: return
                Log.d(LOG_TAG, "UserProfileEdit onActivityResult voice uri: ${uri}")
                uploadProfileVoice(uri)
            }
            REQUEST_RECORD_AUDIO -> {
                //TODO: 录制的wav文件转为mp3后再上传
                //FIXME: 上传时添加文件时长
                uploadProfileVoice(voiceUri)
                //TODO: 上传完成后删除本地文件
            }
            REQUEST_TAKE_PHOTO -> {
                uploadProfilePhoto(imageUri)
            }
        }
    }

    private fun uploadProfilePhoto(uri: Uri) {
        //TODO: 控制内存消耗，改用glide或者参考https://www.jianshu.com/p/81e553fd0bc3
        val context = activity!!.applicationContext
        viewModel.uploadProfileFile(context, uri, UserDataCategory.PROFILE_PHOTO, OSSHelper.FileType.IMAGE)
    }

    private fun uploadProfileVoice(uri: Uri){
        //FIXME: 需要增加音频时长信息回传给红后服务器
        val context = activity!!.applicationContext
        viewModel.uploadProfileFile(context, uri, UserDataCategory.PROFILE_VOICE, OSSHelper.FileType.AUDIO)
    }

    private fun takePhoto(view: View){
        if(!activity!!.requestPermission(Manifest.permission.CAMERA, PERMISSION_REQ_CODE_TAKE_PHOTO)){
            view.longSnackbar("没有拍照权限，请重试")
            return
        }
        imageUri = createImageUri(context!!)
        val intent = Intent()
        intent.action = MediaStore.ACTION_IMAGE_CAPTURE
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intent, REQUEST_TAKE_PHOTO)
    }

    private fun createImageUri(context: Context): Uri{
        val name = "profile_photo" + System.currentTimeMillis()
        val cv = ContentValues()
        cv.put(MediaStore.Images.Media.TITLE, name)
        cv.put(MediaStore.Images.Media.DISPLAY_NAME, "${name}.jpeg")
        cv.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)
    }

    private fun recordAudio(view: View){
        //TODO：目前使用的录音控件还存在若干问题，最好替换或自行实现
        //2. 没有方法查询录音文件的时长
        //TODO: 转码为mp3
        //TODO: 上传后删除本地文件？
        activity!!.requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQ_CODE_WRITE_EXTERNAL_STORAGE)
        if(!activity!!.requestPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_CODE_RECORD_AUDIO)){
            view.longSnackbar("没有录音权限，请重试")
            return
        }
        val audioFilePath = getAudioFilePath()
        voiceUri = Uri.fromFile(File(audioFilePath))
        Log.d(LOG_TAG, "prepareing record to ${audioFilePath}")
        AndroidAudioRecorder.with(this)
            //Required
            .setFilePath(audioFilePath)
            .setColor(ContextCompat.getColor(context!!, R.color.recorder_bg))
            .setRequestCode(REQUEST_RECORD_AUDIO)
            //Optional
            .setSource(AudioSource.MIC)
            .setChannel(AudioChannel.STEREO)
            .setSampleRate(AudioSampleRate.HZ_48000)
            .setAutoStart(false)
            .setKeepDisplayOn(true)
            //Start record
            .recordFromFragment()
    }

    private fun getAudioFilePath(): String{
        val fileName = "profile_audio_${System.currentTimeMillis()}.wav"
        val dir: String = context!!.externalMediaDirs[0].path
        val path = "${dir}/${fileName}"
        return path
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) return
        when (requestCode) {
            PERMISSION_REQ_CODE_RECORD_AUDIO ->
                viewModel.trigerFailure(Failure.PermissionFailure(Manifest.permission.RECORD_AUDIO, "录制语音"))
            PERMISSION_REQ_CODE_WRITE_EXTERNAL_STORAGE ->
                viewModel.trigerFailure(Failure.PermissionFailure(Manifest.permission.WRITE_EXTERNAL_STORAGE, "保存录制的语音数据"))
        }
    }

    private fun deleteUri(context: Context, uri: Uri){
        context.contentResolver.delete(uri, null, null)
    }

}

class RecordFailure(val code: Int, message: String): Failure.FeatureFailure(message)
data class TakePhotoFailure(val code: Int): Failure.FeatureFailure("TODO")
data class GetPhotoFailure(val code: Int): Failure.FeatureFailure("TODO")
data class GetVoiceFailure(val code: Int): Failure.FeatureFailure("TODO")
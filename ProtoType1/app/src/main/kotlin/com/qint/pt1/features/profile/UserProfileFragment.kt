/*
 * Copyright (c) 2019. QINT.TV
 * Author: Matthew Zhang
 * Created on: 3/27/19 9:08 AM
 */

package com.qint.pt1.features.profile

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.qint.pt1.R
import com.qint.pt1.base.extension.invisible
import com.qint.pt1.base.extension.loadFromUrl
import com.qint.pt1.base.extension.observe
import com.qint.pt1.base.extension.viewModel
import com.qint.pt1.base.platform.BaseFragment
import com.qint.pt1.base.platform.BasePagerAdapter
import com.qint.pt1.domain.*
import com.qint.pt1.features.login.Login
import kotlinx.android.synthetic.main.profile_fragment.*
import org.jetbrains.anko.support.v4.toast
import javax.inject.Inject

class UserProfileFragment: BaseFragment() {
    override fun layoutId() = R.layout.profile_fragment

    lateinit var viewModel: UserProfileViewModel

    @Inject lateinit var login: Login

    private lateinit var pagerAdapter: UserProfilePagerAdapter

    private var editMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)

        pagerAdapter = UserProfilePagerAdapter(childFragmentManager)

        viewModel = viewModel(viewModelFactory) {
            observe(userLiveData, ::render)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profile_toolbar, menu)
        editMenuItem = menu.findItem(R.id.action_edit)
        editMenuItem?.isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_edit -> {
            switchToEdit()
            true
        }
        R.id.action_settings -> {
            toast("do settings")
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    private fun initializeView(){
        userProfilePager.adapter = pagerAdapter
        userProfileTab.setupWithViewPager(userProfilePager)
        setHasOptionsMenu(true)
        val userId: UserId = activity!!.intent.extras["userId"] as UserId
        viewModel.loadUserProfile(userId)
    }

    private fun render(user: User?) {
        if (user == null) return

        loadProfilePhoto(user)
        nickName.text = user.profile.nickName
        location.text = user.profile.location.toString()
        status.text = getVisitStatus(user.info)

        declaration.text = user.profile.declaration
        genderAge.text = getGenderAge(user.profile)
        rank.text = "Lv. ${user.info.nobleLevel}" //FIXME: rank应改为noble

        setButtons(user)
    }

    private fun loadProfilePhoto(user: User) {
        val profilePhoto: ImageUrl = user.getProfilePhoto()
        if (profilePhoto.isNotBlank()) profilePhotos.loadFromUrl(profilePhoto)
    }

    private fun getVisitStatus(userInfo: UserInfo) = when (userInfo.status) {
        UserStatus.CHATING -> "在聊天室"
        UserStatus.ONLINE -> "在线"
        UserStatus.OFFLINE -> if (userInfo.lastVisit.isBlank()) "不在线" else userInfo.lastVisit
    }

    private fun getGenderAge(userProfile: UserProfile) =
        "${userProfile.gender.toIconString()} ${userProfile.age}"

    private fun setButtons(user: User) {
        buttonEditProfile.invisible()
        if (login.account?.userId == user.id) {
            messageButton.invisible()
            buttonFollow.invisible()
            editMenuItem?.isVisible = true
/*
            buttonEditProfile.setOnClickListener {
                switchToEdit()
            }
            buttonEditProfile.visible()
*/
        }else{
            //buttonEditProfile.invisible()
            editMenuItem?.isVisible = false
        }
        //TODO: 如果已关注该用户则不显示关注按钮
    }

    private fun switchToEdit(){ //TODO：切换不太流畅，可能有更好的方法，或者避免重新加载数据
        val fm = activity!!.supportFragmentManager
        fm.beginTransaction().replace(R.id.fragmentContainer, UserProfileEditFragment()).addToBackStack(null).commit()
    }
}

class UserProfilePagerAdapter(fm: androidx.fragment.app.FragmentManager): BasePagerAdapter(fm){
    override fun pageTitles() = listOf("资料", "动态", "技能")
}
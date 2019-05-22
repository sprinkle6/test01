/*
 * Copyright (c) 2019. QINT.TV
 * Author: Matthew Zhang
 * Created on: 4/2/19 4:34 PM
 */

package com.qint.pt1.features.users

import android.content.Context
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ToggleButton
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.FlexboxLayout
import com.qint.pt1.R
import com.qint.pt1.base.exception.Failure
import com.qint.pt1.base.extension.*
import com.qint.pt1.base.navigation.Navigator
import com.qint.pt1.base.platform.BaseFragment
import com.qint.pt1.base.platform.OrderOption
import com.qint.pt1.domain.*
import com.qint.pt1.features.login.Login
import com.qint.pt1.util.LOG_TAG
import kotlinx.android.synthetic.main.common_empty.*
import kotlinx.android.synthetic.main.users_filter_list_fragment.*
import javax.inject.Inject

class UsersFilterListFragment : BaseFragment() {
    override fun layoutId() = R.layout.users_filter_list_fragment

    @Inject internal lateinit var login: Login
    @Inject internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var usersListAdapter: UsersListAdapter

    private lateinit var usersListViewModel: UsersListViewModel

    private lateinit var skillOptionsLayoutParams: FlexboxLayout.LayoutParams

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)

        usersListViewModel = viewModel(viewModelFactory) {
            observe(usersLiveData, ::renderUsersList)
            observe(skillMapLiveData, ::renderSkillOptions)
            failure(failureLiveData, ::handleFailure)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun onPause() {
        super.onPause()
        usersListAdapter.stop() //stop the voicePlayer
    }

    override fun onDestroyView() {
        super.onDestroyView()
        usersListAdapter.release() //To release the voice player
    }

    fun refresh() = usersListViewModel.refresh()

    private fun renderUsersList(pagedList: PagedList<UsersListViewItem>?) {
        usersListAdapter.submitList(pagedList)
        swipeRefresh.isRefreshing = false
    }

    private fun initView(){
        usersList.visible()
        hideOptionsPanel()
        emptyView.gone()
        showProgress()

        usersList.layoutManager = LinearLayoutManager(context)
        usersList.adapter = usersListAdapter
        usersListAdapter.itemClickListener = { usersViewItem, navigationExtras ->
            navigator.showUserProfile(activity!!, usersViewItem.id)
        }

        swipeRefresh.setOnRefreshListener { refresh() }

        initSelectors()
    }

    private fun initSelectors() {
        usersListViewModel.initSelectors()
        selectorContainer.gone()
        initLocationSelector()
        initGenderSelector()
        initPriceOrderSelector()
        initSkillSelector()
        initMask()
    }

    private fun initLocationSelector(){
        locationFilterButton.isChecked = false
        locationFilterButton.setOnCheckedChangeListener { buttonView, isChecked ->
            hideOptionsPanel()
            if (isChecked) setLocationFilter() else unsetLocationFilter()
            refresh()
        }
    }

    private fun setLocationFilter(){
        Log.d(LOG_TAG, "setting location filter: ${login.location}")
        val selector = usersListViewModel.locationSelector
        val location = login.location
        if(location != null) {
            locationFilterButton.isChecked = true
            locationFilterButton.textOn = location.province
            selector.select(LocationOption.LOCAL_PROVINCE)
            selector.location = location
            usersListViewModel.setSelector(selector)
        }else unsetLocationFilter()
    }

    private fun unsetLocationFilter(){
        Log.d(LOG_TAG, "unsetting location filter")
        val selector = usersListViewModel.locationSelector
        locationFilterButton.isChecked = false
        locationFilterButton.textOff = "地域不限"
        selector.select(LocationOption.UNLIMITED)
        selector.location = Location.NullLocation
        usersListViewModel.unsetSelector(selector)
    }

    private fun initPriceOrderSelector(){
        priceOrderButton.isChecked = false
        val selector = usersListViewModel.priceOrderSelector
        priceOrderButton.setOnClickListener { view ->
            hideOptionsPanel()
            when (selector.getSelectedOption()) {
                OrderOption.UNSET -> { //未设置 -> 升序
                    priceOrderButton.textOn = "价格升序" //注意：一定要先设置textOn文字再设置isChecked属性，否则文字设置不立即生效
                    priceOrderButton.isChecked = true
                    priceOrderButton.setIconColor(R.color.main_blue_c1, R.drawable.triangle_up)
                    selector.select(OrderOption.ASC)
                    usersListViewModel.setSelector(selector)
                }
                OrderOption.ASC -> { //升序 -> 降序
                    priceOrderButton.textOn = "价格降序"
                    priceOrderButton.isChecked = true
                    priceOrderButton.setIconColor(R.color.main_blue_c1)
                    selector.select(OrderOption.DESC)
                    usersListViewModel.setSelector(selector)
                }
                OrderOption.DESC -> { //降序 -> 未设置
                    priceOrderButton.textOff = "价格排序"
                    priceOrderButton.isChecked = false
                    priceOrderButton.setIconColor(R.color.main_gray_f2)
                    selector.reset()
                    usersListViewModel.unsetSelector(selector)
                }
            }
            refresh()
        }
    }

    private fun ToggleButton.setIconColor(color: Int, icon: Int =R.drawable.triangle_down){
        val drawable = resources.getDrawable(icon, null).mutate()
        drawable.setColorFilter(resources.getColor(color), PorterDuff.Mode.SRC_ATOP)
        //drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        //this.setCompoundDrawables(null, null, drawable, null)
        //在没有设置rectangle bound的情况下需要用setCompoundDrawablesWithIntrinsicBounds代替setCompoundDrawables
        this.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
    }

    private fun initGenderSelector() {
        val selector = usersListViewModel.genderSelector

        genderFilterButton.setOnClickListener { view ->
            when(genderOptions.isVisible()){
                true -> hideGenderOptions()
                false -> showGenderOptions()
            }
            resetGenderFilterButton()
        }

        genderOptions.gone()
        genderOptions.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                R.id.genderOptionMale -> {
                    genderFilterButton.setIconColor(R.color.main_blue_c1)
                    selector.select(Gender.MALE)
                }
                R.id.genderOptionFemale -> {
                    genderFilterButton.setIconColor(R.color.main_pink_c2)
                    selector.select(Gender.FAMALE)
                }
                R.id.genderOptionUnlimit -> {
                    genderFilterButton.setIconColor(R.color.main_gray_f2)
                    selector.select(Gender.UNKNOWN)
                }
            }
            hideGenderOptions()
            resetGenderFilterButton()
            usersListViewModel.setSelector(selector)
            refresh()
        }
    }

    private fun resetGenderFilterButton(){
        val selector = usersListViewModel.genderSelector
        if(selector.getSelectedOption() != Gender.UNKNOWN) {
            genderFilterButton.textOn = selector.getSelectedOption().toSelectorOptionLabel()
            genderFilterButton.isChecked = true
        }else{
            genderFilterButton.textOff = selector.getSelectedOption().toSelectorOptionLabel()
            genderFilterButton.isChecked = false
        }
    }

    private fun showGenderOptions(){
        genderOptions.visible()
        skillOptionsPanel.gone()
        selectorContainer.visible()
    }

    private fun hideGenderOptions(){
        genderOptions.gone()
        selectorContainer.gone()
    }

    private fun hideOptionsPanel(){
        genderOptions.gone()
        skillOptionsPanel.gone()
        selectorContainer.gone()
    }

    private fun initSkillSelector() {
        skillFilterButton.setOnClickListener { view ->
            when(skillOptionsPanel.isVisible()){
                true -> confirmSkillOption()
                false -> showSkillOptions()
            }
            resetSkillFilterButton()
        }

        initConfirmSkillOptionButton()
        initResetSkillOptionButton()

        initSkillOptionGroupStyle()

        skillGradePanel.gone()
        skillOptionsPanel.gone()
    }

    private fun initConfirmSkillOptionButton(){
        confirmSkillOptionButton.setOnClickListener{confirmSkillOption()}
    }

    private fun confirmSkillOption(){
        val skillSelector = usersListViewModel.skillSelector
        val skillGradeSelector = usersListViewModel.skillGradeSelector
        if(skillSelector.getSelectedOption() != Skill.NullSkill){
            usersListViewModel.setSelector(skillSelector)
            skillFilterButton.setIconColor(R.color.main_blue_c1)
            if (skillGradeSelector.getSelectedOption() != SkillGrade.NullSkillGrade &&
                skillGradeSelector.getSelectedOption() != SkillGrade.AllSkillGrade
            ) {
                usersListViewModel.setSelector(skillGradeSelector)
            } else {
                usersListViewModel.unsetSelector(skillGradeSelector)
            }
        }else{
            usersListViewModel.unsetSelector(skillSelector)
            usersListViewModel.unsetSelector(skillGradeSelector)
            skillFilterButton.setIconColor(R.color.main_gray_f2)
        }
        refresh() //TODO: 如果筛选条件没有变化则无需刷新数据
        hideOptionsPanel()
        resetSkillFilterButton()
    }

    private fun initResetSkillOptionButton(){
        val skillSelector = usersListViewModel.skillSelector
        val skillGradeSelector = usersListViewModel.skillGradeSelector

        resetSkillOptionButton.setOnClickListener {
            skillSelector.reset()
            skillGradeSelector.reset()
            usersListViewModel.unsetSelector(skillSelector)
            usersListViewModel.unsetSelector(skillGradeSelector)

            skillOptions.clearCheck()
            skillGradeOptions.removeAllViews()
            skillGradePanel.gone()
            resetSkillFilterButton()
        }
    }

    private fun initSkillOptionGroupStyle(){
        skillOptionsLayoutParams = FlexboxLayout.LayoutParams(skillGradeOptions.layoutParams)
        skillOptionsLayoutParams.bottomMargin = baseActivity.dp2px(16).toInt()
        skillOptionsLayoutParams.rightMargin = baseActivity.dp2px(8).toInt()
        skillOptionsLayoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
    }

    private fun resetSkillFilterButton(){
        val selector = usersListViewModel.skillSelector
        if(selector.getSelectedOption() != Skill.NullSkill){
            skillFilterButton.textOn = selector.getSelectedOption().toSelectorOptionLabel()
            skillFilterButton.isChecked = true
        }else{
            skillFilterButton.textOff = "技能"
            skillFilterButton.isChecked = false
        }
    }

    private fun showSkillOptions(){
        skillOptionsPanel.visible()
        genderOptions.gone()
        selectorContainer.visible()
    }

    private fun hideSkillOptions(){
        skillOptionsPanel.gone()
        selectorContainer.gone()
    }

    private fun renderSkillOptions(skillMap: SkillMap?) {
        if (skillMap == null || skillMap.isEmpty()) return

        val gameSkillRoads =
            skillMap.branches.find { it.category == SkillCategory.GAME }?.skillRoads
        val voiceSkillRoads =
            skillMap.branches.find { it.category == SkillCategory.VOICE }?.skillRoads

        val styleContextWrapper =
            ContextThemeWrapper(context, R.style.SelectorGridOptionRadioButtonStyle)

        //将gameSkillOptions和voiceSkillOptions这两个内层RadioButtonGroup从外层RadioButtonGroup skillOptions中移除再加入，
        //以使得动态创建的RadioButton都可以被skillOptions感知到，从而被统一管理，避免两组中的按钮可以同时被选择。
        skillOptions.removeView(gameSkillOptions)
        skillOptions.removeView(voiceSkillOptions)

        gameSkillOptions.removeAllViews()
        voiceSkillOptions.removeAllViews()

        gameSkillRoads?.map { makeSkillOptionButton(it, styleContextWrapper) }
            ?.forEach { gameSkillOptions.addView(it, skillOptionsLayoutParams) }
        voiceSkillRoads?.map { makeSkillOptionButton(it, styleContextWrapper) }
            ?.forEach { voiceSkillOptions.addView(it, skillOptionsLayoutParams) }

        skillOptions.addView(gameSkillOptions, 1)
        skillOptions.addView(voiceSkillOptions)
    }

    private fun makeSkillOptionButton(skillRoad: SkillRoad, context: Context): AppCompatRadioButton {
        val skillSelector = usersListViewModel.skillSelector
        val skillButton = makeOptionButton(skillRoad.skill.title, context)
        skillButton.setOnClickListener {
            skillSelector.select(skillRoad.skill)
            resetSkillGradeOptions(skillRoad, context)
        }
        return skillButton
    }

    private fun resetSkillGradeOptions(skillRoad: SkillRoad, context: Context){
        val skillGradeSelector = usersListViewModel.skillGradeSelector
        skillGradeSelector.reset()
        skillGradeSelector.skill = skillRoad.skill
        if(skillRoad.grades.isNotEmpty()) {
            skillGradeOptions.removeAllViews()
            skillRoad.grades.forEach { grade ->
                val skillGradeButton = makeOptionButton(grade.gradeTitle, context)
                skillGradeButton.setOnClickListener {
                    if (grade != SkillGrade.AllSkillGrade) {
                        skillGradeSelector.select(grade)
                    } else {
                        skillGradeSelector.reset()
                    }
                }
                skillGradeOptions.addView(skillGradeButton, skillOptionsLayoutParams)
            }
            skillGradePanel.visible()
        }else{
            skillGradePanel.gone()
        }
    }

    private fun makeOptionButton(text: String, context: Context): AppCompatRadioButton {
        //程序生成的控件没法直接设置style属性，只能先把控件放在一个xml里设置，再展开。
        val optionButton: AppCompatRadioButton = LayoutInflater.from(context).inflate(
            R.layout.common_option_button, null
        ) as AppCompatRadioButton
        optionButton.text = text
        return optionButton
    }

    private fun initMask(){
        //TODO: 隐藏选项面板的同时取消未确认的选项
        selectorMask.setOnClickListener { hideOptionsPanel() }
    }

    override fun handleFailure(failure: Failure?) {
        renderFailure(failure ?: return, "获取数据失败，请重试", {view -> initView()})
    }

    override fun renderFailure(failure: Failure, actionPrompt: String, action: (View) -> Unit) {
        usersList.gone()
        hideOptionsPanel()
        emptyView.visible()
        hideProgress()
        super.renderFailure(failure, actionPrompt, action)
    }

}
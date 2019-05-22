/*
 * Copyright (c) 2019. QINT.TV
 * Author: Matthew Zhang
 * Created on: 3/22/19 9:47 PM
 */

package com.qint.pt1.features.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.qint.pt1.base.exception.Failure
import com.qint.pt1.base.exception.FailureHandler
import com.qint.pt1.base.extension.getDefaultPagedListConfig
import com.qint.pt1.base.platform.BaseViewModel
import com.qint.pt1.base.platform.Selector
import com.qint.pt1.domain.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject

class UsersListViewModel
@Inject constructor(private val userListDataSourceFactory: UserListDataSourceFactory) : BaseViewModel() {
    private val executor: Executor = Executors.newFixedThreadPool(3)

    private val pageListConfig = getDefaultPagedListConfig()

    init{
        userListDataSourceFactory.failureHandler = UsersListFailureHandler()
    }
    val usersLiveData: LiveData<PagedList<UsersListViewItem>> =
        LivePagedListBuilder(userListDataSourceFactory, pageListConfig).setFetchExecutor(executor).build()

    val skillMapLiveData: MutableLiveData<SkillMap> = MutableLiveData()

    @Inject internal lateinit var usersRepository: UsersRepository

    val locationSelector = LocationSelector(Location.NullLocation)
    val genderSelector = GenderSelector()
    val priceOrderSelector = PriceOrderSelector()
    val skillSelector = SkillSelector()
    val skillGradeSelector = SkillGradeSelector(Skill.NullSkill)

    private val currentSelectors: MutableSet<Selector<*,*>> = mutableSetOf()

    fun setSelector(selector: Selector<*,*>) = currentSelectors.add(selector)
    fun unsetSelector(selector: Selector<*,*>) = currentSelectors.remove(selector)

    fun refresh(){
        userListDataSourceFactory.selectors = currentSelectors
        usersLiveData.value?.dataSource?.invalidate()
    }

    fun initSelectors(){
        CoroutineScope(Dispatchers.IO).launch {
            usersRepository.skillRoads().either(
                { failure -> trigerFailure(failure) },
                { skillRoads -> launch(Dispatchers.Main){initSkillMap(skillRoads) }}
            )
        }
    }

    //TODO: 也许移到Repository中更合理
    //TODO: 这类metadata（包括表情、礼物列表等）应当本地缓存一份
    fun initSkillMap(skillroads: List<SkillRoad>){
        val gameSkillRoads = mutableListOf<SkillRoad>()
        val voiceSkillRoads = mutableListOf<SkillRoad>()
        skillroads.forEach {
            when(it.skill.category){
                SkillCategory.GAME -> gameSkillRoads.add(it)
                SkillCategory.VOICE -> voiceSkillRoads.add(it)
            }
        }
        SkillMap.branches.add(SkillMapBranch(SkillCategory.GAME, gameSkillRoads))
        SkillMap.branches.add(SkillMapBranch(SkillCategory.VOICE, voiceSkillRoads))
        skillMapLiveData.value = SkillMap
    }

    /*
     * 用于传入DataSource中执行Failure处理
    */
    inner class UsersListFailureHandler: FailureHandler.NoopFailureHandler(){
        override fun handleFailure(failure: Failure?) {
            trigerFailure(failure ?: return)
        }
    }

}

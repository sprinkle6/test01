/*
 * Author: Matthew Zhang
 * Created on: 4/11/19 11:06 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.features.users

import com.qint.pt1.base.platform.BaseSelector
import com.qint.pt1.base.platform.OrderOption
import com.qint.pt1.domain.*

enum class LocationOption{
    LOCAL_PROVINCE, LOCAL_CITY, UNLIMITED;
}

class LocationSelector(var location: Location): BaseSelector<LocationOption, String>("location", LocationOption.UNLIMITED){
    override fun getOptions() = listOf(LocationOption.LOCAL_PROVINCE, LocationOption.UNLIMITED)

    override fun getSelectedValue() = when(getSelectedOption()){
        LocationOption.LOCAL_PROVINCE -> location.province
        LocationOption.LOCAL_CITY -> location.city
        LocationOption.UNLIMITED -> ""
    }

    override fun reset() {
        super.reset()
        location = Location.NullLocation
    }
}

class GenderSelector : BaseSelector<Gender, String>("gender", Gender.UNKNOWN){
    override fun getOptions() = Gender.values().toList()
    override fun getSelectedValue() = getSelectedOption().toSelectorOptionValue()
}

fun Gender.toSelectorOptionLabel() = when(this){
    Gender.UNKNOWN -> "性别不限"
    Gender.MALE -> "小哥哥"
    Gender.FAMALE -> "小姐姐"
}

fun Gender.toSelectorOptionValue() = when(this){
    Gender.UNKNOWN -> "ALL"
    Gender.MALE -> "M"
    Gender.FAMALE -> "F"
}

class PriceOrderSelector: BaseSelector<OrderOption, Boolean>("price", OrderOption.UNSET){
    override fun getOptions() = OrderOption.values().toList()
    override fun getSelectedValue() = getSelectedOption().toSelectorOptionValue()
}

fun OrderOption.toSelectorOptionValue() = when(this){
    OrderOption.UNSET -> false
    OrderOption.ASC -> false
    OrderOption.DESC -> true
}

class SkillSelector: BaseSelector<Skill, SkillId>("skill", Skill.NullSkill){
    override fun getOptions() = SkillMap.allSkills
    override fun getSelectedValue() = getSelectedOption().id
}

class SkillGradeSelector(var skill: Skill): BaseSelector<SkillGrade, SkillGradeId>("skill_grade", SkillGrade.AllSkillGrade){
    override fun getOptions() = SkillMap.getSkillRoad(skill)?.grades ?: emptyList()
    override fun getSelectedValue() = getSelectedOption().gradeId
    override fun reset() {
        super.reset()
        skill = Skill.NullSkill
    }
}

fun Skill.toSelectorOptionLabel() = this.title


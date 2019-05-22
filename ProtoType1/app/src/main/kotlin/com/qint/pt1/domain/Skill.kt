/*
 * Copyright (c) 2019. QINT.TV
 * Author: Matthew Zhang
 * Created on: 3/26/19 1:34 PM
 */

package com.qint.pt1.domain

import com.qint.pt1.base.extension.empty

typealias SkillGradeId = String

data class SkillGrade(val gradeTitle: Title){
    var skillId: SkillId = SkillId.empty()
    var gradeId: SkillGradeId = SkillGradeId.empty()

    companion object {
        val NullSkillGrade = SkillGrade("Null SkillGrade")
        val AllSkillGrade = SkillGrade("全部")
        fun empty() = NullSkillGrade
    }
}

data class Skill(val id: SkillId, val category: SkillCategory){
    var title: Title = ""
    var description: String = ""
    var priority: Int = 0
    var icon: ImageUrl? = null
    var skillInfoImg: ImageUrl? = null
    var audio: AudioResource? = null

    companion object {
        val NullSkill = Skill("Null Skill", SkillCategory.empty())
        fun empty() = NullSkill
    }
}

enum class SkillCategory{
    GAME, VOICE, OTHER;

    companion object{
        fun empty() = OTHER

        fun fromString(category: String) = when(category.toUpperCase()){
            "GAME" -> GAME
            "VOICE" -> VOICE
            else -> OTHER
        }

    }
}

/*
 * 用户发展的某项技能，有唯一确定的级别
 */
data class SkillCareer(
    val skill: Skill,
    var grade: SkillGrade
){
    init{
        grade.skillId = skill.id
    }

    companion object {
        fun empty() = SkillCareer(Skill.empty(), SkillGrade.empty())
    }
}

/*
 * 某项技能的级别发展路径
 */
data class SkillRoad(
    val skill: Skill,
    val grades: List<SkillGrade>
){
    companion object {
        val NullSkillRoad = SkillRoad(Skill.NullSkill, emptyList())
        fun empty() = NullSkillRoad
    }
}

/*
 * 技能树的分支，一个分支对应一个大的技能类别，下含该类别的所有SkillRoad
 */
data class SkillMapBranch(val category: SkillCategory, var skillRoads: List<SkillRoad>)

/*
 * 技能地图，包含所有的技能分支
 */
object SkillMap{
    val branches: MutableList<SkillMapBranch> = mutableListOf()

    fun isEmpty() = branches.isEmpty()

    val allSkills: List<Skill> get() = branches.map { it.skillRoads }.flatten().map { it.skill }

    val allSkillRoads: List<SkillRoad> get() = branches.map { it.skillRoads }.flatten()

    fun getSkillRoad(skill: Skill): SkillRoad? = allSkillRoads.find { it.skill == skill }
}
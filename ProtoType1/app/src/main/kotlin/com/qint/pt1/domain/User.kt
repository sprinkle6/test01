package com.qint.pt1.domain

import com.qint.pt1.base.extension.empty

typealias Age = Int
typealias LastAlive = String
typealias LastVisit = LastAlive
typealias NobleLevel = Level //爵位
typealias VIPLevel = Level
typealias Phone = String
typealias UserStar = String
typealias UserCareer = String

data class User(val id: UserId){
    constructor(): this("nullUser")

    constructor(id: UserId, nickName: NickName, avatar: Avatar): this(id){
        profile.nickName = nickName
        profile.avatar = avatar
    }

    val profile: UserProfile = UserProfile(id)
    val info: UserInfo = UserInfo(id)

    companion object {
        val NullUser = User()
        fun empty() = NullUser
    }
}

/*
 * 相对静态的用户数据，用户基本信息
*/
data class UserProfile(val id: UserId){
    var nickName: NickName = NickName.empty()
    var gender: Gender = Gender.UNKNOWN
    var age: Age = 0
    var avatar: Avatar = Avatar.empty()
    var location: Location = Location.empty() //用户填的地区，不是用户当前实际物理位置。
    var declaration: String = "他/她什么也不想说"
    var career: UserCareer = UserCareer.empty()
    var description: String = "没有什么可说的"
    var primarySkillTag: String = ""
    var promotionAudio: AudioResource = AudioResource.empty()
    val profilePhotos: MutableList<ImageUrl> = mutableListOf()
    var primarySkillCategory: SkillCategory = SkillCategory.OTHER
}

/*
 * 会随用户使用活动变化的用户数据
 */
data class UserInfo(val id: UserId){
    var status: UserStatus = UserStatus.empty()
    var lastVisit: LastVisit = LastVisit.empty()
    var star: UserStar = UserStar.empty()
    var nobleLevel: NobleLevel = Level.EMPTY_LEVEL
    var vipLevel: VIPLevel = Level.EMPTY_LEVEL
    var freshMan: Boolean = false
    var fansNumber: Int = 0
    var giftsNumber: Int = 0
    val gifts: MutableList<GiftPack> = mutableListOf()
    val skills: MutableList<SkillCareer> = mutableListOf()
    val statePhotos: MutableList<ImageUrl> = mutableListOf()
    var currentLocation: Location = Location.empty()
}

enum class UserStatus {
    OFFLINE, ONLINE, CHATING;

    companion object {
        val NullStatus = OFFLINE
        fun empty() = NullStatus
    }

    override fun toString(): String {
        //TODO: maybe this should be move to view or viewmodel layer as extension function
        return when (this) {
            OFFLINE -> "不在线"
            ONLINE -> "在线"
            CHATING -> "在聊天室"
        }
    }
}

enum class Gender {
    UNKNOWN, MALE, FAMALE;

    companion object {
        val NullGender = UNKNOWN
        fun empty() = NullGender

        fun fromString(str: String): Gender = when (str.toLowerCase()) {
            "男", "male", "man", "m", "♂" -> MALE
            "女", "famale", "woman", "f", "♀" -> FAMALE
            else -> UNKNOWN
        }

    }

    override fun toString(): String {
        return toIconString()
    }

    fun toIconString() = when (this) {
        UNKNOWN -> ""
        MALE -> "♂"
        FAMALE -> "♀"
    }

    fun toMessageString() = when(this){
        UNKNOWN -> "U"
        MALE -> "M"
        FAMALE -> "F"
    }
}

/*
 * 用于辅助用户图片、语音、视频等数据存储时分类
 */
enum class UserDataCategory(val categoryPath: String){
    AVATAR("avatar"),
    PROFILE_PHOTO("profile-photo"),
    PROFILE_VOICE("profile-voice"),
    SKILL_PICTURE("skill-picture"),
    STATE_PHOTO("state-photo")
}

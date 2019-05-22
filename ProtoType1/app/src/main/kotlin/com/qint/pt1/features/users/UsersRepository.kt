package com.qint.pt1.features.users

import android.util.Log
import com.qint.pt1.api.sys.MetaData
import com.qint.pt1.base.exception.Failure
import com.qint.pt1.base.extension.empty
import com.qint.pt1.base.functional.Either
import com.qint.pt1.base.platform.APIHandler
import com.qint.pt1.base.platform.NullSelector
import com.qint.pt1.base.platform.ResourceHandler
import com.qint.pt1.base.platform.Selector
import com.qint.pt1.domain.*
import com.qint.pt1.features.login.LoginFailure
import com.qint.pt1.support.aliyun.OSSToken
import com.qint.pt1.util.DEFAULT_ITEM_NUM_PER_PAGE
import com.qint.pt1.util.LOG_TAG
import proto_def.SysMessage
import proto_def.UserMessage
import javax.inject.Inject

interface UsersRepository {
    fun users(page: Int = 0, numPerPage: Int = DEFAULT_ITEM_NUM_PER_PAGE, selectors: Set<Selector<*,*>> = emptySet()): Either<Failure, List<User>>
    fun userDetails(userId: UserId, loginUserId: UserId = UserId.empty()): Either<Failure, User>
    fun login(phone: String, verifyCode: String): Either<Failure, Account>
    fun applyOSSToken(): Either<Failure, OSSToken>
    fun updateLocation(account: Account, location: Location): Either<Failure, Boolean>
    fun skillRoads(): Either<Failure, List<SkillRoad>>
    fun skillRoad(skillId: SkillId): Either<Failure, SkillRoad>
    fun search(keyword: String, page: Int = 0, numPerPage: Int = DEFAULT_ITEM_NUM_PER_PAGE): Either<Failure, List<User>>

    class Network
    @Inject constructor(private val resourceHandler: ResourceHandler,
                        private val apiHandler: APIHandler,
                        private val service: UsersService) : UsersRepository {

        override fun search(keyword: String, page: Int, numPerPage: Int): Either<Failure, List<User>> {
            if(keyword.isBlank()) return Either.Right(emptyList())

            val req = UserMessage.SearchReq.newBuilder()
                .setKeyword(keyword)
                .setPageNumber(page)
                .setNumPerPage(numPerPage)
                .build()
            return resourceHandler.request(service.search(req), {it.usersList.map{it.toUser()}})
        }

        override fun skillRoads(): Either<Failure, List<SkillRoad>> {
            val req = SysMessage.SkillListReq.newBuilder()
                .setSkillId("")
                .build()
            return resourceHandler.request(
                service.skillRoads(req),
                {it.skillsList.map { it.toSkillRoad() }},
                MetaData.SKILLROADS_CACHE_KEY
            )
        }

        //FIXME: 考虑去除此方法，仅保留不带参数的版本
        override fun skillRoad(skillId: SkillId): Either<Failure, SkillRoad> {
            val req = SysMessage.SkillListReq.newBuilder()
                .setSkillId(skillId)
                .build()
            return resourceHandler.request(
                service.skillRoads(req),
                {it.skillsList.map { it.toSkillRoad() }.firstOrNull() ?: SkillRoad.NullSkillRoad},
                "${MetaData.SKILLROADS_CACHE_KEY}.${skillId}"
            )
        }

        /*
         * 按与服务端约定，上报以空格分隔的省、市名称
         */
        override fun updateLocation(account: Account, location: Location): Either<Failure, Boolean> {
            //TODO: 迁移到UserAPI
            val locationData = location.toProvinceAndCityString()
            val req = UserMessage.LocationUpdateReq.newBuilder()
                .setUid(account.userId)
                .setToken(account.getToken(Account.TokenType.REDQUEEN_TOKEN))
                .setLocation(locationData)
                .build()
            return apiHandler.request(
                service.updateLocation(req),
                {it.status == UserMessage.STATUS.OK}
            )
        }

        override fun login(phone: String, verifyCode: String): Either<Failure, Account> {
            //TODO: 迁移到UserAPI
            val req = UserMessage.LoginReq.newBuilder()
                .setPhone(phone)
                .setSmsCode(verifyCode)
                .build()
            val result = apiHandler.request(service.login(req))
            return result.either(
                {failure ->  Either.Left(failure)},
                {resp -> when(resp.code){
                    UserMessage.LoginResp.STATUS.OK -> {
                        Log.d(LOG_TAG, "LoginResp: code:${resp.code}, uid:${resp.uid}, nick:${resp.nickName}, phone:${resp.phone}, token:${resp.token}, avatar:${resp.avatar}")
                        Either.Right(resp.toAccount())
                    }
                    UserMessage.LoginResp.STATUS.SMS_TIMEOUT, //TODO: 细分返回不同类型错误并处理提示
                    UserMessage.LoginResp.STATUS.SMS_ERROR,
                    UserMessage.LoginResp.STATUS.UNRECOGNIZED,
                    null -> {
                        Log.e(LOG_TAG, "call to RedQueen.login error, status code: ${resp.code}")
                        Either.Left(LoginFailure.LoginFailed)
                    }
                    else -> {
                        Log.e(LOG_TAG, "call to RedQueen.login error, status code: ${resp.code}")
                        Either.Left(LoginFailure.LoginFailed)
                    }
                }}
            ) as Either<Failure, Account>
        }

        private fun UserMessage.BanbanListReq.Builder.addSelector(selector: Selector<*, String>){
            Log.d(LOG_TAG, "Selector: ${selector.getName()} ${selector.getSelectedValue()}")
            if(selector.getSelectedValue().isNotBlank())
                this.addFiltersBuilder().setField(selector.getName()).value = selector.getSelectedValue()
        }

        override fun users(page: Int, numPerPage: Int, selectors: Set<Selector<*,*>>): Either<Failure, List<User>> {
            Log.d(LOG_TAG, "UsersRepository.users($page, $numPerPage)")
            val requestBuilder = UserMessage.BanbanListReq.newBuilder()
                .setUserId("") //FIXME: 如果用户已登录应传入用户id，用于个性化推荐
                .setPageNumber(page)
                .setNumPerPage(numPerPage)

            selectors.forEach { selector ->
                when (selector) {
                    is LocationSelector -> requestBuilder.addSelector(selector)
                    is GenderSelector -> requestBuilder.addSelector(selector)
                    is PriceOrderSelector -> {
                        Log.d(LOG_TAG, "Selector: ${selector.getName()} ${selector.getSelectedValue()}")
                        requestBuilder.orderBy = selector.getName()
                        requestBuilder.desc = selector.getSelectedValue()
                    }
                    is SkillSelector -> requestBuilder.addSelector(selector)
                    is SkillGradeSelector -> requestBuilder.addSelector(selector)
                    is NullSelector -> {
                        Log.d(LOG_TAG, "Null Selector: ${selector.getName()} ${selector.getSelectedOption()}")
                    }
                    else -> {
                        Log.d(LOG_TAG, "UnSupported Selector: ${selector.getName()} ${selector.getSelectedOption()}")
                    }
                }
            }
            val req = requestBuilder.build()

            return resourceHandler.request(
                service.recommendUsers(req),
                { it.banbansList.map { it.toUser() } }
            )
        }

        override fun userDetails(userId: UserId, loginUserId: UserId): Either<Failure, User> {
            val req= UserMessage.HomePageReq.newBuilder()
                .setTgtUid(userId)
                .setSrcUid(loginUserId)
                .build()
            return resourceHandler.request(
                service.userDetails(req),
                { it.toUser() }
            )
        }

        override fun applyOSSToken(): Either<Failure, OSSToken> = apiHandler.request(
            //TODO: 迁移到UserAPI
            service.applyOSSToken(),
            { it.toOSSToken() }
        )

    }

}

private fun UserMessage.UserInfoBrief.toUser(): User {
    val user = User(uid)
    val profile = user.profile
    val info = user.info
    profile.nickName = nickName
    profile.avatar = Avatar(avatar)
    profile.location = Location.fromString(location)
    profile.age = age
    profile.gender = Gender.fromString(gender)
    //greatNumber //FIXME
    //level //FIXME
    info.fansNumber = fans
    //follows //FIXME
    //balance //FIXME

    return user
}

fun UserMessage.BanBanListResp.VoiceCharacter.toUser(): User {
    val user = User(id)
    val profile = user.profile
    val info = user.info

    profile.nickName = name
    profile.avatar = Avatar(avatar)
    profile.location = Location.fromString(location)
    profile.declaration = declaration
    profile.gender = Gender.fromString(gender)
    profile.age = age
    profile.promotionAudio = AudioResource(audioUrl, audioDuration)
    profile.primarySkillTag = skill
    profile.primarySkillCategory = SkillCategory.fromString(skillCategory)

    info.status =
        if (chating) UserStatus.CHATING else if (online) UserStatus.ONLINE else UserStatus.OFFLINE
    info.lastVisit = lastAlive
    info.freshMan = freshMan
    info.nobleLevel = NobleLevel(rank, rankStr)
    info.vipLevel = VIPLevel(vipLevel, vipLevelStr)

    return user
}

fun UserMessage.HomePageResp.toUser(): User{
    val user = User(personal.id)
    val profile = user.profile
    val info = user.info

    with(personal) {
        profile.nickName = name
        profile.gender = Gender.fromString(gender)
        profile.age = age
        info.star = star
        profile.career = career
        profile.declaration = declaration
        info.lastVisit = timeInfo
        info.fansNumber = fans
        profile.profilePhotos.addAll(imgUrlsList)
    }

    profile.location = Location.fromString(location)
    //TODO: online
    info.giftsNumber = totalGift
    info.gifts.addAll(giftsList.map { it.toGiftPack() })
    info.skills.addAll(skillsList.map { it.toSkillCareer() })
    info.currentLocation = Location.fromString(location)
    return user
}

fun UserMessage.HomePageResp.Gift.toGiftPack() = GiftPack(Gift(id, icon), amount)

fun UserMessage.HomePageResp.Skill.toSkillCareer(): SkillCareer {
    val skill = Skill(id, SkillCategory.empty())

    skill.description = description
    skill.priority = priority
    skill.skillInfoImg = imgUrl
    skill.audio = AudioResource(audioUrl)

    val grade = SkillGrade(grade)
    grade.skillId = skill.id

    return SkillCareer(skill, grade)
}


//FIXME: get detailed user info from local cache or repository
fun UserMessage.LoginResp.toAccount(): Account {
    val account = Account(uid)
    account.setToken(token)
    account.setToken(Account.TokenType.NIM_TOKEN, token) //目前云信直接使用红后token，可能有安全隐患
    account.phone = phone
    account.avatar = Avatar(avatar)

    val user = account.user
    val profile = user.profile
    profile.nickName = nickName
    profile.avatar = Avatar(avatar)
    profile.age = this.user.age
    profile.gender = Gender.fromString(this.user.gender)
    //...
    //FIXME: more information

    user.info.nobleLevel = NobleLevel(this.user.levelId, this.user.level) //FIXME, query system level dictionary of levelId
    user.info.vipLevel = VIPLevel(this.user.vip, "")

    return account
}

fun UserMessage.TokenOssResp.toOSSToken() =
    OSSToken(accessKeyId, accessKeySecret, securityToken, expiration)

fun SysMessage.SkillListResp.Skill.toSkillRoad(): SkillRoad{
    val skill = Skill(id, category.toSkillCategory())
    skill.title = title
    skill.icon = iconUrl

    return SkillRoad(skill, gradesList.map { it.toSkillGrade() })
}

fun SysMessage.SkillListResp.Category.toSkillCategory(): SkillCategory = when(this){
    SysMessage.SkillListResp.Category.VOICE -> SkillCategory.VOICE
    SysMessage.SkillListResp.Category.GAME -> SkillCategory.GAME
    SysMessage.SkillListResp.Category.UNRECOGNIZED -> SkillCategory.OTHER
}

fun SysMessage.SkillListResp.SkillGrade.toSkillGrade(): SkillGrade {
    val skillGrade = SkillGrade(gradeTitle)
    skillGrade.skillId = skillId
    skillGrade.gradeId = gradeId
    return skillGrade
}
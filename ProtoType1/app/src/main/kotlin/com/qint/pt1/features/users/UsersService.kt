package com.qint.pt1.features.users

import proto_def.SysMessage
import proto_def.UserMessage
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersService
@Inject constructor(retrofit: Retrofit) : UsersApi {
    private val usersApi: UsersApi by lazy { retrofit.create(UsersApi::class.java) }

    override fun recommendUsers(usersReq: UserMessage.BanbanListReq) = usersApi.recommendUsers(usersReq)

    override fun userDetails(homePageReq: UserMessage.HomePageReq) = usersApi.userDetails(homePageReq)

    override fun login(loginReq: UserMessage.LoginReq) = usersApi.login(loginReq)

    override fun applyOSSToken() = usersApi.applyOSSToken()

    override fun updateLocation(updateLocationReq: UserMessage.LocationUpdateReq) =
        usersApi.updateLocation(updateLocationReq)

    override fun skillRoads(skillsReq: SysMessage.SkillListReq) = usersApi.skillRoads(skillsReq)

    override fun search(searchReq: UserMessage.SearchReq) = usersApi.search(searchReq)
}

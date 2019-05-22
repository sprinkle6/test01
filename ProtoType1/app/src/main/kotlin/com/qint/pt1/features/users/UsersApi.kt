package com.qint.pt1.features.users

import proto_def.SysMessage
import proto_def.UserMessage
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

internal interface UsersApi {
    companion object {
        private const val moduleBees = "bees"
        private const val moduleDemo = "demo"
        private const val module = moduleDemo

        private const val RECOMMEND_USERS = "${module}/banban_list"
        private const val HOME_PAGE = "${module}/home_page"
        private const val LOGIN = "${module}/login"
        private const val APPLY_OSS_TOKEN = "${module}/apply_oss_token"
        private const val APPLY_AGORA_TOKEN = "${module}/apply_agora_token"
        private const val CAREER_LIST = "${module}/career_list"
        private const val SKILL_LIST = "${module}/skill_list"
        private const val UPDATE_USER_INFO = "${module}/update_user_info"
        private const val UPDATE_USER_INTEREST_SKILL = "${module}/update_user_interest_skill"
        private const val LOCATION_UPDATE = "${module}/location_update"
        private const val NAME_CHECK = "${module}/name_check"
        private const val USER_OVERVIEW = "${module}/user_overview"
        private const val SKILL_DETAIL = "${module}/skill_detail"
        private const val SEARCH = "${module}/search"
    }

    @POST(RECOMMEND_USERS)
    fun recommendUsers(@Body usersReq: UserMessage.BanbanListReq): Call<UserMessage.BanBanListResp>

    @POST(HOME_PAGE)
    fun userDetails(@Body homePageReq: UserMessage.HomePageReq): Call<UserMessage.HomePageResp>

    @POST(LOGIN)
    fun login(@Body loginReq: UserMessage.LoginReq): Call<UserMessage.LoginResp>

    @GET(APPLY_OSS_TOKEN)
    fun applyOSSToken(): Call<UserMessage.TokenOssResp>

    @POST(LOCATION_UPDATE)
    fun updateLocation(@Body updateLocationReq: UserMessage.LocationUpdateReq): Call<UserMessage.LocationUpdateResp>

    @POST(SKILL_LIST)
    fun skillRoads(@Body skillsReq: SysMessage.SkillListReq): Call<SysMessage.SkillListResp>

    @POST(SEARCH)
    fun search(@Body searchReq: UserMessage.SearchReq): Call<UserMessage.SearchResp>
}

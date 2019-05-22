package com.qint.pt1.features.login

import android.util.Log
import com.qint.pt1.base.exception.Failure
import com.qint.pt1.base.extension.empty
import com.qint.pt1.base.interactor.UseCase
import com.qint.pt1.base.platform.RequestCallback
import com.qint.pt1.domain.Account
import com.qint.pt1.domain.Location
import com.qint.pt1.domain.User
import com.qint.pt1.domain.UserId
import com.qint.pt1.features.im.IM
import com.qint.pt1.features.users.UsersRepository
import com.qint.pt1.util.LOG_TAG
import com.qint.pt1.util.LocationHelper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Login @Inject constructor(private val usersRepository: UsersRepository,
                                private val im: IM) {

    var account: Account? = null
        private set

    val user: User? get() = account?.user

    var location: Location? = null

    val isLogined: Boolean get() = account != null

    val userId: UserId = account?.userId ?: UserId.empty()

    fun isMe(userId: UserId?) = userId != null && userId == account?.userId

    fun login(account: Account) {
        this.account = account
    }

    fun logout(){
        this.account = null
        im.logout()
    }

    fun login(mobile: String, verifyCode: String, callback: RequestCallback<LoginInfo>) {
        val loginUseCase = LoginUseCase(usersRepository)
        loginUseCase(LoginUseCase.AuthInfo(mobile, verifyCode)) { result -> result.either(
            { failure -> callback.onFailure(failure) },
            { account ->
                im.login(
                    LoginInfo(account.userId, account.getToken(Account.TokenType.NIM_TOKEN)),
                    object : RequestCallback<LoginInfo> {
                        override fun onSuccess(loginInfo: LoginInfo?) {
                            //FIXME: 暂时强制必须云信登录成功才能登录成功以便处理即时通信和聊天室相关业务
                            //       可以弱化限制，在红后登录成功，云信登录失败的情况下，允许使用非IM/聊天室业务
                            //       在需要使用IM/聊天室业务时自动尝试重新登录，失败再提示用户错误信息
                            login(account)
                            callback.onSuccess(null)
                        }

                        override fun onFailure(failure: Failure) {
                            Log.e(LOG_TAG, "NIM login failed: $failure")
                            callback.onFailure(failure)
                        }
                    }
                )
            })
        }
    }

    fun reportLocation(locationHelper: LocationHelper){
        locationHelper.requestAndReportLocation()
    }
}

data class LoginInfo(val account: String, val authToken: String?)

class LoginUseCase
@Inject constructor(private val usersRepository: UsersRepository) :
    UseCase<Account, LoginUseCase.AuthInfo>() {
    override suspend fun run(authInfo: AuthInfo) =
        usersRepository.login(authInfo.mobile, authInfo.verifyCode)

    data class AuthInfo(val mobile: String, val verifyCode: String)
}
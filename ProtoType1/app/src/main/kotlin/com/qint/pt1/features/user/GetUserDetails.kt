package com.qint.pt1.features.user

import com.qint.pt1.base.interactor.UseCase
import com.qint.pt1.domain.User
import com.qint.pt1.features.login.Login
import com.qint.pt1.features.users.UsersRepository
import javax.inject.Inject

class GetUserDetails
@Inject constructor(private val usersRepository: UsersRepository,
                    private val login: Login) : UseCase<User, GetUserDetails.Params>() {

    override suspend fun run(params: Params) = usersRepository.userDetails(params.id, login.userId)

    data class Params(val id: String)
}

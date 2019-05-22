package com.qint.pt1.features.users

import com.qint.pt1.base.interactor.UseCase
import com.qint.pt1.domain.User
import javax.inject.Inject

class GetUsers
@Inject constructor(private val usersRepository: UsersRepository) : UseCase<List<User>, UseCase.None>() {
    override suspend fun run(params: None) = usersRepository.users()
}

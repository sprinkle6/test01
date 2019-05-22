/*
 * Copyright (c) 2019. QINT.TV
 * Author: Matthew Zhang
 * Created on: 3/27/19 10:16 AM
 */

package com.qint.pt1.domain

import com.qint.pt1.base.extension.empty

/*
 * 当前登录用户的手机号、第三方SDK token等私密信息
 */
data class Account(val userId: UserId){
    var avatar: Avatar = Avatar.empty()
    var phone: Phone = Phone.empty()
    val user: User = User(userId)

    private val tokens = mutableMapOf<TokenType, Token>()
    init {
        TokenType.values().forEach { tokens[it] = Token.empty() }
    }

    fun getToken() = getToken(TokenType.REDQUEEN_TOKEN)
    fun setToken(token: Token) = setToken(TokenType.REDQUEEN_TOKEN, token)
    fun getToken(type: TokenType): Token? = tokens[type]
    fun setToken(type: TokenType, token: Token) = tokens.set(type, token)

    enum class TokenType{
        REDQUEEN_TOKEN, AGORA_TOKEN, NIM_TOKEN, ALIYUN_OSS_TOKEN;
    }

    companion object {
        val NullAccount = Account("0")
        fun empty() = NullAccount
    }
}
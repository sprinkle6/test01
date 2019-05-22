/*
 * $filename
 * Author: Matthew Zhang
 * Created on: 4/7/19 6:25 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.features.login

import com.qint.pt1.base.exception.Failure

open class LoginFailure: Failure.FeatureFailure("登录异常") {
    object LoginFailed : LoginFailure()
}
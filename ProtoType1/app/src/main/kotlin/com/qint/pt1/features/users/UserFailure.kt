package com.qint.pt1.features.users

import com.qint.pt1.base.exception.Failure

open class UserFailure : Failure.FeatureFailure("TODO") {
    object ListNotAvailable : UserFailure()
    object NonExistentUser : UserFailure()
}

/**
 * Copyright (C) 2018 Fernando Cejas Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qint.pt1.base.exception

/**
 * Base Class for handling errors/failures/exceptions.
 * Every feature specific failure should extend [FeatureFailure] class.
 */
sealed class Failure(val message: String = "", action: Action = Action.NullAction) {
    class NetworkConnectionError(val code: Int = 0) : Failure("网络未连接，请连接网络后重试")
    class ServerError(val code: Int = 0, message: String) : Failure(message)
    class PermissionFailure(val permission: String, reason: String): Failure("需要${permission}权限，以便${reason}")
    class ParameterInvaildFailure(message: String) : Failure("参数无效：$message")
    class UnknownError(message: String) : Failure(message)

    /** * Extend this class for feature specific failures.*/
    abstract class FeatureFailure(message: String) : Failure(message)

    override fun toString() = "${this::class.java.simpleName}: $message"
}

class Action(val actionPromotion: String, val action: () -> Unit){
    companion object{
        val NullAction = Action("OOPS", {})
    }
}
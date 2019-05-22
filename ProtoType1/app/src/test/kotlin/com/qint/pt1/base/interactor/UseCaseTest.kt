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
package com.qint.pt1.base.interactor

import com.qint.pt1.AndroidTest
import com.qint.pt1.base.exception.Failure
import com.qint.pt1.base.functional.Either
import com.qint.pt1.base.functional.Either.Left
import com.qint.pt1.base.functional.Either.Right
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.*
import org.junit.Test

private typealias R = Either<Failure, UseCaseTest.MyType>

class UseCaseTest : AndroidTest() {
    /*
     * 注意：在UseCase的onResult回调中包含的断言如果失败或有异常并不会导致测试用例失败（由于在异步线程中进行），需要看测试执行的输出有无报异常信息。
     * 不要试图在usecase执行完后在usecase的invoke回调之外检查usecase的执行结果。
     */
    private val TEST_VALUE = "Test"
    private val TEST_PARAM = "ParamTest"
    private val FAILURE = Failure.UnknownError()
    private val EXCEPTION_MESSAGE = "Huston, we have a problem."


    @Test fun `running use case should return 'Either' of use case type`() {
        val params = MyParams(TEST_PARAM)
        val useCase = MyUseCase()
        val result = runBlocking { useCase.run(params) }

        result shouldEqual Right(MyType(TEST_VALUE))
        useCase.isCompleted shouldBe false
    }

    @Test fun `should return correct data when executing use case`() {
        val usecase = MyUseCase()

        val params = MyParams("TestParam")

        runBlocking {//虽然理论上感觉这里不需要runBlocking，不过加上保险一些，没有这个有时有错误显示不出来
            usecase(params) { result: Either<Failure, MyType> ->
                usecase.isCompleted shouldBe true
                result shouldEqual Right(MyType(TEST_VALUE))
                result.either(
                    {},
                    { it.name shouldBeEqualTo (TEST_VALUE) }
                )
            }
        }

        usecase.wait(1000)
        usecase.isCompleted shouldBe true
    }

    @Test
    fun `should return correct data when executing none param use case`() {
        val noneParamUseCase = NoneParamUseCase()
        runBlocking {
            noneParamUseCase() {
                noneParamUseCase.isCompleted shouldBe true
                it shouldEqual Right(MyType(TEST_VALUE))
            }
        }
        noneParamUseCase.wait(1000)
        noneParamUseCase.isCompleted shouldBe true
    }

    @Test
    fun `should return failure in failed use case`() {
        val failedUseCase = FailedUseCase()
        runBlocking {
            failedUseCase() { result ->
                result shouldBeInstanceOf Left::class.java
                failedUseCase.isCompleted shouldBe true
                result.either(
                    {it shouldBeInstanceOf FAILURE::class.java},
                    {}
                )
            }
        }
        failedUseCase.wait(1000)
        failedUseCase.isCompleted shouldBe true
    }

    @Test
    fun `we can check if use case is completed`() {
        val usecase = SleepingUseCase(2000)
        usecase.isCompleted shouldBe false
        runBlocking {
            usecase() {
                usecase.isCompleted shouldBe true
                it shouldEqual Right(MyType(TEST_VALUE))
                it shouldEqual Left(FAILURE)
                it.either(
                    {failure -> failure shouldBeInstanceOf FAILURE::class.java},
                    {}
                )
            }
        }
        usecase.isCompleted shouldBe false
        runBlocking { delay(500) }
        usecase.isCompleted shouldBe false
        runBlocking { delay(2500) }
        usecase.isCompleted shouldBe true
        usecase.wait(3000)
        usecase.isCompleted shouldBe true
    }

    @Test
    fun `running two usecases concurrently is ok`() {
        val usecase1 = MyUseCase()
        val usecase2 = SleepingUseCase(500)
        runBlocking {
            usecase1(MyParams(TEST_PARAM)) {
                usecase1.isCompleted shouldBe true
                (it is Right<MyType>) shouldBe true
                (it is Left<Failure>) shouldBe false
                usecase2.isCompleted shouldBe false
            }
            usecase2() {
                usecase2.isCompleted shouldBe true
                usecase1.isCompleted shouldBe true
            }
        }
        usecase1.wait(1000)
        usecase2.wait(1000)
    }

    @Test
    fun `running two instances of one usecase concurrently is ok`() {
        val usecase1 = SleepingUseCase(100)
        val usecase2 = SleepingUseCase(500)
        runBlocking {
            usecase1 {
                usecase1.isCompleted shouldBe true
                (it is Right<MyType>) shouldBe true
                (it is Left<Failure>) shouldBe false
                usecase2.isCompleted shouldBe false
            }
            usecase2 {
                usecase2.isCompleted shouldBe true
                usecase1.isCompleted shouldBe true
            }
        }
        usecase1.wait(1000)
        usecase2.wait(1000)
    }

    @Test
    fun `exception should be captured and return as either left value of result`() {
        val usecase = ExceptionUseCase()
        usecase.isCompleted shouldBe false
        runBlocking {
            usecase() { result ->
                (result is Left<Failure>) shouldBe true
                (result is Right<MyType>) shouldBe false
                result.either(
                    {
                        it shouldBeInstanceOf Failure.UnknownError::class.java
                        (it as Failure.UnknownError).message!! shouldContain EXCEPTION_MESSAGE
                    },
                    {}
                )
                print("Failure in Exception UseCase: ${result}")
            }
        }
        usecase.wait(1000)
        usecase.isCompleted shouldBe true
    }

    @Test
    fun `calling a usecase twice will get exception`(){
        val usecase = SleepingUseCase(500)
        invoking {
            usecase() {
                usecase.isCompleted shouldBe true
            }
            usecase() {
                it shouldEqual Right(MyType(TEST_VALUE))
            }
        } shouldThrow AnyException
    }

    data class MyType(val name: String)
    data class MyParams(val name: String)

    private inner class MyUseCase : UseCase<MyType, MyParams>() {
        override suspend fun run(params: MyParams) = Right(MyType(TEST_VALUE))
    }

    private inner class NoneParamUseCase : UseCase<MyType, UseCase.None>() {
        override suspend fun run(params: None) = Right(MyType(TEST_VALUE))
    }

    private inner class FailedUseCase : UseCase<MyType, UseCase.None>() {
        override suspend fun run(params: None) = Left(Failure.UnknownError())
    }

    private inner class SleepingUseCase(val sleepingTime: Long) : UseCase<MyType, UseCase.None>() {
        override suspend fun run(params: None): Either<Failure, MyType> {
            delay(sleepingTime)
            return Right(MyType(TEST_VALUE))
        }
    }

    private inner class ExceptionUseCase : UseCase<MyType, UseCase.None>() {
        override suspend fun run(params: None): Either<Failure, MyType> {
            throw Exception(EXCEPTION_MESSAGE)
        }
    }

    /*
     * 等待直到usecase确实完成了（成功或异常或被cancel）
     */
    fun <T : Any, P> UseCase<T, P>.wait(maxWaitTime: Long = 0) {
        var timeWaitted = 0L
        val checkTime = 100L
        while (!this.isCompleted) { //不知为何，如果不加超时判断这里有可能会无限循环
            runBlocking { delay(checkTime) }
            timeWaitted += checkTime
            if (maxWaitTime > 0 && timeWaitted >= maxWaitTime) return
        }
    }
}

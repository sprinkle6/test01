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
package com.qint.pt1.base.functional

import com.qint.pt1.UnitTest
import com.qint.pt1.base.functional.Either.Left
import com.qint.pt1.base.functional.Either.Right
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldEqualTo
import org.junit.Test

class EitherTest : UnitTest() {
    private val TEST_VALUE: String = "ironman"
    private val TEST_INT_VALUE = 123
    private val TEST_STRING_VALUE = TEST_INT_VALUE.toString()

    @Test fun `Either Right should return correct type`() {
        val result = Right(TEST_VALUE)

        result shouldBeInstanceOf Either::class.java
        result.isRight shouldBe true
        result.isLeft shouldBe false
        result.either({},
                { right ->
                    right shouldBeInstanceOf TEST_VALUE.javaClass
                    right shouldEqualTo TEST_VALUE
                })
    }

    @Test fun `Either Left should return correct type`() {
        val result = Left(TEST_VALUE)

        result shouldBeInstanceOf Either::class.java
        result.isLeft shouldBe true
        result.isRight shouldBe false
        result.either(
                { left ->
                    left shouldBeInstanceOf TEST_VALUE::class.java
                    left shouldEqualTo TEST_VALUE
                }, {})
    }

    @Test fun `Either map should return correct type and value`(){
        val result = Right(TEST_INT_VALUE)
        result.map{it.toString()}.either(
            {},
            {right ->
                right shouldBeInstanceOf TEST_STRING_VALUE.javaClass
                right shouldEqualTo TEST_STRING_VALUE
            }
        )
    }
}
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
package com.qint.pt1

import android.app.Application
import com.qint.pt1.api.sys.MetaData
import com.qint.pt1.base.di.ApplicationComponent
import com.qint.pt1.base.di.ApplicationModule
import com.qint.pt1.base.di.DaggerApplicationComponent
import com.qint.pt1.features.im.IM
import com.squareup.leakcanary.LeakCanary
import javax.inject.Inject


class FlowerLanguageApplication : Application() {

    @Inject internal lateinit var im: IM
    @Inject internal lateinit var metaData: MetaData

    val appComponent: ApplicationComponent by lazy(mode = LazyThreadSafetyMode.NONE) {
        DaggerApplicationComponent
                .builder()
                .applicationModule(ApplicationModule(this))
                .build()
    }

    override fun onCreate() {
        super.onCreate()
        injectMembers()

        initComponents()
        //initializeLeakDetection()
    }

    private fun injectMembers() = appComponent.inject(this)

    private fun initializeLeakDetection() {
        if (BuildConfig.DEBUG) LeakCanary.install(this)
    }

    private fun initComponents(){
        //云信SDK要求必须在Application.onCreate中初始化配置
        im.config(this) //TODO: 改为注入？

        metaData.init()
    }
}

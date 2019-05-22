/*
 * Author: Matthew Zhang
 * Created on: 4/16/19 3:28 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.features.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.LivePagedListBuilder
import com.qint.pt1.base.exception.Failure
import com.qint.pt1.base.exception.FailureHandler
import com.qint.pt1.base.extension.getDefaultPagedListConfig
import com.qint.pt1.base.platform.BaseViewModel
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject

typealias Keyword = String

class SearchViewModel
@Inject constructor(private val searchResultDataSourceFactory: SearchResultDataSourceFactory): BaseViewModel() {
    //FIXME: datasource加载数据异常时同时还触发了结果更新，导致先提示异常后紧接着提示搜索无结果
    private val executor: Executor = Executors.newFixedThreadPool(3)

    private val pageListConfig = getDefaultPagedListConfig()

    init{
        //必须在初始化LivePagedListBuilder之前设置DataSourceFactory的failureHandler
        searchResultDataSourceFactory.failureHandler = SearchFailureHandler()
    }

    val searchResultLiveData = LivePagedListBuilder(searchResultDataSourceFactory, pageListConfig)
        .setFetchExecutor(executor).build()

    private val _queryLiveData = MutableLiveData<String>()
    val queryLiveData: LiveData<String> = _queryLiveData

    fun search(keyword: Keyword){ //TODO: 如何把对datasource的更新集中到一处处理？
        _queryLiveData.value = keyword
        searchResultDataSourceFactory.keyword = keyword
        searchResultLiveData.value?.dataSource?.invalidate()
    }

    /*
     * 用于传入DataSource中执行Failure处理
     */
    inner class SearchFailureHandler: FailureHandler.NoopFailureHandler(){
        override fun handleFailure(failure: Failure?) {
            trigerFailure(failure ?: return)
        }
    }
}

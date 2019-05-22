/*
 * Author: Matthew Zhang
 * Created on: 4/16/19 2:15 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.features.search

import android.annotation.SuppressLint
import android.app.Activity
import android.app.SearchManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.qint.pt1.R
import com.qint.pt1.base.extension.*
import com.qint.pt1.base.navigation.Navigator
import com.qint.pt1.base.platform.BaseActivity
import com.qint.pt1.util.LOG_TAG
import kotlinx.android.synthetic.main.search_activity.*
import javax.inject.Inject

class SearchActivity : BaseActivity() {

    override fun layoutId() = R.layout.search_activity

    @Inject internal lateinit var navigator: Navigator
    @Inject internal lateinit var resultAdapter: SearchResultAdapter
    private lateinit var searchViewModel: SearchViewModel
    private lateinit var suggestionAdapter: SearchSuggestionAdapter

    private val SUGGESTION_LIMIT = 10
    private val SEARCH_TYPE_USER = "user"
    private val HISTORY_DISPLAY_MAX_COUNT = 20

    private val searchRecentSuggestions = SearchRecentSuggestions(
        this,
        SearchSuggestionProvider.AUTHORITY,
        SearchSuggestionProvider.MODE
    )

    private val enableSuggestion = false //显示效果比较丑，而且搜索提示显示搜索历史意义不大，先屏蔽搜索提示显示

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)

        searchViewModel = viewModel(viewModelFactory){
            observe(searchResultLiveData, ::renderResult)
            failure(failureLiveData, ::handleFailure)
        }

        initView()

        initSearch()
    }

    override fun onNewIntent(intent: Intent) {
        setIntent(intent)
        handleIntent(intent)
    }

    private fun initView(){
        searchResult.layoutManager = LinearLayoutManager(this)
        searchResult.adapter = resultAdapter

        resultAdapter.itemClickListener = {userItem ->
            navigator.showUserProfile(this, userItem.id)
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun initSearch(){
        setupSearchInput()

        initSearchHistory()

        handleIntent(intent)
    }

    private fun setupSearchInputListeners(){
        setDefaultKeyMode(Activity.DEFAULT_KEYS_SEARCH_LOCAL) //enable type-to-search

        searchInput.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                doSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if(newText.isNotBlank()) {
                    historyArea.gone()
                    if(enableSuggestion) {
                        val cursor = getRecentSuggestions(newText, SUGGESTION_LIMIT)
                        suggestionAdapter.changeCursor(cursor)
                    }
                }else{
                    reloadRecentSearchHistory(HISTORY_DISPLAY_MAX_COUNT)
                    historyArea.visible()
                }
                return false
            }
        })
    }

    private fun setupSearchInput(){
        setupSearchInputListeners()

        //searchInput.onActionViewExpanded() //左侧🔎在搜索框内
        searchInput.setIconifiedByDefault(false) //左侧🔎在搜索框外
        searchInput.isSubmitButtonEnabled = true //打开搜索框右侧的>形提交按钮
        searchInput.requestFocus() //自动取得焦点，打开输入键盘

        //以下两行去除搜索框底部的横线
        searchInput.findViewById<View>(R.id.search_plate).background = null
        searchInput.findViewById<View>(R.id.submit_area).background = null

        initSearchSuggestion()
    }

    @SuppressLint("RestrictedApi") //for threshold setting
    private fun initSearchSuggestion(){
        //TODO: 实现输入时即时请求搜索服务接口并将结果显示在提示中

        if(!enableSuggestion) return

        //FIXME: 这里searchableinfo的设置貌似并不能加载searchable.xml中的配置项
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchInput.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        suggestionAdapter = SearchSuggestionAdapter(this, null)
        searchInput.suggestionsAdapter = suggestionAdapter

        //设置输入一个字符就触发搜索提示，默认是2个字符
        searchInput.findViewById<SearchView.SearchAutoComplete>(R.id.search_src_text).threshold = 1

        searchInput.setOnSuggestionListener(object: SearchView.OnSuggestionListener{
            override fun onSuggestionSelect(position: Int) = false

            override fun onSuggestionClick(position: Int): Boolean {
                searchInput.setQuery(suggestionAdapter.getSuggestionText(position), true)
                return true
            }

        })

    }

    private fun initSearchHistory(){
        clearHistory.setOnClickListener {
            clearSearchHistory()
            reloadRecentSearchHistory(HISTORY_DISPLAY_MAX_COUNT)
        }
        reloadRecentSearchHistory(HISTORY_DISPLAY_MAX_COUNT)
    }

    private fun reloadRecentSearchHistory(numberOfItems: Int){
        searchHistory.removeAllViews()

        val cursor = getRecentSuggestions("", numberOfItems)
        if(cursor == null) return
        Log.d(LOG_TAG, "${cursor.count} historys loaded")
        var count = 0
        while(cursor.move(1) && count++ < numberOfItems){
            val historyItem = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1))
            val historyItemView = buildHistoryViewItem(historyItem)
            searchHistory.addView(historyItemView)
        }
        cursor.close()
    }

    private fun buildHistoryViewItem(historyItem: String): View{
        val historyItemView = searchHistory.inflate(R.layout.search_history_item) as TextView
        historyItemView.text = historyItem

        historyItemView.setOnClickListener {
            searchInput.setQuery(historyItem, true)
        }

        return historyItemView
    }

    private fun handleIntent(intent: Intent){
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                doSearch(query)
            }
        }
    }

    private fun renderResult(pagedList: PagedList<SearchResultUserItem>?){
        if(!searchViewModel.queryLiveData.value.isNullOrBlank() //判断是否第一次进入还没有实际执行搜索
            && searchViewModel.searchResultLiveData.value.isNullOrEmpty()){ //搜索结果为空
            notify("找不到这样的用户呢")
        }
        resultAdapter.submitList(pagedList)
    }

    private fun doSearch(query: String?){
        if(query.isNullOrBlank()) return
        searchViewModel.search(query)

        searchRecentSuggestions.saveRecentQuery(query, SEARCH_TYPE_USER) //第二个参数用于将来区分不同的搜索类型，避免升级数据库
    }

    private fun clearSearchHistory() = searchRecentSuggestions.clearHistory()

    private fun getRecentSuggestions(query: String, limit: Int): Cursor?{
        val uriBuilder = Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(SearchSuggestionProvider.AUTHORITY)
        uriBuilder.appendPath(SearchManager.SUGGEST_URI_PATH_QUERY)

        val selection = " ?"
        val selArgs = arrayOf(query)

        if(limit > 0){
            uriBuilder.appendQueryParameter(SearchManager.SUGGEST_PARAMETER_LIMIT, limit.toString())
        }

        val uri = uriBuilder.build()

        return contentResolver.query(uri, null, selection, selArgs, null)
    }

}

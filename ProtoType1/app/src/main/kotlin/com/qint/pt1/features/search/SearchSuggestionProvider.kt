/*
 * Author: Matthew Zhang
 * Created on: 4/16/19 11:20 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.features.search

import android.content.SearchRecentSuggestionsProvider

class SearchSuggestionProvider : SearchRecentSuggestionsProvider() {
    init {
        setupSuggestions(AUTHORITY, MODE)
    }

    companion object {
        const val AUTHORITY = "com.qint.pt1.features.search.SearchSuggestionProvider"
        const val MODE =
            SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES or SearchRecentSuggestionsProvider.DATABASE_MODE_2LINES
    }
}

/*
 * Author: Matthew Zhang
 * Created on: 4/17/19 10:29 AM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.features.search

import android.app.SearchManager
import android.content.Context
import android.database.Cursor
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cursoradapter.widget.CursorAdapter
import com.qint.pt1.R
import com.qint.pt1.base.extension.inflate

class SearchSuggestionAdapter(context: Context, cursor: Cursor?): CursorAdapter(context, cursor, false) {
    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        //val view = LayoutInflater.from(context).inflate(R.layout.search_suggestion_item, parent, false)
        val view = parent.inflate(R.layout.search_suggestion_item)
        val viewHolder = ViewHolder(view)
        view.setTag(viewHolder)
        return view
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val viewHolder = view.getTag() as ViewHolder
        viewHolder.title.text =
                cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1))
    }

    fun getSuggestionText(position: Int): String? =
        if(position >= 0 && position < cursor.count){
            cursor.moveToPosition(position)
            cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1))
        }else {
            null
        }

    inner class ViewHolder(val itemView: View){
        val title = itemView.findViewById<TextView>(R.id.title)
    }
}

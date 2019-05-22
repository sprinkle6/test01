/*
 * Author: Matthew Zhang
 * Created on: 4/12/19 9:33 AM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.base.platform

interface Selector<T, V>{
    fun getName(): String
    fun getOptions(): List<T>
    fun getDefaultOption(): T
    fun getSelectedOption(): T
    fun getSelectedValue(): V
    fun select(option: T)
    fun reset()
    fun isChangedFromDefault(): Boolean
}

abstract class BaseSelector<T, V>(private val _name: String, private val _defaultOption: T): Selector<T, V>{
    protected var _selectedOption = _defaultOption

    override fun getName() = _name

    override fun getDefaultOption() = _defaultOption

    override fun getSelectedOption() = _selectedOption

    override fun select(option: T) {
        _selectedOption = option
    }

    override fun reset() = select(_defaultOption)

    override fun isChangedFromDefault() = _selectedOption != _defaultOption
}

object NullSelector: BaseSelector<Unit, Unit>("", Unit){
    override fun getOptions(): List<Unit> = emptyList()
    override fun getSelectedValue() = Unit
}

enum class OrderOption{
    UNSET, ASC, DESC
}
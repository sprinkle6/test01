/*
 * Author: Matthew Zhang
 * Created on: 5/22/19 11:13 AM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.domain

typealias ProductId = Int

enum class ProductGrade {
    NONE, BROZEN, SILVER, GOLDEN
}

data class ProductPack(val product: Product, val count: Int, val expire: String = "")

sealed class Product(
    val id: ProductId,
    val title: Title,
    val icon: ImageUrl,
    val price: Price,
    val period: Period,
    val priority: Int,
    val grade: ProductGrade = ProductGrade.NONE
){
    override fun toString() = "Product${valueString()}"

    fun valueString() = "(id=${id}, title=${title}, icon=${icon}, price=${price}, period=${period}, grade=${grade})"

    class General(
        id: ProductId,
        title: Title,
        icon: ImageUrl,
        price: Price,
        period: Period,
        priority: Int,
        grade: ProductGrade
    ) : Product(id, title, icon, price, period, priority, grade){
        override fun toString() = "GeneralProduct${valueString()}"
    }

    class Box(
        id: ProductId,
        title: Title,
        icon: ImageUrl,
        price: Price,
        period: Period,
        priority: Int,
        grade: ProductGrade
    ) : Product(id, title, icon, price, period, priority, grade){
        override fun toString() = "Box${valueString()}"
    }

    class Key(
        id: ProductId,
        title: Title,
        icon: ImageUrl,
        price: Price,
        period: Period,
        priority: Int,
        grade: ProductGrade
    ) : Product(id, title, icon, price, period, priority, grade){
        override fun toString() = "Key${valueString()}"
    }

    class Gift(
        id: ProductId,
        title: Title,
        icon: ImageUrl,
        price: Price,
        period: Period,
        priority: Int,
        grade: ProductGrade
    ) : Product(id, title, icon, price, period, priority, grade){
        override fun toString() = "Gift${valueString()}"
    }

    class Noble(
        id: ProductId,
        title: Title,
        icon: ImageUrl,
        price: Price,
        period: Period,
        priority: Int,
        grade: ProductGrade
    ) : Product(id, title, icon, price, period, priority, grade){
        override fun toString() = "Noble${valueString()}"
    }
}



